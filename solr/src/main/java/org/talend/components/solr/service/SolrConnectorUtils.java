package org.talend.components.solr.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.talend.components.solr.common.FilterCriteria;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.schema.Schema;
import org.talend.sdk.component.api.service.schema.Type;

import javax.json.JsonObject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class SolrConnectorUtils {

    private static final String SOLR_FIELD_PROPERTY_INDEXED = "indexed";

    private static final String SOLR_FIELD_PROPERTY_STORED = "stored";

    private static final String SOLR_FIELD_PROPERTY_NAME = "name";

    private static final String SOLR_FIELD_PROPERTY_TYPE = "type";

    private static final Set SOLR_FIELD_PROPERTY_TYPES_DOUBLE = Stream.of("pdouble", "pfloat").collect(Collectors.toSet());

    private static final Set SOLR_FIELD_PROPERTY_TYPES_INT = Stream.of("plong", "pint").collect(Collectors.toSet());

    private static final Set SOLR_FIELD_PROPERTY_TYPES_BOOL = Stream.of("boolean").collect(Collectors.toSet());

    public String trimQuotes(String value) {
        int length = value.length();
        if (length >= 2 && (value.charAt(0) == '"' || value.charAt(0) == '\'')
                && (value.charAt(length - 1) == '"' || value.charAt(length - 1) == '\'')) {
            return value.substring(1, length - 1);
        }
        return value;
    }

    public String createQueryFromRecord(JsonObject record) {
        StringBuilder query = new StringBuilder();
        Set<String> keySet = record.keySet();
        boolean isFirst = true;
        for (String key : keySet) {
            String value = getStringValue(key, record);
            if (StringUtils.isNotBlank(checkQuotes(value))) {
                String subQuery = (isFirst ? "" : " AND ") + key + ":" + checkQuotes(value);
                query.append(subQuery);
                isFirst = false;
            }
        }

        return query.toString();
    }

    private String getStringValue(String key, JsonObject record) {
        return record.get(key) != null ? record.get(key).toString() : null;
    }

    private String checkQuotes(String value) {
        return addQuotes(trimQuotes(value));
    }

    private String addQuotes(String value) {
        int length = value.length();
        if (length >= 2 && !(value.charAt(0) == '"' && value.charAt(length - 1) == '"')
                && StringUtils.containsWhitespace(value)) {
            return "\"" + value + "\"";
        }
        return value;
    }

    public Schema getSchemaFromRepresentation(SchemaRepresentation representation) {
        if (representation == null) {
            return new Schema(Collections.emptyList());
        }
        List<Map<String, Object>> fields = representation.getFields();
        List<Schema.Entry> entries = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            String fieldName = getFieldName(field);
            if (fieldName != null && checkIndexed(field) && checkStored(field)) {
                entries.add(new Schema.Entry(fieldName, getFieldType(field)));
            }
        }
        return new Schema(entries);
    }

    private boolean checkIndexed(Map<String, Object> field) {
        Object indexed = field.get(SOLR_FIELD_PROPERTY_INDEXED);
        return (indexed == null || indexed.equals(true));
    }

    private boolean checkStored(Map<String, Object> field) {
        Object stored = field.get(SOLR_FIELD_PROPERTY_STORED);
        return (stored == null || stored.equals(true));
    }

    private String getFieldName(Map<String, Object> field) {
        Object name = field.get(SOLR_FIELD_PROPERTY_NAME);
        if (name == null) {
            return null;
        }
        return name.toString();
    }

    private Type getFieldType(Map<String, Object> field) {
        Object type = field.get(SOLR_FIELD_PROPERTY_TYPE);
        if (SOLR_FIELD_PROPERTY_TYPES_INT.contains(type)) {
            return Type.INT;
        } else if (SOLR_FIELD_PROPERTY_TYPES_BOOL.contains(type)) {
            return Type.BOOLEAN;
        } else if (SOLR_FIELD_PROPERTY_TYPES_DOUBLE.contains(type)) {
            return Type.DOUBLE;
        } else {
            return Type.STRING;
        }
    }

    public Collection<String> getCoreListFromResponse(CoreAdminResponse cores) {
        if (cores != null) {
            return IntStream.range(0, cores.getCoreStatus().size()).mapToObj(i -> cores.getCoreStatus().getName(i))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void addFilterQuery(FilterCriteria row, SolrQuery query) {
        String field = row.getField();
        String value = row.getValue();
        if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(value)) {
            query.addFilterQuery(field + ":" + value);
        }
    }

    public String wrapFqValue(String fqValue) {
        if (fqValue.contains(" ")) {
            return "\"" + fqValue + "\"";
        }
        return fqValue;
    }

    public Integer parseInt(String value) {
        Integer result = 0;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn(e.getMessage());
        }
        return result;
    }

    public String getMessages(Throwable e) {
        Set<String> messages = new LinkedHashSet<>();
        while (e != null) {
            if (StringUtils.isNotBlank(e.getMessage())) {
                messages.add(e.getMessage().trim());
            }
            e = e.getCause();
        }
        return messages.stream().collect(Collectors.joining("\n"));
    }

    public String getCustomLocalizedMessage(String message, Messages i18n) {
        if (message.contains("Bad credentials")) {
            return i18n.badCredentials();
        }
        return message;
    }

}