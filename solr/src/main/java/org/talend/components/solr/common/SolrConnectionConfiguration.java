package org.talend.components.solr.common;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

@Data
@GridLayout({ @GridLayout.Row({ "solrUrl" }), @GridLayout.Row({ "core" }) })
public class SolrConnectionConfiguration {

    @Option
    @Documentation("Solr server URL DataStore")
    private SolrDataStore solrUrl;

    @Option
    @Required
    @Documentation("the name of Solr Core")
    @Suggestable(value = "coreList", parameters = { "solrUrl/url" })
    private String core;

    public String getFullUrl() {
        String solr = solrUrl.getUrl();
        boolean addSlash = !solr.endsWith("/") && !solr.endsWith("\\");
        return (addSlash ? solr + "/" : solr) + core;
    }
}
