package org.talend.components.zendesk.common;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.service.http.ZendeskHttpClientService;
import org.talend.sdk.component.api.service.Service;

import java.io.Serializable;

@Slf4j
@Service
public class HealthChecker implements Serializable {

    @Service
    private ZendeskHttpClientService zendeskHttpClientService;

    public boolean checkHealth(ZendeskDataStore dataStore) {
        zendeskHttpClientService.getCurrentUser(dataStore);
        return true;
    }
}