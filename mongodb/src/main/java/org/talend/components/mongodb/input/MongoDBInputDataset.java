package org.talend.components.mongodb.input;

import lombok.Data;
import org.talend.components.mongodb.datastore.MongoDBDataStore;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Structure;
import org.talend.sdk.component.api.configuration.ui.widget.TextArea;
import org.talend.sdk.component.api.meta.Documentation;

import java.util.List;

@Data
@DataSet("Input")
@GridLayout(value = { @GridLayout.Row("dataStore"), @GridLayout.Row("collection"), @GridLayout.Row("schema"),
        @GridLayout.Row({ "setReadPreference", "readPreference" }), @GridLayout.Row("query"), @GridLayout.Row("mapping"),
        @GridLayout.Row("sort"), @GridLayout.Row("limite") })
@Documentation("MongoDBInputDataset")
public class MongoDBInputDataset {

    @Option
    @Documentation("datastore")
    private MongoDBDataStore dataStore;

    @Option
    @Documentation("collection")
    private String collection;

    @Option
    @Structure
    @Documentation("schema")
    private List<String> schema;

    @Option
    @Documentation("setReadPreference")
    private boolean setReadPreference;

    @Option
    @ActiveIf(target = "setReadPreference", value = "true")
    @Documentation("readPreference")
    private ReadPreference readPreference;

    public enum ReadPreference {
        Primary,
        Primary_Preferred,
        Secondary,
        Secondary_Preferred,
        Nearest
    }

    @Option
    @TextArea
    @Documentation("query")
    private String query = "{}";

    @Option
    @Documentation("limite")
    private int limite;

    @Option
    @Documentation("mapping")
    private List<InputMapping> mapping;

    @Option
    @Documentation("mapping")
    private List<Sort> sort;

}
