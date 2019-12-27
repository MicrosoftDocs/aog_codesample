package com.vianet.azure.sdk.manage.storage;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.vianet.azure.sdk.manage.Configure;

import java.io.File;

/**
 * Created by chen.rui on 4/8/2016.
 */
public class TestCloudAppendBlob {

    public static void main(String[] args) throws Exception {
        Configure configure = new Configure();
        final CloudBlobContainer container = AzureStorageServiceFactory.getInstance(configure).createBlobService().getContainerReference("test");

        CloudAppendBlob appendBlob = container.getAppendBlobReference("appendblob1.tmp");
        appendBlob.createOrReplace();

        try {
            appendBlob.createOrReplace();
            for(int i = 0; i < 50001; i++) {
                appendBlob.appendText("testtesttesttest");
                System.out.println(i);
            }
        } catch (StorageException s) {
            appendBlob = null;
            s.printStackTrace();
        }

//        // Download the blob
//        if (appendBlob != null) {
//            System.out.println("\nDownload the blob.");
//            String downloadedAppendBlobPath = String.format("%scopyof-%s", System.getProperty("java.io.tmpdir"), appendBlob.getName());
//            System.out.println(String.format("\tDownload the blob from \"%s\" to \"%s\".", appendBlob.getUri().toURL(), downloadedAppendBlobPath));
//            appendBlob.downloadToFile(downloadedAppendBlobPath);
//            new File(downloadedAppendBlobPath).deleteOnExit();
//            System.out.println("\tSuccessfully downloaded the blob.");
//        }
//
//
//        //remove this bolb
//        System.out.println("\nRemove the blob.");
//        appendBlob.delete();
//        System.out.println("\tSuccessfully removed the blob.");
    }


}
