package geo.azure.test;

import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;


public class Test {

	@org.junit.Test
	public void test() throws Exception {
/*		VmOperation vmOp = new VmOperation("tenant id","client id", "client secret", "sub id");


		vmOp.create("geogroup", "geowin-test-004", "devstoragerm",  "george", "1QAZxsw2@1",
				KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER, Region.CHINA_NORTH,
				VirtualMachineSizeTypes.STANDARD_A0);
		vmOp.create("geogroup", "geowin-test-005", "devstoragerm",  "george", "1QAZxsw2@1",
				KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER, Region.CHINA_NORTH,
				VirtualMachineSizeTypes.STANDARD_A0);
		vmOp.create("geogroup", "geowin-test-006", "devstoragerm",  "george", "1QAZxsw2@1",
				KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER, Region.CHINA_NORTH,
				VirtualMachineSizeTypes.STANDARD_A0);*/
		
		VmDiagnosticOperation diagnosticOperation = new VmDiagnosticOperation("tenant id","client id", "client secret", "sub id");
		
		String resourceName ="geogroup";
		String vmName ="geowin-test-005";
		String vmLocation ="China North";
		String storageAccountName = "devstoragerm";
		String storageAccountKey="0ODWrzNxe40OccfnPTwqUA+7KCAuD6A40vnqGjA+E+9nD0FSAefBbr8+Xu9z7uyACS4oNeqVi19xqFyiFI2ggg==";
		
		String result = diagnosticOperation.EnableVMDiagnostic(resourceName, vmName, vmLocation,storageAccountName, storageAccountKey);
		System.out.println(result);
		
		
		/*
		vmOp.update("geogroup",  "geowin-premium-001", "geovmstore", 10);
		
		vmOp.create("geogroup", "geowin-standard-001", "devstoragerm",  "george", "1QAZxsw2@1",
				KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER, Region.CHINA_NORTH,
				VirtualMachineSizeTypes.STANDARD_A1);
		
		vmOp.update("geogroup",  "geowin-standard-001", "geovmstore", "vhds","geowin-premium-001-data-disk-0-648d0b6c-c1ee-4cec-ae51-e27458d3e602.vhd");*/
	}
	

}
