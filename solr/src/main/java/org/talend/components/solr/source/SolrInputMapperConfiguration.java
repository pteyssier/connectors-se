package org.talend.components.solr.source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.talend.components.solr.common.FilterCriteria;
import org.talend.components.solr.common.SolrDataset;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.constraint.Pattern;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;
import lombok.Data;

@Data
@GridLayout({ @GridLayout.Row({ "dataset" }), @GridLayout.Row({ "filterQuery" }), @GridLayout.Row({ "start" }),
        @GridLayout.Row({ "rows" }) })
@GridLayout(value = { @GridLayout.Row({ "raw" }) }, names = { GridLayout.FormType.ADVANCED })
@Documentation("Solr Input Configuration")
public class SolrInputMapperConfiguration implements Serializable {

    @Option
    @Documentation("Solr URL. Including core")
    private SolrDataset dataset;

    @Option
    @Pattern("^[0-9]{0,9}$")
    @Documentation("Start field. Points to a started document")
    private String start = "0";

    @Option
    @Pattern("^[0-9]{0,9}$")
    @Documentation("Rows field. Points to numbers of documents")
    private String rows = "10";

    @Option
    @Documentation("Filter query table. Every row sets a new condition")
    private List<FilterCriteria> filterQuery = new ArrayList<>();

    @Option
    @Documentation("raw query")
    @Suggestable(value = "raw", parameters = { "filterQuery", "start", "rows" })
    private String raw = "";

}