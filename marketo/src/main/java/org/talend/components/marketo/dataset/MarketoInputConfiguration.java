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
package org.talend.components.marketo.dataset;

import static org.talend.components.marketo.service.UIActionService.ACTIVITIES_LIST;
import static org.talend.components.marketo.service.UIActionService.CUSTOM_OBJECT_NAMES;
import static org.talend.components.marketo.service.UIActionService.FIELD_NAMES;
import static org.talend.components.marketo.service.UIActionService.LEAD_KEY_NAME_LIST;
import static org.talend.components.marketo.service.UIActionService.LIST_NAMES;

import java.io.Serializable;
import java.util.List;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;
import lombok.ToString;

@Data
@GridLayout({ //
        @GridLayout.Row({ "dataSet" }), //
        @GridLayout.Row({ "leadAction" }), //
        @GridLayout.Row({ "otherAction" }), //
        @GridLayout.Row({ "listAction" }), //
        @GridLayout.Row({ "leadKeyName" }), //
        @GridLayout.Row({ "leadKeyValues" }), //
        @GridLayout.Row({ "leadId", }), //
        @GridLayout.Row({ "leadIds" }), //
        @GridLayout.Row({ "assetIds" }), //
        @GridLayout.Row({ "listId" }), //
        @GridLayout.Row({ "customObjectName" }), //
        @GridLayout.Row({ "activityTypeIds" }), //
        @GridLayout.Row({ "filterType" }), //
        @GridLayout.Row({ "filterValues" }), //
        @GridLayout.Row({ "useCompoundKey" }), //
        @GridLayout.Row({ "compoundKey" }), //
        @GridLayout.Row({ "sinceDateTime" }), //
        @GridLayout.Row({ "listIds" }), //
        @GridLayout.Row({ "listName" }), //
        @GridLayout.Row({ "programName" }), //
        @GridLayout.Row({ "workspaceName" }), //
        @GridLayout.Row({ "fields" }), //
}) //
@Documentation("Marketo Source Configuration")
@ToString(callSuper = true)
public class MarketoInputConfiguration implements Serializable {

    public static final String NAME = "MarketoInputConfiguration";

    /*
     * DataSet
     */
    @Option
    @Required
    @Documentation("Marketo DataSet")
    private MarketoDataSet dataSet;

    /*
     * Lead DataSet parameters
     */
    @Option
    @DefaultValue(value = "getLead")
    @ActiveIf(target = "../dataSet/entity", value = { "Lead" })
    @Documentation("Lead Action")
    private LeadAction leadAction;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead" })
    @ActiveIf(target = "leadAction", value = "getLead")
    @Documentation("Lead Id")
    private Integer leadId;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead" })
    @ActiveIf(target = "leadAction", value = "getMultipleLeads")
    @Suggestable(value = LEAD_KEY_NAME_LIST)
    @Documentation("Key Name")
    private String leadKeyName;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead" })
    @ActiveIf(target = "leadAction", value = "getMultipleLeads")
    @Documentation("Values (Comma-separated)")
    private String leadKeyValues;

    @Option
    @DefaultValue(value = "list")
    @ActiveIf(target = "../dataSet/entity", value = { "List" })
    @Documentation("List Action")
    private ListAction listAction;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "List" })
    @Suggestable(value = LIST_NAMES, parameters = { "../dataSet/dataStore" })
    @Documentation("List Name : Comma-separated list of static list names to return.")
    private String listName;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "List" })
    @ActiveIf(target = "listAction", value = { "list" })
    @Documentation("List ids : Comma-separated list of static list ids to return.")
    private String listIds;

    /*
     * List Entity DataSet Parameters
     */
    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "List" })
    @Documentation("Program Name : Comma-separated list of program names.")
    private String programName;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "List" })
    @Documentation("Workspace Name : Comma-separated list of workspace names.")
    private String workspaceName;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead" })
    @ActiveIf(target = "leadAction", value = { "getLeadChanges", "getLeadActivity" })
    @Documentation("Static List Id")
    private Integer listId;

    /*
     * Changes & Activities
     */
    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead" })
    @ActiveIf(target = "leadAction", value = { "getLeadChanges", "getLeadActivity" })
    // @Pattern("/^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}$/")
    @Documentation("Since Date Time")
    private String sinceDateTime;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead", "List" })
    @ActiveIf(target = "leadAction", value = { "getLeadChanges", "getLeadActivity" })
    @Documentation("Lead Ids (Comma-separated Lead Ids)")
    private String leadIds;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead" })
    @ActiveIf(target = "leadAction", value = { "getLeadChanges", "getLeadActivity" })
    @Documentation("Asset Ids (Comma-separated Asset Ids)")
    private String assetIds;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead" })
    @ActiveIf(target = "leadAction", value = "getLeadActivity")
    @Suggestable(value = ACTIVITIES_LIST, parameters = { "../dataSet/dataStore" })
    @Documentation("Activity Type Ids (10 max supported")
    private List<String> activityTypeIds;

    @Option
    @DefaultValue(value = "describe")
    @ActiveIf(target = "../dataSet/entity", value = { "CustomObject", "Company", "Opportunity", "OpportunityRole" })
    @Documentation("Action")
    private OtherEntityAction otherAction;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "CustomObject" })
    @ActiveIf(target = "otherAction", value = { "get", "describe" })
    @Suggestable(value = CUSTOM_OBJECT_NAMES, parameters = { "../dataSet/dataStore" })
    @Documentation("Custom Object Name")
    private String customObjectName;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "CustomObject", "Company", "Opportunity", "OpportunityRole" })
    @ActiveIf(target = "otherAction", value = { "get" })
    @Suggestable(value = FIELD_NAMES, parameters = { "../dataSet/dataStore", "../dataSet/entity", "customObjectName" })
    @Documentation("Filter Type")
    private String filterType;

    /*
     * Other Entities DataSet parameters
     */
    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "CustomObject", "Company", "Opportunity", "OpportunityRole" })
    @ActiveIf(target = "otherAction", value = { "get" })
    @Documentation("Filter Values")
    private String filterValues;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "CustomObject", "OpportunityRole" })
    @ActiveIf(target = "otherAction", value = { "get" })
    @Documentation("Use Compound Key")
    private Boolean useCompoundKey = Boolean.FALSE;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "CustomObject", "OpportunityRole" })
    @ActiveIf(target = "otherAction", value = { "get" })
    @ActiveIf(target = "useCompoundKey", value = { "true" })
    @Documentation("Compound Key")
    private List<CompoundKey> compoundKey;

    @Option
    @ActiveIf(target = "../dataSet/entity", value = { "Lead", "CustomObject", "Company", "Opportunity", "OpportunityRole" })
    @Suggestable(value = FIELD_NAMES, parameters = { "../dataSet/dataStore", "../dataSet/entity", "customObjectName" })
    @Documentation("Fields")
    private List<String> fields;

    public enum LeadAction {
        getLead,
        getMultipleLeads,
        getLeadActivity,
        getLeadChanges,
        describeLead
    }

    public enum ListAction {
        list,
        get,
        isMemberOf,
        getLeads
    }

    public enum OtherEntityAction {
        describe,
        list,
        get
    }

}
