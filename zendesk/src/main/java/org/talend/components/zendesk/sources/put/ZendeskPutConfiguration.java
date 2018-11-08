package org.talend.components.zendesk.sources.put;

import lombok.Data;
import org.talend.components.zendesk.common.ZendeskDataSet;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Data
@GridLayout({ @GridLayout.Row({ "dataSet" }) })
@Documentation("'Put component' configuration")
public class ZendeskPutConfiguration implements Serializable {

    @Option
    @Documentation("Connection to server")
    private ZendeskDataSet dataSet;

}