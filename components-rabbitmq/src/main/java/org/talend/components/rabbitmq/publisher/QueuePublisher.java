package org.talend.components.rabbitmq.publisher;

import com.rabbitmq.client.Channel;
import org.talend.components.rabbitmq.output.ActionOnQueue;
import org.talend.components.rabbitmq.output.OutputConfiguration;
import org.talend.components.rabbitmq.service.I18nMessage;

import java.io.IOException;

import static org.talend.components.rabbitmq.output.ActionOnQueue.DELETE_AND_CREATE_QUEUE;

public class QueuePublisher implements MessagePublisher {

    private I18nMessage i18n;

    private Channel channel;

    private String queue;

    public QueuePublisher(Channel channel, OutputConfiguration configuration, final I18nMessage i18nMessage) throws IOException {
        this.channel = channel;
        this.queue = configuration.getBasicConfig().getQueue();
        this.i18n = i18nMessage;
        onQueue(channel, configuration.getActionOnQueue(), queue);
        channel.queueDeclare(configuration.getBasicConfig().getQueue(), configuration.getBasicConfig().getDurable(), false,
                configuration.getBasicConfig().getAutoDelete(), null);
    }

    private void onQueue(Channel channel, ActionOnQueue action, String queueName) {
        try {
            if (action == DELETE_AND_CREATE_QUEUE) {
                channel.queueDelete(queueName);
            }
        } catch (IOException e) {
            throw new IllegalStateException(i18n.errorCantRemoveQueue());
        }
    }

    @Override
    public void publish(String message) throws IOException {
        channel.basicPublish("", queue, null, message.getBytes());
    }
}