package com.vianet.azure.sdk.manage.storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.analytics.CloudAnalyticsClient;
import com.microsoft.azure.storage.blob.*;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.function.Consumer;

/**
 * Created by chen.rui on 5/23/2016.
 */
public class TestPageStorage {

    public static void main(String[] args) throws Exception {
        StorageCredentials credentials = new StorageCredentialsAccountAndKey("kevinstorage1", "oLbZhKoC8HaMUwjGWInitvAJG8rkh4Gmmnpo8JIUc0Vpk/8D8lxT8DpE9A0WVvXtDvjwdhR2AHkj5x5LvHturw==");
        CloudStorageAccount account = new CloudStorageAccount(credentials, true, "core.chinacloudapi.cn");
        CloudBlobClient client = account.createCloudBlobClient();

        CloudBlobContainer blobContainer = client.getContainerReference("kevincache-redis-persistence");
        CloudBlobDirectory directory = blobContainer.getDirectoryReference("kevincache-0-1-rdb");
        CloudPageBlob pageBlob = directory.getPageBlobReference("1:e4f57b7a-7725-4908-b0ff-fa6974ba717a:backup:0");

        final long start = pageBlob.downloadPageRanges().get(0).getStartOffset();
        Long end = pageBlob.downloadPageRanges().get(0).getEndOffset() ;


//        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\chen.rui\\dump.rdb1"));
        byte[] bytes = new byte[20000];
        pageBlob.downloadToByteArray(bytes, 1000);

        for(byte b : bytes) {
            System.out.println(b);
        }
    }

}
