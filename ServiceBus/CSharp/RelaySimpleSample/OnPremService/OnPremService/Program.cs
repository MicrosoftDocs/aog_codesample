namespace OnPremService
{
    using System;
    using System.IO;
    using System.Threading;
    using System.Threading.Tasks;
    using Microsoft.Azure.Relay;

    public class Program
    {
        // e.g. "Endpoint=sb://{RelayNamespace}.servicebus.chinacloudapi.cn/;SharedAccessKeyName={SASListenerKeyName};SharedAccessKey={SASListenerKey};EntityPath={HybridConnectionName}";
        private const string ConnectionString = "connection string with listen permission";

        public static void Main(string[] args)
        {
            RunAsync().GetAwaiter().GetResult();
        }

        private static async Task RunAsync()
        {
            var cts = new CancellationTokenSource();

            var listener = new HybridConnectionListener(ConnectionString);

            // Subscribe to the status events
            listener.Connecting += (o, e) => { Console.WriteLine("Connecting"); };
            listener.Offline += (o, e) => { Console.WriteLine("Offline"); };
            listener.Online += (o, e) => { Console.WriteLine("Online"); };

            // Opening the listener will establish the control channel to
            // the Azure Relay service. The control channel will be continuously 
            // maintained and reestablished when connectivity is disrupted.
            await listener.OpenAsync(cts.Token);
            Console.WriteLine("Server listening");

            // Providing callback for cancellation token that will close the listener.
            cts.Token.Register(() => listener.CloseAsync(CancellationToken.None));

            // Start a new thread that will continuously read the console.
            new Task(() => Console.In.ReadLineAsync().ContinueWith((s) => { cts.Cancel(); })).Start();

            // Accept the next available, pending connection request. 
            // Shutting down the listener will allow a clean exit with 
            // this method returning null
            while (true)
            {
                var relayConnection = await listener.AcceptConnectionAsync();
                if (relayConnection == null)
                {
                    break;
                }

                ProcessMessagesOnConnection(relayConnection, cts);
            }

            // Close the listener after we exit the processing loop
            await listener.CloseAsync(cts.Token);
        }

        private static async void ProcessMessagesOnConnection(HybridConnectionStream relayConnection, CancellationTokenSource cts)
        {
            Console.WriteLine("New session");

            // The connection is a fully bidrectional stream. 
            // We put a stream reader and a stream writer over it 
            // which allows us to read UTF-8 text that comes from 
            // the sender and to write text replies back.
            var reader = new StreamReader(relayConnection);
            var writer = new StreamWriter(relayConnection) { AutoFlush = true };
            while (!cts.IsCancellationRequested)
            {
                try
                {
                    // Read a line of input until a newline is encountered
                    var line = await reader.ReadLineAsync();

                    if (string.IsNullOrEmpty(line))
                    {
                        // If there's no input data, we will signal that 
                        // we will no longer send data on this connection
                        // and then break out of the processing loop.
                        await relayConnection.ShutdownAsync(cts.Token);
                        break;
                    }

                    // Output the line on the console
                    Console.WriteLine(line);

                    // Write the line back to the client, prepending "Echo:"
                    await writer.WriteLineAsync($"Echo: {line}");
                }
                catch (IOException)
                {
                    // Catch an IO exception that is likely caused because
                    // the client disconnected.
                    Console.WriteLine("Client closed connection");
                    break;
                }
            }

            Console.WriteLine("End session");

            // Closing the connection
            await relayConnection.CloseAsync(cts.Token);
        }
    }
}
