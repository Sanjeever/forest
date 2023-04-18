package org.dromara.forest.lifecycles.authorization;

import org.dromara.forest.extensions.BasicAuth;
import org.dromara.forest.http.ForestRequest;
import org.dromara.forest.lifecycles.MethodAnnotationLifeCycle;
import org.dromara.forest.reflection.ForestMethod;
import org.dromara.forest.utils.StringUtil;

public class BasicAuthLifeCycle implements MethodAnnotationLifeCycle<BasicAuth, Object> {

    @Override
    public void onInvokeMethod(ForestRequest request, ForestMethod method, Object[] args) {

    }

    @Override
    public boolean beforeExecute(ForestRequest request) {
        String username = (String) getAttribute(request, "username");
        String password = (String) getAttribute(request, "password");
        if (StringUtil.isNotEmpty(username) || StringUtil.isNotEmpty(password)) {
            request.authenticator(new org.dromara.forest.auth.BasicAuth(username, password));
        }
        return true;
    }


    @Override
    public void onMethodInitialized(ForestMethod method, BasicAuth annotation) {

    }
}
