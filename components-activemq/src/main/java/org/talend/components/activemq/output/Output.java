// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.activemq.output;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.json.JsonObject;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.activemq.service.I18nMessage;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.AfterGroup;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Processor;

import org.talend.components.activemq.service.JmsService;
import org.talend.sdk.component.api.service.Service;

import static org.talend.components.activemq.MessageConst.MESSAGE_CONTENT;

@Slf4j
@Version(1)
@Icon(value = Icon.IconType.CUSTOM, custom = "ActiveMQOutput")
@Processor(name = "Output")
@Documentation("Main class for JMSOutput records processing")
public class Output implements Serializable {

    private final OutputConfiguration configuration;

    private final I18nMessage i18n;

    @Service
    private final JmsService service;

    private Connection connection;

    private Session session;

    private Destination destination;

    private MessageProducer producer;

    public Output(@Option("configuration") final OutputConfiguration configuration, final JmsService service,
            final I18nMessage i18nMessage) {
        this.configuration = configuration;
        this.service = service;
        this.i18n = i18nMessage;
    }

    @PostConstruct
    public void init() {

        // create ConnectionFactory
        ConnectionFactory connectionFactory = service.createConnectionFactory(configuration.getBasicConfig().getConnection());
        try {
            try {
                connection = service.getConnection(connectionFactory,
                        configuration.getBasicConfig().getConnection().isUserIdentity(),
                        configuration.getBasicConfig().getConnection().getUserName(),
                        configuration.getBasicConfig().getConnection().getPassword());
            } catch (JMSException e) {
                throw new IllegalStateException(i18n.errorInvalidConnection());
            }

            try {
                connection.start();
            } catch (JMSException e) {
                throw new IllegalStateException(i18n.errorStartMessagesDelivery());
            }

            session = service.getSession(connection, configuration.getBasicConfig().getConnection().getTransacted());

            destination = service.getDestination(session, configuration.getBasicConfig().getDestination(),
                    configuration.getBasicConfig().getMessageType());

            producer = session.createProducer(destination);
            producer.setDeliveryMode(configuration.getDeliveryMode().getIntValue());
        } catch (JMSException e) {
            throw new IllegalStateException(i18n.errorCreateJMSInstance());
        }

    }

    @ElementListener
    public void onNext(@Input final JsonObject record) {
        try {
            producer.send(session.createTextMessage(getMessage(record)));
        } catch (JMSException e) {
            throw new IllegalStateException(i18n.errorCantSendMessage());
        }
    }

    @AfterGroup
    public void after() {
        service.commit(session);
    }

    private String getMessage(JsonObject record) {
        return record.getString(MESSAGE_CONTENT);
    }

    @PreDestroy
    public void release() {
        service.closeProducer(producer);
        service.closeSession(session);
        service.closeConnection(connection);
    }
}