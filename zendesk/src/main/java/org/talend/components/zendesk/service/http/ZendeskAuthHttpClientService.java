package org.talend.components.zendesk.service.http;

import lombok.extern.slf4j.Slf4j;
import org.talend.sdk.component.api.service.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class ZendeskAuthHttpClientService {

    private final static String AUTH_SERVER = "https://login.microsoftonline.com/";

    @Service
    private ZendeskAuthHttpClient zendeskAuthHttpClient;

    @PostConstruct
    public void init() {
        setBase();
    }

    private void setBase() {
        zendeskAuthHttpClient.base(AUTH_SERVER);
    }

}
