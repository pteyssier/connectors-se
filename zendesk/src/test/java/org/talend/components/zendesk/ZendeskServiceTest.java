package org.talend.components.zendesk;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.talend.components.zendesk.common.SelectionType;
import org.talend.components.zendesk.common.ZendeskDataSet;
import org.talend.components.zendesk.service.ZendeskService;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit5.WithComponents;

import java.util.stream.Stream;

@Slf4j
@WithComponents("org.talend.components.zendesk")
//@ExtendWith(ZendeskTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ZendeskServiceTest {

    @Service
    private ZendeskService zendeskService;

//    private ZendeskTestExtension.TestContext testContext;

//    @BeforeAll
//    private void init(ZendeskTestExtension.TestContext testContext) {
//        log.info("init service test");
//        this.testContext = testContext;
//    }

    @ParameterizedTest
    @MethodSource("methodSourceSelectionType")
    @DisplayName("Schema discovery")
    void schemaDiscoveryListTest(SelectionType selectionType) {
        log.info("Integration test 'Schema discovery'. Selection type: " + selectionType);
        ZendeskDataSet dataSet = new ZendeskDataSet();
        dataSet.setSelectionType(selectionType);

//        Schema schema = zendeskService.guessTableSchemaList(dataSet);
//        Assertions.assertTrue(schema.getEntries().stream().map(Schema.Entry::getName).collect(Collectors.toList())
//                .containsAll(Arrays.asList("id", "subject", "description")));
    }

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

    private Stream<Arguments> methodSourceSelectionType() {
        return Stream.of(Arguments.of(SelectionType.REQUESTS), Arguments.of(SelectionType.TICKETS));
    }

}
