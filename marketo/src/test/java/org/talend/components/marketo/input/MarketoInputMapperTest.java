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

import static org.junit.Assert.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.marketo.MarketoBaseTest;
import org.talend.components.marketo.dataset.MarketoDataSet.MarketoEntity;
import org.talend.sdk.component.junit5.WithComponents;

@WithComponents("org.talend.components.marketo")
class MarketoInputMapperTest extends MarketoBaseTest {

    MarketoInputMapper mapper;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        mapper = new MarketoInputMapper(inputConfiguration, service);
        mapper.init();
    }

    @Test
    void estimateSize() {
        assertEquals(300, mapper.estimateSize());
    }

    @Test
    void split() {
        assertEquals(1, mapper.split(1).size());
    }

    @Test
    void createWorker() {
        inputConfiguration.getDataSet().setEntity(MarketoEntity.Lead);
        assertTrue(mapper.createWorker() instanceof LeadSource);
        inputConfiguration.getDataSet().setEntity(MarketoEntity.List);
        assertTrue(mapper.createWorker() instanceof ListSource);
        inputConfiguration.getDataSet().setEntity(MarketoEntity.Company);
        assertTrue(mapper.createWorker() instanceof CompanySource);
        inputConfiguration.getDataSet().setEntity(MarketoEntity.CustomObject);
        assertTrue(mapper.createWorker() instanceof CustomObjectSource);
        inputConfiguration.getDataSet().setEntity(MarketoEntity.Opportunity);
        assertTrue(mapper.createWorker() instanceof OpportunitySource);
        inputConfiguration.getDataSet().setEntity(MarketoEntity.OpportunityRole);
        assertTrue(mapper.createWorker() instanceof OpportunitySource);
    }
}
