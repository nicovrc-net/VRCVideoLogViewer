@echo off

if exist ".\start.ps1" (
) else (
    echo "Remove-Item ./start.ps1" >> start.ps1
    echo "Invoke-WebRequest -Uri https://raw.githubusercontent.com/nicovrc-net/VRCVideoLogViewer/refs/heads/release/run-batch/start.ps1 -OutFile ./start.ps1" >> start.ps1
)

powershell -NoProfile -ExecutionPolicy Unrestricted .\start.ps1
exit