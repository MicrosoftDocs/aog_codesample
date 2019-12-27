using Microsoft.Azure.Management.IotHub;
using Microsoft.Azure.Management.ResourceManager.Fluent;
using Microsoft.Rest;
using Microsoft.Rest.Azure.Authentication;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace IoTHubManagementDemo
{
    class Program
    {
        static void Main(string[] args)
        {
            var resourceGroupName = "your resource group name of iot hub";
            var iothubName = "your iot hub name";
            var tenantId = "your tenant";
            var subscriptionId = "your subscription";            

            #region for native client, based on user login
            var nativeClientId = "your native client id";
            var redirectUri = "your native client redirect uri";
            var adServiceSettings = new ActiveDirectoryServiceSettings
            {
                AuthenticationEndpoint = new Uri(AzureEnvironment.AzureChinaCloud.AuthenticationEndpoint),
                TokenAudience = new Uri(AzureEnvironment.AzureChinaCloud.ResourceManagerEndpoint),
                ValidateAuthority = true
            };
            var adClientSettings = new ActiveDirectoryClientSettings()
            {
                ClientId = nativeClientId,
                ClientRedirectUri = new Uri(redirectUri)
            };
            SynchronizationContext.SetSynchronizationContext(new SynchronizationContext());
            ServiceClientCredentials azureCredential = null;
            try
            {
                azureCredential = UserTokenProvider.LoginWithPromptAsync(tenantId, adClientSettings, adServiceSettings).Result;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Acquire credential failed: {ex.Message}");
            }
            #endregion

            #region for web client, based on clientid and clientsecret
            //var webClientId = "your web client id";
            //azureCredential = SdkContext.AzureCredentialsFactory.FromServicePrincipal(
            //        webClientId,
            //        "!!123abc",
            //        tenantId,
            //        AzureEnvironment.AzureChinaCloud); 
            #endregion

            if (azureCredential != null)
            {
                var iothubClient = new IotHubClient(new Uri("https://management.chinacloudapi.cn/"), azureCredential, new RetryDelegatingHandler());
                iothubClient.SubscriptionId = subscriptionId;

                var iothubResource = iothubClient.IotHubResource;

                // get iothub description
                var iothubDescription = iothubResource.Get(resourceGroupName, iothubName);
                Console.WriteLine($"Get iothub successfully: {iothubDescription.Name}");

                // set C2D message default ttl to 2 hours
                iothubDescription.Properties.CloudToDevice.DefaultTtlAsIso8601 = TimeSpan.FromHours(2);

                try
                {
                    // commit the change          
                    iothubResource.CreateOrUpdate(resourceGroupName, iothubName, iothubDescription);
                    Console.WriteLine("Update iothub successfully!");
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Update iothub failed: {ex.Message}");
                }
            }
            
            Console.WriteLine("Press ENTER to exit!");
            Console.ReadLine();
        }
    }
}
