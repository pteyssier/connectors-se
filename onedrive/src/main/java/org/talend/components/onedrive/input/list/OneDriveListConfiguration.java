package org.talend.components.onedrive.input.list;

import lombok.Data;
import org.talend.components.onedrive.common.OneDriveDataStore;
import org.talend.components.onedrive.helpers.ConfigurationHelper;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Structure;
import org.talend.sdk.component.api.meta.Documentation;

import java.util.ArrayList;
import java.util.List;

@Data
@DataSet(ConfigurationHelper.DATA_SET_LIST_ID)
@GridLayout({ @GridLayout.Row({ "dataStore" }), @GridLayout.Row({ "objectPath" }), @GridLayout.Row({ "objectType" }),
        @GridLayout.Row({ "recursively" }) })
@GridLayout(names = GridLayout.FormType.ADVANCED, value = { @GridLayout.Row({ "dataStore" }), @GridLayout.Row({ "fields" }) })
@Documentation("Input component configuration")
public class OneDriveListConfiguration {

    @Option
    @Documentation("Connection to Magento CMS")
    private OneDriveDataStore dataStore;

    @Option
    @Structure(discoverSchema = ConfigurationHelper.DISCOVER_SCHEMA_LIST_ID, type = Structure.Type.OUT)
    @Documentation("The schema of the component. Use 'Discover schema' button to fil it with sample data. "
            + "Schema is discovering by getting the frist record from particular data table, "
            + "e.g. first product in case of 'Product' selection type")
    private List<String> fields = new ArrayList<>();

    @Option
    @Documentation("Full path to OneDrive directory/file")
    private String objectPath;

    @Option
    @Documentation("Full path to OneDrive directory/file")
    private OneDriveObjectType objectType = OneDriveObjectType.DIRECTORY;

    @Option
    @Documentation("List directory recursively")
    private boolean recursively;
}