package org.talend.components.zendesk.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.common.HealthChecker;
import org.talend.components.zendesk.common.ZendeskDataSet;
import org.talend.components.zendesk.common.ZendeskDataStore;
import org.talend.components.zendesk.helpers.ConfigurationHelper;
import org.talend.components.zendesk.messages.Messages;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.asyncvalidation.AsyncValidation;
import org.talend.sdk.component.api.service.asyncvalidation.ValidationResult;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.api.service.schema.DiscoverSchema;
import org.zendesk.client.v2.model.Request;
import org.zendesk.client.v2.model.Ticket;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

@Service
@Slf4j
public class ZendeskService {

    @Service
    private Messages i18n;

    @Service
    private HealthChecker healthChecker;

    @Service
    private RecordBuilderFactory recordBuilderFactory;

    @DiscoverSchema(ConfigurationHelper.DISCOVER_SCHEMA_LIST_ID)
    public Schema guessTableSchemaList(final ZendeskDataSet dataSet) {
        Schema.Builder schemaBuilder = recordBuilderFactory.newSchemaBuilder(Schema.Type.RECORD);
        Class clazz = null;
        switch (dataSet.getSelectionType()) {
        case REQUESTS:
            // case REQUESTS_CC:
            clazz = Request.class;
            break;
        case TICKETS:
            clazz = Ticket.class;
            break;
        }

        if (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    Schema.Entry schemaEntry = recordBuilderFactory.newEntryBuilder().withName(getFieldName(clazz, field))
                            .withType(getSchemaType(field)).build();
                    schemaBuilder.withEntry(schemaEntry);
                } catch (NoSuchMethodException e) {

                }
            }
        }
        return schemaBuilder.build();
    }

    private String getFieldName(Class parentClass, Field field) throws NoSuchMethodException {
        // JsonProperty annotation = field.getDeclaredAnnotation(JsonProperty.class);
        String fieldName = field.getName();
        JsonProperty annotation = parentClass.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1))
                .getAnnotation(JsonProperty.class);

        if (annotation != null && !annotation.value().isEmpty()) {
            fieldName = annotation.value();
        }
        return fieldName;
    }

    private Schema.Type getSchemaType(Field field) {
        Schema.Type schemaType;
        if (field.isEnumConstant() || field.getType().equals(String.class)) {
            schemaType = Schema.Type.STRING;
        } else if (Arrays.<Class> asList(Long.class).contains(field.getType())) {
            schemaType = Schema.Type.LONG;
        } else if (Arrays.<Class> asList(Integer.class, Short.class, Byte.class).contains(field.getType())) {
            schemaType = Schema.Type.INT;
        } else if (Arrays.<Class> asList(Boolean.class).contains(field.getType())) {
            schemaType = Schema.Type.BOOLEAN;
        } else if (Arrays.<Class> asList(Date.class).contains(field.getType())) {
            schemaType = Schema.Type.DATETIME;
        } else {
            schemaType = Schema.Type.RECORD;
        }
        return schemaType;
    }

    @HealthCheck(ConfigurationHelper.DATA_STORE_HEALTH_CHECK)
    public HealthCheckStatus validateBasicConnection(@Option final ZendeskDataStore dataStore) {
        try {
            log.debug("start health check");
            ConfigurationHelper.setupServices();
            healthChecker.checkHealth(dataStore);
        } catch (Exception e) {
            return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18n.healthCheckFailed(e.getMessage()));
        }
        return new HealthCheckStatus(HealthCheckStatus.Status.OK, i18n.healthCheckOk());
    }

    @AsyncValidation("validateServerUrl")
    public ValidationResult validateServerUrl(String serverUrl) {
        if (serverUrl.isEmpty()) {
            return new ValidationResult(ValidationResult.Status.KO, i18n.healthCheckServerUrlIsEmpty());
        }
        return new ValidationResult(ValidationResult.Status.OK, "");
    }

    @AsyncValidation("validateAuthenticationLogin")
    public ValidationResult validateAuthenticationLogin(String authenticationLogin) {
        if (authenticationLogin.isEmpty()) {
            return new ValidationResult(ValidationResult.Status.KO, i18n.healthCheckLoginIsEmpty());
        }
        return new ValidationResult(ValidationResult.Status.OK, "");
    }

    @AsyncValidation("validateApiToken")
    public ValidationResult validateApiToken(String apiToken) {
        if (apiToken.isEmpty()) {
            return new ValidationResult(ValidationResult.Status.KO, i18n.healthCheckLoginIsEmpty());
        }
        return new ValidationResult(ValidationResult.Status.OK, "");
    }
}