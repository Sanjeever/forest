package org.dromara.forest.spring.test;

import org.dromara.forest.Forest;
import org.dromara.forest.config.ForestConfiguration;
import org.dromara.forest.http.ForestAsyncMode;
import org.dromara.forest.spring.test.client2.GithubClient;
import junit.framework.TestCase;
import org.dromara.forest.spring.test.client0.BeastshopClient;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author gongjun[jun.gong@thebeastshop.com]
 * @since 2017-04-24 19:39
 */
public class ClientTest extends TestCase {

    private ClassPathXmlApplicationContext applicationContext;


    public void testScan() {
        applicationContext = new ClassPathXmlApplicationContext(
                new String[] { "classpath:client-test.xml" });
        BeastshopClient beastshopClient =
                (BeastshopClient) applicationContext.getBean("beastshopClient");
        assertNotNull(beastshopClient);
        String result = beastshopClient.index();
        assertNotNull(result);
        ForestConfiguration configuration = Forest.config();
        assertThat(configuration.getConnectTimeout()).isEqualTo(10000);
        assertThat(configuration.getMaxConnections()).isEqualTo(700);
        assertThat(configuration.getMaxRouteConnections()).isEqualTo(600);
        assertThat(configuration.getMaxRequestQueueSize()).isEqualTo(300);
        assertThat(configuration.getMaxAsyncThreadSize()).isEqualTo(256);
        assertThat(configuration.getMaxAsyncQueueSize()).isEqualTo(128);
        assertThat(configuration.getAsyncMode()).isEqualTo(ForestAsyncMode.KOTLIN_COROUTINE);
        Object baseUrl = configuration.getVariableValue("baseUrl");
        assertThat(baseUrl).isNotNull().isEqualTo("http://www.thebeastshop.com");
    }


    public void testGithub() {
        applicationContext = new ClassPathXmlApplicationContext(
                new String[] { "classpath:client-test.xml" });
        GithubClient githubClient =
                (GithubClient) applicationContext.getBean("githubClient");
        String content = githubClient.index();
        assertThat(content).isNotNull();
    }

}