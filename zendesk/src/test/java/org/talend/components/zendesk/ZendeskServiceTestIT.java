package org.talend.components.zendesk;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.talend.components.zendesk.service.ZendeskService;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Suite of test for the Zendesk components")
@WithComponents("org.talend.components.zendesk")
@ExtendWith(ZendeskTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ZendeskServiceTestIT {

    @Service
    private ZendeskService zendeskService;

    private ZendeskTestExtension.TestContext testContext;

    @BeforeAll
    private void init(ZendeskTestExtension.TestContext testContext) {
        log.info("init service test");
        this.testContext = testContext;
    }

    @Test
    @DisplayName("Health check")
    void healthCheckTest() {
        log.info("Integration test 'Health Check' start " + testContext);
        HealthCheckStatus healthCheckStatus = zendeskService.validateBasicConnection(testContext.getDataStoreLoginPassword());
        assertEquals(HealthCheckStatus.Status.OK, healthCheckStatus.getStatus());
    }

}
