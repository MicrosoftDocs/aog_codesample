package com.vianet.azure.sdk.manage.database;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.naming.ServiceUnavailableException;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UpgradeDataBase {

    private final static String AUTHORITY = "https://login.chinacloudapi.cn/common";                //DON'T　CHANGE
    private final static String CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2";                 //DON'T　CHANGE
    private final static String MANAGEMENT_EBDPOINT = "https://management.core.chinacloudapi.cn/";  //DON'T　CHANGE

    private final static String SUB_ID = "<your sub id>";                    //USING YOUR SUB ID
    private final static String SQL_SERVER = "k5y0zqmnry";                                          //要升级的DB所在的Server
    private final static String SQL_DB = "test";                                                    //要升级的DB
    private final static String DB_LEVEL = "S2";                                                    //要升级的DB级别

    private final static String USERNAME = "cietest03@microsoftinternal.partner.onmschina.cn";      //USING YOUR ACCOUNT
    private final static String PASSWORD = "AzureCIE@M55";                                          //USING YOUR PASSWORD


    public static void main(String[] args) throws Exception {
        String accessToken = getAccessTokenFromUserCredentials(USERNAME, PASSWORD).getAccessToken();
        String serviceObjectiveId = getServiceObjectiveId(accessToken);

        System.out.println(UpgradeDataBase(accessToken, serviceObjectiveId));
        System.exit(0);
    }

    private static String getServiceObjectiveId(String accessToken) throws Exception {
        URL url = new URL(String.format("https://management.core.chinacloudapi.cn/%s/services/sqlservers/servers/%s/serviceobjectives", SUB_ID, SQL_SERVER));
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("x-ms-version", "2012-03-01");
        conn.setRequestProperty("Authorization","Bearer " + accessToken);
        String resStr = getResponseStringFromConn(conn);
        if(resStr.startsWith("<Error")) {
            throw new RuntimeException("调用接口失败!");
        }
        DocumentBuilder e = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = e.parse(new InputSource(new ByteArrayInputStream(resStr.getBytes("utf-8"))));
        document.getDocumentElement().normalize();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression expr = xpath.compile("/ServiceResources/ServiceResource[Name='"+DB_LEVEL+"']/Id");
        Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
        if(node == null) {
            throw new RuntimeException(String.format("Cant found Service Level %s in %s server!", DB_LEVEL, SQL_SERVER));
        }
        return node.getFirstChild().getNodeValue();
    }

    private static String UpgradeDataBase(String accessToken, String objectId) throws Exception {
        URL url = new URL(String.format("https://management.core.chinacloudapi.cn/%s/services/sqlservers/servers/%s/databases/%s", SUB_ID, SQL_SERVER, SQL_DB));

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("x-ms-version", "2012-03-01");
        conn.setRequestProperty("Authorization","Bearer " + accessToken);
        conn.setDoOutput(true);

        String body =
                "<ServiceResource xmlns=\"http://schemas.microsoft.com/windowsazure\">" +
                "   <Name>" + SQL_DB + "</Name>" +
                "   <Edition>Standard</Edition>" +
                "   <ServiceObjectiveId>" +objectId+ "</ServiceObjectiveId>" +
                "</ServiceResource>";
        DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());
        requestStream.write(body.getBytes());
        requestStream.flush();
        requestStream.close();
        String goodRespStr = getResponseStringFromConn(conn);
        return goodRespStr;
    }

    public static String getResponseStringFromConn(HttpURLConnection conn) throws IOException {
        BufferedReader reader = null;
        int code = conn.getResponseCode();
        if(code == 200 || code == 202) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            stringBuffer.append(line);
        }

        return stringBuffer.toString();
    }

    public static AuthenticationResult getAccessTokenFromUserCredentials(String username, String password)
            throws Exception {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(AUTHORITY, false, service);

            Future<AuthenticationResult> future = context.acquireToken(
                    MANAGEMENT_EBDPOINT,
                    CLIENT_ID,
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
}
