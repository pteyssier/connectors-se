package org.talend.components.zendesk.service.http;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.common.ZendeskDataStore;
import org.talend.components.zendesk.helpers.StringHelper;
import org.talend.components.zendesk.service.zendeskclient.ZendeskClientService;
import org.talend.components.zendesk.sources.get.InputIterator;
import org.talend.sdk.component.api.service.Service;
import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.Request;
import org.zendesk.client.v2.model.Ticket;
import org.zendesk.client.v2.model.User;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReaderFactory;

@Service
@Slf4j
public class ZendeskHttpClientService {

    @Service
    private ZendeskClientService zendeskClientService;

    @Service
    private JsonReaderFactory jsonReaderFactory;

    @Service
    private JsonBuilderFactory jsonBuilderFactory;

    public User getCurrentUser(ZendeskDataStore dataStore) {
        log.debug("get current user");
        Zendesk zendeskServiceClient = zendeskClientService.getZendeskClientWrapper(dataStore).getZendeskServiceClient();
        User user = zendeskServiceClient.getCurrentUser();
        return user;
    }

    public InputIterator getRequests(ZendeskDataStore dataStore) {
        log.debug("get requests");
        Zendesk zendeskServiceClient = zendeskClientService.getZendeskClientWrapper(dataStore).getZendeskServiceClient();
        Iterable<Request> data = zendeskServiceClient.getRequests();
        return new InputIterator(data.iterator(), jsonReaderFactory);
    }

    public JsonObject putRequest(ZendeskDataStore dataStore, Request request) {
        log.debug("put requests");
        Zendesk zendeskServiceClient = zendeskClientService.getZendeskClientWrapper(dataStore).getZendeskServiceClient();
        Request newItem = zendeskServiceClient.createRequest(request);
        return StringHelper.objectToJson(newItem, jsonReaderFactory);
    }

    public InputIterator getCCRequests(ZendeskDataStore dataStore) {
        log.debug("get CC requests");
        Zendesk zendeskServiceClient = zendeskClientService.getZendeskClientWrapper(dataStore).getZendeskServiceClient();
        Iterable<Request> data = zendeskServiceClient.getCCRequests();
        return new InputIterator(data.iterator(), jsonReaderFactory);
    }

    public InputIterator getTickets(ZendeskDataStore dataStore) {
        log.debug("get tickets");
        Zendesk zendeskServiceClient = zendeskClientService.getZendeskClientWrapper(dataStore).getZendeskServiceClient();
        Iterable<Ticket> data = zendeskServiceClient.getTickets();
        return new InputIterator(data.iterator(), jsonReaderFactory);
    }

    public JsonObject putTicket(ZendeskDataStore dataStore, Ticket ticket) {
        log.debug("put tickets");
        Zendesk zendeskServiceClient = zendeskClientService.getZendeskClientWrapper(dataStore).getZendeskServiceClient();
        Ticket newItem = zendeskServiceClient.createTicket(ticket);
        return StringHelper.objectToJson(newItem, jsonReaderFactory);
    }
}
