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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.*;

import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;


public class ReceiveRunnable implements Runnable {

	private String queueName;

	private MessageListener listener;

	private ExceptionListener exceptionListener;

	ConnectionFactory connectionFactory;

	Connection connection;

	Session session;

	MessageConsumer receiveConsumer;

	public ReceiveRunnable(String queueName, ConnectionFactory connectionFactory, MessageListener listener, ExceptionListener exceptionListener) {
		this.queueName = queueName;
		this.connectionFactory = connectionFactory;
		this.listener = listener;
		this.exceptionListener = exceptionListener;
	}

	@Override
	public void run() {
		try {
			connection = connectionFactory.createConnection();
			connection.setExceptionListener(exceptionListener);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			receiveConsumer = session.createConsumer(QueueImpl.createQueue(queueName));
			receiveConsumer.setMessageListener(listener);
			connection.start();
		} catch (Exception ex) {
			System.out.println("cccc");
			ex.printStackTrace();
		}
	}

	public void close() {
		try {
			session.close();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}


