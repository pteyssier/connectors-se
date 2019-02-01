// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo;

import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_ACCESS_TOKEN;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_CODE;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_ERRORS;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_MESSAGE;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_SUCCESS;
import static org.talend.components.marketo.service.AuthorizationClient.CLIENT_CREDENTIALS;

import java.io.Serializable;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriterFactory;

import org.slf4j.Logger;
import org.talend.components.marketo.dataset.MarketoDataSet;
import org.talend.components.marketo.service.AuthorizationClient;
import org.talend.components.marketo.service.I18nMessage;
import org.talend.components.marketo.service.MarketoService;
import org.talend.components.marketo.service.Toolbox;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.record.Schema.Type;
import org.talend.sdk.component.api.service.http.Response;

public class MarketoSourceOrProcessor implements Serializable {

    protected final MarketoService marketoService;

    protected final I18nMessage i18n;

    protected final AuthorizationClient authorizationClient;

    protected final JsonBuilderFactory jsonFactory;

    protected final JsonReaderFactory jsonReader;

    protected final JsonWriterFactory jsonWriter;

    protected transient String nextPageToken;

    protected transient String accessToken;

    private MarketoDataSet dataSet;

    private transient static final Logger LOG = getLogger(MarketoSourceOrProcessor.class);

    public MarketoSourceOrProcessor(@Option("configuration") final MarketoDataSet dataSet, //
            final MarketoService service, //
            final Toolbox tools) {
        this.dataSet = dataSet;
        this.i18n = tools.getI18n();
        this.jsonFactory = tools.getJsonFactory();
        this.jsonReader = tools.getJsonReader();
        this.jsonWriter = tools.getJsonWriter();
        this.marketoService = service;
        this.authorizationClient = service.getAuthorizationClient();
        this.authorizationClient.base(this.dataSet.getDataStore().getEndpoint());
    }

    @PostConstruct
    public void init() {
        nextPageToken = null;
        retrieveAccessToken();
    }

    public String getAccessToken() {
        if (accessToken == null) {
            retrieveAccessToken();
        }
        return accessToken;
    }

    /**
     * Retrieve an set an access token for using API
     */
    public void retrieveAccessToken() {
        Response<JsonObject> result = authorizationClient.getAuthorizationToken(CLIENT_CREDENTIALS,
                dataSet.getDataStore().getClientId(), dataSet.getDataStore().getClientSecret());
        LOG.debug("[retrieveAccessToken] [{}] : {}.", result.status(), result.body());
        if (result.status() == 200) {
            accessToken = result.body().getString(ATTR_ACCESS_TOKEN);
        } else {
            String error = i18n.accessTokenRetrievalError(result.status(), result.headers().toString());
            LOG.error("[retrieveAccessToken] {}", error);
            throw new RuntimeException(error);
        }
    }

    /**
     * Convert Marketo Errors array to a single String (generally for Exception throwing).
     *
     * @param errors
     * @return flattened string
     */
    public String getErrors(JsonArray errors) {
        StringBuffer error = new StringBuffer();
        for (JsonObject json : errors.getValuesAs(JsonObject.class)) {
            error.append(String.format("[%s] %s", json.getString(ATTR_CODE), json.getString(ATTR_MESSAGE)));
        }

        return error.toString();
    }

    /**
     * Handle a typical Marketo response's payload to API call.
     *
     * @param response the http response
     * @return Marketo API result
     */
    public JsonObject handleResponse(final Response<JsonObject> response) {
        LOG.debug("[handleResponse] [{}] body: {}.", response.status(), response.body());
        if (response.status() == 200) {
            if (response.body().getBoolean(ATTR_SUCCESS)) {
                return response.body();
            } else {
                throw new RuntimeException(getErrors(response.body().getJsonArray(ATTR_ERRORS)));
            }
        }
        throw new RuntimeException(response.error(String.class));
    }

    public JsonObject toJson(final Record record) {
        String recordStr = record.toString().replaceAll("AvroRecord\\{delegate=(.*)\\}$", "$1");
        JsonReader reader = jsonReader.createReader(new StringReader(recordStr));
        Throwable throwable = null;
        JsonObject json;
        try {
            json = reader.readObject();
        } catch (Throwable throwable1) {
            throwable = throwable1;
            throw throwable1;
        } finally {
            if (reader != null) {
                if (throwable != null) {
                    try {
                        reader.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                } else {
                    reader.close();
                }
            }
        }
        return json;
    }

    public Record convertToRecord(final JsonObject json, final Map<String, Schema.Entry> schema) {
        LOG.debug("[convertToRecord] json: {} with schema: {}.", json, schema);
        LOG.debug("[convertToRecord] master schema :{}", schema);
        Record.Builder b = marketoService.getRecordBuilder().newRecordBuilder();
        Set<Entry<String, JsonValue>> props = json.entrySet();
        for (Entry<String, JsonValue> p : props) {
            String key = p.getKey();
            Schema.Entry e = schema.get(key);
            LOG.debug("schema entry : {}.", e);
            Type type = null;
            ValueType jsonType = p.getValue().getValueType();
            if (e == null) {
                type = Type.STRING;
                switch (p.getValue().getValueType()) {
                case NUMBER:
                    // if ("integer".equals())
                    type = Type.DOUBLE;
                    break;
                case TRUE:
                case FALSE:
                    type = Type.BOOLEAN;
                    break;
                case ARRAY:
                    type = Type.ARRAY;
                    break;
                case NULL:
                case OBJECT:
                case STRING:
                    type = Type.STRING;
                    break;
                }
            } else {
                type = e.getType();
                if (Type.LONG.equals(type) && "datetime".equals(e.getComment())) {
                    type = Type.DATETIME;
                }
            }
            LOG.debug("[convertToRecord] {} - {} ({})", p, e, p.getValue().getValueType());
            // try {
            switch (type) {
            case STRING:
                switch (jsonType) {
                case ARRAY:
                    b.withString(key,
                            json.getJsonArray(key).stream().map(jsonValue -> jsonValue.toString()).collect(joining(",")));
                    break;
                case OBJECT:
                    b.withString(key, String.valueOf(json.getJsonObject(key).toString()));
                    break;
                case STRING:
                    b.withString(key, json.getString(key));
                    break;
                case NUMBER:
                    b.withString(key, String.valueOf(json.getJsonNumber(key)));
                    break;
                case TRUE:
                case FALSE:
                    b.withString(key, String.valueOf(json.getBoolean(key)));
                    break;
                case NULL:
                    b.withString(key, null);
                    break;
                }
                break;
            case INT:
                b.withInt(key, jsonType.equals(ValueType.NULL) ? 0 : json.getInt(key));
                break;
            case LONG:
                b.withLong(key, jsonType.equals(ValueType.NULL) ? 0 : json.getJsonNumber(key).longValue());
                break;
            case FLOAT:
                b.withFloat(key, jsonType.equals(ValueType.NULL) ? 0 : Float.valueOf(json.getString(key)));
                break;
            case DOUBLE:
                b.withDouble(key, jsonType.equals(ValueType.NULL) ? 0 : json.getJsonNumber(key).doubleValue());
                break;
            case BOOLEAN:
                b.withBoolean(key, jsonType.equals(ValueType.NULL) ? false : json.getBoolean(key));
                break;
            case DATETIME:
                try {
                    b.withDateTime(key, jsonType.equals(ValueType.NULL) ? null
                            : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(json.getString(key)));
                } catch (ParseException e1) {
                    LOG.error("[convertToRecord] Date parsing error: {}.", e1.getMessage());
                }
                break;
            case ARRAY:
                String ary = json.getJsonArray(key).stream().map(jsonValue -> jsonValue.toString()).collect(joining(","));
                // not in a sub array
                if (!ary.contains("{")) {
                    ary = ary.replaceAll("\"", "").replaceAll("(\\[|\\])", "");
                }
                b.withString(key, ary);
                break;
            case BYTES:
            case RECORD:
                b.withString(key, json.getString(key));
            }
        }

        return b.build();
    }
}
