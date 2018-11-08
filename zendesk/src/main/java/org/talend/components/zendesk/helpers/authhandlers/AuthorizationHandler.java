package org.talend.components.zendesk.helpers.authhandlers;

import org.talend.components.zendesk.common.ZendeskDataStore;

public interface AuthorizationHandler {

    String getAuthorization(ZendeskDataStore zendeskDataStore);
}
