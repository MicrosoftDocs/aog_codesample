<# 	This script is for Automatic ILPIP search.
    Please confirm your current working path and run this script.
	
    OUTPUT: ILPIP count
	Author: Joi Ge, China Azure Support Team (Net pod), Microsoft. 
#>

<#
# login manually
Login-AzureRmAccount -EnvironmentName AzureChinaCloud
#>

# Get ILPIP
""
Write-Host -ForegroundColor 14 "-- getILPIP.ps1 --`n"
Write-Host -ForegroundColor 3 "Getting ILPIPs...";

$cvm = Get-AzureVM;
$ilpipAssArray = [System.Collections.ArrayList]@();   # object ArrayList for ILPIP

$i = 0; 
while ($i -lt $cvm.Count) {
	$vmName = $cvm[$i].VM.RoleName;
	$vmPip = $cvm[$i].PublicIPAddress;
    $vmDip = $cvm[$i].IpAddress;
	if ($vmpip -gt 0) {
        $ilpipInstance =  "" | Select-Object -Property PublicIP, VMname, DIP;
        $ilpipInstance.PublicIP = $vmPip; 
        $ilpipInstance.VMname = $vmName;
        $ilpipInstance.DIP = $vmDip;
        [void]$ilpipAssArray.Add($ilpipInstance);
	}
	$i++;
} 

Write-Host -ForegroundColor 3 "`nTotal ILPIP: " $ilpipAssArray.Count;
$ilpipAssArray;
echo `n-- Done.
