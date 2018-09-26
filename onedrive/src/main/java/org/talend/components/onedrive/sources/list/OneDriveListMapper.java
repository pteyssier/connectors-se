package org.talend.components.onedrive.sources.list;

import org.talend.components.onedrive.helpers.ConfigurationHelper;
import org.talend.components.onedrive.service.configuration.ConfigurationServiceList;
import org.talend.components.onedrive.service.graphclient.GraphClientService;
import org.talend.components.onedrive.service.http.OneDriveAuthHttpClientService;
import org.talend.components.onedrive.service.http.OneDriveHttpClientService;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.*;
import org.talend.sdk.component.api.meta.Documentation;

import javax.json.JsonReaderFactory;
import java.io.Serializable;
import java.util.List;

import static java.util.Collections.singletonList;

@Version(1)
@Icon(value = Icon.IconType.CUSTOM, custom = "onedrive_list")
@PartitionMapper(name = "List")
@Documentation("List mapper class")
public class OneDriveListMapper implements Serializable {

    private final OneDriveListConfiguration configuration;

    private final OneDriveAuthHttpClientService oneDriveAuthHttpClientService;

    private final OneDriveHttpClientService oneDriveHttpClientService;

    private final JsonReaderFactory jsonReaderFactory;

    private final GraphClientService graphClientService;

    public OneDriveListMapper(@Option("configuration") final OneDriveListConfiguration configuration,
            OneDriveAuthHttpClientService oneDriveAuthHttpClientService, OneDriveHttpClientService oneDriveHttpClientService,
            ConfigurationServiceList configurationServiceList, JsonReaderFactory jsonReaderFactory,
            GraphClientService graphClientService) {
        this.configuration = configuration;
        this.oneDriveAuthHttpClientService = oneDriveAuthHttpClientService;
        this.oneDriveHttpClientService = oneDriveHttpClientService;
        this.jsonReaderFactory = jsonReaderFactory;
        this.graphClientService = graphClientService;
        ConfigurationHelper.setupServicesList(configuration, configurationServiceList, oneDriveAuthHttpClientService);
    }

    @Assessor
    public long estimateSize() {
        return 1L;
    }

    @Split
    public List<OneDriveListMapper> split(@PartitionSize final long bundles) {
        return singletonList(this);
    }

    @Emitter(name = "List")
    public OneDriveListSource createWorker() {
        return new OneDriveListSource(configuration, oneDriveHttpClientService, jsonReaderFactory, graphClientService);
    }
}