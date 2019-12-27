<# 	This script is for Automatic SLB configuration search.
    Please edit your $resultPath before running this script.
	Confirm your current working path and run this script.
	
	INPUT Param: SLBname, ResourceGroupName	
    OUTPUT: SLB configuration as $resultPath
	Author: Joi Ge, China Azure Support Team (Net pod), Microsoft. 
#>


param(
    [string]$slbName = $(throw "Parameter missing: -slbName slbName") ,
    [string]$resGroupName = $(throw "Parameter missing: -resGroupName resGroupName"),
    [string]$resultPath = "C:\temp\SLB-" + $slbName + "-info.txt"
)

#$resultPath = "C:\temp\SLB-" + $slbName + "-info.txt";
# ----------------------------------------Please modify resultPath before use it----------------------------------
$WarningPreference = "SilentlyContinue";

$nicArray = @();	# backend pool nic 
$beArray = @();		# server vm
$lbRuleResult = "";
$lbProbeResult = "";
$lbNATResult = "";
$lbRuleAssArray = [System.Collections.ArrayList]@();   # object ArrayList for lb rule association

# Login-AzureRmAccount -EnvironmentName AzureChinaCloud

# Get SLB
""
Write-Host -ForegroundColor 14 "-- SLBinfo.ps1 v1.1 --`n   This script is currently available for SLB with 1 backend pool and 1 frontend."
Write-Host -ForegroundColor 3 "Getting Load Balancer...";
$slb = Get-AzureRmLoadBalancer -Name $slbName -ResourceGroupName $resGroupName;

if ($slb.FrontendIpConfigurations.publicipaddress -eq $null) { 
    Write-Host -ForegroundColor 14 "Exit. This is ILB. The script is currently only available for SLB."
    exit;
}

# 从slb获取frontend的IP
Write-Host -ForegroundColor 3 "Getting Frontend IP Config...";
$fe = $slb.FrontendIpConfigurations[0]; # currently only for the first Frontend IP
$feName = $fe.Name;   
$feIpId = $fe.PublicIpAddress.Id;
$feIpConfStr = $feIpId.Split("/")[8];
$feIpConf = Get-AzureRmPublicIpAddress -Name $feIpConfStr -ResourceGroupName $resGroupName;
$feConf = "`n`t" + $feName + ": " + $feIpConf.IpAddress + ", " + $feIpConf.Location;

# 从slb获取backendpool的nic
Write-Host -ForegroundColor 3 "Getting Backendpool settings...";
$be = $slb.BackendAddressPools[0]; # currently only for the first Backend Pool
$beName = "`n`t" + $be.Name;
$be.BackendIpConfigurations.id | foreach {
	$nic = $_.Split("/")[8];   # get nic now
	$nicArray += $nic;
}

# 从nic查对应哪台vm
Get-AzureRmVM  -ResourceGroupName $resGroupName | foreach {
	$vmName = $_.Name;
	$avSetStr = $_.AvailabilitySetReference.Id;
    if($avSetStr -ne $null) {
	    $avSetTemp = $avSetStr.Split("/")[8];
	    $vmNicStr = $_.NetworkInterfaceIDs;
	    $vmNic = $vmNicStr.Split("/")[8];
        $nicConf = Get-AzureRmNetworkInterface -Name $vmNic -ResourceGroupName $resGroupName;
        $hardwareConf = $_.HardwareProfile.VmSize;
        if($_.OSProfile.WindowsConfiguration -ne $null) { $os = "Windows"; }
        else { $os = "Linux"; }

	    foreach ($n in $nicArray) {
		    if ($n -eq $vmNic) {
                $vmStr = $vmName + "`n`t`t     " + $nicConf.IpConfigurations.PrivateIpAddress + ", NIC: " + $vmNic + ", " + $hardwareConf + ", " + $os;
			    $beArray += $vmStr;
			    $avSet = "`n`t" + $avSetTemp;
		    }
	    }
    }
}

# 从slb获取load balance rule & Association
Write-Host -ForegroundColor 3 "Getting Load Balancing Rules...";
if($slb.LoadBalancingRules.Name -eq $null) {
    $lbRuleResult += "No LoadBalancing Rule";
    [void]$lbRuleAssArray.Add("`tNo Association");
} else {
    $slb.LoadBalancingRules | foreach {
        $lbRuleName = $_.Name;
        $lbRuleFE = $_.FrontendPort;
        $lbRuleBE = $_.BackendPort;
        $lbRuleIdle = $_.IdleTimeoutInMinutes;
        $lbRuleDistr = $_.LoadDistribution;
        $lbRuleProbe = $_.Probe.Id;
        $lbRuleProbe = $lbRuleProbe.Split("/")[10];
        $lbRuleResult += "`n`t" + $lbRuleName + "`n`t`tFrontendPort " + $lbRuleFE + ", BackendPort " + $lbRuleBE + ", IdleTimeoutInMinutes " + $lbRuleIdle + "`n`t`tLoadDistribution " + $lbRuleDistr;# + "`n`t`tAssociated Probe " + $lbRuleProbe;

        # Get Association: Frontend -----LBRules----- Backend Pool ----- Active Probe$slb
        $lbFeId = $_.FrontendIPConfiguration.Id;
        $lbFeName = $lbFeId.Split("/")[10];
        $lbBeId = $_.BackendAddressPool.Id;
        $lbfeIpConf = Get-AzureRmPublicIpAddress -Name $feIpConfStr -ResourceGroupName $resGroupName;
        $lbfeConf = $feIpConf.IpAddress + "(" + $lbFeName + ")";
        $lbBeName = $lbBeId.Split("/")[10];
        $lbProbeId = $_.Probe.Id;
        $lbProbeName = $lbProbeId.Split("/")[10];
        $lbRuleAssociation =  "" | Select-Object -Property SLBpublicIP, BackendPool, LBRule, ProbeRule; #, FrontendName
        $lbRuleAssociation.SLBpublicIP = $lbfeConf; 
        $lbRuleAssociation.BackendPool = $lbBeName;
        $lbRuleAssociation.LBRule = $lbRuleName;
        $lbRuleAssociation.ProbeRule = $lbProbeName;
        [void]$lbRuleAssArray.Add($lbRuleAssociation);
    }
}

# 从slb获取Probes
Write-Host -ForegroundColor 3 "Getting Probes Settings...";
if($slb.Probes.Name -eq $null) {
    $lbProbeResult += "No Probe";
} else {
    foreach ($probe in $slb.Probes) {
        $lbProbeName = $probe.Name;
        $lbProbeProtocal = $probe.Protocol;
        $lbProbePort = $probe.Port;
        $lbProbeInt = $probe.IntervalInSeconds;
        $lbProbeNum = $probe.NumberOfProbes;
        $lbProbeResult += "`n`t" + $lbProbeName + "`n`t`tProtocol " + $lbProbeProtocal + ", Port " + $lbProbePort + ", IntervalInSeconds " + $lbProbeInt + ", NumberOfProbes " + $lbProbeNum;
    }
}

# 从slb获取NAT Rules
Write-Host -ForegroundColor 3 "Getting Inbound NAT Rules...";
if($slb.InboundNatRules.Name -eq $null) {
    $lbNATResult += "No Inbound NAT Rule";
} else {
    foreach ($nat in $slb.InboundNatRules) {
        $lbNATName = $nat.Name;
        $lbNATfePort = $nat.FrontendPort;
        $lbNATidle = $nat.IdleTimeoutInMinutes;
        $lbNATbePort = $nat.BackendPort;
        $lbNATprotocal = $nat.Protocol;
        $lbNATResult += "`n`t" + $lbNATName + "`n`t`tProtocol " + $lbNATprotocal + ", Frontend Port " + $lbNATfePort + ", Backend Port " + $lbNATbePort + ", Idle TimeOut " + $lbNATidle;
    }
}

# Output file
Write-Host -ForegroundColor 3 "Writing results...";
$output = "-- " + $slbName + " Configuration results " + "`n--[Basic Info]" + "`n   Resource Group: " + $resGroupName + "`n   Frontend " + $feConf;
$output += "`n   Backend Availability Set " + $avSet;
$output += "`n   BackendPool " + $beName; #+ $beArray; 
foreach($beVM in $beArray) {
    $output += "`n`t`t·" + $beVM + " ";
}
$output += "`n   Load Balancing Rule: " + $lbRuleResult + "`n   Probes: " + $lbProbeResult + "`n   Inbound NAT rules: " + $lbNATResult + "`n`n--[Association]";
$output | Out-File $resultPath;
$output;
$lbRuleAssArray;

Add-Content $resultPath $lbRuleAssArray;
"`n-- Done.`n`tResults has been written to " + $resultPath;
""
