using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SBRestApiDemo
{
    class ServiceBusHttpMessage
    {
        public byte[] Body;
        public string Location;
        public BrokerProperties SystemProperties;
        public NameValueCollection CustomProperties;

        public ServiceBusHttpMessage()
        {
            SystemProperties = new BrokerProperties();
            CustomProperties = new NameValueCollection();
        }
    }
}
