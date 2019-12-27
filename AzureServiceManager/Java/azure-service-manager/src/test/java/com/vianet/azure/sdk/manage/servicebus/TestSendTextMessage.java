package com.vianet.azure.sdk.manage.servicebus;

import com.vianet.azure.sdk.manage.Configure;

/**
 * Created by chen.rui on 5/17/2016.
 */
public class TestSendTextMessage {

    public static void main(String[] args) {
        Configure configure = new Configure();
        AzureServiceBusServiceFactory serviceFactory = AzureServiceBusServiceFactory.getInstance(configure);
        ServiceBusQueueService serviceBusQueueService = serviceFactory.createQueueService();
        serviceBusQueueService.sendTextMessage("testqueue", "kyessss");
        System.exit(0);
    }
}
