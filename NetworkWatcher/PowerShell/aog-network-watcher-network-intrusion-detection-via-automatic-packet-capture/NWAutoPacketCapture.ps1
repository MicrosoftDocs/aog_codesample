# This script is used to take packet capture with Network Watcher

# Packet capture vars
$date = Get-Date -Format 'yyyyMMdd-HHmmss'
$packetCaptureName = "PSpacketCapture-" + $date
$packetCaptureLimit = 10  # max number of packets
$packetCaptureDuration = 10 # time limits of packet capture, unit in minutes
$filePath = "{your output log path, E.g. c:\logs\PacketCaptureLogs.txt}" 
"[$packetCaptureName]" | Out-File -Append $filePath

# Login to Azure
$azureAccountName = "{YourAccountName}"
$azurePwd = ConvertTo-SecureString "{YourPassword}" -AsPlainText -Force
$psCred = New-Object System.Management.Automation.PSCredential($azureAccountName, $azurePwd);
Login-AzureRmAccount -EnvironmentName AzureChinaCloud -Credential $psCred;

"-- Login Finished" | Out-File -Append $filePath

# Storage account ID to save captures in
$storageaccountid = "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Storage/storageAccounts/{storageAccountName}"

# Target VM ID to get capture
$vmId = "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}"

# Get the Network Watcher in the VM's region
$nw = Get-AzurermResource | Where {$_.ResourceType -eq "Microsoft.Network/networkWatchers" -and $_.Location -eq "chinanorth" } 
$networkWatcher = Get-AzureRmNetworkWatcher -Name $nw.Name -ResourceGroupName $nw.ResourceGroupName

# Get existing packetCaptures
$packetCaptures = Get-AzureRmNetworkWatcherPacketCapture -NetworkWatcher $networkWatcher

"-- Gather basic information Finished" | Out-File -Append $filePath

# Initiate packet capture on the VM
if ((Get-AzureRmNetworkWatcherPacketCapture -NetworkWatcher $networkWatcher).Count -lt $packetCaptureLimit){
    "-- Initiating Packet Capture" | Out-File -Append $filePath
    # $filter1 = New-AzureRmPacketCaptureFilterConfig -Protocol TCP -RemoteIPAddress "1.1.1.1-255.255.255" -LocalIPAddress "10.0.0.3" -LocalPort "1-65535" -RemotePort "20;80;443"
    # $filter2 = New-AzureRmPacketCaptureFilterConfig -Protocol UDP 
    New-AzureRmNetworkWatcherPacketCapture -NetworkWatcher $networkWatcher -TargetVirtualMachineId $vmId -PacketCaptureName $packetCaptureName -StorageAccountId $storageaccountId -TimeLimitInSeconds $packetCaptureDuration #-Filter $filter1, $filter2
    "-- Packet Capture Done for $vmId" | Out-File -Append $filePath
    "-- Stored in $storageaccountId" | Out-File -Append $filePath
    "" | Out-File -Append $filePath
} else {
    "[Warning]: Excceeding packet capture limits. Too many VM captures. Please clear first." | Out-File -Append $filePath
}

# pause # For Debug
# Remove existing packet capture created by the function (if it exists)
#$packetCaptures | %{if($_.Name -eq $packetCaptureName)
#{ 
    #Remove-AzureRmNetworkWatcherPacketCapture -NetworkWatcher $networkWatcher -PacketCaptureName $packetCaptureName
#}}