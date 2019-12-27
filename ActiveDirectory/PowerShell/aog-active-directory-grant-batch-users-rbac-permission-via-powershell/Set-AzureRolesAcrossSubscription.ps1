$cred = Get-Credential -Message "Input Admin Username & Password"
Add-AzureAccount -Environment azurechinacloud -Credential $cred
Login-AzureRmAccount -Environment azurechinacloud -Credential $cred

$path = $env:userprofile+"\Downloads\userlist.csv"
$userlist = Import-Csv -Path $path
foreach ($principal in $userlist) {
    $result = $null
    try{
        $subscription = Select-AzureRmSubscription -SubscriptionId $principal.Subid -ErrorAction Stop
    } Catch [System.ArgumentException] {
        Write-Host "Your admin account cannot access subscription" $principal.Subid "."
    }
    
    if ($principal.UserOrApp -like "user") {
        if (!$principal.resourcegroup) {
            try {
                $result = New-AzureRmRoleAssignment -SignInName $principal.UPN -RoleDefinitionName $principal.rolename -ErrorAction Stop
            } catch [System.Collections.Generic.KeyNotFoundException] {
                Write-Host $($_.Exception.Message) -ForegroundColor Red
            } catch [Microsoft.Rest.Azure.CloudException] {
                Write-Host $($_.Exception.Message) -ForegroundColor Red
            }
        } else {
            try {
                $result = New-AzureRmRoleAssignment -SignInName $principal.UPN -RoleDefinitionName $principal.rolename -ResourceGroupName $principal.resourcegroup -ErrorAction Stop
            } catch [System.Collections.Generic.KeyNotFoundException] {
                Write-Host $($_.Exception.Message) -ForegroundColor Red
            } catch [Microsoft.Rest.Azure.CloudException] {
                Write-Host $($_.Exception.Message) -ForegroundColor Red
            }
        }
    } elseif ($principal.UserOrApp -like "app") {
        $app = Get-AzureRmADApplication -ObjectId $principal.AppObjectID
        $servicep = Get-AzureRmADServicePrincipal -ServicePrincipalName $app.ApplicationId
        if ($servicep -ne $null) {
            if (!$principal.resourcegroup) {
                try {
                    $scope = "/subscriptions/"+$principal.Subid
                    $result = New-AzureRmRoleAssignment -ObjectId $servicep.Id -RoleDefinitionName $principal.rolename -Scope $scope -ErrorAction Stop
                } catch [System.Collections.Generic.KeyNotFoundException] {
                    Write-Host $($_.Exception.Message) -ForegroundColor Red
                } catch [Microsoft.Rest.Azure.CloudException] {
                    Write-Host $($_.Exception.Message) -ForegroundColor Red
                } catch [Hyak.Common.CloudException] {
                    Write-Host $($_.Exception.Message) -ForegroundColor Red
                }
            } else {
                try {
                    $result = New-AzureRmRoleAssignment -ObjectId $servicep.Id -RoleDefinitionName $principal.rolename -ResourceGroupName $principal.resourcegroup -ErrorAction Stop
                } catch [System.Collections.Generic.KeyNotFoundException] {
                    Write-Host $($_.Exception.Message) -ForegroundColor Red
                } catch [Microsoft.Rest.Azure.CloudException] {
                    Write-Host $($_.Exception.Message) -ForegroundColor Red
                } catch [Hyak.Common.CloudException] {
                    Write-Host $($_.Exception.Message) -ForegroundColor Red
                }
            }
        } else {
            Write-Host "Unable to find the service principal for application"$app.ApplicationId -ForegroundColor Red
        }
    } else {
        Write-Host "Principal type missing for" $principal.UPN $principal.AppObjectID -ForegroundColor Red
    }

    if ($result.Scope -ne $null) {
        Write-Host "Principal" $result.DisplayName "has been granted" $result.RoleDefinitionName "over scope" $result.Scope "."
    }
}

