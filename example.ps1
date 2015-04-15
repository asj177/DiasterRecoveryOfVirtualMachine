Get-pssnapin -Registered | add-pssnapin

Connect-VIServer 130.65.132.110 -User administrator -Password 12!@qwQW

Add-VMHost -Name 130.65.132.223 -Location (Get-Datacenter -Name T10-DC-DEE) -User root -Password 12!@qwQW -Force