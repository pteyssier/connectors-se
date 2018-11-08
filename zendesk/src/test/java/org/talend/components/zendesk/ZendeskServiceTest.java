package org.talend.components.zendesk;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.talend.components.zendesk.service.ZendeskService;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit5.WithComponents;

@Slf4j
@WithComponents("org.talend.components.onedrive")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ZendeskServiceTest {

    @Service
    private ZendeskService zendeskService;

    // @Test
    // @DisplayName("Schema discovery List")
    // void schemaDiscoveryListTest() {
    // log.info("Integration test 'Schema discovery list' start ");
    // OneDriveListConfiguration dataSet = new OneDriveListConfiguration();
    // Schema schema = zendeskService.guessTableSchemaList(dataSet);
    // assertTrue(schema.getEntries().stream().map(Schema.Entry::getName).collect(Collectors.toList())
    // .containsAll(Arrays.asList("id", "createdDateTime", "eTag", "lastModifiedDateTime", "name", "webUrl", "cTag",
    // "size", "createdBy", "lastModifiedBy", "parentReference", "fileSystemInfo", "folder", "file", "root")));
    // }
    //
    // @Test
    // @DisplayName("Schema discovery Delete")
    // void schemaDiscoveryDeleteTest() {
    // log.info("Integration test 'Schema discovery delete' start ");
    // OneDriveDeleteConfiguration dataSet = new OneDriveDeleteConfiguration();
    // Schema schema = zendeskService.guessTableSchemaDelete(dataSet);
    // assertTrue(schema.getEntries().stream().map(Schema.Entry::getName).collect(Collectors.toList())
    // .containsAll(Arrays.asList("id")));
    // }
}
