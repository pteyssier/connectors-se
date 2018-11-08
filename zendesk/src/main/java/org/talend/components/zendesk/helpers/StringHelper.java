package org.talend.components.zendesk.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.json.JsonObject;
import javax.json.JsonReaderFactory;
import java.io.StringReader;

public class StringHelper {

    public static final String STRING_CHARSET = "UTF-8";

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static JsonObject objectToJson(Object obj, JsonReaderFactory jsonReaderFactory
    // JsonBuilderFactory jsonBuilderFactory
    ) {
        try {
            String jsonStr = objectMapper.writeValueAsString(obj);
            JsonObject jsonObject = jsonReaderFactory.createReader(new StringReader(jsonStr)).readObject();
            // jsonObject = jsonBuilderFactory.createObjectBuilder(jsonObject).add("obj_type", obj.getClass().toString()).build();
            return jsonObject;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
