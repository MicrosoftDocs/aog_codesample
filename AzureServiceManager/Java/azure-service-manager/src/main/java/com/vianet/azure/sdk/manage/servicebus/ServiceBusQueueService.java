/*
 * Copyright (c) 2015-2020, Chen Rui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vianet.azure.sdk.manage.servicebus;

import java.util.Map;
import java.util.Map.Entry;

import javax.jms.*;

import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;

public class ServiceBusQueueService {
	

	private ConnectionFactory connectionFactory;
	
	private String defaultQueue;
	
	protected ServiceBusQueueService(ConnectionFactory connectionFactory, String defaultQueue) {
		this.connectionFactory = connectionFactory;
		this.defaultQueue = defaultQueue;
	}

	public ReceiveRunnable createReceiver(String queueName, MessageListener listener, ExceptionListener exceptionListener) throws JMSException {
		ReceiveRunnable runnable = new ReceiveRunnable(queueName, connectionFactory, listener, exceptionListener);
		new Thread(runnable).start();
		return runnable;
	}

	public void createReceiver(MessageListener listener, ExceptionListener exceptionListener) throws JMSException {
		this.createReceiver(this.defaultQueue, listener, exceptionListener);
	}

	public void sendTextMessage(String message) {
		this.sendTextMessage(this.defaultQueue, message);
	}
	
	public void sendTextMessage(String queue, String message) {
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(QueueImpl.createQueue(queue));
			producer.send(session.createTextMessage(message));
		} catch (JMSException e) {
			e.printStackTrace();
		} finally {
			try {
				session.close();
				producer.close();
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendMapMessage(Map<String, Object> message) {
		this.sendMapMessage(this.defaultQueue, message);
	}
	
	public void sendMapMessage(String queue, Map<String, Object> message) {
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(QueueImpl.createQueue(queue));
			MapMessage mapMessage = session.createMapMessage();
			for(Entry<String, Object> entry : message.entrySet()) {
				mapMessage.setObject(entry.getKey(), entry.getValue());
			}
			producer.send(mapMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		} finally {
			try {
				session.close();
				producer.close();
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	
}
