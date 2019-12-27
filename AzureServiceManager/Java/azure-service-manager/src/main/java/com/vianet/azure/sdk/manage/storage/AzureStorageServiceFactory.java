/*
 * Copyright (c) 2015-2020, Chen Rui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vianet.azure.sdk.manage.storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.analytics.CloudAnalyticsClient;
import com.microsoft.azure.storage.blob.CloudBlobClient;

import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.table.*;
import com.vianet.azure.sdk.manage.Configure;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Azure Storage Connnect Session Factory
 *
 * @author Chen Rui
 */
public class AzureStorageServiceFactory {

    private static AzureStorageServiceFactory serviceFactory = null;

    private Configure conf;

    private CloudStorageAccount account = null;
	
    private CloudBlobClient blobClient = null;

    private CloudTableClient tableClient = null;

    private CloudQueueClient queueClient = null;

    private CloudFileClient fileClient = null;

    private CloudAnalyticsClient analyticsClient = null;


    private AzureStorageServiceFactory(Configure conf) {
        this.conf = conf;
    }

    public synchronized static  AzureStorageServiceFactory getInstance(Configure conf) throws NoSuchAlgorithmException, KeyManagementException {
        if(serviceFactory == null) {
            serviceFactory = new AzureStorageServiceFactory(conf);
        }
        return serviceFactory;
    }

    public StorageBlobService createBlobService() {
        StorageBlobService service = new StorageBlobService(this.getBlobClient());
        return service;
    }

    public StorageQueueService createQueueService() {
        StorageQueueService service = new StorageQueueService(this.getQueueClient());
        return service;
    }

    public StorageTableService createTableService() {
        StorageTableService service = new StorageTableService(this.getTableClient());
        return service;
    }

    public StorageFileService createFileService() {
        StorageFileService service = new StorageFileService(this.getFileClient());
        return service;
    }

    public StorageAnalyticsService createAnalyticsService() {
        StorageAnalyticsService service = new StorageAnalyticsService(this.getAnalyticsClient());
        return service;
    }

	protected synchronized CloudBlobClient getBlobClient() throws AzureStorageException {
        if(blobClient == null) {
			try {
				CloudStorageAccount account = getStorageAccount();
                blobClient = account.createCloudBlobClient();
			} catch (URISyntaxException | IOException e) {
				throw new AzureStorageException("Storage connect exception", e);
			}
        }
        return blobClient;
    }

    protected synchronized CloudQueueClient getQueueClient() {
        if(queueClient == null) {
            try {
                CloudStorageAccount account = getStorageAccount();
                queueClient = account.createCloudQueueClient();

            } catch (URISyntaxException | IOException e) {
                throw new AzureStorageException("Storage connect exception", e);
            }
        }
        return queueClient;
    }

    protected synchronized CloudTableClient getTableClient() {
        if(tableClient == null) {
            try {
                CloudStorageAccount account = getStorageAccount();
                tableClient = account.createCloudTableClient();

            } catch (URISyntaxException | IOException e) {
                throw new AzureStorageException("Storage connect exception", e);
            }
        }
        return tableClient;
    }


    protected CloudFileClient getFileClient() {
        if(fileClient == null) {
            try {
                CloudStorageAccount account = getStorageAccount();
                fileClient = account.createCloudFileClient();

            } catch (URISyntaxException | IOException e) {
                throw new AzureStorageException("Storage connect exception", e);
            }
        }
        return fileClient;
    }

    protected CloudAnalyticsClient getAnalyticsClient() {
        if(analyticsClient == null) {
            try {
                CloudStorageAccount account = getStorageAccount();
                analyticsClient = account.createCloudAnalyticsClient();

            } catch (URISyntaxException | IOException e) {
                throw new AzureStorageException("Storage connect exception", e);
            }
        }
        return analyticsClient;
    }

	private synchronized CloudStorageAccount getStorageAccount() throws IOException, URISyntaxException {
        if(account == null) {
            String protocol = this.conf.getProperty("azure.storage.protocol");
            String name = this.conf.getProperty("azure.storage.account");
            String accesskey = this.conf.getProperty("azure.storage.accesskey");
            String storageRootUri = this.conf.getProperty("azure.storage.storageRootUri");

            Boolean useHttps = false;
            if("https".equals(protocol)) {
                useHttps = true;
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};

                try {
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    e.printStackTrace();
                }
            }

            StorageCredentials credentials = new StorageCredentialsAccountAndKey(name, accesskey);
            account = new CloudStorageAccount(credentials, useHttps, storageRootUri);
        }
        return account;
    }

}
