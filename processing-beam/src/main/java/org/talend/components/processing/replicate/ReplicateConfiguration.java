package org.talend.components.processing.replicate;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.configuration.ui.widget.Code;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Data
@Documentation("ReplicateConfiguration, empty for the moment.")
@OptionsOrder({ "repeat", "overrideValuesAction", "overrideValues" })
public class ReplicateConfiguration implements Serializable {

    @Option
    @Documentation("The number of times to repeat the input dataset.")
    private int repeat = 1;

    private boolean useOverrideValues = false;

    @Option
    @ActiveIf(target = "useOverrideValues", value = "true")
    @Documentation("The number of rows.")
    private OverrideValuesAction overrideValuesAction = OverrideValuesAction.NONE;

    @Option
    @Code("json")
    @ActiveIf(target = "useOverrideValues", value = "true")
    @ActiveIf(target = "overrideValuesAction", value = { "REPLACE", "APPEND" })
    @Documentation("Values to use.")
    private String overrideValues = "";

    /**
     * Add or replace the values in the dataset.
     */
    public enum OverrideValuesAction {
        /** Do not override the values in the dataset. */
        NONE,
        /** Replace the values specified in the dataset by the ones specified in this component. */
        REPLACE,
        /** Use the values in this component in addition to the ones specified in the dataset. */
        APPEND
    }
}
