package org.talend.components.zendesk.common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Validable;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@GridLayout({ @GridLayout.Row({ "authenticationLogin" }), @GridLayout.Row({ "apiToken" }) })
@Documentation("'API Token' authentication settings")
public class AuthenticationApiTokenConfiguration implements Serializable, AuthenticationConfiguration {

    @Option
    @Documentation("Authentication login")
    @Validable("validateAuthenticationLogin")
    private String authenticationLogin = "";

    @Option
    @Documentation("Api token")
    @Validable("validateApiToken")
    private String apiToken = "";

}
