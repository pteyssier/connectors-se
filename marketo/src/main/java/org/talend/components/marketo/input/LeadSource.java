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
import static org.talend.components.marketo.MarketoApiConstants.ATTR_ACCESS_TOKEN;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_FIELDS;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_FILTER_TYPE;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_FILTER_VALUES;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_NEXT_PAGE_TOKEN;
import static org.talend.components.marketo.MarketoApiConstants.HEADER_CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED;
import static org.talend.components.marketo.MarketoApiConstants.REQUEST_PARAM_QUERY_METHOD_GET;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.talend.components.marketo.dataset.MarketoInputConfiguration;
import org.talend.components.marketo.service.LeadClient;
import org.talend.components.marketo.service.MarketoService;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.http.Response;

public class LeadSource extends MarketoSource {

    private final LeadClient leadClient;

    private transient static final Logger LOG = getLogger(LeadSource.class);

    public LeadSource(@Option("configuration") final MarketoInputConfiguration configuration, //
            final MarketoService service) {
        super(configuration, service);
        this.leadClient = service.getLeadClient();
        this.leadClient.base(this.configuration.getDataSet().getDataStore().getEndpoint());
    }

    @Override
    public JsonObject runAction() {
        switch (configuration.getLeadAction()) {
        case getLead:
            return getLead();
        case getMultipleLeads:
            return getMultipleLeads();
        case getLeadActivity:
            return getLeadActivities();
        case getLeadChanges:
            return getLeadChanges();
        case describeLead:
            return describeLead();
        }
        throw new RuntimeException(i18n.invalidOperation());
    }

    private JsonObject describeLead() {
        return handleResponse(leadClient.describeLead(accessToken));
    }

    private JsonObject getLead() {
        Integer leadId = configuration.getLeadId();
        String fields = configuration.getFields() == null ? null : configuration.getFields().stream().collect(joining(","));
        return handleResponse(leadClient.getLeadById(accessToken, leadId, fields));
    }

    private Boolean isLeadUrlSizeGreaterThan8k(String filterType, String filterValues, String fields) {
        int pathSize = 20;
        int endpointSize = configuration.getDataSet().getDataStore().getEndpoint().length();
        int queryParameterNamesSize = ATTR_ACCESS_TOKEN.length() + 1 + (accessToken == null ? 0 : accessToken.length()) + //
                ATTR_NEXT_PAGE_TOKEN.length() + 1 + (nextPageToken == null ? 0 : nextPageToken.length()) + //
                endpointSize + //
                pathSize + //
                ATTR_ACCESS_TOKEN.length() + 1 + //
                ATTR_FILTER_TYPE.length() + 1 + //
                ATTR_FILTER_VALUES.length() + 1 + //
                ATTR_FIELDS.length() + 1; //
        int queryParameterValuesSize = (filterType == null ? 0 : filterType.length())
                + (filterValues == null ? 0 : filterValues.length()) + (fields == null ? 0 : fields.length());
        int total = queryParameterNamesSize + queryParameterValuesSize;
        return total >= (8 * 1024);
    }

    private String buildLeadForm(String filterType, String filterValues, String fields) {
        StringBuilder sb = new StringBuilder();
        sb.append(ATTR_FILTER_TYPE + "=" + filterType.trim());
        sb.append("&");
        sb.append(ATTR_FILTER_VALUES + "=" + filterValues.trim());
        sb.append("&");
        sb.append(ATTR_FIELDS + "=" + fields.trim());

        return sb.toString();
    }

    private JsonObject getMultipleLeads() {
        String filterType = configuration.getLeadKeyName();
        String filterValues = configuration.getLeadKeyValues();
        String fields = configuration.getFields() == null ? null : configuration.getFields().stream().collect(joining(","));
        if (isLeadUrlSizeGreaterThan8k(filterType, filterValues, fields)) {
            return handleResponse(leadClient.getLeadByFilterType(HEADER_CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED,
                    REQUEST_PARAM_QUERY_METHOD_GET, accessToken, nextPageToken, buildLeadForm(filterType, filterValues, fields)));
        } else {
            return handleResponse(
                    leadClient.getLeadByFilterTypeByQueryString(accessToken, filterType, filterValues, fields, nextPageToken));
        }
    }

    private JsonObject getLeadActivities() {
        String sinceDateTime = getPagingToken(configuration.getSinceDateTime());
        String activityTypeIds = "";
        if (configuration.getActivityTypeIds().isEmpty()) {

        } else {
            activityTypeIds = configuration.getActivityTypeIds().stream().collect(joining(","));
        }
        String assetIds = configuration.getAssetIds();
        Integer listId = configuration.getListId();
        String leadIds = configuration.getLeadIds();
        return handleResponse(
                leadClient.getLeadActivities(accessToken, sinceDateTime, activityTypeIds, assetIds, listId, leadIds));
    }

    private JsonObject getLeadChanges() {
        if (nextPageToken == null) {
            nextPageToken = getPagingToken(configuration.getSinceDateTime());
        }
        Integer listId = configuration.getListId();
        String leadIds = configuration.getLeadIds();
        String fields = configuration.getFields() == null ? null : configuration.getFields().stream().collect(joining(","));
        return handleResponse(leadClient.getLeadChanges(accessToken, nextPageToken, listId, leadIds, fields));
    }

    public JsonObject getActivities() {
        return handleResponse(leadClient.getActivities(accessToken));
    }

    public String getPagingToken(String dateTime) {
        Response<JsonObject> pt = leadClient.getPagingToken(accessToken, dateTime);
        return pt.body().getString(ATTR_NEXT_PAGE_TOKEN);
    }

}
