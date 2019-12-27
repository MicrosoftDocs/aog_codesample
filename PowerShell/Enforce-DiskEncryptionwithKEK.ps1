function Enforce-DiskEncryptionwithKEK
{
 <#
 .Synopsis
    Enforce-DiskEncryptionwithKEK
 .DESCRIPTION
    Enforce-DiskEncryptionwithKEK
 
 .PARAMETER ResourceGroupName
 Specifies the name of a resource group.
 
 .PARAMETER VaultName
 Specifies the name of existing key vault.
 
 .PARAMETER appName
 Specifies the name of aad application.
 
 .PARAMETER aadClientSecret
 Specifies the secret to create the aad application.
 
 .PARAMETER vmname
 Specifies the name of the VM to be encrypted.
 
 .PARAMETER KEKName
 Specifies the name of key encryption key.
 
 .EXAMPLE
    Enforce-DiskEncryptionwithKEK -RGName resourcegroup -VaultName keyvault -appName "con636 252" -aadClientSecret "password" -vmname vmtest -KEKName testKEK

    Getting Key Vault...
    Creating AAD APP...
    Application with Uri https:// con636252 .com already exists!
    Setting Key Vault policy...
    Enforcing ADE... May take several minutes...
 
    RequestId IsSuccessStatusCode StatusCode ReasonPhrase
    --------- ------------------- ---------- ------------
                             True         OK OK          

    This command enforces azure disk encryption for the VM.
 
 .NOTES
     Microsoft verson 1, 6/20/2017
 
 #>
    param
    (
         [Parameter(Mandatory=$true)]
         $RGName,
         
         [Parameter(Mandatory=$true)]
         $VaultName,
 
         [Parameter(Mandatory=$true)]
         $appName,
 
         [Parameter(Mandatory=$true)]
         $aadClientSecret,
 
         [Parameter(Mandatory=$true)]
         $vmname,
 
         [Parameter(Mandatory=$true)]
         $KEKName
 
    )
    Write-Host " "
    Write-Host "Getting Key Vault..."
    Try 
    {
        $KeyVault = Get-AzureRmKeyVault -VaultName $VaultName -ResourceGroupName $RGName -ErrorAction Stop
    } 
    Catch 
    {
        Write-Host "KeyVault"$VaultName" in Resource Group "$RGName" does not exist!"
        Break
    }
    Write-Host "Creating AAD APP..."
    If ((Get-AzureRmADApplication -IdentifierUri ("https://"+$appname.replace(' ','')+".com")) -eq $null) 
    {
        $azureAdApplication = New-AzureRmADApplication -DisplayName $appname -HomePage ("https://"+$appname.replace(' ','')+".com") -IdentifierUris ("https://"+$appname.replace(' ','')+".com") -Password $aadClientSecret
        $AADClientID = $azureAdApplication.ApplicationId
        $servicePrincipal = New-AzureRmADServicePrincipal –ApplicationId $AADClientID
    } 
    Else 
    {
        Write-Host "Application with Uri https://"$appname.replace(' ','')".com already exists, leveraging the existing application..."
        $azureAdApplication = Get-AzureRmADApplication -IdentifierUri ("https://"+$appname.replace(' ','')+".com")
        $AADClientID = $azureAdApplication.ApplicationId
        $servicePrincipal = Get-AzureRmADServicePrincipal -ServicePrincipalName ("https://"+$appname.replace(' ','')+".com")
    }
     
    $diskEncryptionKeyVaultUrl = $KeyVault.VaultUri
    $KeyVaultResourceId = $KeyVault.ResourceId
    Write-Host "Setting Key Vault policy..."
    Set-AzureRmKeyVaultAccessPolicy -VaultName $VaultName -ServicePrincipalName $AADClientID -PermissionsToKeys all -PermissionsToSecrets all -ResourceGroupName $RGName
    Set-AzureRmKeyVaultAccessPolicy -VaultName $VaultName -ResourceGroupName $RGName –EnabledForDiskEncryption
 
    $KEK = Add-AzureKeyVaultKey -VaultName $VaultName -Name $KEKName -Destination "Software"
    $KeyEncryptionKeyUrl = $KEK.Key.kid
    Write-Host "Enforcing ADE... May take several minutes..."
    Set-AzureRmVMDiskEncryptionExtension -ResourceGroupName $RGName -VMName $vmname -AadClientID $AADClientID -AadClientSecret $AADClientSecret -DiskEncryptionKeyVaultUrl $DiskEncryptionKeyVaultUrl -DiskEncryptionKeyVaultId $KeyVaultResourceId -KeyEncryptionKeyUrl $KeyEncryptionKeyUrl -KeyEncryptionKeyVaultId $KeyVaultResourceId 
}

