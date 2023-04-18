package org.dromara.forest.test.proxy;

import org.dromara.forest.annotation.BaseURL;
import org.dromara.forest.annotation.Request;
import org.dromara.forest.config.ForestConfiguration;
import org.dromara.forest.proxy.InterfaceProxyHandler;
import org.dromara.forest.proxy.ProxyFactory;
import org.dromara.forest.reflection.MetaRequest;
import org.dromara.forest.test.http.client.GetClient;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * @author gongjun[jun.gong@thebeastshop.com]
 * @since 2017-05-17 19:32
 */
public class TestInterfaceProxyHandler {

    private static ForestConfiguration configuration = ForestConfiguration.configuration();


    @Test
    public void testGetProxyFactory() {
        ProxyFactory<GetClient> getClientProxyFactory = new ProxyFactory<>(configuration, GetClient.class);
        InterfaceProxyHandler<GetClient> interfaceProxyHandler =
                new InterfaceProxyHandler(configuration, getClientProxyFactory, GetClient.class);
        assertEquals(getClientProxyFactory, interfaceProxyHandler.getProxyFactory());
    }

    @BaseURL("http://localhost")
    interface LocalhostBaseURLClient {
    }


    @BaseURL("localhost")
    interface NonProtocolBaseURLClient {
    }


    @BaseURL("")
    interface EmptyBaseURLClient {
    }

    interface RenamedMethodClient {

        @Request(url = "http://localhost/misc")
        String test();

        @Request(url = "http://localhost/misc/${0}")
        String test(String a);

    }


    @Test
    public void testBaseURL() {
        ProxyFactory<LocalhostBaseURLClient> getClientProxyFactory = new ProxyFactory<>(configuration, LocalhostBaseURLClient.class);
        InterfaceProxyHandler<LocalhostBaseURLClient> interfaceProxyHandler =
                new InterfaceProxyHandler(configuration, getClientProxyFactory, LocalhostBaseURLClient.class);
        MetaRequest metaRequest = interfaceProxyHandler.getBaseMetaRequest();
        assertEquals("http://localhost", metaRequest.getUrl());

    }

    @Test
    public void testNonProtocolBaseURL() {
        ProxyFactory<NonProtocolBaseURLClient> getClientProxyFactory = new ProxyFactory<>(configuration, NonProtocolBaseURLClient.class);
        InterfaceProxyHandler<NonProtocolBaseURLClient> interfaceProxyHandler =
                new InterfaceProxyHandler(configuration, getClientProxyFactory, NonProtocolBaseURLClient.class);
        MetaRequest metaRequest = interfaceProxyHandler.getBaseMetaRequest();
        assertEquals("localhost", metaRequest.getUrl());
    }



    @Test
    public void testEmptyBaseURL() {
        ProxyFactory<EmptyBaseURLClient> getClientProxyFactory = new ProxyFactory<>(configuration, EmptyBaseURLClient.class);
        InterfaceProxyHandler<EmptyBaseURLClient> interfaceProxyHandler =
                new InterfaceProxyHandler(configuration, getClientProxyFactory, EmptyBaseURLClient.class);
        MetaRequest metaRequest = interfaceProxyHandler.getBaseMetaRequest();
        assertNull(metaRequest.getUrl());
    }


    @Test
    public void testRenamedMethod() {
        ProxyFactory<RenamedMethodClient> getClientProxyFactory = new ProxyFactory<>(configuration, RenamedMethodClient.class);
        InterfaceProxyHandler<RenamedMethodClient> interfaceProxyHandler =
                new InterfaceProxyHandler(configuration, getClientProxyFactory, RenamedMethodClient.class);
    }


}