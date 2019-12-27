package com.vianet.azure.sdk.manage.computer;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.compute.ComputeManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementService;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse;
import com.microsoft.windowsazure.management.network.NetworkManagementClient;
import com.microsoft.windowsazure.management.network.NetworkManagementService;
import com.vianet.azure.sdk.manage.AbstactTest;
import org.junit.*;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.Assert.*;


/**
 * Hosted Service API Test
 */
public class TestHostedSerivcesAPI extends AbstactTest {

    int random = (int)(Math.random()* 100);
    String hostName = "kevinvm-" + random;

    ComputeManagementClient computeManagementClient;
    NetworkManagementClient networkManagementClient;

    public TestHostedSerivcesAPI() throws IOException {

    }

    @Before
    public void setUp() {
        computeManagementClient = ComputeManagementService.create(getConfig());
        networkManagementClient = NetworkManagementService.create(getConfig());
    }

    @Test
    public void createHostedService() throws Exception {
        HostedServiceCreateParameters createParameters = new HostedServiceCreateParameters();
        //required
        createParameters.setLabel(hostName);
        //required
        createParameters.setServiceName(hostName);
        createParameters.setDescription("kevin test vm");
        //required
        createParameters.setLocation("China North");
        System.out.println("Creating Hosted Service " + hostName);
        OperationResponse op = this.computeManagementClient.getHostedServicesOperations().create(createParameters);
        assertEquals(op.getStatusCode(), 201);
        System.out.println("Created Hosted Service " + hostName + " successed!");

        System.out.println("Geting Hosted Service " + hostName);
        HostedServiceGetResponse response = this.computeManagementClient.getHostedServicesOperations().get(hostName);
        assertEquals(response.getServiceName(), hostName);
        System.out.println("Geted Hosted Service " + hostName + " successed!");
        System.out.println("\n===========================================================");
        System.out.println("Host Service Name: " + response.getServiceName());
        System.out.println("Label : " + response.getProperties().getLabel());
        System.out.println("Description : " + response.getProperties().getDescription());
        System.out.println("Location : " + response.getProperties().getLocation());
        System.out.println("AffinityGroup : " + response.getProperties().getAffinityGroup());
        System.out.println("DateCreated : " + response.getProperties().getDateCreated().getTime());
        System.out.println("ExtendedProperties : " + response.getProperties().getExtendedProperties());
        System.out.println("===========================================================\n");

        System.out.println("Deleting Hosted Service " + hostName);
        op = this.computeManagementClient.getHostedServicesOperations().delete(hostName);
        assertEquals(op.getStatusCode(), 200);
        System.out.println("Deleted Hosted Service " + hostName + " successed!");
    }

    @Test
    public void listHostedService() throws Exception {
        HostedServiceListResponse listResponse = this.computeManagementClient.getHostedServicesOperations().list();
        System.out.println("List All Hosted Service ");
        listResponse.forEach(new Consumer<HostedServiceListResponse.HostedService>() {
            @Override
            public void accept(HostedServiceListResponse.HostedService hostedService) {
                System.out.println("\n===========================================================");
                System.out.println("Hosted Service Name: " + hostedService.getServiceName());
                System.out.println("Label : " + hostedService.getProperties().getLabel());
                System.out.println("Description : " + hostedService.getProperties().getDescription());
                System.out.println("Location : " + hostedService.getProperties().getLocation());
                System.out.println("AffinityGroup : " + hostedService.getProperties().getAffinityGroup());
                System.out.println("DateCreated : " + hostedService.getProperties().getDateCreated().getTime());
                System.out.println("ExtendedProperties : " + hostedService.getProperties().getExtendedProperties());
                System.out.println("===========================================================");
//                for(String size : hostedService.getComputeCapabilities().getVirtualMachinesRoleSizes()) {
//                    System.out.println("\tHost VM Role Size: " + size);
//                }
            }
        });
    }

}
