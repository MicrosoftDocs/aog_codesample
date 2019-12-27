package com.vianet.azure.sdk.manage.computer;

import java.net.URI;
import java.util.ArrayList;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.management.compute.*;
import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.network.NetworkManagementClient;
import com.microsoft.windowsazure.management.network.NetworkManagementService;
import com.microsoft.windowsazure.management.scheduler.*;

public class TestClassicVmCreate {

	int random = (int)(Math.random()* 100);

	String testVMName = "kevin" + random + "-";

	Configuration config;

	ComputeManagementClient computeManagementClient;
	CloudServiceManagementClient cloudServiceManagementClient;
	NetworkManagementClient networkManagementClient;

	public TestClassicVmCreate(Configuration config) {
		this.config = config;
		this.computeManagementClient = ComputeManagementService.create(config);
		this.cloudServiceManagementClient = CloudServiceManagementService.create(config);
		this.networkManagementClient = NetworkManagementService.create(config);
	}

	public static void main(String[] args) {
		try {
			Configuration config = PublishSettingsLoader.createManagementConfiguration("D:\\Users\\chen.rui\\Documents\\china.publishsettings", "<your sub id>");
			TestClassicVmCreate app = new TestClassicVmCreate(config);
			app.createVirtualMachines();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getOSSourceImage() throws Exception {
		String sourceImageName = null;
		VirtualMachineOSImageListResponse virtualMachineImageListResponse = computeManagementClient.getVirtualMachineOSImagesOperations().list();
		ArrayList<VirtualMachineOSImageListResponse.VirtualMachineOSImage> virtualMachineOSImagelist = virtualMachineImageListResponse.getImages();

		for (VirtualMachineOSImageListResponse.VirtualMachineOSImage virtualMachineImage : virtualMachineOSImagelist) {
			if ((virtualMachineImage.getOperatingSystemType().equals("Windows"))) {
				sourceImageName = virtualMachineImage.getName();
				break;
			}
		}
		return sourceImageName;
	}

	private void createHostedService() throws Exception  {
		//hosted service required for vm deployment
		String hostName = testVMName + "vm";
		HostedServiceCreateParameters createParameters = new HostedServiceCreateParameters();
		//required
		createParameters.setLabel(hostName);
		//required
		createParameters.setServiceName(hostName);
		createParameters.setDescription("kevin test vm");
		//required
		createParameters.setLocation("China North");

		OperationResponse hostedServiceOperationResponse = computeManagementClient.getHostedServicesOperations().create(createParameters);
	}

	private void createVirtualMachines() throws Exception {
		VirtualMachineOperations vmop = computeManagementClient.getVirtualMachinesOperations();

		String roleName = testVMName + "vm";

		this.createHostedService();

		VirtualMachineCreateDeploymentParameters parameters = new VirtualMachineCreateDeploymentParameters();
		parameters.setLabel(roleName);
		parameters.setDeploymentSlot(DeploymentSlot.Production);
		parameters.setName(roleName);

		ArrayList<Role> roles = new ArrayList<Role>();
		Role role = new Role();
		role.setLabel(roleName);
		role.setRoleName(roleName);
		role.setRoleSize(VirtualMachineRoleSize.SMALL);
		role.setRoleType(VirtualMachineRoleType.PersistentVMRole.toString());
		role.setProvisionGuestAgent(true);

		// OS DISK
		String osVHarddiskName =testVMName + "oshdname"+ random;
		String operatingSystemName ="Windows";
		String sourceImageName = getOSSourceImage();
		URI mediaLinkUriValue =  new URI("http://kevinstorage1.blob.core.chinacloudapi.cn/vhds/" + roleName + random + ".vhd");
		OSVirtualHardDisk oSVirtualHardDisk = new OSVirtualHardDisk();
		oSVirtualHardDisk.setName(osVHarddiskName);
		oSVirtualHardDisk.setHostCaching(VirtualHardDiskHostCaching.READWRITE);
		oSVirtualHardDisk.setOperatingSystem(operatingSystemName);
		oSVirtualHardDisk.setMediaLink(mediaLinkUriValue);
		oSVirtualHardDisk.setSourceImageName(sourceImageName);
		role.setOSVirtualHardDisk(oSVirtualHardDisk);

		// Config Set
		ConfigurationSet provisioningSet = new ConfigurationSet();
		provisioningSet.setConfigurationSetType(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION);
		provisioningSet.setComputerName(roleName);
		provisioningSet.setAdminUserName("kevin");
		provisioningSet.setAdminPassword("Chenrui1");
		provisioningSet.setEnableAutomaticUpdates(true);
		provisioningSet.setHostName(roleName + ".chinacloudapp.cn");

		ConfigurationSet netConfSet = new ConfigurationSet();
		netConfSet.setConfigurationSetType(ConfigurationSetTypes.NETWORKCONFIGURATION);
		InputEndpoint httpEndpoint = new InputEndpoint();
		httpEndpoint.setName("http");
		httpEndpoint.setPort(80);
		httpEndpoint.setLocalPort(80);
		httpEndpoint.setProtocol("tcp");
		InputEndpoint rdpEndpoint = new InputEndpoint();
		rdpEndpoint.setName("RDP");
		rdpEndpoint.setPort(3389);
		rdpEndpoint.setLocalPort(3389);
		rdpEndpoint.setProtocol("tcp");
		ArrayList<InputEndpoint> inputEndpoints = new ArrayList<InputEndpoint>();
		inputEndpoints.add(httpEndpoint);
		inputEndpoints.add(rdpEndpoint);
		netConfSet.setInputEndpoints(inputEndpoints);

		ArrayList<ConfigurationSet> configurationSets = new ArrayList<ConfigurationSet>();
		configurationSets.add(provisioningSet);
		configurationSets.add(netConfSet);
		role.setConfigurationSets(configurationSets);

        // Resource Extension
        VirtualMachineExtensionListResponse extensions = this.computeManagementClient.getVirtualMachineExtensionsOperations().list();
        VirtualMachineExtensionListResponse.ResourceExtension vmAccess = null;
        for(VirtualMachineExtensionListResponse.ResourceExtension resourceExtension : extensions.getResourceExtensions()) {
            if(resourceExtension.getName().equals("VMAccessAgent")) {
                vmAccess = resourceExtension;
                break;
            }
        }
        ArrayList<ResourceExtensionReference> resourceExtensionReferences = new ArrayList<ResourceExtensionReference>();
        ResourceExtensionReference resourceExtensionReference = new ResourceExtensionReference();
        resourceExtensionReference.setName(vmAccess.getName());
        resourceExtensionReference.setForceUpdate(false);
        resourceExtensionReference.setPublisher(vmAccess.getPublisher());
        resourceExtensionReference.setState("enable");
        resourceExtensionReference.setReferenceName(vmAccess.getName());
        resourceExtensionReference.setVersion(vmAccess.getVersion());
        role.setResourceExtensionReferences(resourceExtensionReferences);
		roles.add(role);
		parameters.setRoles(roles);
		OperationStatusResponse response = vmop.createDeployment(roleName, parameters);
		System.out.println(response.getStatus());
	}

}
