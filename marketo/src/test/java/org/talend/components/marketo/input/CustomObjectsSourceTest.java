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

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_NAME;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.marketo.dataset.CompoundKey;
import org.talend.components.marketo.dataset.MarketoDataSet.MarketoEntity;
import org.talend.components.marketo.dataset.MarketoInputConfiguration.OtherEntityAction;
import org.talend.sdk.component.junit.http.junit5.HttpApi;
import org.talend.sdk.component.junit5.WithComponents;

@HttpApi(useSsl = true, responseLocator = org.talend.sdk.component.junit.http.internal.impl.MarketoResponseLocator.class)
@WithComponents("org.talend.components.marketo")
public class CustomObjectsSourceTest extends SourceBaseTest {

    CustomObjectSource source;

    String fields = "createdAt,marketoGUID,updatedAt,VIN,customerId,model,year";

    String CUSTOM_OBJECT_NAME = "car_c";

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        inputConfiguration.getDataSet().setEntity(MarketoEntity.CustomObject);
        inputConfiguration.setFields(asList(fields.split(",")));
        inputConfiguration.setUseCompoundKey(false);
    }

    void initSource() {
        source = new CustomObjectSource(inputConfiguration, service);
        source.init();
    }

    @Test
    void testListCustomObjects() {
        inputConfiguration.setOtherAction(OtherEntityAction.list);
        initSource();
        while ((result = source.next()) != null) {
            assertNotNull(result);
            assertNotNull(result.getString(ATTR_NAME));
        }
    }

    @Test
    void testGetCustomObjects() {
        inputConfiguration.setOtherAction(OtherEntityAction.get);
        inputConfiguration.getDataSet().setCustomObjectName(CUSTOM_OBJECT_NAME);
        inputConfiguration.setFilterType("marketoGUID");
        inputConfiguration.setFilterValues("a215bdf6-3fed-42e5-9042-3c4258768afb");
        initSource();
        while ((result = source.next()) != null) {
            assertNotNull(result);
        }
    }

    @Test
    void testGetCustomObjectsWithCompoundKey() {
        inputConfiguration.setOtherAction(OtherEntityAction.get);
        inputConfiguration.getDataSet().setCustomObjectName(CUSTOM_OBJECT_NAME);
        inputConfiguration.setFilterType("dedupeFields");
        inputConfiguration.setUseCompoundKey(true);
        List<CompoundKey> compoundKey = new ArrayList<>();
        compoundKey.add(new CompoundKey("customerId", "5"));
        compoundKey.add(new CompoundKey("VIN", "ABC-DEF-12345-GIN"));
        inputConfiguration.setCompoundKey(compoundKey);
        initSource();
        while ((result = source.next()) != null) {
            assertNotNull(result);
        }
    }

    @Test
    void testGetCustomObjectsFails() {
        inputConfiguration.setOtherAction(OtherEntityAction.get);
        inputConfiguration.getDataSet().setCustomObjectName(CUSTOM_OBJECT_NAME);
        inputConfiguration.setFilterType("billingCountry");
        inputConfiguration.setFilterValues("France");
        try {
            initSource();
        } catch (RuntimeException e) {
            assertEquals("[1003] Invalid filterType 'billingCountry'", e.getMessage());
        }
    }

}
