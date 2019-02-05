package org.talend.components.rabbitmq.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.talend.components.rabbitmq.RabbitMQTestExtention;
import org.talend.components.rabbitmq.datastore.RabbitMQDataStore;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithComponents("org.talend.components.rabbitmq")
@ExtendWith(RabbitMQTestExtention.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActionServiceTestIT {

    @Service
    private ActionService actionService;

    private RabbitMQTestExtention.TestContext testContext;

    private RabbitMQDataStore dataStore;

    @BeforeAll
    private void init(RabbitMQTestExtention.TestContext testContext) throws InterruptedException {
        this.testContext = testContext;
        this.dataStore = testContext.getDataStore();
        Thread.sleep(6000);
    }

    @Test
    public void testSuccessfulConnection() {
        HealthCheckStatus status = actionService.validateBasicDatastore(dataStore);

        assertEquals(HealthCheckStatus.Status.OK, status.getStatus());
    }
}
