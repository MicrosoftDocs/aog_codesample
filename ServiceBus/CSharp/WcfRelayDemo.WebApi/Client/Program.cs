using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Net.Http;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using System.Web;

namespace Client
{
    class Program
    {
        static void Main(string[] args)
        {
            // TODO: update the values
            var serviceBusNamespace = "your service bus namespace";
            var relayServicePath = "data"; // you can define your own value
            var keyName = "key with sender permission";
            var key = "your key";

            var resourceUriString = $"https://{serviceBusNamespace}.servicebus.chinacloudapi.cn/{relayServicePath}";
            var token = createToken(resourceUriString, keyName, key);

            try
            {
                using (var httpClient = new HttpClient())
                {
                    httpClient.DefaultRequestHeaders.Add("Authorization", token);

                    var requestUri = $"{resourceUriString}/GetRandomString";
                    Console.WriteLine($"Invoke service via Service Bus Relay: {requestUri}");
                    Console.WriteLine($"with sas token: {token}");

                    using (var response = httpClient.GetAsync(requestUri).Result)
                    {
                        response.EnsureSuccessStatusCode();
                        var result = response.Content.ReadAsStringAsync().Result;
                        Console.WriteLine($"\nResult: {result}");
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Exception happened: {ex.Message}");
            }

            Console.WriteLine("\nPress [Enter] to exit");
            Console.ReadLine();
        }

        private static string createToken(string resourceUri, string keyName, string key)
        {
            TimeSpan sinceEpoch = DateTime.UtcNow - new DateTime(1970, 1, 1);
            // expiration one week
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
    }
}
