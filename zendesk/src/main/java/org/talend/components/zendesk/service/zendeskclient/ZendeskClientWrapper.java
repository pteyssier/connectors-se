package org.talend.components.zendesk.service.zendeskclient;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.common.AuthenticationType;
import org.talend.components.zendesk.common.ZendeskDataStore;
import org.zendesk.client.v2.Zendesk;

@Slf4j
public class ZendeskClientWrapper {

    @Getter
    private Zendesk zendeskServiceClient;

    public ZendeskClientWrapper(ZendeskDataStore dataStore) {
        if (dataStore.getAuthenticationType() == AuthenticationType.LOGIN_PASSWORD) {
            zendeskServiceClient = new Zendesk.Builder(dataStore.getServerUrl())
                    .setUsername(dataStore.getAuthenticationLoginPasswordConfiguration().getAuthenticationLogin())
                    .setPassword(dataStore.getAuthenticationLoginPasswordConfiguration().getAuthenticationPassword()).build();
        } else if (dataStore.getAuthenticationType() == AuthenticationType.API_TOKEN) {
            zendeskServiceClient = new Zendesk.Builder(dataStore.getServerUrl())
                    .setUsername(dataStore.getAuthenticationApiTokenConfiguration().getAuthenticationLogin())
                    .setToken(dataStore.getAuthenticationApiTokenConfiguration().getApiToken()).build();
        } else {
            throw new UnsupportedOperationException("Unknown authentication type");
        }
    }
}
