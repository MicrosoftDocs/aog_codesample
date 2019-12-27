using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Azure.Relay;

namespace OutsideClient
{
    class Program
    {
        // e.g. "Endpoint=sb://{RelayNamespace}.servicebus.chinacloudapi.cn/;SharedAccessKeyName={SASSenderKeyName};SharedAccessKey={SASSenderKey};EntityPath={HybridConnectionName}";
        private const string ConnectionString = "connection string with send permission";

        static void Main(string[] args)
        {
            RunAsync().GetAwaiter().GetResult();
        }

        private static async Task RunAsync()
        {
            Console.WriteLine("Enter lines of text to send to the server with ENTER");

            // Create a new hybrid connection client
            var client = new HybridConnectionClient(ConnectionString);

            // Initiate the connection
            var relayConnection = await client.CreateConnectionAsync();

            // We run two conucrrent loops on the connection. One 
            // reads input from the console and writes it to the connection 
            // with a stream writer. The other reads lines of input from the 
            // connection with a stream reader and writes them to the console. 
            // Entering a blank line will shut down the write task after 
            // sending it to the server. The server will then cleanly shut down
            // the connection which will terminate the read task.

            var reads = Task.Run(async () => {
                // Initialize the stream reader over the connection
                var reader = new StreamReader(relayConnection);
                var writer = Console.Out;
                do
                {
                    // Read a full line of UTF-8 text up to newline
                    string line = await reader.ReadLineAsync();
                    // If the string is empty or null, we are done.
                    if (String.IsNullOrEmpty(line))
                        break;
                    // Write to the console
                    await writer.WriteLineAsync(line);
                }
                while (true);
            });

            // Read from the console and write to the hybrid connection
            var writes = Task.Run(async () => {
                var reader = Console.In;
                var writer = new StreamWriter(relayConnection) { AutoFlush = true };
                do
                {
                    // Read a line form the console
                    string line = await reader.ReadLineAsync();
                    // Write the line out, also when it's empty
                    await writer.WriteLineAsync(line);
                    // Quit when the line was empty
                    if (String.IsNullOrEmpty(line))
                        break;
                }
                while (true);
            });

            // Wait for both tasks to complete
            await Task.WhenAll(reads, writes);
            await relayConnection.CloseAsync(CancellationToken.None);
        }
    }
}
