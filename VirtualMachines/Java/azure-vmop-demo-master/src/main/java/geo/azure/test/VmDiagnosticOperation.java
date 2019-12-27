package geo.azure.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

public class VmDiagnosticOperation {

	private String tentantId;
	private String clientId;
	private String clientSecret;
	private String subscriptionId;

	public VmDiagnosticOperation(String tentantId, String clientId, String clientSecret, String subscriptionId) {
		this.tentantId = tentantId;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.subscriptionId = subscriptionId;
	}

	private String putRequest(String accessToken, String requestUrl, String requestBody) {
		HttpClient httpclient = HttpClients.createDefault();

		try {
			URIBuilder builder = new URIBuilder(requestUrl);

			URI uri = builder.build();
			HttpPut request = new HttpPut(uri);
			request.setHeader("Authorization", "Bearer " + accessToken);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Host", "management.chinacloudapi.cn");

			StringEntity reqEntity = new StringEntity(requestBody.toString());
			request.setEntity(reqEntity);
			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				return EntityUtils.toString(entity);
			}

			return "No Result!";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String getAccessToken() {

		AuthenticationContext context = null;
		AuthenticationResult result = null;
		ExecutorService service = null;
		service = Executors.newFixedThreadPool(1);

		try {
			context = new AuthenticationContext(String.format("%s/%s", "https://login.chinacloudapi.cn", tentantId),
					true, service);
			ClientCredential cred = new ClientCredential(clientId, clientSecret);
			Future<AuthenticationResult> future = context.acquireToken("https://management.chinacloudapi.cn/", cred,
					null);

			result = future.get();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			service.shutdown();
		}

		return result.getAccessToken();
	}

	private String generatorWadCfg(String subscriptionId, String resourceName, String vmName) {

		String content = null;
		try {
			content = Files.toString(
					new File("E:\\maven-workspace\\azure-demo-beta\\src\\main\\java\\geo\\azure\\test\\WadCfg.xml"),
					Charsets.UTF_8);

			content = content.replace("{0}", subscriptionId).replace("{1}", resourceName).replace("{2}", vmName);
			System.out.println(content);

			final Base64.Encoder encoder = Base64.getEncoder();
			final byte[] textByte = content.getBytes("UTF-8");
			content = encoder.encodeToString(textByte);
			System.out.println(content);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}

	private String generatorBodyCfg(String subscriptionId, String resourceName, String vmName, String vmLocation,
			String xmlCfg, String storageAccountName, String storageAccountKey) {
		StringBuilder requestBody = new StringBuilder();

		requestBody.append("{'type':'Microsoft.Compute/virtualMachines/extensions',");
		requestBody.append("'id':'/subscriptions/" + subscriptionId + "/resourceGroups/" + resourceName + "/providers/Microsoft.Compute/virtualMachines/" + vmName + "/extensions/IaaSDiagnostics',");
		requestBody.append("'location':'" + vmLocation + "',");
		requestBody.append("'name':'IaaSDiagnostics',");
		requestBody.append("'properties':{'type':'IaaSDiagnostics','publisher':'Microsoft.Azure.Diagnostics','typeHandlerVersion':'1.5',");
		requestBody.append("'settings':{'StorageAccount':'" + storageAccountName + "','xmlCfg':'" + xmlCfg + "'},");
		requestBody.append("'protectedSettings':{'storageAccountName':'" + storageAccountName + "','storageAccountKey':'" + storageAccountKey + "','storageAccountEndPoint':'https://core.chinacloudapi.cn'},");
		requestBody.append("'autoUpgradeMinorVersion':true,'instanceView':null}}");
		System.out.println(requestBody.toString());
		return requestBody.toString();
	}

	public String EnableVMDiagnostic(String resourceName, String vmName, String vmLocation, String storageAccountName,
			String storageAccountKey) {

		String token = this.getAccessToken();
		String xmlCfg = this.generatorWadCfg(subscriptionId, resourceName, vmName);
		String requestUrl = "https://management.chinacloudapi.cn/subscriptions/" + subscriptionId + "/resourceGroups/"+ resourceName + "/providers/Microsoft.Compute/virtualMachines/" + vmName + "/extensions/IaaSDiagnostics?api-version=2016-03-30";
		String requestBody = this.generatorBodyCfg(subscriptionId, resourceName, vmName, vmLocation, xmlCfg,storageAccountName, storageAccountKey);
		String result = this.putRequest(token, requestUrl, requestBody);
		return result;
	}

}
