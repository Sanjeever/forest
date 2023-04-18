package org.dromara.forest.auth;

import org.dromara.forest.http.ForestRequest;
import org.dromara.forest.utils.Base64Util;
import org.dromara.forest.utils.StringUtil;

/**
 * Forest BasicAuth 认证器
 * <p>为请求提供 BasicAuth 认证信息</p>
 *
 * @author gongjun
 * @since 1.5.28
 */
public class BasicAuth implements ForestAuthenticator {

    private String userInfo;

    public BasicAuth() {
    }

    public BasicAuth(final String userInfo) {
        this.userInfo = userInfo;
    }

    public BasicAuth(final String username, final String password) {
        this.userInfo = username + ":" + password;
    }


    public String userInfo() {
        return userInfo;
    }

    public BasicAuth userInfo(final String userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public BasicAuth userInfo(final String username, final String password) {
        this.userInfo = username + ":" + password;
        return this;
    }


    @Override
    public void enhanceAuthorization(ForestRequest request) {
        String userInfo = this.userInfo;
        if (StringUtil.isEmpty(userInfo)) {
            userInfo = request.getUserInfo();
        }
        if (StringUtil.isNotEmpty(userInfo)) {
            String basic = "Basic " + Base64Util.encode(userInfo);
            request.addHeader("Authorization", basic);
        }
    }
}
