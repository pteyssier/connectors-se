package org.talend.components.zendesk.sources.get;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.service.http.ZendeskHttpClientService;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.meta.Documentation;

import javax.annotation.PostConstruct;
import javax.json.JsonObject;
import java.io.Serializable;

@Slf4j
@Documentation("Input data processor")
public class ZendeskGetSource implements Serializable {

    private final ZendeskGetConfiguration configuration;

    private ZendeskHttpClientService zendeskHttpClientService;

    private InputIterator itemIterator;

    public ZendeskGetSource(@Option("configuration") final ZendeskGetConfiguration configuration,
            final ZendeskHttpClientService zendeskHttpClientService) {
        this.configuration = configuration;
        this.zendeskHttpClientService = zendeskHttpClientService;
    }

    @PostConstruct
    public void init() {
        switch (configuration.getDataSet().getSelectionType()) {
        case REQUESTS:
            itemIterator = zendeskHttpClientService.getRequests(configuration.getDataSet().getDataStore());
            break;
        case TICKETS:
            itemIterator = zendeskHttpClientService.getTickets(configuration);
            break;
        }
    }

    @Producer
    public JsonObject next() {
        return itemIterator.next();
    }
}