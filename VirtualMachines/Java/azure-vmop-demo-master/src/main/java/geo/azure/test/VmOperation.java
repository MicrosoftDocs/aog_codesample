package geo.azure.test;

import java.io.IOException;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;

public class VmOperation {
	private ApplicationTokenCredentials tokenCredentials;
	private Azure azure;

	public VmOperation(String tentant, String clientId, String clientSecret, String subId) {
		try {
			this.tokenCredentials = new ApplicationTokenCredentials(clientId, tentant, clientSecret,
					AzureEnvironment.AZURE_CHINA).withDefaultSubscriptionId(subId);
			this.azure = Azure.authenticate(tokenCredentials).withDefaultSubscription();
			System.out.println("认证成功！");

		} catch (CloudException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void create(String resourceGroupName, String vmName, String vmStorageAccountName, String vmUserName,
			String vmPassword, KnownWindowsVirtualMachineImage vmImage, Region vmRegion, VirtualMachineSizeTypes vmSize)
			throws Exception {

		if (azure == null) {
			return;
		}

		StorageAccount storageAccount = azure.storageAccounts().getByGroup(resourceGroupName, vmStorageAccountName);

		VirtualMachine vm = azure.virtualMachines().define(vmName).withRegion(vmRegion)
				.withNewResourceGroup(resourceGroupName).withNewPrimaryNetwork("10.0.0.0/28")
				.withPrimaryPrivateIpAddressDynamic().withoutPrimaryPublicIpAddress().withPopularWindowsImage(vmImage)
				.withAdminUserName(vmUserName).withPassword(vmPassword).withNewDataDisk(10)
				.withExistingStorageAccount(storageAccount).withSize(vmSize).create();

		System.out.println("create vm successful!");
	}

	public void update(String resourceGroupName, String vmName, String vmStorageAccountName, int diskSize)
			throws Exception {

		VirtualMachine vm = azure.virtualMachines().getByGroup(resourceGroupName, vmName);
		vm.update().withNewDataDisk(diskSize).apply();
		// vm.update().withExistingDataDisk(storageAccountName, containerName,
		// vhdName)
		System.out.println("update vm successful!");
	}

	public void update(String resourceGroupName, String vmName, String storageAccountName, String containerName,
			String vhdName) throws Exception {

		VirtualMachine vm = azure.virtualMachines().getByGroup(resourceGroupName, vmName);
		vm.update().withExistingDataDisk(storageAccountName, containerName, vhdName).apply();
		System.out.println("update vm successful!");
		
	}
}
