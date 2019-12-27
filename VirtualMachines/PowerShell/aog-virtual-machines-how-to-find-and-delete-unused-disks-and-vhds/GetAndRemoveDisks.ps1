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
    #定义一个参数 $ResourceGroupName 用于指定需要删除磁盘的资源组
    #定义一个数组 $DiskNames，用于指定需要删除的磁盘名称
    #定义一个数组 $UnDeleteDisksNames，用于指定不需要删除的磁盘名称
    Param($ResourceGroupName, [string[]]$DiskNames, [String[]]$UnDeleteDiskNames);

    #用于存储查找出来的磁盘
    $disks = New-Object System.Collections.ArrayList;
    If($ResourceGroupName -eq $Null)
    {
        #如果未指定资源组，则查找当前订阅中的磁盘
        $disks = Get-AzureRmDisk;
    }
    Else
    {
        #查找特定资源组中的磁盘
        $disks = Get-AzureRmDisk -ResourceGroupName $ResourceGroupName;
    }
    Foreach($disk in $disks)
    {
        If($UnDeleteDiskNames -ccontains($disk.Name))
        {
            #保留磁盘
        }
        else{
            if($DiskNames.Count -ne 0)
            {
                if($DiskNames -ccontains($disk.Name))
                {
                    #如果 OwnerId 为空，表明当前磁盘未被使用
                    If($disk.OwnerId -eq $Null)
                    {
                        #检查磁盘是否被锁定
                        $lockId = (Get-AzureRmResourceLock -ResourceGroupName $disk.ResourceGroupName -ResourceName $disk.Name -ResourceType Microsoft.Compute/disks).LockId;
                        if($lockId -ne $Null)
                        {
                            #解锁
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
                 #如果 DisksName 为空，删除除保留磁盘外其他未使用的磁盘。
                If($disk.OwnerId -eq $Null)
                {
                    #检查磁盘是否被锁定
                    $lockId = (Get-AzureRmResourceLock -ResourceGroupName $disk.ResourceGroupName -ResourceName $disk.Name -ResourceType Microsoft.Compute/disks).LockId;
                    if($lockId -ne $Null)
                    {
                        #解锁
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