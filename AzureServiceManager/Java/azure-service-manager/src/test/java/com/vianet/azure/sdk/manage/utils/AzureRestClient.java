package com.vianet.azure.sdk.manage.utils;

import com.google.gson.JsonObject;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.vianet.azure.sdk.manage.AbstactTest;
import net.minidev.json.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;

import javax.naming.ServiceUnavailableException;
import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Azure Rest Client
 */
public class AzureRestClient {

    private static String genkeyStore(String subId, String publishSettingFile) throws IOException {
        File publishSettingsFile = new File(publishSettingFile);
        String outputKeyStore = System.getProperty("user.home") + File.separator + ".azure" + File.separator + AbstactTest.SUB_ID + ".out";
        PublishSettingLoader.createCertficateFromPublishSettingsFile(publishSettingsFile, subId, outputKeyStore);
        return outputKeyStore;
    }

    public static String processGetRequest(URL url, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        return processGetRequest(url, requestProperties, AbstactTest.SUB_ID, AbstactTest.publishsetting);
    }

    public static String processGetRequest(URL url, Map<String, String> requestProperties, String subId, String publishSettingFile) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        HttpsURLConnection con = sendRequest("GET", url, null, requestProperties, subId, publishSettingFile);
        String responseMessage = getResponse(con);
        return responseMessage;
    }

    public static String getResponse(HttpsURLConnection connection) throws IOException {
        System.out.println("Response Code : " + connection.getResponseCode());
    	connection.getResponseCode();
    	InputStream stream = connection.getErrorStream();
        if (stream == null) {
            stream = connection.getInputStream();
        }
        String response = null;
        // This is a try with resources, Java 7+ only
        // If you use Java 6 or less, use a finally block instead
        try (Scanner scanner = new Scanner(stream)) {
            scanner.useDelimiter("\\Z");
            response = scanner.next();
        }
        if(response.startsWith("{")) {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(response, Object.class);

            String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            return indented;
        }
        return response;
    }

    public static int processPostRequest(URL url, byte[] data, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        return processPostRequest(url, data, requestProperties, AbstactTest.SUB_ID, AbstactTest.publishsetting);
    }

    public static int processPostRequest(URL url, byte[] data, Map<String, String> requestProperties, String subId, String publishSettingFile) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        HttpsURLConnection con = sendRequest("POST", url, data, requestProperties, subId, publishSettingFile);
        int code = con.getResponseCode();
        System.out.println(getResponse(con));
        return code;
    }

    public static int processPutRequest(URL url, byte[] data, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        return processPutRequest(url, data, requestProperties, AbstactTest.SUB_ID, AbstactTest.publishsetting);
    }

    public static int processPutRequest(URL url, byte[] data, Map<String, String> requestProperties, String subId, String publishSettingFile) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        HttpsURLConnection con = sendRequest("PUT", url, data, requestProperties, subId, publishSettingFile);
        int code = con.getResponseCode();
        System.out.println(getResponse(con));
        return code;
    }

    public static int processDeleteRequest(URL url, byte[] data, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        return processDeleteRequest(url, data, requestProperties, AbstactTest.SUB_ID, AbstactTest.publishsetting);
    }

    public static int processDeleteRequest(URL url, byte[] data, Map<String, String> requestProperties, String subId, String publishSettingFile) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        HttpsURLConnection con = sendRequest("DELETE", url, data, requestProperties, subId, publishSettingFile);
        int code = con.getResponseCode();
        System.out.println(getResponse(con));
        return code;
    }
    
    private static HttpsURLConnection sendRequest(String RequestMethod, URL url, byte[] data, Map<String, String> requestProperties, String subId, String publishSettingFile) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException  {
        SSLSocketFactory sslFactory = getSSLSocketFactory(subId, publishSettingFile);
        HttpsURLConnection con = null;
        con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(sslFactory);
        con.setDoOutput(true);
        con.setRequestMethod(RequestMethod);
        if(requestProperties != null) {
            for(String key : requestProperties.keySet()) {
                con.addRequestProperty(key, requestProperties.get(key));
            }
        }
        if(data != null) {
            DataOutputStream requestStream = new DataOutputStream(con.getOutputStream());
            requestStream.write(data);
            requestStream.flush();
            requestStream.close();
        }
        return con;
    }

    public static SSLSocketFactory getSSLSocketFactory(String subId, String publishSettingFile)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
            IOException {
        String keyStoreName = genkeyStore(subId, publishSettingFile);
        KeyStore ks = getKeyStore(keyStoreName, "");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(ks, "".toCharArray());
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }
        }};
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new SecureRandom());
        return context.getSocketFactory();
    }

    private static KeyStore getKeyStore(String keyStoreName, String password) throws IOException {
        KeyStore ks = null;
        FileInputStream fis = null;
        try {
            ks = KeyStore.getInstance("JKS");
            char[] passwordArray = password.toCharArray();
            fis = new java.io.FileInputStream(keyStoreName);
            ks.load(fis, passwordArray);
            fis.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return ks;
    }

    public static AuthenticationResult getAccessTokenFromUserCredentials(String tenlentId, String clientId, String username, String password, String managerEndpoint)
            throws Exception {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext("https://login.chinacloudapi.cn/" + tenlentId,  false, service);

            Future<AuthenticationResult> future = context.acquireToken(
                    managerEndpoint,
                    clientId,
                    username,
                    password,
                    null);
            result = future.get();
        } finally {
            // service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }
        System.out.println("Access Token - " + result.getAccessToken());
        System.out.println("Refresh Token - " + result.getRefreshToken());
        System.out.println("ID Token - " + result.getAccessToken());
        return result;
    }

    public static String getAccessTokenFromClientCredentials(String tenlentId, String clientId, String clientSecret, String managerEndpoint)
            throws Exception {
        URL url = new URL(String.format("https://login.chinacloudapi.cn/%s/oauth2/token?api-version=1.0", tenlentId));

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());
        byte[] data = String.format("grant_type=client_credentials&resource=%s&client_id=%s&client_secret=%s", managerEndpoint, clientId, URLEncoder.encode( clientSecret, "UTF-8")).getBytes();
        if(data != null) {
            requestStream.write(data);
        }
        requestStream.flush();
        requestStream.close();

        InputStream stream = conn.getErrorStream();
        if (stream == null) {
            stream = conn.getInputStream();
        }
        String response = null;

        try (Scanner scanner = new Scanner(stream)) {
            scanner.useDelimiter("\\Z");
            response = scanner.next();
        }
        ObjectMapper mapper = new ObjectMapper();
        JSONObject object = mapper.readValue(response, JSONObject.class);
        if(object.get("access_token") != null) {
            return object.get("access_token").toString();
        }
        return null;
    }
}
