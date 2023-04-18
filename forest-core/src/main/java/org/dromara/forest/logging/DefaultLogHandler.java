package org.dromara.forest.logging;

import org.dromara.forest.backend.HttpBackend;
import org.dromara.forest.http.ForestAsyncMode;
import org.dromara.forest.http.ForestRequest;
import org.dromara.forest.http.ForestResponse;
import org.dromara.forest.utils.StringUtil;

import java.util.Iterator;
import java.util.List;

/**
 * 默认日志处理器
 * @author gongjun[jun.gong@thebeastshop.com]
 * @since 2020-09-14 17:31
 */
public class DefaultLogHandler implements ForestLogHandler {

    private ForestLogger logger = new ForestLogger();

    /**
     * 获取请求头日志内容
     * @param requestLogMessage 请求日志消息
     * @return 请求头日志内容
     */
    protected String requestLoggingHeaders(RequestLogMessage requestLogMessage) {
        StringBuilder builder = new StringBuilder();
        List<LogHeaderMessage> headers = requestLogMessage.getHeaders();
        if (headers == null) {
            return "";
        }
        for (int i = 0; i < headers.size(); i++) {
            LogHeaderMessage headerMessage = headers.get(i);
            String name = headerMessage.getName();
            String value = headerMessage.getValue();
            builder.append("\t\t" + name + ": " + value);
            if (i < headers.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * 获取请求体日志内容
     * @param requestLogMessage 请求日志消息
     * @return 请求体字符串
     */
    protected String requestLoggingBody(RequestLogMessage requestLogMessage) {
        LogBodyMessage logBodyMessage = requestLogMessage.getBody();
        if (logBodyMessage == null) {
            return "";
        }
        return logBodyMessage.getBodyString();
    }

    /**
     * 获取请求类型变更历史日志内容
     * @param requestLogMessage 请求日志消息
     * @return 日志内容字符串
     */
    protected String requestTypeChangeHistory(RequestLogMessage requestLogMessage) {
        List<String> typeChangeHistory = requestLogMessage.getTypeChangeHistory();
        if (typeChangeHistory == null || typeChangeHistory.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[Type Change]: ");
        for (Iterator<String> iterator = typeChangeHistory.iterator(); iterator.hasNext(); ) {
            String type = iterator.next();
            builder.append(type).append(" -> ");
        }
        builder.append(requestLogMessage.getType()).append("\n\t");
        return builder.toString();
    }

    protected String asyncModeContent(RequestLogMessage requestLogMessage) {
        ForestRequest request = requestLogMessage.getRequest();
        if (!request.isAsync()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("[async");
        if (ForestAsyncMode.KOTLIN_COROUTINE == request.getAsyncMode()) {
            builder.append(": kotlin");
        }
        builder.append("] ");
        return builder.toString();
    }

    /**
     * 后端框架名称
     * @param requestLogMessage 请求日志消息，{@link RequestLogMessage}类实例
     * @return 后端框架名称字符串
     */
    protected String backendContent(RequestLogMessage requestLogMessage) {
        HttpBackend backend = requestLogMessage.getRequest().getBackend();
        if (backend == null) {
            return "";
        }
        return "(" + backend.getName() + ")";
    }

    /**
     * 请求失败重试信息
     * @param requestLogMessage 请求日志消息，{@link RequestLogMessage}类实例
     * @return 重试信息字符串
     */
    protected String retryContent(RequestLogMessage requestLogMessage) {
        int retryCount = requestLogMessage.getRetryCount();
        if (retryCount > 0) {
            return "[Retry]: " + retryCount + "\n\t";
        }
        return "";
    }

    /**
     * 请求重定向信息
     *
     * @param requestLogMessage 请求日志消息对象
     * @return 请求重定向信息字符串
     */
    protected String redirection(RequestLogMessage requestLogMessage) {
        ForestRequest request = requestLogMessage.getRequest();
        if (request.isRedirection()) {
            ForestRequest prevRequest = request.getPrevRequest();
            ForestResponse prevResponse = request.getPrevResponse();
            return "[Redirect]: From " +
                    prevRequest.getType().getName() +
                    " " +
                    prevRequest.getUrl() +
                    " -> " +
                    prevResponse.getStatusCode() +
                    "\n\t";
        }
        return "";
    }

    /**
     * 正向代理信息
     * @param requestLogMessage 请求日志消息，{@link RequestLogMessage}类实例
     * @return 正向代理日志字符串
     */
    protected String proxyContent(RequestLogMessage requestLogMessage) {
        RequestProxyLogMessage proxyLogMessage = requestLogMessage.getProxy();
        StringBuilder builder = new StringBuilder();
        if (proxyLogMessage != null) {
            builder.append("[Proxy]: host: ")
                    .append(proxyLogMessage.getHost())
                    .append(", port: ")
                    .append(proxyLogMessage.getPort());
            String[] headers = proxyLogMessage.getHeaders();
            if (headers != null && headers.length > 0) {
                builder.append(", headers: {");
                for (String header : headers) {
                    builder.append(header);
                }
                builder.append("}");
            }
            builder.append("\n\t");
            return builder.toString();
        }
        return "";
    }

    /**
     * 请求日志打印的内容
     * @param requestLogMessage 请求日志字符串
     * @return 请求日志字符串
     */
    protected String requestLoggingContent(RequestLogMessage requestLogMessage) {
        StringBuilder builder = new StringBuilder();
        builder.append("Request ");
        builder.append(asyncModeContent(requestLogMessage));
        builder.append(backendContent(requestLogMessage));
        builder.append(": \n\t");
        builder.append(retryContent(requestLogMessage));
        builder.append(redirection(requestLogMessage));
        builder.append(proxyContent(requestLogMessage));
        builder.append(requestTypeChangeHistory(requestLogMessage));
        builder.append(requestLogMessage.getRequestLine());
        String headers = requestLoggingHeaders(requestLogMessage);
        if (StringUtil.isNotEmpty(headers)) {
            builder.append("\n\tHeaders: \n");
            builder.append(headers);
        }
        String body = requestLoggingBody(requestLogMessage);
        if (StringUtil.isNotEmpty(body)) {
            builder.append("\n\tBody: ");
            builder.append(body);
        }
        return builder.toString();
    }

    /**
     * 请求响应日志打印的内容
     * @param responseLogMessage 请求响应日志字符串
     * @return 请求响应日志字符串
     */
    protected String responseLoggingContent(ResponseLogMessage responseLogMessage) {
        ForestResponse response = responseLogMessage.getResponse();
        if (response != null && response.getException() != null) {
            return "Response: [Network Error]: " + response.getException().getMessage();
        }
        int status = responseLogMessage.getStatus();
        if (status >= 0) {
            return "Response: Status = " + responseLogMessage.getStatus() + ", Time = " + responseLogMessage.getTime() + "ms";
        } else {
            return "Response: [Network Error]: Unknown Network Error!";
        }
    }


    /**
     * 打印日志内容
     * @param content 日志内容字符串
     */
    @Override
    public void logContent(String content) {
        getLogger().info("[Forest] " + content);
    }


    @Override
    public ForestLogger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(ForestLogger logger) {
        this.logger = logger;
    }

    /**
     * 打印请求日志
     * @param requestLogMessage 请求日志消息
     */
    @Override
    public void logRequest(RequestLogMessage requestLogMessage) {
        String content = requestLoggingContent(requestLogMessage);
        logContent(content);
    }

    /**
     * 打印响应状态日志
     * @param responseLogMessage 响应日志消息
     */
    @Override
    public void logResponseStatus(ResponseLogMessage responseLogMessage) {
        String content = responseLoggingContent(responseLogMessage);
        logContent(content);
    }

    /**
     * 打印响应内容日志
     * @param responseLogMessage 响应日志消息
     */
    @Override
    public void logResponseContent(ResponseLogMessage responseLogMessage) {
        if (responseLogMessage.getResponse() != null) {
            String content = responseLogMessage.getResponse().getContent();
            if (StringUtil.isNotEmpty(content)) {
                logContent("Response Content:\n\t" + content);
            }
        }
    }

}
