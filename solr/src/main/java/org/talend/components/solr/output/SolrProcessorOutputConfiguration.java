package org.talend.components.solr.output;

import lombok.Data;
import org.talend.components.solr.common.SolrConnectionConfiguration;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Data
@DataSet("Solr Output")
@GridLayout({ @GridLayout.Row({ "solrConnection" }), @GridLayout.Row({ "action" }) })
@Documentation("Solr Processor output")
public class SolrProcessorOutputConfiguration implements Serializable {

    @Option
    @Documentation("Solr URL. Including core")
    private SolrConnectionConfiguration solrConnection;

    @Option
    @Documentation("Combobox field. Update and Delete values are available")
    private ActionEnum action = ActionEnum.UPDATE;

}