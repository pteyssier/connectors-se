package org.talend.components.zendesk.service.zendeskclient;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.common.ZendeskDataStore;
import org.talend.sdk.component.api.service.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ZendeskClientService {

    private Map<ZendeskDataStore, ZendeskClientWrapper> zendeskClients = new ConcurrentHashMap<>();

    public ZendeskClientWrapper getZendeskClientWrapper(ZendeskDataStore zendeskDataStore) {
        return zendeskClients.computeIfAbsent(zendeskDataStore, key -> new ZendeskClientWrapper(zendeskDataStore));
    }

}
