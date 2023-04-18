package org.dromara.forest.test.misc;

import junit.framework.TestCase;
import org.dromara.forest.http.ForestURL;
import org.dromara.forest.utils.URLUtil;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class URLTest extends TestCase {

    public void testUrl() {
        assertEquals("http://www.baidu.com", URLUtil.getValidURL("http://www.baidu.com", ""));
        assertEquals("http://www.baidu.com/xxx", URLUtil.getValidURL("http://www.baidu.com", "xxx"));
        assertEquals("http://www.baidu.com/xxx", URLUtil.getValidURL("http://www.baidu.com/", "xxx"));
        assertEquals("http://www.baidu.com/xxx", URLUtil.getValidURL("http://www.baidu.com/", "/xxx"));
        assertEquals("http://www.baidu.com/xxx", URLUtil.getValidURL("http://www.baidu.com", "/xxx"));

        assertEquals("http://www.baidu.com", URLUtil.getValidURL("www.baidu.com", ""));
        assertEquals("http://www.baidu.com/xxx", URLUtil.getValidURL("www.baidu.com", "xxx"));
        assertEquals("http://www.baidu.com/xxx", URLUtil.getValidURL("www.baidu.com/", "xxx"));
        assertEquals("http://www.baidu.com/xxx", URLUtil.getValidURL("www.baidu.com/", "/xxx"));
        assertEquals("http://www.baidu.com/xxx", URLUtil.getValidURL("www.baidu.com", "/xxx"));


        assertEquals("http://www.baidu.com", URLUtil.getValidURL("http://www.baidu.com", "http://www.baidu.com"));
        assertEquals("http://www.baidu.com", URLUtil.getValidURL("www.baidu.com", "http://www.baidu.com"));
        assertEquals("http://www.baidu.com", URLUtil.getValidURL("google.com", "http://www.baidu.com"));
        assertEquals("http://www.baidu.com", URLUtil.getValidURL("xxx", "http://www.baidu.com"));
    }

    @Test
    public void testForestURL() throws MalformedURLException {
        ForestURL url = new ForestURL(new URL("http://localhost:8080/xxx/yyy"));
        assertThat(url.getScheme()).isEqualTo("http");
        assertThat(url.isSSL()).isFalse();
        assertThat(url.getHost()).isEqualTo("localhost");
        assertThat(url.getPort()).isEqualTo(8080);
        assertThat(url.getAuthority()).isEqualTo("localhost:8080");
        assertThat(url.getPath()).isEqualTo("/xxx/yyy");
        assertThat(url.toString()).isEqualTo("http://localhost:8080/xxx/yyy");

        assertThat(url.setScheme("https").getScheme()).isEqualTo("https");
        assertThat(url.isSSL()).isTrue();
        assertThat(url.setHost("127.0.0.1").getHost()).isEqualTo("127.0.0.1");
        assertThat(url.setPort(8888).getPort()).isEqualTo(8888);
        assertThat(url.setUserInfo("username:pwd").getUserInfo()).isEqualTo("username:pwd");
        assertThat(url.setPath("zzz").getPath()).isEqualTo("/zzz");
        assertThat(url.toString()).isEqualTo("https://username:pwd@127.0.0.1:8888/zzz");
    }
}
