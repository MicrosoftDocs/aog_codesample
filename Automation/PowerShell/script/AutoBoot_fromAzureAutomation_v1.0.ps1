InlineScript {
    <#
    .Disclaimer
    This sample code is provided for the purpose of illustration only and is not intended to be used in a production environment.  
    THIS SAMPLE CODE AND ANY RELATED INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.  
    We grant You a nonexclusive, royalty-free right to use and modify the Sample Code and to reproduce and distribute the object code form of the Sample Code, provided that You agree:
    (i)     to not use Our name, logo, or trademarks to market Your software product in which the Sample Code is embedded;
    (ii)	to include a valid copyright notice on Your software product in which the Sample Code is embedded; 
    (iii)	to indemnify, hold harmless, and defend Us and Our suppliers from and against any claims or lawsuits, including attorneys’ fees, that arise or result from the use or distribution of the Sample Code.
    #>

    $usercred = Get-AutomationPSCredential -Name 'credname'
    $loggedsub = Login-AzureRMAccount -Environment AzureChinaCloud -Credential $usercred
    $StorageSubscriptionId = 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'
    $selectedsub = Select-AzureRmSubscription -SubscriptionId $StorageSubscriptionId

    $StorageResourceGroup = 'chesharm'
    $StorageAccountName = 'chesharmstorage'
    $VMlistContainername = 'autoboot'
    $logcontainername = 'autobootlogs'
    $StorageAccountKey = Get-AzureRmStorageAccountKey -ResourceGroupName $StorageResourceGroup -Name $StorageAccountName
    $StorageContext = New-AzureStorageContext -StorageAccountName $StorageAccountName -StorageAccountKey $StorageAccountKey.Value[0] -Environment AzureChinaCloud

    $Date = Get-Date -format yyyy_MM_dd
    $VMlistFileName = $Date+"_vmtostart.csv"
    $CloudBlob = Get-AzureStorageBlob -Container $VMlistContainername -Blob $VMlistFileName -Context $StorageContext
    $logcontainer = Get-AzureStorageContainer -Name $logcontainername -Context $StorageContext
    $VMlist = $CloudBlob.ICloudBlob.DownloadText() -split [Environment]::NewLine
    $logfilename = $Date+"_vmstartlog.log"
    $logfile = $logcontainer.CloudBlobContainer.GetAppendBlobReference($logfilename)
    if (!$logfile.Exists()) {
        $logfile.CreateOrReplace();
    }

    $logoutput1 = (Get-Date).ToUniversalTime().ToString() + " UTC  Info: AutoBoot script started..."
    Write-Output $logoutput1
    $logfile.AppendText([String]::Format("{0}",$logoutput1))

    for ($i = 1; $i -lt $VMlist.Count; $i++){
        $VMitem = $VMlist[$i]
        $VMresourcegroup = $VMitem.split(',')[0]
        $VMname = $VMitem.split(',')[1]
        $VMsubscriptionid = $VMitem.split(',')[2]
        Try {
            $selectedsub = Select-AzureRmSubscription -SubscriptionId $VMsubscriptionid -ErrorAction Stop
        } Catch [System.ArgumentException]  {
            $logoutput0 = (Get-Date).ToUniversalTime().ToString()+" UTC  Warning: You don't have access to subscription "+$VMsubscriptionid+" or it does not exist."
            Write-Output $logoutput0
            $logfile.AppendText([String]::Format("`n{0}",$logoutput0))
        }
        $ARMVM = Get-AzureRmVM -ResourceGroupName $VMresourcegroup -Name $VMname -Status -ErrorAction SilentlyContinue

        if ($ARMVM -eq $null) {
            $logoutput2 = (Get-Date).ToUniversalTime().ToString()+" UTC  Error: Cannot find VM "+$VMname+" in Resource Group "+$VMresourcegroup+", Subscription "+$VMsubscriptionid+"!"
            Write-Output $logoutput2
            $logfile.AppendText([String]::Format("`n{0}",$logoutput2))
        } else {
            if ($ARMVM.Statuses[1].Code -eq 'PowerState/running'){
                $logoutput3 = (Get-Date).ToUniversalTime().ToString()+" UTC  Info: VM "+$VMname+" in Resource Group "+$VMresourcegroup+" is already running in Subscription "+$VMsubscriptionid+"."
                Write-Output $logoutput3
                $logfile.AppendText([String]::Format("`n{0}",$logoutput3))
            } else {
                Try {
                    $logoutput4 = (Get-Date).ToUniversalTime().ToString()+" UTC  Info: Trying to start VM "+$VMname+" in Resource Group "+$VMresourcegroup+", Subscription "+$VMsubscriptionid+"..."
                    Write-Output $logoutput4
                    $logfile.AppendText([String]::Format("`n{0}",$logoutput4))
                    $result = Start-AzureRmVM -ResourceGroupName $VMresourcegroup -Name $VMname -ErrorAction Stop
                } Catch [Microsoft.Azure.Commands.Compute.Common.ComputeCloudException]{
                    Write-Output "Error: Caught an exception"
                    $logfile.AppendText([String]::Format("`n{0}","Error: Caught an exception"))
                    Write-Output "Exception Message: $($_.Exception.Message)"   
                    $logfile.AppendText([String]::Format("`n{0}","Exception Message: $($_.Exception.Message)"))
                } Finally {
                    $ARMVMafterboot = Get-AzureRmVM -ResourceGroupName $VMresourcegroup -Name $VMname -Status -ErrorAction SilentlyContinue
                    if ($ARMVMafterboot.Statuses[1].Code -eq 'PowerState/running'){
                        $logoutput5 = (Get-Date).ToUniversalTime().ToString()+" UTC  Info: VM "+$VMname+" in Resource Group "+$VMresourcegroup+" has been booted up, Subscription "+$VMsubscriptionid+"."
                        Write-Output $logoutput5
                        $logfile.AppendText([String]::Format("`n{0}",$logoutput5))
                    }
                }
            }
        }
    }
}