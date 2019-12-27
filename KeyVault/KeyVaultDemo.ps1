# scripts to
# 1. create key vault
# 2. store connection string into key vault
# 3. register app and authorize permission

# TODO: update below settings based on your environment
$subscriptionId = ""
$keyVaultName = ""
$keyVaultResourceGroupName = ""
$location = "" # China East, China North
$appName = ""
$appKey = "" # password you want to set
$appUri = "" # such as http://localhost:7880
$secretName = "" # secret name you want to save
$stConnStr = "" # secret you want to save

Login-AzureRmAccount -EnvironmentName AzureChinaCloud

# In case the account has multiple subscription, make sure the expected subscription is selected
$selectedSub = Select-AzureRmSubscription -SubscriptionId $subscriptionId
$tenantId = $selectedSub.Tenant.TenantId

New-AzureRmResourceGroup -Name $keyVaultResourceGroupName -Location $location

# If you see the error The subscription is not registered to use namespace 'Microsoft.KeyVault' when you try to create your new key vault
# Run Register-AzureRmResourceProvider -ProviderNamespace "Microsoft.KeyVault, and then rerun your New-AzureRmKeyVault command
New-AzureRmKeyVault -VaultName $keyVaultName -ResourceGroupName $keyVaultResourceGroupName -Location $location

$secretValue = ConvertTo-SecureString $stConnStr -AsPlainText -Force
$stConnStrSecret = Set-AzureKeyVaultSecret -VaultName $keyVaultName -Name $secretName -SecretValue $secretValue

# Register your app to Azure AD now
# And get its client id and client key
$app = New-AzureRmADApplication -DisplayName $appName -HomePage $appUri -IdentifierUris $appUri -Password $appKey
$appSP = New-AzureRmADServicePrincipal -ApplicationId $app.ApplicationId

# Authorize the app to use the secret
Set-AzureRmKeyVaultAccessPolicy -VaultName $keyVaultName -ServicePrincipalName $appSP.ApplicationId -PermissionsToSecrets get

Write-Host ("Target tenant ID is: {0}" -f $tenantId)
Write-Host ("Storage connection string key vault URI: {0}" -f $stConnStrSecret.Id)
Write-Host ("App Client Id is: {0}" -f $appSP.ApplicationId)
Write-Host ("App Client Key is: {0}" -f $appKey)