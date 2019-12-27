using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading;
using Microsoft.ServiceBus;
using Microsoft.ServiceBus.Messaging;
using Microsoft.WindowsAzure;
using Microsoft.WindowsAzure.ServiceRuntime;
using System.Threading.Tasks;
using System.Globalization;

namespace WorkerRoleWithSBQueue1
{
    public class WorkerRole : RoleEntryPoint
    {
        // TODO: update the name of your queue
        const string QueueName = "yourqueuename";

        const int PrefetchCount = 1000;
        const int MaxThreadsCount = 1000;

        // QueueClient is thread-safe. Recommended that you cache 
        // rather than recreating it on every request
        QueueClient Client;
        ManualResetEvent CompletedEvent = new ManualResetEvent(false);

        public override void Run()
        {
            Trace.WriteLine("Starting processing of messages");

            // Initiates the message pump and callback is invoked for each message that is received, calling close on the client will stop the pump.
            Client.OnMessageAsync(async (receivedMessage) =>
            {
                Trace.WriteLine($"Rcv Msg {receivedMessage.MessageId} --- C:{receivedMessage.Properties["CreateTime"]} | E:{receivedMessage.EnqueuedTimeUtc.ToString("HH:mm:ss.fff", CultureInfo.InvariantCulture)} | R:{DateTime.UtcNow.ToString("HH:mm:ss.fff", CultureInfo.InvariantCulture)}");
                // sleep 1s to simulate processing
                await Task.Delay(1000);
                // Process the message
                Trace.WriteLine($"End Msg {receivedMessage.MessageId} --- F:{DateTime.UtcNow.ToString("HH:mm:ss.fff", CultureInfo.InvariantCulture)}");
            }, new OnMessageOptions { AutoComplete = true, MaxConcurrentCalls = MaxThreadsCount });

            CompletedEvent.WaitOne();
        }

        public override bool OnStart()
        {
            // Set the maximum number of concurrent connections 
            ServicePointManager.DefaultConnectionLimit = 12;

            // Create the queue if it does not exist already
            string connectionString = CloudConfigurationManager.GetSetting("Microsoft.ServiceBus.ConnectionString");
            var namespaceManager = NamespaceManager.CreateFromConnectionString(connectionString);
            if (!namespaceManager.QueueExists(QueueName))
            {
                namespaceManager.CreateQueue(QueueName);
            }

            // Initialize the connection to Service Bus Queue
            Client = QueueClient.CreateFromConnectionString(connectionString, QueueName);
            Client.PrefetchCount = PrefetchCount;

            return base.OnStart();
        }

        public override void OnStop()
        {
            // Close the connection to Service Bus Queue
            Client.Close();
            CompletedEvent.Set();
            base.OnStop();
        }
    }
}
