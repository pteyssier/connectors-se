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
package org.talend.components.rabbitmq.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.talend.components.rabbitmq.datastore.RabbitMQDataStore;
import org.talend.sdk.component.api.service.Service;
import lombok.extern.slf4j.Slf4j;
import javax.net.ssl.SSLContext;

@Slf4j
@Service
public class RabbitMQService {

    @Service
    private I18nMessage i18n;

    public Connection getConnection(RabbitMQDataStore store) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(store.getUserName());
        factory.setPassword(store.getPassword());
        factory.setHost(store.getHostname());
        factory.setPort(store.getPort());

        if (store.getTLS()) {
            factory.useSslProtocol(getSSLContext());
        }
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new IllegalStateException(i18n.errorInvalidConnection());
        }
    }

    private SSLContext getSSLContext() {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(i18n.errorTLS());
        }

        return sslContext;
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                log.warn(i18n.warnConnectionCantBeClosed(), e);
            }
        }
    }

}
