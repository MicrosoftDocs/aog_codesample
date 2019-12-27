package com.hula.sample.nature_mqtt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class App {
	public static void main(String[] args) throws MqttException, IOException, InterruptedException {
		int maxInFlight = 100;
		String hostName = ""; // e.g. iothubdemo.azure-devices.cn
		String deviceId = "";
		String deviceKey = "";
				
		String serverURI = "ssl://" + hostName + ":8883";
		String topic = "devices/" + deviceId + "/messages/events/";
		
		// demo for topic name with property_bag: devices/{device_id}/messages/events/{property_bag}
		//String topicProperty = URLEncoder.encode("testp", "UTF-8") + "=" + URLEncoder.encode("testv", "UTF-8");
		//String topicWithProperty = "devices/" + deviceId + "/messages/events/" + topicProperty;

		MqttAsyncClient client = new MqttAsyncClient(serverURI, deviceId, new MemoryPersistence());
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(hostName + "/" + deviceId);
		connOpts.setPassword(generateSasToken(hostName + "/devices/" + deviceId, deviceKey).toCharArray());
		connOpts.setMaxInflight(maxInFlight);
		connOpts.setCleanSession(false);
		connOpts.setAutomaticReconnect(true);

		IMqttToken conToken = client.connect(connOpts);
		conToken.waitForCompletion();
		System.out.println("Connected to IOT Hub server " + hostName);

		try {
			// send 5 messages
			for (int i = 1; i <= 5; i++) {
				String msgStr = "message " + i;
				MqttMessage message = new MqttMessage(msgStr.getBytes());
				message.setQos(1);
				IMqttDeliveryToken delToken = client.publish(topic, message);
				// demo for topic name with property_bag
				// in this way, all events will contains the property
				//IMqttDeliveryToken delToken = client.publish(topicWithProperty, message);
				delToken.waitForCompletion();
				System.out.println("sent message " + i);
			}
			
			IMqttToken discToken = client.disconnect();
			discToken.waitForCompletion();
			System.out.println("connection is disconnected!");
			
		} catch (Exception e) {
			System.out.println("got exception: ");
			e.printStackTrace();
		} finally {			
			client.close();
			System.out.println("client is closed!");
		}
	}

	private static String generateSasToken(String serverUri, String key) throws UnsupportedEncodingException {
		String tokenFormat = "SharedAccessSignature sig=%s&se=%s&sr=%s";
		String scope = URLEncoder.encode(serverUri, StandardCharsets.UTF_8.toString());
		long expiryTime = System.currentTimeMillis() / 1000l + 600 + 1l;
		
		String signatureStr = generateSignature(scope, expiryTime, key); 

		return String.format(tokenFormat, signatureStr, expiryTime, scope);
	}
	
	private static String generateSignature(String resrouceUri, long expiryTime, String deviceKey) throws UnsupportedEncodingException
	{
		byte[] rawSig = String.format("%s\n%s", resrouceUri, expiryTime).getBytes(StandardCharsets.UTF_8);
		byte[] decodedDeviceKey = Base64.decodeBase64(deviceKey.getBytes());
		
		String hmacSha256 = "HmacSHA256";
		SecretKeySpec secretKey = new SecretKeySpec(decodedDeviceKey, hmacSha256);
		byte[] encryptedSig = null;
		try
		{
			Mac hMacSha256 = Mac.getInstance(hmacSha256);
			hMacSha256.init(secretKey);
			encryptedSig = hMacSha256.doFinal(rawSig);
		}
		catch (NoSuchAlgorithmException e)
		{ 
			// should never happen, since the algorithm is hard-coded
		}
		catch (InvalidKeyException e)
		{
			// should never happen, since the algorithm is hard-coded 
		}
		
		byte[] encryptedSigBase64 = Base64.encodeBase64(encryptedSig);
		String utf8Sig = new String(encryptedSigBase64, StandardCharsets.UTF_8);		
		String signatureStr = URLEncoder.encode(utf8Sig, StandardCharsets.UTF_8.name());
		
		return signatureStr;
	}
}
