package org.dromara.forest.lifecycles.proxy;

import org.dromara.forest.annotation.HTTPProxy;
import org.dromara.forest.callback.HTTPProxySource;
import org.dromara.forest.exceptions.ForestRuntimeException;
import org.dromara.forest.http.ForestProxy;
import org.dromara.forest.http.ForestRequest;
import org.dromara.forest.lifecycles.MethodAnnotationLifeCycle;
import org.dromara.forest.mapping.MappingTemplate;
import org.dromara.forest.reflection.ForestMethod;
import org.dromara.forest.utils.HeaderUtil;
import org.dromara.forest.utils.StringUtil;

import java.util.Arrays;

/**
 * HTTP正向代理生命周期类
 *
 * @author gongjun[dt_flys@hotmail.com]
 * @since 1.5.0-BETA5
 */
public class HTTPProxyLifeCycle implements MethodAnnotationLifeCycle<HTTPProxy, Object> {

    private final static String PARAM_KEY_HTTP_PROXY_SOURCE = "__http_proxy_source";
    private final static String PARAM_KEY_HTTP_PROXY = "__http_proxy";

    @Override
    public void onInvokeMethod(ForestRequest request, ForestMethod method, Object[] args) {
        String hostStr = (String) getAttribute(request, "host");
        String portStr = (String) getAttribute(request, "port");
        String usernameStr = (String) getAttribute(request, "username");
        String passwordStr = (String) getAttribute(request, "password");
        String[] headersStr = (String[]) getAttribute(request, "headers");

        MappingTemplate hostTemplate = method.makeTemplate(HTTPProxy.class, "host", hostStr);
        MappingTemplate portTemplate = method.makeTemplate(HTTPProxy.class, "port", portStr);
        MappingTemplate usernameTemplate = method.makeTemplate(HTTPProxy.class, "username", usernameStr);
        MappingTemplate passwordTemplate = method.makeTemplate(HTTPProxy.class, "password", passwordStr);

        addAttribute(request, "host_temp", hostTemplate);
        addAttribute(request, "port_temp", portTemplate);
        addAttribute(request, "username_temp", usernameTemplate);
        addAttribute(request, "password_temp", passwordTemplate);
        addAttribute(request, "headers_temp", Arrays.stream(headersStr)
                .map(headerStr -> method.makeTemplate(HTTPProxy.class, "headers", headerStr))
                .toArray(MappingTemplate[]::new));
    }

    @Override
    public boolean beforeExecute(ForestRequest request) {
        final MappingTemplate hostTemplate = (MappingTemplate) getAttribute(request, "host_temp");
        final MappingTemplate portTemplate = (MappingTemplate) getAttribute(request, "port_temp");
        final MappingTemplate usernameTemplate = (MappingTemplate) getAttribute(request, "username_temp");
        final MappingTemplate passwordTemplate = (MappingTemplate) getAttribute(request, "password_temp");
        final MappingTemplate[] headersTemplates = (MappingTemplate[]) getAttribute(request, "headers_temp");
        final Object httpProxySource = request.getMethod().getExtensionParameterValue(PARAM_KEY_HTTP_PROXY_SOURCE);

        final Object[] args = request.getArguments();
        final String host = hostTemplate.render(args);
        final String portStr = portTemplate.render(args);

        String username = null, password = null;

        if (usernameTemplate != null) {
            username = usernameTemplate.render(args);
        }
        if (passwordTemplate != null) {
            password = passwordTemplate.render(args);
        }

        int port = 80;
        if (StringUtil.isBlank(host)) {
            if (httpProxySource != null && httpProxySource instanceof HTTPProxySource) {
                request.setProxy(((HTTPProxySource) httpProxySource).getProxy(request));
                return true;
            }
            throw new ForestRuntimeException("[Forest] Proxy host cannot be empty!");
        }
        if (StringUtil.isNotBlank(portStr)) {
            try {
                port = Integer.parseInt(portStr);
            } catch (Throwable th) {
            }
        }
        ForestProxy proxy = new ForestProxy(host, port);
        if (StringUtil.isNotEmpty(username)) {
            proxy.setUsername(username);
        }
        if (StringUtil.isNotEmpty(password)) {
            proxy.setPassword(password);
        }
        if (headersTemplates != null && headersTemplates.length > 0) {
            HeaderUtil.addHeaders(proxy, headersTemplates, args);
        }
        request.setProxy(proxy);
        return true;
    }


    @Override
    public void onMethodInitialized(ForestMethod method, HTTPProxy annotation) {
        Class<? extends HTTPProxySource> clazz = annotation.source();
        if (clazz != null && !clazz.isInterface()) {
            HTTPProxySource proxySource = method.getConfiguration().getForestObject(clazz);
            method.setExtensionParameterValue(PARAM_KEY_HTTP_PROXY_SOURCE, proxySource);
        }
        method.setExtensionParameterValue(PARAM_KEY_HTTP_PROXY, annotation);
    }
}
