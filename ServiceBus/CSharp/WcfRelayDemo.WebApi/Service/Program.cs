using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel.Web;
using System.Text;
using System.Threading.Tasks;

namespace Service
{
    class Program
    {
        static void Main(string[] args)
        {
            // TODO: update the values
            var serviceBusNamespace = "allenlsbrelay";
            var relayServicePath = "data";

            // for mooncake
            var dataSvcUriString = $"https://{serviceBusNamespace}.servicebus.chinacloudapi.cn/{relayServicePath}";

            var webServiceHost = new WebServiceHost(typeof(DataService), new Uri(dataSvcUriString));  
            webServiceHost.Open();

            Console.WriteLine("Invoke service via Service Bus Relay with following request (require sas token): ");
            Console.WriteLine($"{dataSvcUriString}/GetRandomString");
            Console.WriteLine();
            Console.WriteLine("Press [Enter] to exit");
            Console.ReadLine();

            webServiceHost.Close();
        }
    }
}
