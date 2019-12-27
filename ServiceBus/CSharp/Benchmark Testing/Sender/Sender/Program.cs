using Microsoft.ServiceBus;
using Microsoft.ServiceBus.Messaging;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Sender
{
    class Program
    {
        static void Main(string[] args)
        {
            // TODO: update with your own value here
            var sbConnStr = "yourservicebusconnectionstring";
            var queueName = "yourqueuename";

            var totalMessagesCount = 1000;
            var batchSize = 100;

            SendMessages(sbConnStr, queueName, totalMessagesCount, batchSize);

            Console.WriteLine("Press ENTER to exit!");
            Console.ReadLine();
        }

        private static void SendMessages(string sbConnectionString, string entityName, int messagesCount, int batchSize)
        {
            var namespaceManager = NamespaceManager.CreateFromConnectionString(sbConnectionString);
            if (!namespaceManager.QueueExists(entityName))
            {
                namespaceManager.CreateQueue(entityName);
            }
            else
            {
                var queue = namespaceManager.GetQueue(entityName);
                Console.WriteLine($"Properties of queue named {entityName}");
                Console.WriteLine($"EnableExpress: {queue.EnableExpress}");
                Console.WriteLine($"EnableBatchedOperations: {queue.EnableBatchedOperations}");
            }

            var queueClient = QueueClient.CreateFromConnectionString(sbConnectionString, entityName);

            // Generate message batches            
            var batchMsgDic = new Dictionary<int, List<BrokeredMessage>>();
            var batchMsgs = new List<BrokeredMessage>(batchSize);
            var batchCount = 0;
            for (int i = 1; i <= messagesCount; i++)
            {
                // Create message, passing a string message for the body.
                var message = new BrokeredMessage($"Test message {i}");

                // Set additional custom app-specific property.
                message.Properties["MessageId"] = Guid.NewGuid();
                message.Properties["CreateTime"] = DateTime.UtcNow.ToString("HH:mm:ss.fff", CultureInfo.InvariantCulture);
                Console.WriteLine($"{DateTime.UtcNow.ToString("HH:mm:ss.fff", CultureInfo.InvariantCulture)} --- Create message {i}");

                batchMsgs.Add(message);
                if (i % batchSize == 0)
                {
                    batchCount++;
                    batchMsgDic.Add(batchCount, batchMsgs);
                    batchMsgs = new List<BrokeredMessage>(batchSize);
                }
            }

            // Send message batches asynchronoursly without waiting
            var sendTasks = new List<Task>(batchCount);
            foreach (var batch in batchMsgDic)
            {
                sendTasks.Add(queueClient.SendBatchAsync(batch.Value));
                Console.WriteLine($"{DateTime.UtcNow.ToString("HH:mm:ss.fff", CultureInfo.InvariantCulture)} --- Sent batch {batch.Key}");
            }
            Task.WaitAll(sendTasks.ToArray());
            Console.WriteLine("All messages are sent!");

            queueClient.CloseAsync().ContinueWith((t) => Console.WriteLine("Queue Client is closed!"));
        }
    }
}
