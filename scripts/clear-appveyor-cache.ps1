#!/usr/bin/env pwsh

$appveyor_project = "oldratlee/transmittable-thread-local"

# https://stackoverflow.com/questions/24649019/how-to-use-confirm-in-powershell
$confirmation = Read-Host "Are you Sure You Want To Clear cache of appveyor project $appveyor_project [y/N]"
if ($confirmation -ne 'y')
{
    Write-Output "do nothing and exit"
    exit
}

# PowerShell: Run command from script's directory - Stack Overflow
# https://stackoverflow.com/questions/4724290
# $script_path = $MyInvocation.MyCommand.Path
# $script_dir = Split-Path $script_path

# Equivalent of bash's `source` command in Powershell? - Super User
#   https://superuser.com/questions/71446
# Script scope and dot sourcing - PowerShell | Microsoft Docs
#   https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_scripts#script-scope-and-dot-sourcing
# PowerShell: How to quickly switch to your home directory
#   https://sid-500.com/2017/08/03/powershell-how-to-quickly-switch-to-your-home-directory/
. "$home\.appveyor_token.ps1"
# file appveyor_token.ps1 provide appveyor token. content sample:
#   $appveyor_token = xxx

# https://www.appveyor.com/docs/build-cache/#remove-cache-entry-from-build-config
# https://www.appveyor.com/docs/api/#authentication

$headers = @{ }
$headers['Authorization'] = "Bearer $appveyor_token"
$headers["Content-type"] = "application/json"
$uri = "https://ci.appveyor.com/api/projects/$appveyor_project/buildcache"

Invoke-RestMethod -Uri $uri -Headers $headers -Method Delete
