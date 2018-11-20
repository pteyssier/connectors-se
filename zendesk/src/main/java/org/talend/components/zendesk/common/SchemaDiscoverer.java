package org.talend.components.zendesk.common;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.service.http.ZendeskHttpClientService;
import org.talend.components.zendesk.sources.get.InputIterator;
import org.talend.components.zendesk.sources.get.ZendeskGetConfiguration;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.service.Service;

import javax.json.JsonValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Documentation("Schema discovering class")
@Service
public class SchemaDiscoverer implements Serializable {

    @Service
    private ZendeskHttpClientService httpClientService;

    public List<String> getColumns(ZendeskGetConfiguration configuration) {
        List<String> result = new ArrayList<>();
        try {
            InputIterator itemIterator = null;
            switch (configuration.getDataSet().getSelectionType()) {
            case REQUESTS:
                itemIterator = httpClientService.getRequests(configuration.getDataSet().getDataStore());
                break;
            case TICKETS:
                itemIterator = httpClientService.getTickets(configuration);
                break;
            }
            if (itemIterator.hasNext()) {
                JsonValue val = itemIterator.next();
                val.asJsonObject().forEach((columnName, value) -> result.add(columnName));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }
}