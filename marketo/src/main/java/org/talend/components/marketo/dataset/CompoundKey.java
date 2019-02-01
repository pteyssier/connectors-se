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
package org.talend.components.marketo.dataset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.talend.components.marketo.service.UIActionService.FIELD_NAMES;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.meta.Documentation;

@Data
@AllArgsConstructor
@NoArgsConstructor
@OptionsOrder({ "key", "value" })
@Documentation("Compound Key")
public class CompoundKey implements Serializable {

    @Option
    @Required
    @Suggestable(value = FIELD_NAMES, parameters = { "../../dataSet" }) //
    @Documentation("Key field")
    private String key;

    @Option
    @Required
    @Documentation("Value field")
    private String value;

}
