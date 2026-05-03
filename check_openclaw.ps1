$dirs = @(
    'C:\Users\WZB\.openclaw',
    'C:\Users\WZB\openclaw-cn',
    'C:\Users\WZB\.cc-switch\backups\openclaw',
    'C:\tmp\openclaw'
)
$total = 0
foreach ($d in $dirs) {
    if (Test-Path $d) {
        $size = (Get-ChildItem $d -Recurse -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        $sizeGB = [math]::Round($size / 1GB, 2)
        $total += $size
        Write-Output "$d : $sizeMB MB ($sizeGB GB)"
    } else {
        Write-Output "$d : not found"
    }
}
$totalMB = [math]::Round($total / 1MB, 2)
$totalGB = [math]::Round($total / 1GB, 2)
Write-Output "Total: $totalMB MB ($totalGB GB)"
