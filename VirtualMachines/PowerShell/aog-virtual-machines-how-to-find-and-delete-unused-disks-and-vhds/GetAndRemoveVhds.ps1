# ��ȡδ��ʹ�õ�Դ .vhd �ļ�
Function Get-UnusedVHDs()
{
    #ResourceGroupName:��Դ�����ƣ�AccountName���洢�˻����ƣ�Container��Blob��������
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
        #����Ĭ������
        $Container = "vhds";
    }

    #�������еĴ洢�˻����� vhds �����и��� Blob ���ļ����� Url ���ҳ�δ��ʹ�õ� VHD��
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
                    #���� Blob �������жϵ�ǰԴ .vhd �ļ��Ƿ�����
                    if($blob.ICloudBlob.Properties.LeaseState -eq "Available")
                    {
                        if($blob.ICloudBlob.Properties.LeaseStatus -eq "Locked")
                        {
                            Write-Host "Locked vhd, Storage Account: " $storageAccountName "�� Container Name: " $Container " Blob Name: " $blob.Name " URL: " $url;
                        }
                        else
                        {
                            Write-Host "UnLocked vhd, Storage Account: " $storageAccountName "�� Container Name: " $Container " Blob Name: " $blob.Name " URL: " $url;
                        }
                    }
                }
            }
        }
    }
}

# ɾ��δʹ�õ�Դ .vhd �ļ�
Function Remove-UnusedVHDs()
{
    # ResourceGroupName:��Դ������
    # AccountName: �洢�˻�����
    # Container: Blob ��������
    # VhdNames: Ҫɾ����Դ .vhd �ļ�
    # ReservedVhds����Ҫ������Դ .vhd �ļ�
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
        #����Ĭ������
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
        #�������еĴ洢�˻����� vhds �����и��� Blob ���ļ����� Url ���ҳ�δ��ʹ�õ� VHD��
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
                        #���� Blob �������жϵ�ǰԴ .vhd �ļ� �Ƿ�����
                        if($blob.ICloudBlob.Properties.LeaseState -eq "Available")
                        {
                            if($ReservedVhds -ccontains($blob.Name))
                            {
                                #����Դ .vhd �ļ�
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