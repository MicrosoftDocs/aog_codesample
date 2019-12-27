import base64
import hmac
import urllib.parse

import paho.mqtt.client as mqtt
import time

hubAddress = "freeiottest.azure-devices.cn"
deviceId = "myDeviceId"
deviceKey = '<your device key>'

hubUser = hubAddress + '/' + deviceId
endpoint = hubAddress + '/devices/' + deviceId
hubTopicPublish = 'devices/' + deviceId + '/messages/events/'
hubTopicSubscribe = 'devices/' + deviceId + '/messages/devicebound/#'


def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe(hubTopicSubscribe)
    client.publish(hubTopicPublish, "testmessage")


def on_message(client, userdata, msg):
    print("{0} - {1} ".format(msg.topic, str(msg.payload)))


def generate_sas_token(uri, key, expiry=3600):
    ttl = int(time.time()) + expiry
    urlToSign = urllib.parse.quote(uri, safe='')
    sign_key = "%s\n%d" % (urlToSign, int(ttl))
    h = hmac.new(base64.b64decode(key), msg="{0}\n{1}".format(urlToSign, ttl).encode('utf-8'), digestmod='sha256')
    signature = urllib.parse.quote(base64.b64encode(h.digest()), safe='')
    return "SharedAccessSignature sr={0}&sig={1}&se={2}".format(urlToSign,
                                                                urllib.parse.quote(base64.b64encode(h.digest()),
                                                                                   safe=''), ttl)


client = mqtt.Client(client_id=deviceId, protocol=mqtt.MQTTv311)
client.on_connect = on_connect
client.on_message = on_message


client.tls_set("WS_CA1_NEW.crt")
client.username_pw_set(username=hubUser,
                       password=generate_sas_token(endpoint, deviceKey))
client.connect("freeiottest.azure-devices.cn", port=8883, keepalive=60)

client.loop_forever()