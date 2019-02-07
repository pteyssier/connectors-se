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
package org.talend.components.activemq.configuration;

import lombok.Data;
import org.talend.components.activemq.datastore.JmsDataStore;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Structure;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;
import java.util.List;

import static org.talend.components.activemq.service.ActionService.DISCOVER_SCHEMA;

@DataSet("JMSDataSet")
@Data
@GridLayout(value = { @GridLayout.Row({ "connection" }), @GridLayout.Row({ "messageType" }),
        @GridLayout.Row({ "destination" }) }, names = GridLayout.FormType.MAIN)
@GridLayout(value = { @GridLayout.Row({ "schema" }) }, names = GridLayout.FormType.ADVANCED)
public class BasicConfiguration implements Serializable {

    @Option
    @Documentation("JMS connection information")
    private JmsDataStore connection;

    @Option
    @Documentation("Drop down list for Message Type")
    private MessageType messageType = MessageType.TOPIC;

    @Option
    @Required
    @Documentation("Input for TOPIC/QUEUE Name")
    private String destination;

    @Option
    @Structure(type = Structure.Type.OUT, discoverSchema = DISCOVER_SCHEMA)
    @Documentation("Guess schema")
    private List<String> schema;
}
