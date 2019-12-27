package com.vianet.azure.sdk.manage.servicebus;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.*;


public class AzureServiceMQ {

    private String topic = "kevintopic";
    private String subName = "HighMessages";

    ServiceBusContract service;

    public AzureServiceMQ() throws ServiceException {
        this.service = ServiceBusService.create(ServiceBusConfiguration.configureWithSASAuthentication(
                "kevinsb",
                "RootManageSharedAccessKey",
                "fKnBTS7+OUVaaNof1CbpxdvRg5F6NEbNN+HotxYviVk=",
                ".servicebus.chinacloudapi.cn"));
        createSub();
    }

    private void createSub() throws ServiceException {
        SubscriptionInfo subInfo = new SubscriptionInfo(subName);
        GetSubscriptionResult subscriptionResult = service.getSubscription(topic, subName);
        if(subscriptionResult == null) {
            CreateSubscriptionResult result = service.createSubscription(topic, subInfo);
            RuleInfo ruleInfo = new RuleInfo("myRuleGT3");
            ruleInfo = ruleInfo.withSqlExpressionFilter("MessageNumber > 3");
            CreateRuleResult ruleResult = service.createRule(topic, subName, ruleInfo);
            // Delete the default rule, otherwise the new rule won't be invoked.
            service.deleteRule(topic, subName, "$Default");
        }
    }

    public void sendMessage(String text, Integer number) {
        new Thread(new SendRunable(text, number)).start();
    }

    public void receiveMessage() {
        new Thread(new ReceiveRunable()).start();
    }

    private class SendRunable implements  Runnable {

        private String text;

        private Integer number;

        SendRunable(String text, Integer number) {
            this.text = text;
            this.number = number;
        }

        public void run() {
            for(int i = 0; i < number; i++) {
                try {
                    BrokeredMessage message = new BrokeredMessage(text + " [" + number + "] : " + i);
                    message.setProperty("MessageNumber", 5);
                    long time = System.currentTimeMillis();
                    service.sendTopicMessage(topic, message);
                    long endtime = System.currentTimeMillis();
                    System.out.println("SendMessageTime: " + (endtime-time) + "ms");
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ReceiveRunable implements  Runnable {
        @Override
        public void run() {
            ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
            opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
            while (true) {
                try {
                    long time = System.currentTimeMillis();
                    ReceiveSubscriptionMessageResult resultSubMsg = service.receiveSubscriptionMessage(topic, subName, opts);
                    BrokeredMessage message = resultSubMsg.getValue();
                    long endtime = System.currentTimeMillis();
                    System.out.println("ReceiveMessageTime: " + (endtime-time) + "ms");

                    if(message == null) continue;

                    System.out.println("MessageID: " + message.getMessageId());
                    // Display the topic message.
                    System.out.println("From topic: " + topic);
                    System.out.print("Message Body: ");
                    byte[] b = new byte[200];
                    String s = null;
                    int numRead = message.getBody().read(b);
                    while (-1 != numRead) {
                        s = new String(b);
                        s = s.trim();
                        System.out.print(s);
                        numRead = message.getBody().read(b);
                    }
                    System.out.println();
                    System.out.println("Custom Property: " + message.getProperty("MessageNumber"));
                    // Delete message.
                    System.out.println("Deleting this message.");
                    service.deleteMessage(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws ServiceException {
        AzureServiceMQ mq = new AzureServiceMQ();
        mq.sendMessage("test1", 100);
        mq.receiveMessage();
    }

}
