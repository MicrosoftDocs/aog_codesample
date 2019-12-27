#------------------------------------------------------------------------------
#
# Copyright ?2017 Microsoft Corporation.  All rights reserved.
#
# THIS CODE AND ANY ASSOCIATED INFORMATION ARE PROVIDED “AS IS” WITHOUT
# WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
# LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
# FOR A PARTICULAR PURPOSE. THE ENTIRE RISK OF USE, INABILITY TO USE, OR 
# RESULTS FROM THE USE OF THIS CODE REMAINS WITH THE USER.
#
#----------------------------------------------------------------------------- 
 
<#
.SYNOPSIS
  Connects to Azure China Storage Account and export specified table to CSV file.

.DESCRIPTION
  This Script connects to Azure China Storage account and export specified table to CSV file in your defined path.  

.EXAMPLE
./ExportStorageAccountTableToCsvWithParam.ps1 -StorageAccountName <StorageAccountName> -StorageAccountKey <key> -TableName <tablename> -Path <path>

.PARAMETER StorageAccountName
   Required.
   A valid China Storage Account Name where stores your table.

.PARAMETER StorageAccountKey
   Required.
   A Valid storage account key used to authenticate you to access to the storage account.

.PARAMETER TableName
   Required.
   The Table Name that you want to export.

.PARAMETER Path
   Required.
   A local folder path that you store the exported data in.

.NOTES
   AUTHOR: CSS  
   LASTEDIT: Nov 3, 2017
#>

param(
[string]$StorageAccountName=$(throw "Parameter missing: -StorageAccountName Name") ,
[string]$StorageAccountKey=$(throw "Parameter missing: -StorageAccountKey Key") ,
[string]$TableName=$(throw "Parameter missing: -TableName Name") ,
[string]$Path=$(throw "Parameter missing: -Path Path") 
)

$Creds = New-Object Microsoft.WindowsAzure.Storage.Auth.StorageCredentials("$StorageAccountName","$StorageAccountKey") 
$CloudStorageAccount = New-Object Microsoft.WindowsAzure.Storage.CloudStorageAccount($Creds, "core.chinacloudapi.cn",$true) 
$CloudTableClient = $CloudStorageAccount.CreateCloudTableClient() 
$Table = $CloudTableClient.GetTableReference($TableName) 
 
$Query = New-Object "Microsoft.WindowsAzure.Storage.Table.TableQuery" 
$Datas = $Table.ExecuteQuery($Query) 
                         
$ExportObjs = @() 
                         
Foreach($Data in $Datas) 
{ 
   
    $Obj = New-Object PSObject 
 
    $Obj | Add-Member -Name PartitionKey -Value $Data.PartitionKey -MemberType NoteProperty 
    $Obj | Add-Member -Name RowKey -Value $Data.RowKey -MemberType NoteProperty  
 
    $Data.Properties.Keys | Foreach{$Value = $data.Properties[$_].PropertyAsObject; 
    $Obj | Add-Member -Name $_ -Value $value -MemberType NoteProperty; } 
 
    $ExportObjs += $Obj 
}  
 
#Export the entities of table storage to csv file.  
$ExportObjs | Export-Csv "$Path\$TableName.csv" -NoTypeInformation 
Write-Host "Successfully exported the table storage to csv file." 