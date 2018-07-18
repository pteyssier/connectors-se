package org.talend.components.netsuite.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.avro.Schema;
import org.talend.components.netsuite.runtime.avro.converter.Converter;
import org.talend.components.netsuite.runtime.avro.converter.EnumToStringConverter;
import org.talend.components.netsuite.runtime.avro.converter.NullConverter;
import org.talend.components.netsuite.runtime.avro.converter.ObjectToJsonConverter;
import org.talend.components.netsuite.runtime.avro.converter.Unconverted;
import org.talend.components.netsuite.runtime.avro.converter.XMLGregorianCalendarToDateTimeConverter;
import org.talend.components.netsuite.runtime.client.MetaDataSource;
import org.talend.components.netsuite.runtime.client.NetSuiteClientService;
import org.talend.components.netsuite.runtime.client.NsRef;
import org.talend.components.netsuite.runtime.json.NsTypeResolverBuilder;
import org.talend.components.netsuite.runtime.model.CustomFieldDesc;
import org.talend.components.netsuite.runtime.model.FieldDesc;
import org.talend.components.netsuite.runtime.model.RecordTypeInfo;
import org.talend.components.netsuite.runtime.model.SimpleFieldDesc;
import org.talend.components.netsuite.runtime.model.TypeDesc;
import org.talend.components.netsuite.runtime.model.beans.BeanInfo;
import org.talend.components.netsuite.runtime.model.beans.Beans;
import org.talend.components.netsuite.runtime.model.customfield.CustomFieldRefType;
import org.talend.components.netsuite.runtime.schema.NetSuiteSchemaConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import lombok.Data;

/**
 * Responsible for translating of NetSuite data object to/from {@code IndexedRecord}.
 */

@Data
public abstract class NsObjectTransducer {

    /** NetSuite client used. */
    protected NetSuiteClientService<?> clientService;

    /** Source of meta data used. */
    protected MetaDataSource metaDataSource;

    /** XML data type factory used. */
    protected final DatatypeFactory datatypeFactory;

    /** JSON-Object mapper used. */
    protected final ObjectMapper objectMapper;

    /** Cached value converters by value class. */
    protected Map<Class<?>, Converter<?, ?>> valueConverterCache = new HashMap<>();

    /**
     * Creates instance of transducer using given NetSuite client.
     *
     * @param clientService client to be used
     */
    protected NsObjectTransducer(NetSuiteClientService<?> clientService) {
        this.clientService = clientService;

        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            // TODO:
            throw new RuntimeException();
            // throw new ComponentException(e);
        }

        objectMapper = new ObjectMapper();

        // Customize typing of JSON objects.
        objectMapper.setDefaultTyping(new NsTypeResolverBuilder(clientService.getBasicMetaData()));

        // Register JAXB annotation module to perform mapping of data model objects to/from JSON.
        JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
        objectMapper.registerModule(jaxbAnnotationModule);

        setMetaDataSource(clientService.getMetaDataSource());
    }

    /**
     * Get dynamic schema using given type descriptor and design schema.
     *
     * @param typeDesc NetSuite data model object type descriptor
     * @param designSchema design schema
     * @param targetSchemaName name of target schema
     * @return schema with all fields
     */
    protected Schema getDynamicSchema(TypeDesc typeDesc, Schema designSchema, String targetSchemaName) {
        RecordTypeInfo recordTypeInfo = metaDataSource.getRecordType(typeDesc.getTypeName());

        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();

        String dynamicPosProp = designSchema.getProp(NetSuiteSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION);
        List<Schema.Field> fields = new ArrayList<>();

        if (dynamicPosProp != null) {
            Set<String> designFieldNames = new HashSet<>(designSchema.getFields().size());
            for (Schema.Field field : designSchema.getFields()) {
                String fieldName = NetSuiteDatasetRuntimeImpl.getNsFieldName(field);
                designFieldNames.add(fieldName);
            }

            int dynPos = Integer.parseInt(dynamicPosProp);
            int dynamicColumnSize = fieldMap.size() - designSchema.getFields().size();

            List<FieldDesc> dynaFieldDescList = new ArrayList<>(dynamicColumnSize);
            for (FieldDesc fieldDesc : fieldMap.values()) {
                String fieldName = fieldDesc.getName();
                if (!designFieldNames.contains(fieldName)) {
                    dynaFieldDescList.add(fieldDesc);
                }
            }

            if (designSchema.getFields().size() > 0) {
                for (Schema.Field field : designSchema.getFields()) {
                    // Dynamic column is first or middle column in design schema
                    if (dynPos == field.pos()) {
                        for (int i = 0; i < dynamicColumnSize; i++) {
                            // Add dynamic schema fields
                            FieldDesc fieldDesc = dynaFieldDescList.get(i);
                            fields.add(createSchemaField(fieldDesc));
                        }
                    }

                    // Add fields of design schema
                    Schema.Field avroField = new Schema.Field(field.name(), field.schema(), null, field.defaultVal());
                    Map<String, Object> fieldProps = field.getObjectProps();
                    for (String propName : fieldProps.keySet()) {
                        Object propValue = fieldProps.get(propName);
                        if (propValue != null) {
                            avroField.addProp(propName, propValue);
                        }
                    }

                    fields.add(avroField);

                    // Dynamic column is last column in design schema
                    if (field.pos() == (designSchema.getFields().size() - 1) && dynPos == (field.pos() + 1)) {
                        for (int i = 0; i < dynamicColumnSize; i++) {
                            // Add dynamic schema fields
                            FieldDesc fieldDesc = dynaFieldDescList.get(i);
                            fields.add(createSchemaField(fieldDesc));
                        }
                    }
                }
            } else {
                // All fields are included in dynamic schema
                for (String fieldName : fieldMap.keySet()) {
                    FieldDesc fieldDesc = fieldMap.get(fieldName);
                    fields.add(createSchemaField(fieldDesc));
                }
            }
        } else {
            // All fields are included in dynamic schema
            for (String fieldName : fieldMap.keySet()) {
                FieldDesc fieldDesc = fieldMap.get(fieldName);
                fields.add(createSchemaField(fieldDesc));
            }
        }

        Schema schema = Schema.createRecord(targetSchemaName, null, null, false, fields);
        NetSuiteDatasetRuntimeImpl.augmentSchemaWithCustomMetaData(metaDataSource, schema, recordTypeInfo, typeDesc.getFields());
        return schema;
    }

    /**
     * Create schema field for given NetSuite data model object field descriptor.
     *
     * @param fieldDesc field descriptor
     * @return schema field
     */
    protected Schema.Field createSchemaField(FieldDesc fieldDesc) {
        Schema avroFieldType = NetSuiteDatasetRuntimeImpl.inferSchemaForField(fieldDesc);
        Schema.Field avroField = new Schema.Field(fieldDesc.getName(), avroFieldType, null, (Object) null);
        return avroField;
    }

    /**
     * Build and get map of field values by names, including custom fields.
     *
     * <p>
     * Custom fields in data model object are stored in separate {@code customFieldList} field
     * as list of {@code CustomFieldRef} objects.
     *
     * @param nsObject NetSuite data model object which to extract field values from
     * @param schema target schema
     * @param typeDesc type descriptor
     * @return table of fields' values by field names
     */
    protected Map<String, Object> getMapView(Object nsObject, Schema schema, TypeDesc typeDesc) {
        Map<String, Object> valueMap = new HashMap<>();

        BeanInfo beanInfo = Beans.getBeanInfo(typeDesc.getTypeClass());
        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();

        Map<String, CustomFieldDesc> customFieldMap = new HashMap<>();

        // Extract normal fields

        for (Schema.Field field : schema.getFields()) {
            // Get actual name of the field
            String nsFieldName = NetSuiteDatasetRuntimeImpl.getNsFieldName(field);
            FieldDesc fieldDesc = fieldMap.get(nsFieldName);

            if (fieldDesc == null) {
                continue;
            }

            if (fieldDesc instanceof CustomFieldDesc) {
                // It's custom field, we will extract it in next stage.
                customFieldMap.put(nsFieldName, (CustomFieldDesc) fieldDesc);
            } else {
                Object value = Beans.getSimpleProperty(nsObject, fieldDesc.getName());
                valueMap.put(nsFieldName, value);
            }
        }

        // Extract custom fields

        if (!customFieldMap.isEmpty() && beanInfo.getProperty("customFieldList") != null) {
            List<?> customFieldList = (List<?>) Beans.getProperty(nsObject, "customFieldList.customField");
            if (customFieldList != null && !customFieldList.isEmpty()) {
                // Traverse all received custom fields and extract fields specified in schema
                for (Object customField : customFieldList) {
                    String scriptId = (String) Beans.getSimpleProperty(customField, "scriptId");
                    CustomFieldDesc customFieldInfo = customFieldMap.get(scriptId);
                    if (customFieldInfo != null) {
                        String fieldName = customFieldInfo.getName();
                        valueMap.put(fieldName, customField);
                    }
                }
            }
        }

        return valueMap;
    }

    /**
     * Read a value from a field.
     *
     * @param valueMap map containing raw values by names
     * @param fieldDesc field descriptor
     * @return value of a field or <code>null</code>
     */
    protected Object readField(Map<String, Object> valueMap, FieldDesc fieldDesc) {
        String fieldName = fieldDesc.getName();
        Converter valueConverter = getValueConverter(fieldDesc);
        if (fieldDesc instanceof CustomFieldDesc) {
            Object customField = valueMap.get(fieldName);
            if (customField != null) {
                Object value = Beans.getSimpleProperty(customField, "value");
                return valueConverter.convertToAvro(value);
            }
            return null;
        } else {
            Object value = valueMap.get(fieldName);
            return valueConverter.convertToAvro(value);
        }
    }

    /**
     * Write a value to a field.
     *
     * @param nsObject target NetSuite data model object which to write field value to
     * @param fieldDesc field descriptor
     * @param customFieldMap map of native custom field objects by names
     * @param nullFieldNames collection to register null'ed fields
     * @param value value to be written, can be <code>null</code>
     */
    protected void writeField(Object nsObject, FieldDesc fieldDesc, Map<String, Object> customFieldMap,
            Collection<String> nullFieldNames, Object value) {
        writeField(nsObject, fieldDesc, customFieldMap, true, nullFieldNames, value);
    }

    /**
     * Write a value to a field.
     *
     * @param nsObject target NetSuite data model object which to write field value to
     * @param fieldDesc field descriptor
     * @param customFieldMap map of native custom field objects by names
     * @param replace specifies whether to forcibly replace a field's value
     * @param nullFieldNames collection to register null'ed fields
     * @param value value to be written, can be <code>null</code>
     */
    protected void writeField(Object nsObject, FieldDesc fieldDesc, Map<String, Object> customFieldMap, boolean replace,
            Collection<String> nullFieldNames, Object value) {
        if (fieldDesc instanceof CustomFieldDesc) {
            writeCustomField(nsObject, fieldDesc.asCustom(), customFieldMap, replace, nullFieldNames, value);
        } else {
            writeSimpleField(nsObject, fieldDesc.asSimple(), replace, nullFieldNames, value);
        }
    }

    /**
     * Write a custom field which is not defined by NetSuite standard data model.
     *
     * @param nsObject target NetSuite data model object which to write field value to
     * @param fieldDesc field descriptor
     * @param customFieldMap map of native custom field objects by names
     * @param replace specifies whether to forcibly replace a field's value
     * @param nullFieldNames collection to register null'ed fields
     * @param value value to be written, can be <code>null</code>
     */
    protected void writeCustomField(Object nsObject, CustomFieldDesc fieldDesc, Map<String, Object> customFieldMap,
            boolean replace, Collection<String> nullFieldNames, Object value) {

        NsRef ref = fieldDesc.getCustomizationRef();
        CustomFieldRefType customFieldRefType = fieldDesc.getCustomFieldType();

        // Create custom field list wrapper if required
        Object customFieldListWrapper = Beans.getSimpleProperty(nsObject, "customFieldList");
        if (customFieldListWrapper == null) {
            customFieldListWrapper = clientService.getBasicMetaData().createInstance("CustomFieldList");
            Beans.setSimpleProperty(nsObject, "customFieldList", customFieldListWrapper);
        }
        List<Object> customFieldList = (List<Object>) Beans.getSimpleProperty(customFieldListWrapper, "customField");

        Object customField = customFieldMap.get(ref.getScriptId());
        Converter valueConverter = getValueConverter(fieldDesc);

        Object targetValue = valueConverter.convertToDatum(value);

        if (targetValue == null) {
            if (replace && customField != null && customFieldList != null) {
                customFieldList.remove(customField);
                nullFieldNames.add(fieldDesc.getName());
            }
        } else {
            if (customField == null) {
                // Custom field instance doesn't exist,
                // create new instance and set identifiers
                customField = clientService.getBasicMetaData().createInstance(customFieldRefType.getTypeName());
                Beans.setSimpleProperty(customField, "scriptId", ref.getScriptId());
                Beans.setSimpleProperty(customField, "internalId", ref.getInternalId());

                customFieldList.add(customField);
                customFieldMap.put(ref.getScriptId(), customField);
            }

            Beans.setSimpleProperty(customField, "value", targetValue);
        }
    }

    /**
     * Write a value to a simple field which is defined by NetSuite standard data model.
     *
     * @param nsObject target NetSuite data model object which to write field value to
     * @param fieldDesc field descriptor
     * @param replace specifies whether to forcibly replace a field's value
     * @param nullFieldNames collection to register null'ed fields
     * @param value value to be written, can be <code>null</code>
     */
    protected void writeSimpleField(Object nsObject, SimpleFieldDesc fieldDesc, boolean replace,
            Collection<String> nullFieldNames, Object value) {

        Converter valueConverter = getValueConverter(fieldDesc);

        Object targetValue = valueConverter.convertToDatum(value);

        if (targetValue == null) {
            if (replace) {
                Beans.setSimpleProperty(nsObject, fieldDesc.getPropertyName(), null);
                nullFieldNames.add(fieldDesc.getName());
            }
        } else {
            Beans.setSimpleProperty(nsObject, fieldDesc.getPropertyName(), targetValue);
        }
    }

    /**
     * Determine value class for given custom field type.
     *
     * @param customFieldRefType custom field type
     * @return value class or {@code null} for
     * {@link CustomFieldRefType#SELECT} and {@link CustomFieldRefType#MULTI_SELECT} types
     */
    protected Class<?> getCustomFieldValueConverterTargetClass(CustomFieldRefType customFieldRefType) {
        Class<?> valueClass;
        switch (customFieldRefType) {
        case BOOLEAN:
            valueClass = Boolean.class;
            break;
        case STRING:
            valueClass = String.class;
            break;
        case LONG:
            valueClass = Long.class;
            break;
        case DOUBLE:
            valueClass = Double.class;
            break;
        case DATE:
            valueClass = XMLGregorianCalendar.class;
            break;
        case SELECT:
            valueClass = getPicklistClass();
            break;
        case MULTI_SELECT:
        default:
            valueClass = null;
            break;
        }
        return valueClass;
    }

    /**
     * Get value converter for given field descriptor.
     *
     * @param fieldDesc field descriptor
     * @return value converter
     */
    public Converter<?, ?> getValueConverter(FieldDesc fieldDesc) {
        Class<?> valueClass;
        if (fieldDesc instanceof CustomFieldDesc) {
            CustomFieldDesc customFieldDesc = (CustomFieldDesc) fieldDesc;
            CustomFieldRefType customFieldRefType = customFieldDesc.getCustomFieldType();
            valueClass = getCustomFieldValueConverterTargetClass(customFieldRefType);
        } else {
            valueClass = fieldDesc.getValueType();
        }

        Converter<?, ?> converter = null;
        if (valueClass != null) {
            converter = getValueConverter(valueClass);
        }
        if (converter == null) {
            converter = new NullConverter<>(valueClass, null);
        }
        return converter;
    }

    protected abstract String getApiVersion();

    public Class<?> getPicklistClass() {
        String version = getApiVersion();
        String pattern = "20\\d{2}\\.\\d+";
        if (version != null && Pattern.matches(pattern, version)) {
            Class<?> valueClass;
            try {
                valueClass = Class
                        .forName("com.netsuite.webservices.v" + version.replace('.', '_') + ".platform.core.ListOrRecordRef");
            } catch (ClassNotFoundException e) {
                return null;
                // ignore
            }
            return valueClass;
        }
        return null;
    }

    /**
     * Get value converter for given class.
     *
     * <p>
     * Converters are created on demand and cached.
     *
     * @param valueClass value class
     * @return value converter or {@code null}
     */
    public Converter<?, ?> getValueConverter(Class<?> valueClass) {
        Converter<?, ?> converter = valueConverterCache.get(valueClass);
        if (converter == null) {
            converter = createValueConverter(valueClass);
            if (converter != null) {
                valueConverterCache.put(valueClass, converter);
            }
        }
        return converter;
    }

    /**
     * Create new instance of value converter for given class.
     *
     * @param valueClass value class
     * @return value converter or {@code null}
     */
    protected Converter<?, ?> createValueConverter(Class<?> valueClass) {
        if (valueClass == Boolean.TYPE || valueClass == Boolean.class || valueClass == Integer.TYPE || valueClass == Integer.class
                || valueClass == Long.TYPE || valueClass == Long.class || valueClass == Double.TYPE || valueClass == Double.class
                || valueClass == String.class) {
            return new Unconverted<>(valueClass, null);
        } else if (valueClass == XMLGregorianCalendar.class) {
            return new XMLGregorianCalendarToDateTimeConverter(datatypeFactory);
        } else if (valueClass.isEnum()) {
            Class<Enum<?>> enumClass = (Class<Enum<?>>) valueClass;
            return new EnumToStringConverter<>((Class<Enum>) valueClass, Beans.getEnumAccessor((enumClass)));
        } else if (!valueClass.isPrimitive()) {
            return new ObjectToJsonConverter<>(valueClass, objectMapper);
        }
        return null;
    }

}