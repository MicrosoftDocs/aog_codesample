package com.vianet.azure.sdk.manage.storage;


import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.management.storage.models.StorageAccount;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.vianet.azure.sdk.manage.AbstactTest;
import org.junit.*;

import java.util.function.Consumer;

public class TestStorageManage extends AbstactTest {

    StorageManagementClient storageManagementClient;

    @Before
    public void setUp() {
        storageManagementClient = StorageManagementService.create(getConfig());
    }

    @Test
    public void testGetStroageAccount() throws Exception {
        storageManagementClient.getStorageAccountsOperations().list().forEach(new Consumer<StorageAccount>() {
            @Override
            public void accept(StorageAccount storageAccount) {
                System.out.println("==========================================");
                System.out.println("Name : " + storageAccount.getName());
                System.out.println("Location : " + storageAccount.getProperties().getLocation());
                System.out.println("AffinityGroup : " + storageAccount.getProperties().getAffinityGroup());
            }
        });
    }

    @Test
    public void testCreateStroageAccount() throws Exception {
        StorageAccountCreateParameters createParameters = new StorageAccountCreateParameters();
        createParameters.setName("kevinstorage3");
        createParameters.setLabel("1212123123");
        createParameters.setLocation("China North");
        createParameters.setAccountType("Standard_LRS");

        //act
        storageManagementClient.getStorageAccountsOperations().create(createParameters);
    }


}
