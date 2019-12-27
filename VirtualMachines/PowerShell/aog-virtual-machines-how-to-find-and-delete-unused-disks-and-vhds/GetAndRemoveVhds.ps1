# 获取未被使用的源 .vhd 文件
Function Get-UnusedVHDs()
{
    #ResourceGroupName:资源组名称；AccountName：存储账户名称；Container：Blob容器名称
    Param($ResourceGroupName, $AccountName, $Container);
    $storages = New-Object System.Collections.ArrayList; 
    if(($ResourceGroupName -ne $Null) -and ($AccountName -ne $Null))
    {
        $storages = Get-AzureRmStorageAccount -ResourceGroupName $ResourceGroupName -AccountName $AccountName -WarningAction Ignore;
    }
    elseif(($ResourceGroupName -ne $Null) -and ($AccountName -eq $Null))
    {
        $storages = Get-AzureRmStorageAccount -ResourceGroupName $ResourceGroupName -WarningAction Ignore;
    }
    else
    {
        $storages = Get-AzureRmStorageAccount -WarningAction Ignore;
    }

    if($Container -eq $Null)
    {
        #设置默认容器
        $Container = "vhds";
    }

    #遍历所有的存储账户，在 vhds 容器中根据 Blob 的文件名和 Url 查找出未被使用的 VHD。
    Foreach ($storage in $storages)
    {
        $storageAccountName = $storage.StorageAccountName;
        $blobs = Get-AzureStorageBlob -Context $storage.Context -Container $Container -ErrorAction Ignore;

        foreach ($blob in $blobs)
        {
            if ($blob.BlobType -eq "PageBlob")
            {
                $url = $blob.ICloudBlob.Uri.AbsoluteUri;
                if ($url.EndsWith(".vhd"))
                {
                    #根据 Blob 的属性判断当前源 .vhd 文件是否被租用
                    if($blob.ICloudBlob.Properties.LeaseState -eq "Available")
                    {
                        if($blob.ICloudBlob.Properties.LeaseStatus -eq "Locked")
                        {
                            Write-Host "Locked vhd, Storage Account: " $storageAccountName "， Container Name: " $Container " Blob Name: " $blob.Name " URL: " $url;
                        }
                        else
                        {
                            Write-Host "UnLocked vhd, Storage Account: " $storageAccountName "， Container Name: " $Container " Blob Name: " $blob.Name " URL: " $url;
                        }
                    }
                }
            }
        }
    }
}

# 删除未使用的源 .vhd 文件
Function Remove-UnusedVHDs()
{
    # ResourceGroupName:资源组名称
    # AccountName: 存储账户名称
    # Container: Blob 容器名称
    # VhdNames: 要删除的源 .vhd 文件
    # ReservedVhds：需要保留的源 .vhd 文件
    Param($ResourceGroupName, $AccountName, $Container, [String[]]$VhdNames, [String[]]$ReservedVhds);
    $storages = New-Object System.Collections.ArrayList; 
    if(($ResourceGroupName -ne $Null) -and ($AccountName -ne $Null))
    {
        $storages = Get-AzureRmStorageAccount -ResourceGroupName $ResourceGroupName -AccountName $AccountName -WarningAction Ignore;
    }
    elseif(($ResourceGroupName -ne $Null) -and ($AccountName -eq $Null))
    {
        $storages = Get-AzureRmStorageAccount -ResourceGroupName $ResourceGroupName -WarningAction Ignore;
    }
    else
    {
        $storages = Get-AzureRmStorageAccount -WarningAction Ignore;
    }

    if($Container -eq $Null)
    {
        #设置默认容器
        $Container = "vhds";
    }

    if($VhdNames -ne $Null)
    {
        foreach($vhd in $VhdNames)
        {
            foreach ($storage in $storages)
            {
                $blob = Get-AzureStorageBlob -Context $storage.Context -Container $Container -Blob $vhd -ErrorAction Ignore;
                if($blob -ne $Null)
                {
                    if($blob.ICloudBlob.Properties.LeaseStatus -eq "Locked")
                    {
                        $blob.ICloudBlob.BreakLease();
                        Write-Host "Successfully break lease on blob: " $blob.Name " URL: " $url;
                    }
                    Remove-AzureStorageBlob -Context $storage.Context -Container $Container -Blob $vhd -Force;
                    Write-Host "Already delete blob: " $vhd;  
                }
            }
        }
    }
    else
    {
        #遍历所有的存储账户，在 vhds 容器中根据 Blob 的文件名和 Url 查找出未被使用的 VHD。
        Foreach ($storage in $storages)
        {
            $blobs = Get-AzureStorageBlob -Context $storage.Context -Container $Container -ErrorAction Ignore;

            foreach ($blob in $blobs)
            {
                if ($blob.BlobType -eq "PageBlob")
                {
                    $url = $blob.ICloudBlob.Uri.AbsoluteUri;
                    if ($url.EndsWith(".vhd"))
                    {
                        #根据 Blob 的属性判断当前源 .vhd 文件 是否被租用
                        if($blob.ICloudBlob.Properties.LeaseState -eq "Available")
                        {
                            if($ReservedVhds -ccontains($blob.Name))
                            {
                                #保留源 .vhd 文件
                            }
                            else
                            {
                                if($blob.ICloudBlob.Properties.LeaseStatus -eq "Locked")
                                {
                                    $blob.ICloudBlob.BreakLease();
                                    Write-Host "Successfully break lease on blob: " $blob.Name " URL: " $url;
                                }
                                
                                Remove-AzureStorageBlob -Context $storage.Context -Container $Container -Blob $blob.Name -Force;
                                Write-Host "Already delete blob: " $blob.Name " URL: " $url;                            
                            }
                        }
                    }
                }
            }
        }
    }
}