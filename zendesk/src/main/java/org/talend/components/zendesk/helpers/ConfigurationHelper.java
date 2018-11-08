package org.talend.components.zendesk.helpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.talend.components.zendesk.service.http.ZendeskAuthHttpClientService;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationHelper {

    public static final String DATA_STORE_ID = "ZendeskDataStore";

    public static final String DATA_SET_ID = "ZendeskDataSet";
    // public static final String DATA_SET_GET_ID = "ZendeskDataSetGet";
    //
    // public static final String DATA_SET_PUT_ID = "ZendeskDataSetPut";

    public static final String DATA_STORE_HEALTH_CHECK = "DataStoreHealthCheck";

    public static final String DISCOVER_SCHEMA_LIST_ID = "DiscoverSchemaList";

    public static void setupServices(ZendeskAuthHttpClientService zendeskAuthHttpClientService) {
    }

}
