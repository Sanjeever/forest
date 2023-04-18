package org.dromara.forest.backend.okhttp3.executor;

import org.dromara.forest.backend.BodyBuilder;
import org.dromara.forest.backend.HttpExecutor;
import org.dromara.forest.backend.ResponseHandler;
import org.dromara.forest.backend.okhttp3.body.OkHttp3BodyBuilder;
import org.dromara.forest.backend.okhttp3.logging.OkHttp3LogBodyMessage;
import org.dromara.forest.backend.url.QueryableURLBuilder;
import org.dromara.forest.backend.url.URLBuilder;
import org.dromara.forest.exceptions.ForestRetryException;
import org.dromara.forest.http.ForestHeader;
import org.dromara.forest.http.ForestRequest;
import org.dromara.forest.http.ForestRequestType;
import org.dromara.forest.http.ForestResponse;
import org.dromara.forest.http.ForestResponseFactory;
import org.dromara.forest.logging.LogBodyMessage;
import org.dromara.forest.logging.LogConfiguration;
import org.dromara.forest.logging.ForestLogHandler;
import org.dromara.forest.logging.LogHeaderMessage;
import org.dromara.forest.logging.RequestLogMessage;
import org.dromara.forest.logging.RequestProxyLogMessage;
import org.dromara.forest.logging.ResponseLogMessage;
import org.dromara.forest.utils.RequestNameValue;
import org.dromara.forest.utils.StringUtils;
import org.dromara.forest.backend.okhttp3.conn.OkHttp3ConnectionManager;
import org.dromara.forest.backend.okhttp3.response.OkHttp3ForestResponseFactory;
import org.dromara.forest.backend.okhttp3.response.OkHttp3ResponseFuture;
import org.dromara.forest.backend.okhttp3.response.OkHttp3ResponseHandler;
import org.dromara.forest.converter.json.ForestJsonConverter;
import org.dromara.forest.exceptions.ForestNetworkException;
import org.dromara.forest.handler.LifeCycleHandler;
import org.dromara.forest.mapping.MappingTemplate;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Date;
import java.util.List;


/**
 * @author gongjun[jun.gong@thebeastshop.com]
 * @since 2018-02-27 17:55
 */
public class OkHttp3Executor implements HttpExecutor {

    private static final BodyBuilder BODY_BUILDER = new OkHttp3BodyBuilder();

    private static final URLBuilder URL_BUILDER = new QueryableURLBuilder();

    protected final ForestRequest request;

    private final OkHttp3ConnectionManager connectionManager;

    private final OkHttp3ResponseHandler okHttp3ResponseHandler;

    private Call call;

    protected RequestLogMessage buildRequestMessage(int retryCount, Request okRequest) {
        RequestLogMessage message = new RequestLogMessage();
        HttpUrl url = okRequest.url();
        String scheme = url.scheme().toUpperCase();
        String uri = url.toString();
        String method = okRequest.method();
        message.setUri(uri);
        message.setType(method);
        message.setScheme(scheme);
        message.setRetryCount(retryCount);
        setLogHeaders(message, okRequest);
        setLogBody(message, okRequest);
        return message;
    }

    protected void setLogHeaders(RequestLogMessage message, Request okRequest) {
        Headers headers = okRequest.headers();
        for (int i = 0; i < headers.size(); i++) {
            String name = headers.name(i);
            String value = headers.value(i);
            message.addHeader(new LogHeaderMessage(name, value));
        }
    }

    protected void setLogBody(RequestLogMessage message, Request okRequest) {
        RequestBody requestBody = okRequest.body();
        LogBodyMessage logBodyMessage = new OkHttp3LogBodyMessage(requestBody);
        message.setBody(logBodyMessage);
    }


    public void logRequest(int retryCount,  Request okRequest, OkHttpClient okHttpClient) {
        LogConfiguration logConfiguration = request.getLogConfiguration();
        if (!logConfiguration.isLogEnabled() || !logConfiguration.isLogRequest()) {
            return;
        }
        RequestLogMessage logMessage = buildRequestMessage(retryCount, okRequest);
        logMessage.setRequest(request);
        logMessage.setRetryCount(retryCount);
        Proxy proxy = okHttpClient.proxy();
        if (proxy != null) {
            RequestProxyLogMessage proxyLogMessage = new RequestProxyLogMessage();
            SocketAddress address = proxy.address();
            if (address instanceof InetSocketAddress) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
                proxyLogMessage.setHost(inetSocketAddress.getHostString());
                proxyLogMessage.setPort(inetSocketAddress.getPort() + "");
                logMessage.setProxy(proxyLogMessage);
            }
        }
        request.setRequestLogMessage(logMessage);
        logConfiguration.getLogHandler().logRequest(logMessage);
    }

    public void logResponse(ForestResponse response) {
        LogConfiguration logConfiguration = request.getLogConfiguration();
        if (!logConfiguration.isLogEnabled() || response.isLogged()) {
            return;
        }
        response.setLogged(true);
        ResponseLogMessage logMessage = new ResponseLogMessage(response, response.getStatusCode());
        ForestLogHandler logHandler = logConfiguration.getLogHandler();
        if (logHandler != null) {
            if (logConfiguration.isLogResponseStatus()) {
                logHandler.logResponseStatus(logMessage);
            }
            if (logConfiguration.isLogResponseContent()) {
                logHandler.logResponseContent(logMessage);
            }
        }
    }

    public OkHttp3Executor(ForestRequest request, OkHttp3ConnectionManager connectionManager, OkHttp3ResponseHandler okHttp3ResponseHandler) {
        this.request = request;
        this.connectionManager = connectionManager;
        this.okHttp3ResponseHandler = okHttp3ResponseHandler;
    }


    protected OkHttpClient getClient(ForestRequest request, LifeCycleHandler lifeCycleHandler) {
        return connectionManager.getClient(request, lifeCycleHandler);
    }

    protected void prepareHeaders(Request.Builder builder) {
        ForestJsonConverter jsonConverter = request.getConfiguration().getJsonConverter();
        List<RequestNameValue> headerList = request.getHeaderNameValueList();
        String contentType = request.getContentType();
        String contentEncoding = request.getContentEncoding();
        String contentTypeHeaderName = ForestHeader.CONTENT_TYPE;
        String contentEncodingHeaderName = ForestHeader.CONTENT_ENCODING;
        if (headerList != null && !headerList.isEmpty()) {
            for (RequestNameValue nameValue : headerList) {
                String name = nameValue.getName();
                if (ForestHeader.CONTENT_TYPE.equalsIgnoreCase(name)) {
                    contentTypeHeaderName = name;
                } else if (ForestHeader.CONTENT_ENCODING.equalsIgnoreCase(name)) {
                    contentEncodingHeaderName = name;
                } else {
                    builder.addHeader(name, MappingTemplate.getParameterValue(jsonConverter, nameValue.getValue()));
                }
            }
        }
        if (StringUtils.isNotEmpty(contentType)) {
            builder.addHeader(contentTypeHeaderName, contentType);
        }
        if (StringUtils.isNotEmpty(contentEncoding)) {
            builder.addHeader(contentEncodingHeaderName, contentEncoding);
        }
    }

    protected void prepareMethodAndBody(Request.Builder builder, final LifeCycleHandler lifeCycleHandler) {
        ForestRequestType type = request.getType() == null ? ForestRequestType.GET : request.getType();
        if (type.isNeedBody()) {
            BODY_BUILDER.buildBody(builder, request, lifeCycleHandler);
        } else {
            builder.method(type.getName(), null);
        }
    }

    public void execute(final LifeCycleHandler lifeCycleHandler, int retryCount) {
        OkHttpClient okHttpClient = getClient(request, lifeCycleHandler);
        URLBuilder urlBuilder = URL_BUILDER;
        String url = urlBuilder.buildUrl(request);
        Request.Builder builder = new Request.Builder().url(url);
        prepareMethodAndBody(builder, lifeCycleHandler);
        prepareHeaders(builder);
        final Request okRequest = builder.build();
        call = okHttpClient.newCall(okRequest);
        final OkHttp3ForestResponseFactory factory = new OkHttp3ForestResponseFactory();
        logRequest(retryCount, okRequest, okHttpClient);
        Date startDate = new Date();
        Response okResponse = null;
        ForestResponse response = null;
        try {
            request.pool().awaitRequest(request);
            okResponse = call.execute();
        } catch (Throwable e) {
            response = factory.createResponse(request, null, lifeCycleHandler, e, startDate);
            if (e instanceof IOException && "Canceled".equals(e.getMessage())) {
                lifeCycleHandler.handleCanceled(request, response);
                return;
            }
            ForestRetryException retryException = new ForestRetryException(
                    e, request, request.getMaxRetryCount(), retryCount);
            try {
                request.canRetry(response, retryException);
            } catch (Throwable throwable) {
                response = factory.createResponse(request, null, lifeCycleHandler, throwable, startDate);
                logResponse(response);
                lifeCycleHandler.handleSyncWithException(request, response, throwable);
                return;
            }
            response = factory.createResponse(request, null, lifeCycleHandler, e, startDate);
            logResponse(response);
            execute(lifeCycleHandler, retryCount + 1);
            return;
        } finally {
            request.pool().finish(request);
            if (response == null) {
                response = factory.createResponse(request, okResponse, lifeCycleHandler, null, startDate);
            }
            logResponse(response);
        }
        // 是否重试
        ForestRetryException retryEx = request.canRetry(response);
        if (retryEx != null && retryEx.isNeedRetry() && !retryEx.isMaxRetryCountReached()) {
            response.close();
            execute(lifeCycleHandler, retryCount + 1);
            return;
        }

        // 验证响应
        if (retryEx == null && response.isError()) {
            retryOrDoError(response, okResponse, null, lifeCycleHandler, retryCount);
            return;
        }
        okHttp3ResponseHandler.handleSync(okResponse, response);
//        }
    }


    private void retryOrDoError(
            ForestResponse response, Response okResponse,
            OkHttp3ResponseFuture future, LifeCycleHandler lifeCycleHandler,
            int retryCount) {
        ForestNetworkException networkException =
                new ForestNetworkException(okResponse.message(), okResponse.code(), response);
        ForestRetryException retryException = new ForestRetryException(
                networkException, request, request.getRetryCount(), retryCount);
        try {
            request.canRetry(response, retryException);
        } catch (Throwable throwable) {
            if (future != null) {
                future.failed(new ForestNetworkException(okResponse.message(), okResponse.code(), response));
            }
            logResponse(response);
            okHttp3ResponseHandler.handleSync(okResponse, response);
            return;
        }
        response.close();
        execute(lifeCycleHandler, retryCount + 1);
    }

    @Override
    public ForestRequest getRequest() {
        return this.request;
    }

    @Override
    public void execute(final LifeCycleHandler lifeCycleHandler) {
        execute(lifeCycleHandler, 0);
    }

    @Override
    public ResponseHandler getResponseHandler() {
        return okHttp3ResponseHandler;
    }

    @Override
    public ForestResponseFactory getResponseFactory() {
        return new OkHttp3ForestResponseFactory();
    }

    @Override
    public void close() {
        if (call != null) {
            call.cancel();
        }
    }



}