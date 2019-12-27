package com.vianet.azure.sdk.manage.servicebus;

import com.vianet.azure.sdk.manage.Configure;

import javax.jms.*;

/**
 * Created by chen.rui on 5/16/2016.
 */
public class TestServiceBus {

    public static void main(String[] args) throws JMSException, Exception {
        System.getProperties().put("qpid.sync_publish", true);
        Configure configure = new Configure();
        AzureServiceBusServiceFactory serviceFactory = AzureServiceBusServiceFactory.getInstance(configure);

        ServiceBusQueueService serviceBusQueueService = serviceFactory.createQueueService();
        serviceBusQueueService.createReceiver(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage text = (TextMessage) message;
                try {
                    System.out.println(text.getText());
                } catch (JMSException e) {
                    System.out.println("bbbb");
                    e.printStackTrace();
                }
            }

        }, new ExceptionListener() {
            @Override
            public void onException(JMSException exception) {
                System.out.println("aaaaaaaaaaaaaa");
                exception.printStackTrace(System.out);
                System.exit(1);
            }
        });
    }

}
