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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.dataset.MarketoInputConfiguration;
import org.talend.components.marketo.service.AuthorizationClient;
import org.talend.components.marketo.service.MarketoService;

import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Assessor;
import org.talend.sdk.component.api.input.Emitter;
import org.talend.sdk.component.api.input.PartitionMapper;
import org.talend.sdk.component.api.input.PartitionSize;
import org.talend.sdk.component.api.input.Split;
import org.talend.sdk.component.api.meta.Documentation;

@Version
@Icon(value = Icon.IconType.CUSTOM, custom = "MarketoInput")
@PartitionMapper(family = "Marketo", name = "Input")
@Documentation("Marketo Input Component")
public class MarketoInputMapper implements Serializable {

    private MarketoInputConfiguration configuration;

    private MarketoService service;

    private AuthorizationClient authorizationClient;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoInputMapper.class);

    public MarketoInputMapper(@Option("configuration") final MarketoInputConfiguration configuration, //
            final MarketoService service) {
        this.configuration = configuration;
        this.service = service;
        authorizationClient = service.getAuthorizationClient();
        LOG.debug("[MarketoInputMapper] {}", configuration);
        authorizationClient.base(configuration.getDataSet().getDataStore().getEndpoint());
    }

    @PostConstruct
    public void init() {
        // NOOP
    }

    @Assessor
    public long estimateSize() {
        return 300L;
    }

    @Split
    public List<MarketoInputMapper> split(@PartitionSize final long bundles) {
        return Collections.singletonList(this);
    }

    @Emitter
    public MarketoSource createWorker() {
        switch (configuration.getDataSet().getEntity()) {
        case Lead:
            return new LeadSource(configuration, service);
        case List:
            return new ListSource(configuration, service);
        case CustomObject:
            return new CustomObjectSource(configuration, service);
        case Company:
            return new CompanySource(configuration, service);
        case Opportunity:
        case OpportunityRole:
            return new OpportunitySource(configuration, service);
        }
        throw new IllegalArgumentException(service.getI18n().invalidOperation());
    }

}
