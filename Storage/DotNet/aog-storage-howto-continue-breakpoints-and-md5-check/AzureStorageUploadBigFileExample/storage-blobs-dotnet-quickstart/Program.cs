namespace AzureStorageUploadBigFileExample
{
	using Microsoft.WindowsAzure.Storage;
	using Microsoft.WindowsAzure.Storage.Blob;
	using System;
	using System.Collections.Generic;
	using System.IO;
	using System.Text;
	using System.Threading.Tasks;
	using System.Security.Cryptography;

	public static class Program
	{
		public static void Main()
		{
			Console.WriteLine("Azure-Storage-resume-from-breakpoint-example");
			Console.WriteLine();
			//ProcessAsync().GetAwaiter().GetResult();
			//uploadBigFile().GetAwaiter().GetResult();
			string uri = string.Concat("https://", AzureConstants.Account, ".blob.core.chinacloudapi.cn/");//注意正确修改指向中国版Azure的终结点
																										   //文件路径
			string sourcePath = @"文件路径";
			//上传块大小
			int bufferSize = 1024 * 1024 * 2;
			//断点上传记录文件
			string flagFile = Path.GetFileNameWithoutExtension(sourcePath) + ".txt";
			int fileSize = File.ReadAllBytes(sourcePath).Length;
			string contentHash = md5()(File.ReadAllBytes(sourcePath));
			//块数量
			int fileCount = fileSize / bufferSize + 1;
			int currentCount = 0;
			byte[] buffer = new byte[bufferSize];
			List<string> blobIdList = new List<string>();
			//文件处理方式
			if (!File.Exists(flagFile)) {
				FileStream fs = File.Create(flagFile);
				fs.Close();
				fs.Dispose();
			}
			else {
				string[] blobIds = File.ReadAllLines(flagFile);
				currentCount = blobIds.Length;
				blobIdList.AddRange(blobIds);
			}
			BlobHelper helper = new BlobHelper("BlockBlob", uri);
			try {
				for (int i = currentCount; i < fileCount; i++) {
					using (FileStream fs = new FileStream(sourcePath, FileMode.Open, FileAccess.Read, FileShare.Read, bufferSize)) {
						fs.Read(buffer, 0, buffer.Length);
					}
					string blobId = Convert.ToBase64String(Encoding.UTF8.GetBytes(Guid.NewGuid().ToString()));
					helper.PutBlobAsync("test", "7", buffer, blobId).GetAwaiter().GetResult(); //put blob
					Console.WriteLine("Put blob success!");
					blobIdList.Add(blobId);
					File.AppendAllLines(flagFile, new List<string>() { blobId });
				}
				helper.PutBlobListAsync("test", "7", blobIdList, contentHash).GetAwaiter().GetResult();
			}
			catch (Exception e) {
				Console.WriteLine(e.Message);
			}

			Console.WriteLine("Press any key to exit the sample application.");
			Console.ReadLine();
		}

		internal static Func<byte[], string> md5()
		{
			var hashFunction = MD5.Create();

			return (content) => Convert.ToBase64String(hashFunction.ComputeHash(content));
		}
	}
}