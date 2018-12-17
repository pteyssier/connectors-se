package org.talend.components.zendesk;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.talend.components.zendesk.service.ZendeskService;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit5.WithComponents;

@Slf4j
@WithComponents("org.talend.components.zendesk")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ZendeskServiceTest {

    @Service
    private ZendeskService zendeskService;

    // @Test
    // @DisplayName("validateServerUrl")
    // void validateServerUrl() {
    // log.info("Service test 'validateServerUrl'");
    //
    // Assertions.assertEquals(ValidationResult.Status.OK, zendeskService.validateServerUrl("non empty url").getStatus());
    // Assertions.assertEquals(ValidationResult.Status.KO, zendeskService.validateServerUrl("").getStatus());
    //
    // Assertions.assertEquals(ValidationResult.Status.OK,
    // zendeskService.validateAuthenticationLogin("non empty Authentication login").getStatus());
    // Assertions.assertEquals(ValidationResult.Status.KO, zendeskService.validateAuthenticationLogin("").getStatus());
    //
    // Assertions.assertEquals(ValidationResult.Status.OK, zendeskService.validateApiToken("non empty API token").getStatus());
    // Assertions.assertEquals(ValidationResult.Status.KO, zendeskService.validateApiToken("").getStatus());
    // }
}
