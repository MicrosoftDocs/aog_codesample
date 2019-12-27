package com.vianet.azure.sdk.manage.storage;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.microsoft.windowsazure.core.utils.Constants;
import com.vianet.azure.sdk.manage.Configure;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by chen.rui on 4/1/2016.
 */
public class TestVHDCapacity {

    static long RANGE = 10737418240L;

    public static void main(String[] args) throws Exception {
        Configure configure = new Configure();
        CloudBlobClient client = AzureStorageServiceFactory.getInstance(configure).createBlobService().getClient();
        CloudBlobContainer container = client.getContainerReference("vhds");
        Iterable<ListBlobItem> items = container.listBlobs();
        final Long[] blobSizeInBytes = {0L};
        items.forEach(new Consumer<ListBlobItem>() {
            @Override
            public void accept(ListBlobItem item) {
                CloudBlob blob = (CloudBlob) item;
                try {
                    if(blob.getProperties().getBlobType().equals(BlobType.BLOCK_BLOB)) {
                        blobSizeInBytes[0] += blob.getProperties().getLength();
                    } else if(blob.getProperties().getBlobType().equals(BlobType.PAGE_BLOB)) {
                        CloudPageBlob page = container.getPageBlobReference(blob.getName());
                        System.out.println("Name: " + blob.getName());
                        System.out.println("Length: " + blob.getProperties().getLength()/1024/1024/1024 + "GB");
                        Long pageSizeInBytes = countPageSize(page, blob.getProperties().getLength(), 0L, RANGE);
                        blobSizeInBytes[0] += pageSizeInBytes;
                        System.out.println("Capacity: " + pageSizeInBytes/1024/1024/1024 + "GB");
                        System.out.println("===========================================");
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (StorageException e) {
                    e.printStackTrace();
                }

            }
        });

        System.out.println(blobSizeInBytes[0]);
    }

    public static Long countPageSize(CloudPageBlob page, Long size, Long start, Long range) throws StorageException {
        long loop = (size / range) + 1;
        Long pageSizeInBytes = 0L;
        for(long i = 0; i < loop; i++) {
            try {
                if(i == (loop - 1)) {
                    pageSizeInBytes += getPageRangeSize(page, start, size);
                } else {
                    pageSizeInBytes += getPageRangeSize(page, start, range);
                }
            } catch (StorageException ex) {
                ex.printStackTrace();
                pageSizeInBytes += 0L;
            }
            start += range;
        }
        return pageSizeInBytes;
    }


    public static Long getPageRangeSize(CloudPageBlob page, Long start,  Long range) throws StorageException {
        OperationContext context = new OperationContext();
        BigInteger integer = new BigInteger("0").add(new BigInteger("10")).multiply(new BigInteger("1024")).multiply(new BigInteger("1024")).multiply(new BigInteger("1024")).subtract(new BigInteger("1"));
        String rangeHeader = String.format(
                "bytes=%s-%s",
                start,
                start + range);
        System.out.println("RangeHeader : " + rangeHeader);
        HashMap<String, String> useHeader = new HashMap<String, String>();
        useHeader.put(Constants.HeaderConstants.STORAGE_RANGE_HEADER, rangeHeader);
        context.setUserHeaders(useHeader);

        ArrayList<PageRange> pageRanges = page.downloadPageRanges(null, null, context);
        Long pageSizeInBytes = 0L;
        for(PageRange pageRange : pageRanges) {
            pageSizeInBytes += pageRange.getEndOffset() - pageRange.getStartOffset() + 12;
        }
        return  pageSizeInBytes;
    }

}
