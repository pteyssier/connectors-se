/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.netsuite;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.talend.components.netsuite.dataset.NetSuiteDataSet;
import org.talend.components.netsuite.dataset.NetSuiteInputProperties;
import org.talend.components.netsuite.dataset.NetSuiteOutputProperties;
import org.talend.components.netsuite.dataset.SearchConditionConfiguration;
import org.talend.components.netsuite.datastore.NetSuiteDataStore;
import org.talend.components.netsuite.datastore.NetSuiteDataStore.ApiVersion;
import org.talend.components.netsuite.datastore.NetSuiteDataStore.LoginType;
import org.talend.components.netsuite.runtime.client.NetSuiteClientService;
import org.talend.components.netsuite.service.Messages;
import org.talend.components.netsuite.service.NetSuiteService;
import org.talend.components.netsuite.test.TestCollector;
import org.talend.components.netsuite.test.TestEmitter;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.junit.SimpleComponentRule;
import org.talend.sdk.component.maven.MavenDecrypter;
import org.talend.sdk.component.maven.Server;
import org.talend.sdk.component.runtime.manager.chain.Job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.talend.sdk.component.junit.SimpleFactory.configurationByExample;

public abstract class NetSuiteBaseTest {

    public static final String FAMILY_NAME = "NetSuite";

    public static final String EMITTER = "Input";

    public static final String COLLECTOR = "Output";

    public static final String TEST_FAMILY_NAME = "NetSuiteTest";

    public static final String TEST_EMITTER = "TestEmitter";

    public static final String TEST_COLLECTOR = "TestCollector";

    protected static NetSuiteClientService<?> clientService;

    protected static NetSuiteDataStore dataStore;

    protected NetSuiteDataSet dataSet;

    protected static NetSuiteService service;

    protected static Messages messages;

    protected static RecordBuilderFactory factory;

    @ClassRule
    public static final SimpleComponentRule COMPONENT = new SimpleComponentRule("org.talend.components.netsuite");

    @BeforeAll
    public static void setupOnce() {
        final MavenDecrypter decrypter = new MavenDecrypter();
        Server consumer = decrypter.find("netsuite.consumer");
        Server token = decrypter.find("netsuite.token");
        dataStore = new NetSuiteDataStore();
        dataStore.setEnableCustomization(false);
        dataStore.setAccount(System.getProperty("netsuite.account"));
        dataStore.setEndpoint(System.getProperty("netsuite.endpoint.url"));
        dataStore.setLoginType(LoginType.TBA);
        dataStore.setConsumerKey(consumer.getUsername());
        dataStore.setConsumerSecret(consumer.getPassword());
        dataStore.setTokenId(token.getUsername());
        dataStore.setTokenSecret(token.getPassword());
        dataStore.setApiVersion(ApiVersion.V2018_2);
        service = COMPONENT.findService(NetSuiteService.class);
        clientService = service.getClientService(dataStore);
        messages = COMPONENT.findService(Messages.class);
        factory = COMPONENT.findService(RecordBuilderFactory.class);
    }

    protected String getComponentName(String familyName, String componentType) {
        return String.format("%s://%s", familyName, componentType);
    }

    protected String getComponentWithConfig(String component, String config) {
        return String.format("%s?%s", component, config);
    }

    private void buildAndRunPipeline(String emitter, String collector) {
        Job.components().component("emitter", emitter).component("collector", collector).connections().from("emitter")
                .to("collector").build().run();
    }

    protected void buildAndRunCollectorJob(NetSuiteOutputProperties properties, Record record) {
        TestEmitter.addRecord(record);
        buildAndRunPipeline(getComponentName(TEST_FAMILY_NAME, TEST_EMITTER),
                getComponentWithConfig(getComponentName(FAMILY_NAME, COLLECTOR), getQueryProperties(properties)));
        TestEmitter.reset();
    }

    protected List<Record> buildAndRunEmitterJob(NetSuiteInputProperties properties) {
        TestCollector.reset();
        buildAndRunPipeline(getComponentWithConfig(getComponentName(FAMILY_NAME, EMITTER), getQueryProperties(properties)),
                getComponentName(TEST_FAMILY_NAME, TEST_COLLECTOR));
        return new ArrayList<>(TestCollector.getData());
    }

    protected List<SearchConditionConfiguration> createSearchConditionConfiguration(String value) {
        return Collections.singletonList(new SearchConditionConfiguration("internalId", "List.anyOf", value, ""));
    }

    private <T> String getQueryProperties(T properties) {
        return configurationByExample().forInstance(properties).configured().toQueryString();
    }
}
