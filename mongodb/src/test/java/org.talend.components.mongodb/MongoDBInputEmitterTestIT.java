package org.talend.components.mongodb;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.talend.components.mongodb.datastore.MongoDBDataStore;
import org.talend.components.mongodb.input.MongoDBInputDataset;
import org.talend.components.mongodb.input.MongoDBInputEmitter;
import org.talend.components.mongodb.service.MongoDBService;
import org.talend.sdk.component.api.service.Service;

@Disabled("Need connect to MongoDB")
public class MongoDBInputEmitterTestIT {

    MongoDBInputDataset dataset;

    MongoDBInputEmitter emitter;

    @Service
    MongoDBService service;

    @BeforeEach
    public void before() {
        MongoDBDataStore datastore = new MongoDBDataStore();
        datastore.setServer("localhost");
        datastore.setPort("27017");
        datastore.setDatabase("testdb");
        dataset = new MongoDBInputDataset();
        dataset.setDataStore(datastore);
        dataset.setQuery("{}");
        dataset.setCollection("personalstakesTEST651_12");
        service = new MongoDBService();
        emitter = new MongoDBInputEmitter(dataset, service);
    }

    @Test
    public void testInit() {
        emitter.init();
        // no exception indicate success
        Assert.assertTrue(true);
    }

    @Test
    public void testNext() {
        emitter.init();
        Assert.assertNotNull(emitter.next());
    }
}
