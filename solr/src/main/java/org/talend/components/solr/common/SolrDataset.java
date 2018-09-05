package org.talend.components.solr.common;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Structure;
import org.talend.sdk.component.api.meta.Documentation;

import java.util.List;

@Data
@DataSet("SolrDataSet")
@GridLayout({ @GridLayout.Row({ "dataStore" }), @GridLayout.Row({ "core" }) })
@Documentation("Solr dataSet. Provide connection to Solr Data Collection")
@GridLayout(value = { @GridLayout.Row({ "schema" }) }, names = { GridLayout.FormType.ADVANCED })

public class SolrDataset {

    @Option
    @Documentation("Solr dataStore. Solr server connection")
    private SolrDataStore dataStore;

    @Option
    @Required
    @Documentation("List of Solr data collection")
    @Suggestable(value = "coreList", parameters = { "dataStore" })
    private String core;

    public String getFullUrl() {
        String solrUrl = dataStore.getUrl();
        boolean addSlash = !solrUrl.endsWith("/") && !solrUrl.endsWith("\\");
        return (addSlash ? solrUrl + "/" : solrUrl) + core;
    }

    @Option
    @Structure(type = Structure.Type.OUT, discoverSchema = "discoverSchema")
    @Documentation("Schema of a Solr Document")
    private List<String> schema;
}
