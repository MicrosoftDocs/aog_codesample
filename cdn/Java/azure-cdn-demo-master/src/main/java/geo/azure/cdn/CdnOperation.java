package geo.azure.cdn;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class CdnOperation {
	
	
	public String getRequest(String requestUrl, String authorization ,String requestTime) {
		HttpClient httpclient = HttpClients.createDefault();

		try {
			URIBuilder builder = new URIBuilder(requestUrl);

			URI uri = builder.build();
			HttpGet request = new HttpGet(uri);
			request.setHeader("Authorization", authorization);
			request.setHeader("x-azurecdn-request-date", requestTime);

		
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
	
	public String postRequest(String requestUrl, String authorization ,String requestBody,String requestTime) {
		HttpClient httpclient = HttpClients.createDefault();

		try {
			URIBuilder builder = new URIBuilder(requestUrl);

			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);
			request.setHeader("Authorization", authorization);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("x-azurecdn-request-date", requestTime);

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
	

	public String putRequest(String requestUrl, String authorization ,String requestBody,String requestTime) {
		HttpClient httpclient = HttpClients.createDefault();

		try {
			URIBuilder builder = new URIBuilder(requestUrl);

			URI uri = builder.build();
			HttpPut request = new HttpPut(uri);
			request.setHeader("Authorization", authorization);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("x-azurecdn-request-date", requestTime);

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
	

	public  String calculateAuthorizationHeader(String requestURL, String requestTimestamp, String keyID, String keyValue, String httpMethod) throws Exception {
        URL url = new URL(requestURL);
        String path = url.getPath();

        // Get query parameters
        String query = url.getQuery();      
        String[] params = query.split("&");
        Map<String, String> paramMap = new TreeMap<String, String>();
        for(String param: params) {
              String[] paramterParts = param.split("=");
              if(paramterParts.length != 2) {
                    continue;
              }

              paramMap.put(paramterParts[0], paramterParts[1]);
        }

        String orderedQueries = paramMap.entrySet()
                                        .stream()
                                        .map(entry -> entry.getKey() + ":" + entry.getValue())
                                        .collect(Collectors.joining(", "));

        String content = String.format("%s\r\n%s\r\n%s\r\n%s", path, orderedQueries, requestTimestamp, httpMethod);        
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(keyValue.getBytes(), "HmacSHA256");
        sha256HMAC.init(secret_key);
        byte[] bytes = sha256HMAC.doFinal(content.getBytes());

        StringBuffer hash = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
              hash.append('0');
            }
            hash.append(hex);
        }

        return String.format("AzureCDN %s:%s", keyID, hash.toString().toUpperCase());
}

	
}
