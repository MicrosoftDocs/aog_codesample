<#
.SYNOPSIS
    This script grab all ARM VM VHD file in the subscription and caculate VHD size.
.DESCRIPTION
    This script grab all ARM VM VHD file in the subscription and caculate VHD size.
.Example
    .\Get-ArmVMDiskSize.ps1 -subscriptionid xxxxxxx-xxxx-xxxx-xxxxxxx
    Then input the username and password of Azure China.
.Disclaimer
    This sample code is provided for the purpose of illustration only and is not intended to be used in a production environment.  
    THIS SAMPLE CODE AND ANY RELATED INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.  
    We grant You a nonexclusive, royalty-free right to use and modify the Sample Code and to reproduce and distribute the object code form of the Sample Code, provided that You agree:
    (i)     to not use Our name, logo, or trademarks to market Your software product in which the Sample Code is embedded;
    (ii)	to include a valid copyright notice on Your software product in which the Sample Code is embedded; 
    (iii)	to indemnify, hold harmless, and defend Us and Our suppliers from and against any claims or lawsuits, including attorneys’ fees, that arise or result from the use or distribution of the Sample Code.
#>

param(
    [Parameter(Mandatory = $true)]
    [String]$SubscriptionID
)

function Get-BlobBytes
{
    param (
        [Parameter(Mandatory=$true)]
        [Microsoft.WindowsAzure.Commands.Common.Storage.ResourceModel.AzureStorageBlob]$Blob)
 
    # Base + blob name
    $blobSizeInBytes = 124 + $Blob.Name.Length * 2
 
    # Get size of metadata
    $metadataEnumerator = $Blob.ICloudBlob.Metadata.GetEnumerator()
    while ($metadataEnumerator.MoveNext())
    {
        $blobSizeInBytes += 3 + $metadataEnumerator.Current.Key.Length + $metadataEnumerator.Current.Value.Length
    }
 
    if ($Blob.BlobType -eq [Microsoft.WindowsAzure.Storage.Blob.BlobType]::BlockBlob)
    {
        $blobSizeInBytes += 8
        $Blob.ICloudBlob.DownloadBlockList() | 
            ForEach-Object { $blobSizeInBytes += $_.Length + $_.Name.Length }
    }
    else
    {
        [int64]$rangeSize = 1GB
        [int64]$start = 0; $pages = "Start";
        
        While ($pages)
        {
            try
            {
                $pages = $Blob.ICloudBlob.GetPageRanges($start, $rangeSize)
            }
            catch
            {
                if ($_ -like "*the range specified is invalid*")
                {
                    $pages = $null
                    break
                }
                else
                {
                    write-error $_
                }
            }
            $pages | ForEach-Object { $blobSizeInBytes += 12 + $_.EndOffset - $_.StartOffset }
            $start += $rangeSize
        }
    }
    return @{"vhdlength" = "{0:F2}" -f ($blob.Length / 1GB) -replace ","; "usedsize" = "{0:F2}" -f ($blobSizeInBytes / 1GB) -replace ","}
} 

function Get-VMCoresandMemory 
{
    param (
        [Parameter(Mandatory=$true)]
        [String]$VMSize)

    $csvfile = $env:USERPROFILE+"\Downloads\VMsizes.csv"
    $VMSizeList = Import-Csv $csvfile 
    $VMcoresmem = $VMSizeList | where {$_.Size -eq $VMSize}

    return @{"Cores" = $VMcoresmem.Cores; "Memory" = $vmcoresmem.Memory}
}

Import-Module AzureRM.Compute
$PSversion = (Get-Module -Name AzureRM.Compute).Version

If($PSversion -lt [System.Version]"2.6.0")
{
    Write-Host "PowerShell Version too low. Visit https://docs.microsoft.com/en-us/powershell/azure/overview to install the newest AzureRM module.";
    Exit
}

Login-AzureRmAccount -Environment azurechinacloud 
Get-AzureRmSubscription -SubscriptionId $SubscriptionID
Select-AzureRmSubscription -SubscriptionId $SubscriptionID

$armfile = $env:USERPROFILE+"\Downloads\armvms-"+$subscriptionID+".csv"
Set-Content $armfile -Value "ResourceGroup,VMName,VMSize,Cores,Memory(GB),DiskName,OSorData,VHDUri,StorageAccount,VHDLength,VHDUsedSize"

Write-Verbose "ARM part starts!"

$armvms = Get-AzureRmVM

foreach($armvm in $armvms) 
{
    $vmresourcegroup = $armvm.ResourceGroupName
    $vmname = $armvm.Name
    $vmsize = $armvm.HardwareProfile.VmSize
    $vmcores = (Get-VMCoresandMemory -VMSize $vmsize).Cores
    $vmmemory = (Get-VMCoresandMemory -VMSize $vmsize).Memory

    $vmosdiskname = $armvm.StorageProfile.OsDisk.Name
    If ($armvm.StorageProfile.OsDisk.vhd -eq $null) 
    {
        Write-Host ("The VM "+$vmname+" is using Managed Disks.")
        $vmosdisksize = (Get-AzureRmDisk -ResourceGroupName $vmresourcegroup -DiskName $vmosdiskname).DiskSizeGB
        Add-Content $armfile -Value ($vmresourcegroup+","+$vmname+","+$vmsize+","+$vmosdiskname+",OSDisk,No visible VHD files for Managed Disk VM,No visible storage account for Managed Disk VM,"+$vmosdisksize+",Managed Disks don't support GetBlobSize method")
        $datadisks = $armvm.StorageProfile.DataDisks
        If ($datadisks.Count -eq 0) 
        {
            Write-Host ("The VM "+$vmname+" contains no data disk.")
        } 
        else 
        {
            Write-Host ("The VM "+$vmname+" contains "+$datadisks.Count+" data disk(s).")
            foreach ($datadisk in $datadisks) 
            {
                $vmdatadiskname = $datadisk.Name
                $vmdatadisksize = (Get-AzureRmDisk -ResourceGroupName $vmresourcegroup -DiskName $vmdatadiskname).DiskSizeGB
                Add-Content $armfile -Value ($vmresourcegroup+","+$vmname+","+$vmsize+","+$vmcores+","+$vmmemory+","+$vmdatadiskname+",DataDisk,No visible VHD files for Managed Disk VM,No visible storage account for Managed Disk VM,"+$vmdatadisksize+",Managed Disks don't support GetBlobSize method")
            }
        }
    } 
    else 
    {
        $vmosdiskuri = $armvm.StorageProfile.OsDisk.Vhd.Uri
        $vmosdiskstorageaccountname = ($armvm.StorageProfile.OsDisk.Vhd.Uri.Split("{/,.}"))[2]
        $vmosdiskstorageaccountkey = (Get-AzureRmStorageAccount | ? {$_.StorageAccountName -eq $vmosdiskstorageaccountname} | Get-AzureRmStorageAccountKey)[0].Value
        $vmosdiskstorageaccountcontext = New-AzureStorageContext -StorageAccountName $vmosdiskstorageaccountname -StorageAccountKey $vmosdiskstorageaccountkey
        $vmosdiskcontainername = ($armvm.StorageProfile.OsDisk.Vhd.Uri.Split("/")[3])
        $vmosdiskblobname = ($armvm.StorageProfile.OsDisk.Vhd.Uri.Split("/")[(($armvm.StorageProfile.OsDisk.Vhd.Uri.Split("/")).count) - 1])
        $vmosdiskblob = Get-AzureStorageBlob -Context $vmosdiskstorageaccountcontext -blob $vmosdiskblobname -Container $vmosdiskcontainername

        $osvhdsize = Get-BlobBytes $vmosdiskblob

        If ($vmsize -like "*DS*") 
        {
            Add-Content $armfile -Value ($vmresourcegroup+","+$vmname+","+$vmsize+","+$vmcores+","+$vmmemory+","+$vmosdiskname+",OSDisk,"+$vmosdiskuri+","+$vmosdiskstorageaccountname+","+$osvhdsize+",Premium Disks don't support GetBlobSize method")
        } 
        else 
        {
            Add-Content $armfile -Value ($vmresourcegroup+","+$vmname+","+$vmsize+","+$vmcores+","+$vmmemory+","+$vmosdiskname+",OSDisk,"+$vmosdiskuri+","+$vmosdiskstorageaccountname+","+$osvhdsize.vhdlength+","+$osvhdsize.usedsize)
        }

        $datadisks = $armvm.StorageProfile.DataDisks
        If ($datadisks.count -eq 0) 
        {
            Write-Host ("The VM "+$vmname+" contains no data disk.")
        } 
        else 
        {
            Write-Host ("The VM "+$vmname+" contains "+$datadisks.Count+" data disk(s).")
            foreach ($datadisk in $datadisks) 
            {
                $vmdatadiskname = $datadisk.Name
                $vmdatadiskuri = $datadisk.Vhd.Uri
                $vmdatadiskstorageaccountname = ($vmdatadiskuri.Split("{/,.}"))[2]
                $vmdatadiskstorageaccountkey = (Get-AzureRmStorageAccount | ? {$_.StorageAccountName -eq $vmdatadiskstorageaccountname} | Get-AzureRmStorageAccountKey)[0].Value
                $vmdatadiskstorageaccountcontext = New-AzureStorageContext -StorageAccountName $vmdatadiskstorageaccountname -StorageAccountKey $vmdatadiskstorageaccountkey
                $vmdatadiskcontainername = ($datadisk.Vhd.Uri.Split("/")[3])
                $vmdatadiskblobname = ($datadisk.Vhd.Uri.Split("/")[(($datadisk.Vhd.Uri.Split("/")).count) - 1])
                $vmdatadiskblob = Get-AzureStorageBlob -Context $vmdatadiskstorageaccountcontext -blob $vmdatadiskblobname -Container $vmdatadiskcontainername

                $datavhdsize = Get-BlobBytes $vmdatadiskblob
                
                If ($vmsize -like "*DS*") 
                {
                    Add-Content $armfile -Value (",,,,,"+$vmdatadiskname+",DataDisk,"+$vmdatadiskuri+","+$vmdatadiskstorageaccountname+","+$datavhdsize.vhdlength+",Premium Disks don't support GetBlobSize method")
                } 
                else 
                {
                    Add-Content $armfile -Value (",,,,,"+$vmdatadiskname+",DataDisk,"+$vmdatadiskuri+","+$vmdatadiskstorageaccountname+","+$datavhdsize.vhdlength+","+$datavhdsize.usedsize)
                }
            }
        }
    }
}
Write-Verbose "ARM part finished!"


