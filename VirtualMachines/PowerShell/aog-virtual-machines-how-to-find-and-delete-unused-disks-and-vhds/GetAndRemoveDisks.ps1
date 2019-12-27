Function Get-UnusedDisks()
{
    $disks = Get-AzureRmDisk;
    foreach ($disk in $disks)
    {
        If($disk.OwnerId -eq $Null)
        {
            Write-Host "ResourceGroupName: " $disk.ResourceGroupName "UnusedDiskName:" $disk.Name;
        }
    }
}

Function Remove-UnusedDisks()
{
    #����һ������ $ResourceGroupName ����ָ����Ҫɾ�����̵���Դ��
    #����һ������ $DiskNames������ָ����Ҫɾ���Ĵ�������
    #����һ������ $UnDeleteDisksNames������ָ������Ҫɾ���Ĵ�������
    Param($ResourceGroupName, [string[]]$DiskNames, [String[]]$UnDeleteDiskNames);

    #���ڴ洢���ҳ����Ĵ���
    $disks = New-Object System.Collections.ArrayList;
    If($ResourceGroupName -eq $Null)
    {
        #���δָ����Դ�飬����ҵ�ǰ�����еĴ���
        $disks = Get-AzureRmDisk;
    }
    Else
    {
        #�����ض���Դ���еĴ���
        $disks = Get-AzureRmDisk -ResourceGroupName $ResourceGroupName;
    }
    Foreach($disk in $disks)
    {
        If($UnDeleteDiskNames -ccontains($disk.Name))
        {
            #��������
        }
        else{
            if($DiskNames.Count -ne 0)
            {
                if($DiskNames -ccontains($disk.Name))
                {
                    #��� OwnerId Ϊ�գ�������ǰ����δ��ʹ��
                    If($disk.OwnerId -eq $Null)
                    {
                        #�������Ƿ�����
                        $lockId = (Get-AzureRmResourceLock -ResourceGroupName $disk.ResourceGroupName -ResourceName $disk.Name -ResourceType Microsoft.Compute/disks).LockId;
                        if($lockId -ne $Null)
                        {
                            #����
                            Remove-AzureRmResourceLock -LockId $lockId -Force;
                            Write-Host "Already remove lock on disk: " $disk.Name;
                        }
                        Remove-AzureRmDisk -ResourceGroupName $disk.ResourceGroupName -DiskName $disk.Name -Force;
                        Write-Host "Already delete disk, disk location: ResourceGroupName: " $disk.ResourceGroupName "UnusedDiskName:" $disk.Name;
                    }
                }
            }
            else
            {
                 #��� DisksName Ϊ�գ�ɾ������������������δʹ�õĴ��̡�
                If($disk.OwnerId -eq $Null)
                {
                    #�������Ƿ�����
                    $lockId = (Get-AzureRmResourceLock -ResourceGroupName $disk.ResourceGroupName -ResourceName $disk.Name -ResourceType Microsoft.Compute/disks).LockId;
                    if($lockId -ne $Null)
                    {
                        #����
                        Remove-AzureRmResourceLock -LockId $lockId -Force;
                        Write-Host "Already remove lock on disk: " $disk.Name;
                    }
                    Remove-AzureRmDisk -ResourceGroupName $disk.ResourceGroupName -DiskName $disk.Name -Force;
                    Write-Host "Already delete disk, disk location: ResourceGroupName: " $disk.ResourceGroupName "UnusedDiskName:" $disk.Name;
                }
            }
        }
    }
}