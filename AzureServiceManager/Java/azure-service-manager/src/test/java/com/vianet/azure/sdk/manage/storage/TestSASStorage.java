package com.vianet.azure.sdk.manage.storage;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.vianet.azure.sdk.manage.Configure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.*;


public class TestSASStorage {

    public static void main(String[] args) throws Exception {
        InputStream inputStream = TestSASStorage.downloadFile("https://kevinstorage1.blob.core.chinacloudapi.cn/test/test.ps1", "test");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "/n");
        }
        System.out.println(sb.toString());
        inputStream.close();
        System.exit(0);
    }

    public static InputStream downloadFile(String pathUrl, String path) throws Exception {
        Configure configure = new Configure();
        CloudBlobClient blobClient = AzureStorageServiceFactory.getInstance(configure).createBlobService().getClient();
        URL url = null;
        InputStream is = null;
        String tokenee = "";
        try {
            CloudBlobContainer container = blobClient.getContainerReference(path);
            if (container.exists()) {
                tokenee = "?" + getBlobToken(container);
                System.out.println(pathUrl + tokenee);
                url = new URL(pathUrl + tokenee);
            }
        } catch (MalformedURLException | StorageException | URISyntaxException e) {
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 利用HttpURLConnection对象,我们可以从网络中获取网页数据.
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream(); // 得到网络返回的输入流
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }


    public static String getBlobToken(CloudBlobContainer container) throws Exception {
        String sas = null;
        try {
            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

            calendar.setTime(new Date());
            policy.setSharedAccessStartTime(calendar.getTime());
            calendar.add(Calendar.YEAR, 5);
            policy.setSharedAccessExpiryTime(calendar.getTime());
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.DELETE, SharedAccessBlobPermissions.LIST));
            BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
            containerPermissions.setPublicAccess(BlobContainerPublicAccessType.OFF);
            container.uploadPermissions(containerPermissions);
            sas = container.generateSharedAccessSignature(policy, null);

        } catch (InvalidKeyException e) {
        } catch (StorageException e) {
        }
        return sas;
    }


}
