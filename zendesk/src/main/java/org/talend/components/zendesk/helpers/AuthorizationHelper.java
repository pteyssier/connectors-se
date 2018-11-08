package org.talend.components.zendesk.helpers;

import org.talend.components.zendesk.common.AuthenticationType;
import org.talend.components.zendesk.common.ZendeskDataStore;
import org.talend.components.zendesk.common.UnknownAuthenticationTypeException;
import org.talend.components.zendesk.helpers.authhandlers.AuthorizationHandler;
import org.talend.components.zendesk.helpers.authhandlers.AuthorizationHandlerLoginPassword;
import org.talend.sdk.component.api.service.Service;

@Service
public class AuthorizationHelper {

    @Service
    private AuthorizationHandlerLoginPassword authorizationHandlerLoginPassword;

    public String getAuthorization(ZendeskDataStore zendeskDataStore) {
        // AuthorizationHandler authenticationHandler = getAuthHandler(zendeskDataStore.getAuthenticationType());
        // return authenticationHandler.getAuthorization(zendeskDataStore);
        throw new UnsupportedOperationException();
    }

    private AuthorizationHandler getAuthHandler(AuthenticationType authenticationType) throws UnknownAuthenticationTypeException {
        // if (authenticationType == AuthenticationType.LOGIN_PASSWORD) {
        // return authorizationHandlerLoginPassword;
        // }else if (authenticationType == AuthenticationType.API_TOKEN) {
        // return authorizationHandlerLoginPassword;
        // }
        // throw new UnknownAuthenticationTypeException();
        return null;
    }
}
