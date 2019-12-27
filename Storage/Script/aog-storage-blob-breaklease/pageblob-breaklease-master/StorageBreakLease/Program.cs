using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Auth;
using Microsoft.WindowsAzure.Storage.Blob;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace StorageBreakLease
{
    class Program
    {
        static string initAccount = string.Empty;
        static StorageCredentials credentials =null;
        static CloudStorageAccount storageAccount=null;

        [DllImport("kernel32.dll")]
        static extern bool SetConsoleMode(IntPtr hConsoleHandle, int mode);
        [DllImport("kernel32.dll")]
        static extern bool GetConsoleMode(IntPtr hConsoleHandle, out int mode);
        [DllImport("kernel32.dll")]
        static extern IntPtr GetStdHandle(int handle); const int STD_INPUT_HANDLE = -10; const int ENABLE_QUICK_EDIT_MODE = 0x40 | 0x80;

        public static void EnableQuickEditMode()
        {
            int mode;
            IntPtr handle = GetStdHandle(STD_INPUT_HANDLE);
            GetConsoleMode(handle, out mode);
            mode |= ENABLE_QUICK_EDIT_MODE; SetConsoleMode(handle, mode);
        }


        static void Main(string[] args)
        {
            EnableQuickEditMode();
            // Header.
            Console.WriteLine("");
            Console.WriteLine(" Windows Azure Page Blob Breaklease Tool Ver 1.0 - George He");
            Console.WriteLine(" Usage: breaklease <accountName> <accountKey> <page blob url>");

        Begin:
            ConsoleColor colorFore = Console.ForegroundColor;
            Console.ForegroundColor = ConsoleColor.Yellow;
            Console.WriteLine("Input your script, press enter to run：");
            Console.ForegroundColor = colorFore;

            try
            {


                args = Console.ReadLine().Replace("breaklease ", "").Split(new char[] { ' ' });

                // Validate args.
                if (args.Length != 3)
                {
                    Console.WriteLine(" Invalid number of arguments.");
                    Console.WriteLine("");
                    goto Begin;
                }

                var uri = args[2];
                Console.WriteLine("Breaklease Processing: {0}", uri);
                Console.WriteLine("");

                if (credentials == null || args[0] != initAccount)
                {
                    initAccount = args[0];
                    credentials = new StorageCredentials(args[0], args[1]);
                    storageAccount = new CloudStorageAccount(credentials, new Uri(string.Format("http://{0}.blob.core.chinacloudapi.cn/", args[0])), null, null, null);
                }

                var arrs = args[2].Split(new char[] { '/' });

                CloudBlobClient blobClient = storageAccount.CreateCloudBlobClient();
                CloudBlobContainer container = blobClient.GetContainerReference(arrs[3]);
                CloudBlockBlob blob = container.GetBlockBlobReference(arrs[4]);
                //Timespan is a period of time in seconds, here means 1 sec later break the lease after command execution.
                TimeSpan breakTime = new TimeSpan(0, 0, 1);
                blob.BreakLease(breakTime);
                Console.WriteLine("Breaklease successful, you can try to delete the file again!");
            }
            catch (Exception ex)
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("Error: {0}", ex);
                Console.ForegroundColor = colorFore;
            }

            goto Begin;
            Console.ReadLine();
        }
    }
}
