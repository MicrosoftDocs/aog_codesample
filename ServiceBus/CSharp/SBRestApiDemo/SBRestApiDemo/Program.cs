using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using System.Web;

namespace SBRestApiDemo
{
    class Program
    {
        static string serviceBusNamespace = "yourservicebusnamespace";
        static string queueName = "yourqueuename";

        static void Main(string[] args)
        {            
            var senderKeyName = "nameofkeywithsendpermission";
            var senderKey = @"valueofkeywithsendpermission";
            var receiverKeyName = "nameofkeywithreceivepermission";
            var receiverKey = @"valueofkeywithreceivepermission";

            // mooncake
            var queueUri = $"https://{serviceBusNamespace}.servicebus.chinacloudapi.cn/{queueName}";
            // send message 1
            Console.WriteLine("Sending message 1...");
            var message1 = new ServiceBusHttpMessage();
            var senderSasToken = createToken(queueUri, senderKeyName, senderKey);
            
            message1.Body = Encoding.UTF8.GetBytes("This is the first message.");
            message1.SystemProperties.Label = "M1";
            message1.SystemProperties.TimeToLive = TimeSpan.FromSeconds(10);
            message1.CustomProperties.Add("Priority", "High");
            message1.CustomProperties.Add("Customer", "12345");
            message1.CustomProperties.Add("Customer", "ABC");
            SendMessageAsync(message1, senderSasToken).Wait();

            // receive message with ReceiveAndDelete mode
            Console.WriteLine("Receiving message 1 with ReceiveAndDelete mode...");
            var receiverSasToken = createToken(queueUri, receiverKeyName, receiverKey);
            var receivedMsg = ReceiveMessageAsync(receiverSasToken).Result;
            if (receivedMsg != null)
            {
                Console.WriteLine($"ReceiveAndDeleted message: {Encoding.UTF8.GetString(receivedMsg.Body)} with id {receivedMsg.SystemProperties.MessageId}");
            }

            // send message 2
            Console.WriteLine("Sending message 2...");
            var message2 = new ServiceBusHttpMessage();
            message2.Body = Encoding.UTF8.GetBytes("This is the second message.");
            message2.SystemProperties.Label = "M2";
            message2.SystemProperties.TimeToLive = TimeSpan.FromSeconds(20);
            message2.CustomProperties.Add("Priority", "Middle");
            message2.CustomProperties.Add("User", "12345");
            SendMessageAsync(message2, senderSasToken).Wait();

            // receive message with PeekLock mode
            Console.WriteLine("Receiving message 2 with PeekLock mode...");
            var receivedMsg2 = ReceiveMessageAsync(receiverSasToken, deleteMessage:false).Result;
            if (receivedMsg2 != null)
            {
                var msgId = receivedMsg2.SystemProperties.MessageId;
                var lockToken = receivedMsg2.SystemProperties.LockToken;
                var msgBody = Encoding.UTF8.GetString(receivedMsg2.Body);
                Console.WriteLine($"PeekAndLocked message: {msgBody} with id {msgId} and lock token {lockToken}");
                DeleteMessageAsync(msgId, lockToken, receiverSasToken).Wait();
                Console.WriteLine($"Completed message");
            }            

            Console.WriteLine("Press ENTER to exit!");
            Console.ReadLine();
        }

        static string createToken(string resourceUri, string keyName, string key)
        {
            TimeSpan sinceEpoch = DateTime.UtcNow - new DateTime(1970, 1, 1);
            var week = 60 * 60 * 24 * 7;
            var expiry = Convert.ToString((int)sinceEpoch.TotalSeconds + week);
            string stringToSign = HttpUtility.UrlEncode(resourceUri) + "\n" + expiry;
            HMACSHA256 hmac = new HMACSHA256(Encoding.UTF8.GetBytes(key));
            var signature = Convert.ToBase64String(hmac.ComputeHash(Encoding.UTF8.GetBytes(stringToSign)));
            var sasToken = String.Format(CultureInfo.InvariantCulture, 
                "SharedAccessSignature sr={0}&sig={1}&se={2}&skn={3}", 
                HttpUtility.UrlEncode(resourceUri), 
                HttpUtility.UrlEncode(signature), 
                expiry, 
                keyName);
            return sasToken;
        }

        static async Task SendMessageAsync(ServiceBusHttpMessage message, string token)
        {
            var address = $"https://{serviceBusNamespace}.servicebus.chinacloudapi.cn/{queueName}/messages";

            HttpContent postContent = new ByteArrayContent(message.Body);

            // Serialize BrokerProperties. 
            var brokerProps = JsonConvert.SerializeObject(message.SystemProperties,
                Formatting.None,
                new JsonSerializerSettings { NullValueHandling = NullValueHandling.Ignore, 
                    DefaultValueHandling = DefaultValueHandling.Ignore });

            postContent.Headers.Add("BrokerProperties", brokerProps);

            // Add custom properties. 
            foreach (string key in message.CustomProperties)
            {
                postContent.Headers.Add(key, message.CustomProperties.GetValues(key));
            }

            var httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.Add("Authorization", token);
            httpClient.DefaultRequestHeaders.Add("ContentType", "application/atom+xml;type=entry;charset=utf-8");

            // Send message. 
            HttpResponseMessage response = null;
            try
            {
                response = await httpClient.PostAsync($"{address}?timeout=60", postContent);
                response.EnsureSuccessStatusCode();
                Console.WriteLine("SendMessage successfully!");
            }
            catch (HttpRequestException ex)
            {
                Console.WriteLine($"SendMessage failed: {ex.Message}");
            }
            response.Dispose();
        }

        static async Task<ServiceBusHttpMessage> ReceiveMessageAsync(string token, bool deleteMessage = true)
        {
            var address = $"https://{serviceBusNamespace}.servicebus.chinacloudapi.cn/{queueName}/messages/head";

            var httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.Add("Authorization", token);

            HttpResponseMessage response = null;

            try
            {
                if (deleteMessage)
                {
                    // receive and delete
                    response = await httpClient.DeleteAsync($"{address}?timeout=60");
                }
                else
                {
                    // peek lock
                    response = await httpClient.PostAsync($"{address}?timeout=60", new ByteArrayContent(new Byte[0]));
                }
                
                response.EnsureSuccessStatusCode();
                Console.WriteLine($"ReceiveMessage successfully!");
            }
            catch (HttpRequestException ex)
            {
                Console.WriteLine($"ReceiveMessage failed: {ex.Message}");
                return null;
            }

            var message = await ResolveMessageFromResponse(response);
            response.Dispose();
            return message;
        }

        // Delete message with the specified MessageId and LockToken. 
        static async Task DeleteMessageAsync(string messageId, Guid LockToken, string token)
        {
            var address = $"https://{serviceBusNamespace}.servicebus.chinacloudapi.cn/{queueName}/messages/{messageId}/{LockToken.ToString()}";
            await DeleteMessageAsync(address, token);
        }

        // Delete message with the specified SequenceNumber and LockToken 
        static async Task DeleteMessageAsync(long seqNum, Guid LockToken, string token)
        {
            var address = $"https://{serviceBusNamespace}.servicebus.chinacloudapi.cn/{queueName}/messages/{seqNum}/{LockToken.ToString()}";
            await DeleteMessageAsync(address, token);
        }

        // Delete message with the specified URI. The URI is returned in the Location header of the response of the Peek request. 
        static async Task DeleteMessageAsync(string messageUri, string token)
        {
            var httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.Add("Authorization", token);

            HttpResponseMessage response = null;
            try
            {
                response = await httpClient.DeleteAsync(messageUri + "?timeout=60");
                response.EnsureSuccessStatusCode();
                Console.WriteLine("DeleteMessage successfully: ");
            }
            catch (HttpRequestException ex)
            {
                Console.WriteLine("DeleteMessage failed: " + ex.Message);
            }
            response.Dispose();
        }

        static async Task<ServiceBusHttpMessage> ResolveMessageFromResponse(HttpResponseMessage response)
        {
            // Check if a message was returned. 
            HttpResponseHeaders headers = response.Headers;
            if (!headers.Contains("BrokerProperties"))
            {
                return null;
            }

            // Get message body. 
            ServiceBusHttpMessage message = new ServiceBusHttpMessage();
            message.Body = await response.Content.ReadAsByteArrayAsync();

            // Deserialize BrokerProperties. 
            string brokerProperties = headers.GetValues("BrokerProperties").First();
            message.SystemProperties = JsonConvert.DeserializeObject<BrokerProperties>(brokerProperties);

            // Get custom propoerties. 
            foreach (var header in headers)
            {
                string key = header.Key;
                if (!key.Equals("Transfer-Encoding") && !key.Equals("BrokerProperties") && !key.Equals("ContentType") && !key.Equals("Location") && !key.Equals("Date") && !key.Equals("Server"))
                {
                    foreach (string value in header.Value)
                    {
                        message.CustomProperties.Add(key, value);
                    }
                }
            }

            // Get message URI. 
            if (headers.Contains("Location"))
            {
                IEnumerable<string> locationProperties = headers.GetValues("Location");
                message.Location = locationProperties.FirstOrDefault();
            }
            return message;
        }
    }
}
