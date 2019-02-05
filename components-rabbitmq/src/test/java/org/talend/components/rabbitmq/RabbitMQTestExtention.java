package org.talend.components.rabbitmq;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.talend.components.rabbitmq.datastore.RabbitMQDataStore;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.time.Duration;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;
import static org.talend.components.rabbitmq.testutils.RabbitMQTestConstants.HTTP_PORT;
import static org.talend.components.rabbitmq.testutils.RabbitMQTestConstants.PASSWORD;
import static org.talend.components.rabbitmq.testutils.RabbitMQTestConstants.PORT;
import static org.talend.components.rabbitmq.testutils.RabbitMQTestConstants.USER_NAME;

@Slf4j
public class RabbitMQTestExtention implements ExtensionContext.Store.CloseableResource, BeforeAllCallback, ParameterResolver {

    private TestContext testContext = new TestContext();

    private static final GenericContainer RABBITMQ_CONTAINER = new GenericContainer(
            new ImageFromDockerfile().withDockerfileFromBuilder(
                    builder -> builder.from("registry.datapwn.com/vizotenko/components-integration-test-rabbitmq:1.0.0").build()))
                            .withExposedPorts(PORT).waitingFor(Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(200)));

    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        log.info("extension before all start");
        if (!started) {
            started = true;
            RABBITMQ_CONTAINER.start();
            // The following line registers a callback hook when the root test context is shut down
            extensionContext.getRoot().getStore(GLOBAL).put("any unique name", this);
        }

        String dockerHostAddress = RABBITMQ_CONTAINER.getContainerIpAddress();
        Integer amqpPort = RABBITMQ_CONTAINER.getMappedPort(PORT);

        RabbitMQDataStore dataStore = new RabbitMQDataStore();

        dataStore.setHostname(dockerHostAddress);
        dataStore.setPort(amqpPort);
        dataStore.setTLS(true);
        dataStore.setUserName(USER_NAME);
        dataStore.setPassword(PASSWORD);
        testContext.dataStore = dataStore;
        testContext.httpPort = RABBITMQ_CONTAINER.getMappedPort(HTTP_PORT);

        log.info("docker machine: " + dockerHostAddress + ":" + amqpPort);
    }

    @Override
    public void close() {
        log.info("extension close call");
        RABBITMQ_CONTAINER.stop();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return TestContext.class.equals(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return testContext;
    }

    @Data
    public static class TestContext {

        private RabbitMQDataStore dataStore;
        private Integer httpPort;
    }
}