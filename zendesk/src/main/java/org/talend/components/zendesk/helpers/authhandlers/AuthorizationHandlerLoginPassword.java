package org.talend.components.zendesk.helpers.authhandlers;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.common.ZendeskDataStore;
import org.talend.sdk.component.api.service.Service;

@Slf4j
@Service
public class AuthorizationHandlerLoginPassword implements AuthorizationHandler {

    // private static final String BEARER = "Bearer ";

    // private static Map<AuthenticationLoginPasswordConfiguration, String> cachedTokens = new ConcurrentHashMap<>();

    // @Service
    // private ZendeskAuthHttpClientService zendeskAuthHttpClientService;

    // public void clearTokenCache(AuthenticationLoginPasswordConfiguration authenticationLoginPasswordConfiguration) {
    // cachedTokens.remove(authenticationLoginPasswordConfiguration);
    // }

    @Override
    public String getAuthorization(ZendeskDataStore zendeskDataStore) {
        // String accessToken = getCachedToken(zendeskDataStore);
        // return BEARER + accessToken;
        return "";
    }

    // String getCachedToken(ZendeskDataStore zendeskDataStore) throws UnknownAuthenticationTypeException {
    // AuthenticationLoginPasswordConfiguration authSettings = (AuthenticationLoginPasswordConfiguration) zendeskDataStore
    // .getAuthSettings();
    // return cachedTokens.computeIfAbsent(authSettings, key -> getToken(zendeskDataStore));
    // }

    // private String getToken(ZendeskDataStore zendeskDataStore) {
    // AuthenticationLoginPasswordConfiguration authSettings = (AuthenticationLoginPasswordConfiguration) zendeskDataStore
    // .getAuthSettings();
    // String login = authSettings.getAuthenticationLogin();
    // String password = authSettings.getAuthenticationPassword();
    // String serverUrl = zendeskDataStore.getServerUrl();
    //
    // return zendeskAuthHttpClientService.getToken(serverUrl, login, password);
    // }
}
