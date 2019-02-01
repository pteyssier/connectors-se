// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.input;

import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_DEDUPE_FIELDS;
import static org.talend.components.marketo.MarketoApiConstants.HEADER_CONTENT_TYPE_APPLICATION_JSON;
import static org.talend.components.marketo.MarketoApiConstants.REQUEST_PARAM_QUERY_METHOD_GET;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.talend.components.marketo.dataset.MarketoInputConfiguration;
import org.talend.components.marketo.service.CustomObjectClient;
import org.talend.components.marketo.service.MarketoService;

public class CustomObjectSource extends MarketoSource {

    private final CustomObjectClient customObjectClient;

    public CustomObjectSource(MarketoInputConfiguration dataSet, //
            final MarketoService service) {
        super(dataSet, service);
        this.customObjectClient = service.getCustomObjectClient();
        this.customObjectClient.base(this.configuration.getDataSet().getDataStore().getEndpoint());
    }

    @Override
    public JsonObject runAction() {
        switch (configuration.getOtherAction()) {
        case describe:
            return describeCustomObjects();
        case list:
            return listCustomObjects();
        case get:
            return getCustomObjects();
        }
        throw new RuntimeException(i18n.invalidOperation());
    }

    private JsonObject listCustomObjects() {
        String names = configuration.getFilterValues();
        return handleResponse(customObjectClient.listCustomObjects(accessToken, names));
    }

    private JsonObject describeCustomObjects() {
        String name = configuration.getCustomObjectName();
        return handleResponse(customObjectClient.describeCustomObjects(accessToken, name));
    }

    private transient static final Logger LOG = getLogger(CustomObjectSource.class);

    private JsonObject getCustomObjects() {
        String name = configuration.getCustomObjectName();
        String filterType = configuration.getFilterType();
        String filterValues = configuration.getFilterValues();
        String fields = configuration.getFields() == null ? null : configuration.getFields().stream().collect(joining(","));
        if (configuration.getUseCompoundKey()) {
            JsonObject payload = generateCompoundKeyPayload(ATTR_DEDUPE_FIELDS, fields);
            return handleResponse(customObjectClient.getCustomObjectsWithCompoundKey(HEADER_CONTENT_TYPE_APPLICATION_JSON, name,
                    REQUEST_PARAM_QUERY_METHOD_GET, accessToken, nextPageToken, payload));

        } else {

            return handleResponse(
                    customObjectClient.getCustomObjects(accessToken, name, filterType, filterValues, fields, nextPageToken));
        }
    }
}
