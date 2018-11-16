package org.talend.components.zendesk;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.talend.components.zendesk.common.AuthenticationApiTokenConfiguration;
import org.talend.components.zendesk.common.AuthenticationLoginPasswordConfiguration;
import org.talend.components.zendesk.common.AuthenticationType;
import org.talend.components.zendesk.common.ZendeskDataStore;
import org.talend.components.zendesk.common.UnknownAuthenticationTypeException;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@WithComponents("org.talend.components.zendesk")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ZendeskTest {

    @Test
    public void testConnectionLoginPassword() throws UnknownAuthenticationTypeException {
        AuthenticationLoginPasswordConfiguration authenticationConfiguration = new AuthenticationLoginPasswordConfiguration();
        ZendeskDataStore zendeskDataStore;
        zendeskDataStore = new ZendeskDataStore(null, AuthenticationType.LOGIN_PASSWORD, authenticationConfiguration, null);
        assertEquals(authenticationConfiguration, zendeskDataStore.getAuthSettings());
    }

    @Test
    public void testConnectionApiToken() throws UnknownAuthenticationTypeException {
        AuthenticationApiTokenConfiguration authenticationConfiguration = new AuthenticationApiTokenConfiguration();
        ZendeskDataStore zendeskDataStore;
        zendeskDataStore = new ZendeskDataStore(null, AuthenticationType.API_TOKEN, null, authenticationConfiguration);
        assertEquals(authenticationConfiguration, zendeskDataStore.getAuthSettings());
    }
}
