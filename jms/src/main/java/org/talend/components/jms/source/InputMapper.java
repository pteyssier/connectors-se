package org.talend.components.jms.source;

import static java.util.Collections.singletonList;

import java.io.Serializable;
import java.util.List;

import javax.json.JsonBuilderFactory;

import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Assessor;
import org.talend.sdk.component.api.input.Emitter;
import org.talend.sdk.component.api.input.PartitionMapper;
import org.talend.sdk.component.api.input.PartitionSize;
import org.talend.sdk.component.api.input.Split;
import org.talend.sdk.component.api.meta.Documentation;

import org.talend.components.jms.service.JmsService;

//
// this class role is to enable the work to be distributed in environments supporting it.
//
@Version(1)
// default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = Icon.IconType.CUSTOM, custom = "tJMSInput")
@PartitionMapper(name = "Input")
@Documentation("TODO fill the documentation for this mapper")
public class InputMapper implements Serializable {

    private final InputMapperConfiguration configuration;

    private final JmsService service;

    private final JsonBuilderFactory jsonBuilderFactory;

    public InputMapper(@Option("configuration") final InputMapperConfiguration configuration, final JmsService service,
            final JsonBuilderFactory jsonBuilderFactory) {
        this.configuration = configuration;
        this.service = service;
        this.jsonBuilderFactory = jsonBuilderFactory;
    }

    @Assessor
    public long estimateSize() {
        // this method should return the estimation of the dataset size
        // it is recommended to return a byte value
        // if you don't have the exact size you can use a rough estimation
        return 1L;
    }

    @Split
    public List<InputMapper> split(@PartitionSize final long bundles) {
        // overall idea here is to split the work related to configuration in bundles of size "bundles"
        //
        // for instance if your estimateSize() returned 1000 and you can run on 10 nodes
        // then the environment can decide to run it concurrently (10 * 100).
        // In this case bundles = 100 and we must try to return 10 InputMapper with 1/10 of the overall work each.
        //
        // default implementation returns this which means it doesn't support the work to be split
        return singletonList(this);
    }

    @Emitter
    public InputSource createWorker() {
        // here we create an actual worker,
        // you are free to rework the configuration etc but our default generated implementation
        // propagates the partition mapper entries.
        return new InputSource(configuration, service, jsonBuilderFactory);
    }
}