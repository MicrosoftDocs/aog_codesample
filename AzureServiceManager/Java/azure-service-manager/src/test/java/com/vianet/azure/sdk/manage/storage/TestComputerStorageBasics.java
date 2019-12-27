package com.vianet.azure.sdk.manage.storage;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.analytics.CloudAnalyticsClient;
import com.microsoft.azure.storage.analytics.StorageService;
import com.microsoft.azure.storage.table.*;
import com.vianet.azure.sdk.manage.Configure;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;


public class TestComputerStorageBasics {


    public static void main(String[] args) throws Exception {

        Configure configure = new Configure();
        CloudAnalyticsClient client = AzureStorageServiceFactory.getInstance(configure).createAnalyticsService().getClient();

        CloudTable table = client.getCapacityTable();
        System.out.println(table.getStorageUri());
        CloudTable minuteMetricsTable =client.getMinuteMetricsTable(StorageService.BLOB);

        Iterable<MetricsCapacityBlobEntity> capacityBlobEntities = table.execute(new TableQuery<MetricsCapacityBlobEntity>(), new EntityResolver<MetricsCapacityBlobEntity>() {
            @Override
            public MetricsCapacityBlobEntity resolve(String partitionKey, String rowKey, Date timeStamp, HashMap<String, EntityProperty> properties, String etag) throws StorageException {
                MetricsCapacityBlobEntity capacityBlobEntity = new MetricsCapacityBlobEntity();
                capacityBlobEntity.setPartitionKey(partitionKey);
                capacityBlobEntity.setRowKey(rowKey);
                capacityBlobEntity.setTimestamp(timeStamp);
                capacityBlobEntity.setEtag(etag);
                capacityBlobEntity.setContainerCount(properties.get("ContainerCount").getValueAsLong());
                capacityBlobEntity.setCapacity(properties.get("Capacity").getValueAsLong());
                capacityBlobEntity.setObjectCount(properties.get("ObjectCount").getValueAsLong());
                return capacityBlobEntity;
            }

        });

        capacityBlobEntities.forEach(new Consumer<MetricsCapacityBlobEntity>() {
            @Override
            public void accept(MetricsCapacityBlobEntity metricsCapacityBlobEntity) {
                System.out.println("==============================================================");
                System.out.println("partitionKey : " + metricsCapacityBlobEntity.getPartitionKey());
                System.out.println("rowKey : " + metricsCapacityBlobEntity.getRowKey());
                System.out.println("timeStamp : " + metricsCapacityBlobEntity.getTimestamp());
                System.out.println("etag : " + metricsCapacityBlobEntity.getEtag());
                System.out.println("ContainerCount : " + metricsCapacityBlobEntity.getContainerCount());
                float capacity = (float) metricsCapacityBlobEntity.getCapacity() / 1024 /1024;
                DecimalFormat df = new DecimalFormat("0.00");
                System.out.println("Capacity : " + df.format(capacity) + "M");

                System.out.println("ObjectCount : " + metricsCapacityBlobEntity.getObjectCount());
            }
        });

//        Iterable<LogRecord> logs = client.listLogRecords(StorageService.BLOB);
//        System.out.println("RequestedObjectKey \t\t Requeted Time");
//        for (LogRecord log : logs) {
//            System.out.println(log.getRequestedObjectKey() + "\t\t" + log.getRequestStartTime());
//        }
    }


}

