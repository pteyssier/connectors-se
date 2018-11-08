package org.talend.components.zendesk.common;

public class UnknownAuthenticationTypeException extends RuntimeException {

    public UnknownAuthenticationTypeException() {
        super("Unknown or unset Authentication type");
    }
}
