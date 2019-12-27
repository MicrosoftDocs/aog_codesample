package com.vianet.azure.sdk.manage.computer;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.compute.*;
import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.network.NetworkManagementClient;
import com.microsoft.windowsazure.management.network.NetworkManagementService;
import com.microsoft.windowsazure.management.network.NetworkOperations;
import com.microsoft.windowsazure.management.network.models.NetworkGetConfigurationResponse;
import com.microsoft.windowsazure.management.scheduler.CloudServiceManagementClient;
import com.microsoft.windowsazure.management.scheduler.CloudServiceManagementService;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.Consumer;

/**
 * Created by chen.rui on 5/5/2016.
 */
public class TestClassicVmAPI {

    int random = (int)(Math.random()* 100);

    String testVMName = "kevin" + random + "-";

    Configuration config;

    ComputeManagementClient computeManagementClient;
    CloudServiceManagementClient cloudServiceManagementClient;
    NetworkManagementClient networkManagementClient;

    public TestClassicVmAPI(Configuration config) {
        this.config = config;
        this.computeManagementClient = ComputeManagementService.create(config);
        this.cloudServiceManagementClient = CloudServiceManagementService.create(config);
        this.networkManagementClient = NetworkManagementService.create(config);
    }

    public static void main(String[] args) {
        try {
            Configuration config = PublishSettingsLoader.createManagementConfiguration("D:\\Users\\chen.rui\\Documents\\CIE01.publishsettings", "<your sub id>");
            TestClassicVmAPI app = new TestClassicVmAPI(config);
            app.getDeploymentDetail();
//            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getRDPFile() throws Exception {
        VirtualMachineGetRemoteDesktopFileResponse response = computeManagementClient.getVirtualMachinesOperations().getRemoteDesktopFile("kevinvm", "kevinvm", "kevinvm");
        FileOutputStream fos = new FileOutputStream("D:\\test.rdp");
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");

        // remove unicode
        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getRemoteDesktopFile());
        InputStreamReader reader = new InputStreamReader(inputStream);
        int codePoint;
        while(true){
            codePoint = reader.read();
            if (codePoint == -1) {
                break;
            }
            if (codePoint != 65533 && codePoint != 0 ) {
                osw.write(codePoint);
            }
        }
        osw.close();
        fos.close();
    }

    public void listOSImages() throws Exception {
        ArrayList<VirtualMachineOSImageListResponse.VirtualMachineOSImage> images = computeManagementClient.getVirtualMachineOSImagesOperations().list().getImages();
        for(VirtualMachineOSImageListResponse.VirtualMachineOSImage image : images) {
            System.out.println("=======================================");
            System.out.println(image.getName());
            System.out.println(image.getLabel());
            System.out.println(image.getOperatingSystemType());
            System.out.println(image.getCategory());
            System.out.println(image.getPublishedDate());
        }
    }

    public void listVMImages() throws Exception {
        ArrayList<VirtualMachineVMImageListResponse.VirtualMachineVMImage> images = computeManagementClient.getVirtualMachineVMImagesOperations().list().getVMImages();
        for(VirtualMachineVMImageListResponse.VirtualMachineVMImage image : images) {
            System.out.println("=======================================");
            System.out.println(image.getName());
            System.out.println(image.getLabel());
            System.out.println(image.getCategory());
            System.out.println(image.getPublishedDate());
        }
    }

    public void createOSImages() throws Exception {
        VirtualMachineOSImageCreateParameters createParameters = new VirtualMachineOSImageCreateParameters();
        createParameters.setMediaLinkUri(new URI("https://kevinstorage1.blob.core.chinacloudapi.cn/vhds/kevinvm-kevinvm-2016-05-30.vhd"));
        createParameters.setName("kevinimage-01");
        createParameters.setLabel("kevinimage-01");
        createParameters.setOperatingSystemType(VirtualMachineOSImageOperatingSystemType.WINDOWS);
        createParameters.setPublishedDate(Calendar.getInstance());
        computeManagementClient.getVirtualMachineOSImagesOperations().create(createParameters);
    }

    public void listHostedService() throws Exception {
        HostedServiceListResponse listResponse = computeManagementClient.getHostedServicesOperations().list();
        listResponse.forEach(new Consumer<HostedServiceListResponse.HostedService>() {
            @Override
            public void accept(HostedServiceListResponse.HostedService hostedService) {
                System.out.println(hostedService.getServiceName());
            }
        });
    }

    public void listDataDisk() throws Exception {
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get("keivnvm", "keivnvm", "keivnvm");
        DataVirtualHardDisk dataVirtualHardDisk = virtualMachinesGetResponse.getDataVirtualHardDisks().get(0);
        virtualMachinesGetResponse.getDataVirtualHardDisks().forEach(new Consumer<DataVirtualHardDisk>() {
            @Override
            public void accept(DataVirtualHardDisk dataVirtualHardDisk) {
                System.out.println("===============================");
                System.out.println(dataVirtualHardDisk.getName());
                System.out.println(dataVirtualHardDisk.getLabel());
                System.out.println(dataVirtualHardDisk.getIOType());
                System.out.println(dataVirtualHardDisk.getLogicalUnitNumber());
            }
        });
    }

    public void listDisks() throws Exception {
        VirtualMachineDiskListResponse listResponse = computeManagementClient.getVirtualMachineDisksOperations().listDisks();
        listResponse.getDisks().forEach(new Consumer<VirtualMachineDiskListResponse.VirtualMachineDisk>() {
            @Override
            public void accept(VirtualMachineDiskListResponse.VirtualMachineDisk virtualMachineDisk) {
                System.out.println("===============================");
                System.out.println(virtualMachineDisk.getName());
                System.out.println(virtualMachineDisk.getLabel());
                System.out.println(virtualMachineDisk.getIOType());
                System.out.println(virtualMachineDisk.getOperatingSystemType());
                System.out.println(virtualMachineDisk.getSourceImageName());
                if(virtualMachineDisk.getUsageDetails() != null) {
                    System.out.println(virtualMachineDisk.getUsageDetails().getHostedServiceName());
                }
            }
        });
    }

    public void createDisk() throws Exception {
        URI mediaLinkUriValue =  new URI("https://kevinstorage1.blob.core.chinacloudapi.cn/vhds/kevinvm2-kevinvm2-2016-05-26.vhd");
        VirtualMachineDiskCreateParameters  createParameters = new VirtualMachineDiskCreateParameters();
        createParameters.setName("kevintest-os-disk");
        createParameters.setLabel("kevintest-disk-label");
        createParameters.setMediaLinkUri(mediaLinkUriValue);
        createParameters.setOperatingSystemType(VirtualMachineOSImageOperatingSystemType.WINDOWS);
        computeManagementClient.getVirtualMachineDisksOperations().createDisk(createParameters);
    }

    public void  createDataDiskFromExsit() throws Exception {
        VirtualMachineDataDiskCreateParameters createParameters = new VirtualMachineDataDiskCreateParameters();
        createParameters.setName("kevintest-os-disk");
        createParameters.setLogicalUnitNumber(4);
        createParameters.setHostCaching(VirtualHardDiskHostCaching.READWRITE);
        createParameters.setMediaLinkUri(new URI("https://kevinstorage1.blob.core.chinacloudapi.cn/vhds/kevinvm2-kevinvm2-2016-05-26.vhd"));
        computeManagementClient.getVirtualMachineDisksOperations().createDataDisk("kevinvm","kevinvm","kevinvm", createParameters);
    }

    private void createDataDisk() throws Exception {
        VirtualMachineDataDiskCreateParameters createParameters = new VirtualMachineDataDiskCreateParameters();
        createParameters.setLabel("keivnvm-data-0");
        createParameters.setLogicalDiskSizeInGB(20);
        createParameters.setLogicalUnitNumber(0);
        createParameters.setHostCaching(VirtualHardDiskHostCaching.READWRITE);
        createParameters.setMediaLinkUri(new URI("http://kevinstorage1.blob.core.chinacloudapi.cn/vhds/keivnvm-keivnvm-0.vhd"));
        computeManagementClient.getVirtualMachineDisksOperations().createDataDisk("keivnvm","keivnvm","keivnvm", createParameters);
    }

    private void deleteDataDisk() throws Exception {
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get("kevinvm", "kevinvm", "kevinvm");
        DataVirtualHardDisk dataVirtualHardDisk = virtualMachinesGetResponse.getDataVirtualHardDisks().get(0);
        computeManagementClient.getVirtualMachineDisksOperations().deleteDataDisk("kevinvm", "kevinvm", "kevinvm", 0, false);
    }

    private DataVirtualHardDisk getDataDisk(String serviceName, String deploymentName, String roleName, String dataDiskName) throws Exception {
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(serviceName, deploymentName, roleName);
        for(DataVirtualHardDisk dataVirtualHardDisk : virtualMachinesGetResponse.getDataVirtualHardDisks()) {
            if(dataVirtualHardDisk.getName().equals(dataDiskName)) {
                return dataVirtualHardDisk;
            }
        }
        return null;
    }

    private void updateDataDisk() throws Exception {
        DataVirtualHardDisk dataVirtualHardDisk = getDataDisk("keivnvm", "keivnvm", "keivnvm", "keivnvm-keivnvm-0-201606220255140370");
        VirtualMachineDataDiskUpdateParameters updateParameters = new VirtualMachineDataDiskUpdateParameters();
        updateParameters.setName(dataVirtualHardDisk.getName());
        updateParameters.setMediaLinkUri(dataVirtualHardDisk.getMediaLink());
        updateParameters.setHostCaching(VirtualHardDiskHostCaching.NONE);
        try {
            computeManagementClient.getVirtualMachineDisksOperations().updateDataDisk("keivnvm", "keivnvm", "keivnvm", 0, updateParameters);
        } catch (ServiceException ex) {
            if(ex.getHttpStatusCode() != 0) {
                throw ex;
            }
        }
    }

    private void updateRole() throws Exception  {
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get("kevin40-vm", "kevin40-vm", "kevin40-vm");

        VirtualMachineUpdateParameters updateParameters = new VirtualMachineUpdateParameters();
        updateParameters.setRoleName(virtualMachinesGetResponse.getRoleName());
//        updateParameters.setConfigurationSets(virtualMachinesGetResponse.getConfigurationSets());
//        ArrayList<String> subnetNamesValue = new ArrayList<String>();
//        subnetNamesValue.add("Subnet-2");
//        updateParameters.getConfigurationSets().get(0).setSubnetNames(subnetNamesValue);
        updateParameters.setRoleSize(VirtualMachineRoleSize.MEDIUM);

        //this is required parameters for update
        OSVirtualHardDisk osVirtualHardDisk = virtualMachinesGetResponse.getOSVirtualHardDisk();
        osVirtualHardDisk.setHostCaching(VirtualHardDiskHostCaching.READONLY);
        updateParameters.setOSVirtualHardDisk(osVirtualHardDisk);

        OperationResponse updateoperationResponse = computeManagementClient.getVirtualMachinesOperations().update("kevin40-vm", "kevin40-vm", "kevin40-vm", updateParameters);
    }

    private void deleteDeployment() throws Exception {
        computeManagementClient.getDeploymentsOperations().deleteByName("xuahuvm", "xuahuvm", true);
    }

    private void listVMExtents() {
        VirtualMachineExtensionOperations vmexop = this.computeManagementClient.getVirtualMachineExtensionsOperations();
        try {
            VirtualMachineExtensionListResponse res = vmexop.list();
            res.forEach(new Consumer<VirtualMachineExtensionListResponse.ResourceExtension>() {
                @Override
                public void accept(VirtualMachineExtensionListResponse.ResourceExtension resourceExtension) {
                    System.out.println(resourceExtension.getName());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void getVMIMageDetail() throws Exception {
        VirtualMachineVMImageGetDetailsResponse detailsResponse = computeManagementClient.getVirtualMachineVMImagesOperations().getDetails("gitlab-20160505-629863");
    }

    private void createVMNET() throws Exception {
        NetworkOperations netop = this.networkManagementClient.getNetworksOperations();
        NetworkGetConfigurationResponse operationResponse =  netop.getConfiguration();
        String config  = operationResponse.getConfiguration();
        System.out.println(config);
    }

    private void updateVMInputEndpoint() throws Exception {
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get("kevinvm", "kevinvm", "kevinvm");

        VirtualMachineUpdateParameters updateParameters = new VirtualMachineUpdateParameters();
        //get the configuration list
        ArrayList<ConfigurationSet> configlist = virtualMachinesGetResponse.getConfigurationSets();
        //get inputendpoint list and update it
        ArrayList<InputEndpoint> endpointlist = configlist.get(0).getInputEndpoints();
        InputEndpoint inputEndpoint = new InputEndpoint();
        inputEndpoint.setEnableDirectServerReturn(false);
        inputEndpoint.setPort(10000);
        inputEndpoint.setLocalPort(10000);
        inputEndpoint.setName("TTTT");
        inputEndpoint.setProtocol(InputEndpointTransportProtocol.TCP);
        endpointlist.add(inputEndpoint);
        updateParameters.setConfigurationSets(configlist);

        //required for update
        OSVirtualHardDisk osVirtualHardDisk = virtualMachinesGetResponse.getOSVirtualHardDisk();
        updateParameters.setOSVirtualHardDisk(osVirtualHardDisk);
        updateParameters.setRoleName(virtualMachinesGetResponse.getRoleName());

        OperationResponse updateoperationResponse = computeManagementClient.getVirtualMachinesOperations().update("kevinvm", "kevinvm", virtualMachinesGetResponse.getRoleName(), updateParameters);
    }

    private void getHostedDetail() throws Exception {
        HostedServiceOperations hostop = computeManagementClient.getHostedServicesOperations();
        try {
            HostedServiceGetDetailedResponse response =  hostop.getDetailed("xuhua11");
            response.getDeployments().get(0).getVirtualIPAddresses().forEach(new Consumer<VirtualIPAddress>() {
                @Override
                public void accept(VirtualIPAddress virtualIPAddress) {
                    InetAddress address = virtualIPAddress.getAddress();
                    System.out.println(address.getHostAddress());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void getDeploymentDetail() throws Exception {
        DeploymentGetResponse deploymentGetResponse = computeManagementClient.getDeploymentsOperations().getBySlot("xuhua11", DeploymentSlot.Production);
        for(VirtualIPAddress virtualIPAddress : deploymentGetResponse.getVirtualIPAddresses()) {
            InetAddress address = virtualIPAddress.getAddress();
            System.out.println(address.getHostAddress());
        }
    }


    private void getVMInfo() throws Exception {
        VirtualMachineGetResponse virtualMachineGetResponse = computeManagementClient.getVirtualMachinesOperations().get("kevinvm","xuahuvm","xuahuvm");
        virtualMachineGetResponse.getDataVirtualHardDisks().forEach(new Consumer<DataVirtualHardDisk>() {
            @Override
            public void accept(DataVirtualHardDisk dataVirtualHardDisk) {
                System.out.println(dataVirtualHardDisk.getName());
                System.out.println(dataVirtualHardDisk.getLogicalUnitNumber());
            }
        });
    }

    private String getOSSourceImage(String opr) throws Exception {
        String sourceImageName = null;
        VirtualMachineOSImageListResponse virtualMachineImageListResponse = computeManagementClient.getVirtualMachineOSImagesOperations().list();
        ArrayList<VirtualMachineOSImageListResponse.VirtualMachineOSImage> virtualMachineOSImagelist = virtualMachineImageListResponse.getImages();

        for (VirtualMachineOSImageListResponse.VirtualMachineOSImage virtualMachineImage : virtualMachineOSImagelist) {
            if ((virtualMachineImage.getOperatingSystemType().equals(opr))) {
                sourceImageName = virtualMachineImage.getName();
                break;
            }
        }
        return sourceImageName;
    }

    private void createHostedService(String hostName) throws Exception  {
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

    private void createDeploymentFromExsitOSDisk() throws Exception {
        String roleName = testVMName + "vm";
        this.createHostedService(roleName);
        this.doCreateDeploymentFromExsitOSDisk(roleName, roleName, DeploymentSlot.Production, null);
    }

    private void doCreateDeploymentFromExsitOSDisk(String hostName, String roleName, DeploymentSlot slot, String availabilitySetName) throws Exception  {
        VirtualMachineOperations vmop = computeManagementClient.getVirtualMachinesOperations();

        VirtualMachineCreateDeploymentParameters parameters = new VirtualMachineCreateDeploymentParameters();
        parameters.setLabel(roleName);
        parameters.setDeploymentSlot(slot);
        parameters.setName(roleName);

        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(this.createRoleFromExsitOSDisk(hostName, roleName, availabilitySetName));

        parameters.setRoles(roles);
        OperationStatusResponse response = vmop.createDeployment(hostName, parameters);
        System.out.println(response.getStatus());
    }

    private Role createRoleFromExsitOSDisk(String hostName, String roleName, String availabilitySetName) throws Exception {
        Role role = new Role();
        role.setLabel(roleName);
        role.setRoleName(roleName);
        role.setRoleSize(VirtualMachineRoleSize.SMALL);
        role.setRoleType(VirtualMachineRoleType.PersistentVMRole.toString());
        role.setProvisionGuestAgent(true);

        // 指定已存在的 OS DISK
        String osVHarddiskName ="kevinvm2-kevinvm2-0-201607040250210015";
        OSVirtualHardDisk oSVirtualHardDisk = new OSVirtualHardDisk();
        oSVirtualHardDisk.setName(osVHarddiskName);
        oSVirtualHardDisk.setHostCaching(VirtualHardDiskHostCaching.READWRITE);
        oSVirtualHardDisk.setOperatingSystem("Windows");
        role.setOSVirtualHardDisk(oSVirtualHardDisk);

        ArrayList<ConfigurationSet> configurationSets = new ArrayList<ConfigurationSet>();
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

        configurationSets.add(netConfSet);

        role.setConfigurationSets(configurationSets);
        return  role;
    }

    private void createLinuxDeploymentFromVMImage() throws Exception {
        String roleName = testVMName + "vm";
        this.createHostedService(roleName);
        this.doCreateLinuxDeploymentFromVMImage(roleName, roleName, DeploymentSlot.Production, null);
    }

    private void doCreateLinuxDeploymentFromVMImage(String hostName, String roleName, DeploymentSlot slot, String availabilitySetName) throws Exception {
        VirtualMachineOperations vmop = computeManagementClient.getVirtualMachinesOperations();

        VirtualMachineCreateDeploymentParameters parameters = new VirtualMachineCreateDeploymentParameters();
        parameters.setLabel(roleName);
        parameters.setDeploymentSlot(slot);
        parameters.setName(roleName);

        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(this.createLinuxRoleFromVMImage(hostName, roleName, availabilitySetName));

        parameters.setRoles(roles);
        OperationStatusResponse response = vmop.createDeployment(hostName, parameters);
        System.out.println(response.getStatus());
    }

    private Role createLinuxRoleFromVMImage(String hostName, String roleName, String availabilitySetName) throws Exception {
        Role role = this.configVMRole(hostName, roleName, availabilitySetName, null);
        role.setVMImageName("kevin-ub-20160601-376882");
        return  role;
    }

    private void createLinuxDeployment() throws Exception {
        String roleName = testVMName + "vm";
        this.createHostedService(roleName);
        this.doCreateLinuxDeployment(roleName, roleName, DeploymentSlot.Production, null);
    }

    private void doCreateLinuxDeployment(String hostName, String roleName, DeploymentSlot slot, String availabilitySetName) throws Exception {
        VirtualMachineOperations vmop = computeManagementClient.getVirtualMachinesOperations();

        VirtualMachineCreateDeploymentParameters parameters = new VirtualMachineCreateDeploymentParameters();
        parameters.setLabel(roleName);
        parameters.setDeploymentSlot(slot);
        parameters.setName(roleName);

        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(this.createLinuxRole(hostName, roleName, availabilitySetName));

        parameters.setRoles(roles);
        OperationStatusResponse response = vmop.createDeployment(hostName, parameters);
        if(checkRoleIsReady(hostName, roleName)) {
            System.out.println("create success!");
        }
    }

    public boolean checkRoleIsReady(String hostName, String roleName) throws Exception  {
        String status = getRoleStatus(hostName, roleName);
        while(!"ReadyRole".equals(status)) { // 如果状态不是Ready，继续查询状态
            Thread.sleep(3000);
            System.out.println(status);
            status = getRoleStatus(hostName, roleName);
        }
        return true;
    }

    public String getRoleStatus(String hostName, String roleName) throws Exception {
        DeploymentGetResponse deploymentGetResponse = computeManagementClient.getDeploymentsOperations().getBySlot(hostName, DeploymentSlot.Production);
        if(deploymentGetResponse == null) return null;
        for(RoleInstance roleInstance : deploymentGetResponse.getRoleInstances()) {
            if(roleInstance.getRoleName().equals(roleName)) {
                return roleInstance.getInstanceStatus();
            }
        }
        return null;
    }

    private Role createLinuxRole(String hostName, String roleName, String availabilitySetName) throws Exception {
        Role role = configVMRole(hostName, roleName, availabilitySetName, ConfigurationSetTypes.LINUXPROVISIONINGCONFIGURATION);

        // OS DISK
        String osVHarddiskName =roleName + "-oshdname-"+ random;
        String operatingSystemName ="Linux";
        String sourceImageName = getOSSourceImage(operatingSystemName);
        URI mediaLinkUriValue =  new URI("http://kevinstorage1.blob.core.chinacloudapi.cn/vhds/" + roleName + random + ".vhd");
        OSVirtualHardDisk oSVirtualHardDisk = new OSVirtualHardDisk();
        oSVirtualHardDisk.setName(osVHarddiskName);
        oSVirtualHardDisk.setHostCaching(VirtualHardDiskHostCaching.READWRITE);
        oSVirtualHardDisk.setOperatingSystem(operatingSystemName);
        oSVirtualHardDisk.setMediaLink(mediaLinkUriValue);
        oSVirtualHardDisk.setSourceImageName(sourceImageName);
        role.setOSVirtualHardDisk(oSVirtualHardDisk);
        return  role;
    }

    private Role createRole(String hostName, String roleName, String availabilitySetName) throws Exception {
        Role role = configVMRole(hostName, roleName, availabilitySetName, ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION);

        // OS DISK
        String osVHarddiskName =roleName + "-oshdname-"+ random;
        String operatingSystemName ="Windows";
        String sourceImageName = getOSSourceImage("Windows");
        URI mediaLinkUriValue =  new URI("http://kevinstorage1.blob.core.chinacloudapi.cn/vhds/" + roleName + random + ".vhd");
        OSVirtualHardDisk oSVirtualHardDisk = new OSVirtualHardDisk();
        oSVirtualHardDisk.setName(osVHarddiskName);
        oSVirtualHardDisk.setHostCaching(VirtualHardDiskHostCaching.READWRITE);
        oSVirtualHardDisk.setOperatingSystem(operatingSystemName);
        oSVirtualHardDisk.setMediaLink(mediaLinkUriValue);
        oSVirtualHardDisk.setSourceImageName(sourceImageName);
        role.setOSVirtualHardDisk(oSVirtualHardDisk);

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

        ArrayList<String> subnetNamesValue = new ArrayList<String>();
        subnetNamesValue.add("Subnet-1");
        netConfSet.setSubnetNames(subnetNamesValue);

//        ArrayList<ConfigurationSet.PublicIP> publicIPs = new ArrayList<>();
//        ConfigurationSet.PublicIP publicIP = new ConfigurationSet.PublicIP();
//        publicIP.setName("KevinIP");
//        publicIP.setDomainNameLabel("test");
//        publicIPs.add(publicIP);
//        netConfSet.setPublicIPs(publicIPs);

        role.getConfigurationSets().add(netConfSet);

        // Resource Extension
        VirtualMachineExtensionListResponse extensions = this.computeManagementClient.getVirtualMachineExtensionsOperations().list();
        VirtualMachineExtensionListResponse.ResourceExtension vmAccess = null;
        VirtualMachineExtensionListResponse.ResourceExtension customScript = null;
        for(VirtualMachineExtensionListResponse.ResourceExtension resourceExtension : extensions.getResourceExtensions()) {
            if(resourceExtension.getName().equals("VMAccessAgent")) {
                vmAccess = resourceExtension;

            }
            if(resourceExtension.getName().equals("CustomScriptExtension")) {
                customScript = resourceExtension;
            }
        }
        ArrayList<ResourceExtensionReference> resourceExtensionReferences = new ArrayList<ResourceExtensionReference>();
        ResourceExtensionReference vmAccessAgent = new ResourceExtensionReference();
        vmAccessAgent.setName(vmAccess.getName());
        vmAccessAgent.setForceUpdate(false);
        vmAccessAgent.setPublisher(vmAccess.getPublisher());
        vmAccessAgent.setState("enable");
        vmAccessAgent.setReferenceName(vmAccess.getName());
        vmAccessAgent.setVersion(vmAccess.getVersion());

        ResourceExtensionReference customScriptExtends = new ResourceExtensionReference();
        customScriptExtends.setName("CustomScriptExtension");
        customScriptExtends.setForceUpdate(false);
        customScriptExtends.setPublisher(customScript.getPublisher());
        customScriptExtends.setState("enable");
        customScriptExtends.setReferenceName(customScript.getName());
        customScriptExtends.setVersion(customScript.getVersion());

        String account = "{\"storageAccountName\":\"kevinstorage1\",\"storageAccountKey\": \"oLbZhKoC8HaMUwjGWInitvAJG8rkh4Gmmnpo8JIUc0Vpk/8D8lxT8DpE9A0WVvXtDvjwdhR2AHkj5x5LvHturw==\"}";
        String scriptfile = "{\"fileUris\": [\"" + "https://kevinstorage1.blob.core.chinacloudapi.cn/test/test.ps1" + "\"], \"commandToExecute\":\"powershell -ExecutionPolicy Unrestricted -file " + "test.ps1" + "\"}";

        ArrayList<ResourceExtensionParameterValue> resourceExtensionParameterValues = new ArrayList<ResourceExtensionParameterValue>();
        ResourceExtensionParameterValue privateInfo = new ResourceExtensionParameterValue();
        privateInfo.setKey("CustomScriptExtensionPrivateConfigParameter");
        privateInfo.setValue(account);
        privateInfo.setType("Private");
        resourceExtensionParameterValues.add(privateInfo);

        ResourceExtensionParameterValue publicInfo = new ResourceExtensionParameterValue();
        publicInfo.setKey("CustomScriptExtensionPublicConfigParameter");
        publicInfo.setValue(scriptfile);
        resourceExtensionParameterValues.add(publicInfo);
        customScriptExtends.setResourceExtensionParameterValues(resourceExtensionParameterValues);

        resourceExtensionReferences.add(vmAccessAgent);
        resourceExtensionReferences.add(customScriptExtends);
        role.setResourceExtensionReferences(resourceExtensionReferences);

        return  role;
    }

    private void createTwoDeploymentUnderHostedServcie() throws Exception {
        VirtualMachineOperations vmop = computeManagementClient.getVirtualMachinesOperations();

        String roleName = testVMName + "vm";
        this.createHostedService(roleName);

        this.doCreateDeployment(roleName, roleName + "01", DeploymentSlot.Production, roleName);
        this.doCreateDeployment(roleName, roleName + "02", DeploymentSlot.Staging, roleName);
    }

    private void createDeploymentWithTwoRole() throws Exception  {
        VirtualMachineOperations vmop = computeManagementClient.getVirtualMachinesOperations();

        String roleName = testVMName + "vm";
        this.createHostedService(roleName);

        VirtualMachineCreateDeploymentParameters parameters = new VirtualMachineCreateDeploymentParameters();
        parameters.setLabel(roleName);
        parameters.setDeploymentSlot(DeploymentSlot.Production);
        parameters.setName(roleName);
        parameters.setVirtualNetworkName("kevinet1");
        ArrayList<LoadBalancer> loadBalancersValue = new ArrayList<LoadBalancer>();
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setName("kevinload");
        FrontendIPConfiguration frontendIPConfiguration = new FrontendIPConfiguration();
        frontendIPConfiguration.setSubnetName("Subnet-1");
        frontendIPConfiguration.setType("Private");
        loadBalancer.setFrontendIPConfiguration(frontendIPConfiguration);
        parameters.setLoadBalancers(loadBalancersValue);

        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(this.createRole(roleName, roleName + "01", roleName));
        roles.add(this.createRole(roleName, roleName + "02", roleName));
        parameters.setRoles(roles);

        OperationStatusResponse response = vmop.createDeployment(roleName, parameters);
        System.out.println(response.getStatus());
    }

    private void createDeployment() throws Exception {
        String roleName = testVMName + "vm";
        this.createHostedService(roleName);
        this.doCreateDeployment(roleName, roleName, DeploymentSlot.Production, null);
    }

    private void createDeploymentWithExsit() throws Exception {
        String roleName = testVMName + "vm";
        this.createHostedService(roleName);
        this.doCreateDeployment(roleName, roleName, DeploymentSlot.Production, null);
    }

    private void doCreateDeployment(String hostName, String roleName, DeploymentSlot slot, String availabilitySetName) throws Exception {
        VirtualMachineOperations vmop = computeManagementClient.getVirtualMachinesOperations();

        VirtualMachineCreateDeploymentParameters parameters = new VirtualMachineCreateDeploymentParameters();
        parameters.setLabel(roleName);
        parameters.setDeploymentSlot(slot);
        parameters.setName(roleName);
        parameters.setVirtualNetworkName("kevinet1");

        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(this.createRole(hostName, roleName, availabilitySetName));

        parameters.setRoles(roles);
        OperationStatusResponse response = vmop.createDeployment(hostName, parameters);
        System.out.println(response.getStatus());
    }

    private Role configVMRole(String hostName, String roleName, String availabilitySetName, String configurationSetType) {
        Role role = new Role();
        role.setLabel(roleName);
        role.setRoleName(roleName);
        role.setRoleSize(VirtualMachineRoleSize.SMALL);
        role.setRoleType(VirtualMachineRoleType.PersistentVMRole.toString());
        role.setProvisionGuestAgent(true);
        role.setAvailabilitySetName(availabilitySetName);

        // Config Set
        ArrayList<ConfigurationSet> configurationSets = new ArrayList<ConfigurationSet>();
        if(configurationSetType != null) {
            ConfigurationSet provisioningSet = new ConfigurationSet();
            provisioningSet.setConfigurationSetType(configurationSetType);
            if(configurationSetType.equals(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION)) {
                provisioningSet.setAdminUserName("kevin");
                provisioningSet.setAdminPassword("Chenrui1");
            } else {
                provisioningSet.setUserName("kevin");
                provisioningSet.setUserPassword("Chenrui1");
            }
            provisioningSet.setComputerName(roleName);
            provisioningSet.setEnableAutomaticUpdates(true);
            provisioningSet.setHostName(hostName + ".chinacloudapp.cn");
            configurationSets.add(provisioningSet);
        }
        role.setConfigurationSets(configurationSets);
        return  role;
    }

}
