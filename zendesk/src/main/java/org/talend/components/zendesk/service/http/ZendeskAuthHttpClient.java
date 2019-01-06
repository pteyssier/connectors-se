package org.talend.components.zendesk.service.http;

import org.talend.sdk.component.api.service.http.*;

import javax.json.JsonObject;

public interface ZendeskAuthHttpClient extends HttpClient {

    String HEADER_Content_Type = "Content-Type";

    @Request(method = "POST", path = "{requestPath}")
    Response<JsonObject> getToken(@Path("requestPath") String requestPath, @Header(HEADER_Content_Type) String contentType,
            String body);

    default Response<JsonObject> getToken(String requestPath, String body) {
        return getToken(requestPath, "application/x-www-form-urlencoded", body);
    }

}
