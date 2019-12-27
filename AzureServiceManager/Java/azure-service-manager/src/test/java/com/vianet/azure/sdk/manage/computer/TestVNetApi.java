package com.vianet.azure.sdk.manage.computer;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.network.NetworkManagementClient;
import com.microsoft.windowsazure.management.network.NetworkManagementService;
import com.microsoft.windowsazure.management.network.models.*;

/**
 * Created by chen.rui on 6/3/2016.
 */
public class TestVNetApi {

    Configuration config;

    NetworkManagementClient networkManagementClient;

    public TestVNetApi(Configuration config) {
        this.config = config;
        this.networkManagementClient = NetworkManagementService.create(config);
    }

    public static void main(String[] args) {
        try {
            Configuration config = PublishSettingsLoader.createManagementConfiguration("D:\\Users\\chen.rui\\Documents\\china.publishsettings", "<your sub id>");
            TestVNetApi app = new TestVNetApi(config);
            app.getVNetResource();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getVNetResource() throws Exception {
//        networkManagementClient.getRoutesOperations().getRouteTableForSubnet("kevinet1", "Subnet-1")
        GetRouteTableForSubnetResponse response=  networkManagementClient.getRoutesOperations().getRouteTableForSubnet("kevinet1", "Subnet-1");
        response.getRouteTableName();
    }



}
