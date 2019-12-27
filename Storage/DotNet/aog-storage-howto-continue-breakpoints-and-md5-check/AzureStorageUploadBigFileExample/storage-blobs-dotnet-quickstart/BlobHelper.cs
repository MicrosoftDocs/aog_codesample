using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Globalization;
using System.IO;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Web;

namespace AzureStorageUploadBigFileExample
{
	public class BlobHelper
	{
		public BlobHelper(string blobType, string blobEndPoint)
		{
			BlobType = blobType;
			BlobEndPoint = blobEndPoint;
		}

		public string BlobType { get; set; }
		public string BlobEndPoint { get; set; }

		private string CreateAuthorizationHeader(string canonicalizedstring)
		{
			string signature = string.Empty;
			using (
				System.Security.Cryptography.HMACSHA256 hmacSha256 =
					new System.Security.Cryptography.HMACSHA256(Convert.FromBase64String(AzureConstants.SecretKey))) {
				Byte[] dataToHmac = System.Text.Encoding.UTF8.GetBytes(canonicalizedstring);
				signature = Convert.ToBase64String(hmacSha256.ComputeHash(dataToHmac));
			}

			string authorizationHeader = string.Format(CultureInfo.InvariantCulture, "{0} {1}:{2}",
													   AzureConstants.SharedKeyAuthorizationScheme,
													   AzureConstants.Account, signature);

			return authorizationHeader;
		}

		/// <summary>
		/// 生成授权标识方法
		/// </summary>
		/// <param name="method"></param>
		/// <param name="now"></param>
		/// <param name="request"></param>
		/// <param name="ifMatch"></param>
		/// <param name="md5"></param>
		/// <returns></returns>
		public string AuthorizationHeader(string method, DateTime now, HttpWebRequest request, string ifMatch = "", string md5 = "")
		{
			string MessageSignature;

			MessageSignature = String.Format("{0}\n\n\n{1}\n{5}\n\n\n\n{2}\n\n\n\n{3}{4}",
				method,
				(method == "GET" || method == "HEAD") ? String.Empty : request.ContentLength.ToString(),
				ifMatch,
				GetCanonicalizedHeaders(request),
				GetCanonicalizedResource(request.RequestUri, AzureConstants.Account),
				md5
				);

			byte[] SignatureBytes = System.Text.Encoding.UTF8.GetBytes(MessageSignature);
			System.Security.Cryptography.HMACSHA256 SHA256 = new System.Security.Cryptography.HMACSHA256(Convert.FromBase64String(AzureConstants.SecretKey));
			String AuthorizationHeader = "SharedKey " + AzureConstants.Account + ":" + Convert.ToBase64String(SHA256.ComputeHash(SignatureBytes));
			return AuthorizationHeader;
		}

		public string GetCanonicalizedResource(Uri address, string accountName)
		{
			StringBuilder str = new StringBuilder();
			StringBuilder builder = new StringBuilder("/");
			builder.Append(accountName);
			builder.Append(address.AbsolutePath);
			str.Append(builder.ToString());
			NameValueCollection values2 = new NameValueCollection();
			NameValueCollection values = HttpUtility.ParseQueryString(address.Query);
			foreach (string str2 in values.Keys) {
				ArrayList list = new ArrayList(values.GetValues(str2));
				list.Sort();
				StringBuilder builder2 = new StringBuilder();
				foreach (object obj2 in list) {
					if (builder2.Length > 0) {
						builder2.Append(",");
					}
					builder2.Append(obj2.ToString());
				}
				values2.Add((str2 == null) ? str2 : str2.ToLowerInvariant(), builder2.ToString());
			}
			ArrayList list2 = new ArrayList(values2.AllKeys);
			list2.Sort();
			foreach (string str3 in list2) {
				StringBuilder builder3 = new StringBuilder(string.Empty);
				builder3.Append(str3);
				builder3.Append(":");
				builder3.Append(values2[str3]);
				str.Append("\n");
				str.Append(builder3.ToString());
			}
			return str.ToString();
		}

		public string GetCanonicalizedHeaders(HttpWebRequest request)
		{
			ArrayList headerNameList = new ArrayList();
			StringBuilder sb = new StringBuilder();
			foreach (string headerName in request.Headers.Keys) {
				if (headerName.ToLowerInvariant().StartsWith("x-ms-", StringComparison.Ordinal)) {
					headerNameList.Add(headerName.ToLowerInvariant());
				}
			}
			headerNameList.Sort();
			foreach (string headerName in headerNameList) {
				StringBuilder builder = new StringBuilder(headerName);
				string separator = ":";
				foreach (string headerValue in GetHeaderValues(request.Headers, headerName)) {
					string trimmedValue = headerValue.Replace("\r\n", String.Empty);
					builder.Append(separator);
					builder.Append(trimmedValue);
					separator = ",";
				}
				sb.Append(builder.ToString());
				sb.Append("\n");
			}
			return sb.ToString();
		}

		public ArrayList GetHeaderValues(NameValueCollection headers, string headerName)
		{
			ArrayList list = new ArrayList();
			string[] values = headers.GetValues(headerName);
			if (values != null) {
				foreach (string str in values) {
					list.Add(str.TrimStart(null));
				}
			}
			return list;
		}

		public async Task PutBlobAsync(String containerName, String blobName, byte[] blobContent, String blobid, bool error = false)
		{
			String requestMethod = "PUT";
			String urlPath = String.Format("{0}/{1}", containerName, blobName) + "?comp=block&blockid=" + blobid;
			String storageServiceVersion = "2015-02-21";
			String dateInRfc1123Format = DateTime.UtcNow.ToString("R", CultureInfo.InvariantCulture);

			Int32 blobLength = blobContent.Length;
			//headers
			String canonicalizedHeaders = String.Format(
				"\nx-ms-date:{0}\nx-ms-version:{1}",
				dateInRfc1123Format,
				storageServiceVersion);
			//resources
			String canonicalizedResource = String.Format("/{0}/{1}", AzureConstants.Account, String.Format("{0}/{1}", containerName, blobName) + "\nblockid:" + blobid + "\ncomp:block");
			String stringToSign = String.Format(
			"{0}\n\n\n{1}\n\n\n\n\n\n\n\n{2}\n{3}",
			requestMethod,
			blobLength,
			canonicalizedHeaders,
			canonicalizedResource);
			string authorizationHeader = CreateAuthorizationHeader(stringToSign);
			//上传url
			Uri uri = new Uri(BlobEndPoint + urlPath);
			HttpWebRequest request = (HttpWebRequest)WebRequest.Create(uri);
			request.Method = requestMethod;
			request.Headers["x-ms-date"] = dateInRfc1123Format;
			request.Headers["x-ms-version"] = storageServiceVersion;
			request.Headers["Authorization"] = authorizationHeader;
			request.ContentLength = blobLength;

			try {
				using (Stream requestStream = await request.GetRequestStreamAsync()) {
					requestStream.Write(blobContent, 0, blobLength);
				}

				using (HttpWebResponse response = (HttpWebResponse)await request.GetResponseAsync()) {
					String ETag = response.Headers["ETag"];
					System.Console.WriteLine(ETag);
				}
				error = false;
			}
			catch (WebException ex) {
				System.Console.WriteLine("An error occured. Status code:" + ((HttpWebResponse)ex.Response).StatusCode);
				System.Console.WriteLine("Error information:");
				error = true;
				using (Stream stream = ex.Response.GetResponseStream()) {
					using (StreamReader sr = new StreamReader(stream)) {
						var s = sr.ReadToEnd();
						System.Console.WriteLine(s);
					}
				}
			}
		}

		public async Task PutBlobListAsync(String containerName, String blobName, List<string> blobIdList, string md5, bool error = false)
		{
			String requestMethod = "PUT";
			String urlPath = String.Format("{0}/{1}", containerName, blobName) + "?comp=blocklist";
			String storageServiceVersion = "2015-02-21";
			String dateInRfc1123Format = DateTime.UtcNow.ToString("R", CultureInfo.InvariantCulture);

			String canonicalizedHeaders = String.Format(
				"\nx-ms-blob-content-md5:{0}\nx-ms-date:{1}\nx-ms-version:{2}",
				md5,
				dateInRfc1123Format,
				storageServiceVersion);
			StringBuilder stringbuilder = new StringBuilder();
			stringbuilder.Append("<BlockList>");
			foreach (string item in blobIdList) {
				stringbuilder.Append(" <Latest>" + item + "</Latest>");
			}
			stringbuilder.Append("</BlockList>");

			byte[] data = Encoding.UTF8.GetBytes(stringbuilder.ToString());

			Int32 blobLength = data.Length;
			String canonicalizedResource = String.Format("/{0}/{1}", AzureConstants.Account, String.Format("{0}/{1}", containerName, blobName) + "\ncomp:blocklist");
			String stringToSign = String.Format(
				"{0}\n\n\n{1}\n\n\n\n\n\n\n\n{2}\n{3}",
				requestMethod,
				blobLength,
				canonicalizedHeaders,
				canonicalizedResource);
			String authorizationHeader = CreateAuthorizationHeader(stringToSign);

			Uri uri = new Uri(BlobEndPoint + urlPath);
			HttpWebRequest request = (HttpWebRequest)WebRequest.Create(uri);
			request.Method = requestMethod;
			request.Headers["x-ms-blob-content-md5"] = md5;
			request.Headers["x-ms-date"] = dateInRfc1123Format;
			request.Headers["x-ms-version"] = storageServiceVersion;
			request.Headers["Authorization"] = authorizationHeader;
			request.ContentLength = blobLength;
			try {
				using (Stream requestStream = await request.GetRequestStreamAsync()) {
					requestStream.Write(data, 0, blobLength);
				}

				using (HttpWebResponse response = (HttpWebResponse)await request.GetResponseAsync()) {
					String ETag = response.Headers["ETag"];
					System.Console.WriteLine(ETag);
				}
				error = false;
			}
			catch (WebException ex) {
				System.Console.WriteLine("An error occured. Status code:" + ((HttpWebResponse)ex.Response).StatusCode);
				System.Console.WriteLine("Error information:");
				error = true;
				using (Stream stream = ex.Response.GetResponseStream()) {
					using (StreamReader sr = new StreamReader(stream)) {
						var s = sr.ReadToEnd();
						System.Console.WriteLine(s);
					}
				}
			}
		}
	}
}