using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Net.Http;
using System.Security.Cryptography;
using System.Text;
using System.Web;
using System.Web.Mvc;

namespace WebClient.Controllers
{
    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            // TODO: update the values
            var serviceBusNamespace = "your service bus namespace";
            var relayServicePath = "data"; // you can define your own value
            var keyName = "RootManageSharedAccessKey";
            var key = "your key";

            var resourceUriString = $"https://{serviceBusNamespace}.servicebus.chinacloudapi.cn/{relayServicePath}";
            var token = createToken(resourceUriString, keyName, key);

            try
            {
                using (var httpClient = new HttpClient())
                {
                    httpClient.DefaultRequestHeaders.Add("Authorization", token);

                    var requestUri = $"{resourceUriString}/GetRandomString";

                    using (var response = httpClient.GetAsync(requestUri).Result)
                    {
                        response.EnsureSuccessStatusCode();
                        var result = response.Content.ReadAsStringAsync().Result;
                        ViewBag.Message = result;
                    }
                }
            }
            catch (Exception ex)
            {
                ViewBag.Message = $"Exception happened: {ex.Message}";
            }

            return View();
        }

        private string createToken(string resourceUri, string keyName, string key)
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
    }
}