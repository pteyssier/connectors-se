package org.talend.components.zendesk.sources.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.helpers.CommonHelper;
import org.talend.components.zendesk.helpers.ConfigurationHelper;
import org.talend.components.zendesk.service.http.ZendeskAuthHttpClientService;
import org.talend.components.zendesk.service.http.ZendeskHttpClientService;
import org.talend.components.zendesk.service.zendeskclient.ZendeskClientService;
import org.talend.components.zendesk.sources.Reject;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Output;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.processor.Processor;
import org.zendesk.client.v2.model.Request;
import org.zendesk.client.v2.model.Ticket;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.Serializable;

@Slf4j
@Version(1)
@Icon(value = Icon.IconType.CUSTOM, custom = "zendesk_put")
@Processor(name = "Put")
@Documentation("Data put processor")
public class ZendeskPutSource implements Serializable {

    private final ZendeskPutConfiguration configuration;

    private ZendeskHttpClientService zendeskHttpClientService;

    private ZendeskClientService zendeskClientService;

    public ZendeskPutSource(@Option("configuration") final ZendeskPutConfiguration configuration,
            final ZendeskHttpClientService zendeskHttpClientService,
            final ZendeskAuthHttpClientService zendeskAuthHttpClientService, ZendeskClientService zendeskClientService) {
        this.configuration = configuration;
        this.zendeskHttpClientService = zendeskHttpClientService;
        this.zendeskClientService = zendeskClientService;
        ConfigurationHelper.setupServices(zendeskAuthHttpClientService);
    }

    @ElementListener
    public void onNext(@Input final JsonObject record, final @Output OutputEmitter<JsonObject> success,
            final @Output("reject") OutputEmitter<Reject> reject) throws IOException {
        processOutputElement(record, success, reject);
    }

    private void processOutputElement(final JsonObject record, OutputEmitter<JsonObject> success, OutputEmitter<Reject> reject) {
        log.debug("processOutputElement_local: ");
        try {
            JsonObject newRecord;
            switch (configuration.getDataSet().getSelectionType()) {
            case REQUESTS:
                Request item = getData(record, Request.class);
                newRecord = zendeskHttpClientService.putRequests(configuration.getDataSet().getDataStore(), item);
                break;
            case TICKETS:
                Ticket ticket = getData(record, Ticket.class);
                newRecord = zendeskHttpClientService.putRequests(configuration.getDataSet().getDataStore(), ticket);
                break;
            default:
                throw new UnsupportedOperationException();
            }
            success.emit(newRecord);
        } catch (Exception e) {
            CommonHelper.processException(e, record, reject);
        }
    }

    private Class getDataClass() {
        switch (configuration.getDataSet().getSelectionType()) {
        case TICKETS:
            return Ticket.class;
        }
        throw new UnsupportedOperationException("Unknown selection type");
    }

    private <T> T getData(JsonObject record, final Class<T> clazz) throws IOException {
        return new ObjectMapper().readerFor(clazz).readValue(record.toString());
    }

}