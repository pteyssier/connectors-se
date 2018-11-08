package org.talend.components.zendesk.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import javax.json.JsonObject;
import javax.json.JsonReaderFactory;
import java.io.StringReader;

public class StringHelper {

    public static final String STRING_CHARSET = "UTF-8";

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static JsonObject objectToJson(Object obj, JsonReaderFactory jsonReaderFactory) {
        try {
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.setDateFormat(StdDateFormat.getDateTimeInstance());
            String jsonStr = objectMapper.writeValueAsString(obj);
            JsonObject jsonObject = jsonReaderFactory.createReader(new StringReader(jsonStr)).readObject();
            return jsonObject;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
