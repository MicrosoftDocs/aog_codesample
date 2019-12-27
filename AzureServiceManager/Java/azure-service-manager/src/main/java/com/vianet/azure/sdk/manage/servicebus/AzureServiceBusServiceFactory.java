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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.vianet.azure.sdk.manage.Configure;


public class AzureServiceBusServiceFactory {
	
	private static AzureServiceBusServiceFactory serviceFactory = null;

	private final Configure conf;
	
	private String username;
	
	private String password;
	
	private String host;

	private ServiceBusQueueService serviceBusQueueService;

	private AzureServiceBusServiceFactory(Configure conf) {
		this.conf = conf;
	}
	
	public synchronized static  AzureServiceBusServiceFactory getInstance(Configure conf) {
		if(serviceFactory == null) {
			serviceFactory = new AzureServiceBusServiceFactory(conf);
		}
		return serviceFactory;
	}
	
	public ServiceBusQueueService createQueueService() {
		if(this.serviceBusQueueService == null) {
			try {
				String defaultQueue = this.conf.getProperty("azure.servicebus.queue");
				ConnectionFactory connectionFactory = getConnectFactory();
				serviceBusQueueService = new ServiceBusQueueService(connectionFactory, defaultQueue);
			} catch (NamingException | IOException e) {
				e.printStackTrace();
			};
		}
		return this.serviceBusQueueService;
	}

	private ConnectionFactory getConnectFactory() throws NamingException, IOException {
		this.username = this.conf.getProperty("azure.servicebus.username");
		this.password = this.conf.getProperty("azure.servicebus.password");
		this.host = this.conf.getProperty("azure.servicebus.host");

		String connectionString = "amqps://"+username+":" + encode(password) + "@" + host;
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.amqp_1_0.jms.jndi.PropertiesFileInitialContextFactory");
		env.put("connectionfactory.ServiceBusConnectionFactory", connectionString);

		Context context = new InitialContext(env);
		ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ServiceBusConnectionFactory");
		return connectionFactory;
	}
	
	public static String encode(String encodee) {
		String retval = "";
		try {
			retval = URLEncoder.encode(encodee, "UTF-8");
		} catch (Exception e) {
			System.out.print("Encoding failed\n");
		}
		return retval;
	}

}
