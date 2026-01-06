#requires -Version 5.1
<#
Start-VRCVideoLogViewer.ps1

GUIで:
- tools/ と fonts/ を用意
- 7zip(7za), OpenJDK(ローカル展開), JavaFX(ローカル展開), ImageMagick(ローカル展開), NotoSans fonts をダウンロード＆展開
- さらに「システムの java が 21 未満 or 無い」場合は Oracle JDK 25 をDLしてサイレントインストール
- 最後に tools/jdk-21.0.2/bin/java.exe で jar を起動（従来bat踏襲）
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# ----------------------------
# 設定
# ----------------------------
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$ToolsDir = Join-Path $Root "tools"
$FontsDir = Join-Path $Root "fonts"

$SevenZipDir = Join-Path $ToolsDir "7z2501"
$SevenZrExe   = Join-Path $ToolsDir "7zr.exe"
$SevenZaExe   = Join-Path $SevenZipDir "7za.exe"

$JdkDir    = Join-Path $ToolsDir "jdk-21.0.2"
$FxDir     = Join-Path $ToolsDir "javafx-sdk-21.0.9"
$MagickDir = Join-Path $ToolsDir "ImageMagick-7.1.2-8-portable-Q16-x64"

$JarPath = Join-Path $Root "VRCVideoLogViewer-1.0-SNAPSHOT-all.jar"

# URLs
$Url7zr  = "https://www.7-zip.org/a/7zr.exe"
$Url7z   = "https://www.7-zip.org/a/7z2501-extra.7z"

$UrlJdk  = "https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_windows-x64_bin.zip"
$UrlFx   = "https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip"

$UrlMagick = "https://imagemagick.org/archive/binaries/ImageMagick-7.1.2-11-portable-Q16-x64.7z"

# Oracle JDK 25 (System Javaが古い時だけ)
$OracleJdkUrl = "https://download.oracle.com/java/25/latest/jdk-25_windows-x64_bin.exe"
$OracleJdkInstaller = Join-Path $ToolsDir "jdk-25_windows-x64_bin.exe"

# Fonts
$Fonts = @(
  @{ Name="NotoSansJP-Medium.ttf"; Url='https://fonts.gstatic.com/s/notosansjp/v55/-F6jfjtqLzI2JPCgQBnw7HFyzSD-AsregP8VFCMj75vY0rw-oME.ttf' },
  @{ Name="NotoSansKR-Medium.ttf"; Url='https://fonts.gstatic.com/s/notosanskr/v38/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzztgyeLTq8H4hfeE.ttf' },
  @{ Name="NotoSansSC-Medium.ttf"; Url='https://fonts.gstatic.com/s/notosanssc/v39/k3kCo84MPvpLmixcA63oeAL7Iqp5IZJF9bmaG-3FnYxNbPzS5HE.ttf' },
  @{ Name="NotoSansTC-Medium.ttf"; Url='https://fonts.gstatic.com/s/notosanstc/v38/-nFuOG829Oofr2wohFbTp9ifNAn722rq0MXz75Ky_CpOtma3uNQ.ttf' }
)

# ----------------------------
# 共通ユーティリティ
# ----------------------------
function Ensure-Dir([string]$Path) {
  if (-not (Test-Path $Path)) { New-Item -ItemType Directory -Path $Path | Out-Null }
}

function Download-File([string]$Url, [string]$OutFile, [scriptblock]$Log) {
  Ensure-Dir (Split-Path -Parent $OutFile)
  & $Log "Download: $Url"
  try {
    Invoke-WebRequest -Uri $Url -OutFile $OutFile -UseBasicParsing
  } catch {
    & $Log "Invoke-WebRequest failed. Fallback to curl..."
    $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
    if (-not $curl) { throw }
    & curl.exe -L $Url --output $OutFile | Out-Null
  }
}

function Extract-7z([string]$Archive, [string]$OutputDir, [scriptblock]$Log) {
  if (-not (Test-Path $SevenZaExe)) {
    throw "7za.exe not found: $SevenZaExe"
  }
  Ensure-Dir $OutputDir
  & $Log "Extract: $(Split-Path -Leaf $Archive) -> $OutputDir"
  & $SevenZaExe x "-o$OutputDir" $Archive | Out-Null
}

# ----------------------------
# System Java (Oracle JDK 25) 確保
# ----------------------------
function Get-JavaMajorVersion {
  try {
    $output = & java -version 2>&1 | Out-String
    # "25.0.1" / "21.0.2" / "17.0.10" など
    if ($output -match 'version "(\d+)(?:\.\d+)?') {
      return [int]$Matches[1]
    }
    return 0
  } catch {
    return 0
  }
}

function Refresh-PathForSession {
  $machine = [System.Environment]::GetEnvironmentVariable("PATH","Machine")
  $user    = [System.Environment]::GetEnvironmentVariable("PATH","User")
  $env:PATH = @($machine, $user) -join ";"
}

function Ensure-SystemJava21Plus([scriptblock]$Log) {
  $ver = Get-JavaMajorVersion
  if ($ver -ge 21) {
    & $Log "System Java OK (version $ver)"
    return
  }

  & $Log "System Java < 21 or not found. Installing Oracle JDK 25..."
  Ensure-Dir $ToolsDir

  if (-not (Test-Path $OracleJdkInstaller)) {
    Download-File $OracleJdkUrl $OracleJdkInstaller $Log
  } else {
    & $Log "Installer already exists: $OracleJdkInstaller"
  }

  & $Log "Running Oracle JDK 25 installer (silent). If it fails, run PowerShell as Administrator."
  $proc = Start-Process -FilePath $OracleJdkInstaller -ArgumentList "/s" -Wait -PassThru

  if ($proc.ExitCode -ne 0) {
    throw "Oracle JDK installer failed. ExitCode=$($proc.ExitCode)"
  }

  & $Log "Oracle JDK installed."

  Refresh-PathForSession

  $newVer = Get-JavaMajorVersion
  if ($newVer -lt 21) {
    throw "Java install verification failed. (java -version still < 21 or not found)"
  }

  & $Log "System Java updated successfully (version $newVer)"
}

# ----------------------------
# ツール類の確保
# ----------------------------
function Ensure-7Zip([scriptblock]$Log) {
  Ensure-Dir $ToolsDir
  if (Test-Path $SevenZipDir) {
    & $Log "7-Zip OK ($SevenZipDir)"
    return
  }

  $tmp7z = Join-Path $ToolsDir "7z2501-extra.7z"
  Download-File $Url7zr $SevenZrExe $Log
  Download-File $Url7z  $tmp7z      $Log

  & $Log "Extract 7-Zip extra..."
  & $SevenZrExe x "-o$SevenZipDir" $tmp7z | Out-Null

  Remove-Item $SevenZrExe -Force -ErrorAction SilentlyContinue
  Remove-Item $tmp7z -Force -ErrorAction SilentlyContinue

  if (-not (Test-Path $SevenZaExe)) {
    throw "7za.exe not found after extraction: $SevenZaExe"
  }

  & $Log "7-Zip ready."
}

function Ensure-Jdk([scriptblock]$Log) {
  if (Test-Path $JdkDir) { & $Log "OpenJDK OK ($JdkDir)"; return }
  Ensure-7Zip $Log
  $zip = Join-Path $ToolsDir "openjdk-21.0.2_windows-x64_bin.zip"
  Download-File $UrlJdk $zip $Log
  Extract-7z $zip $ToolsDir $Log
  & $Log "OpenJDK ready."
}

function Ensure-JavaFX([scriptblock]$Log) {
  if (Test-Path $FxDir) { & $Log "OpenJFX OK ($FxDir)"; return }
  Ensure-7Zip $Log
  $zip = Join-Path $ToolsDir "openjfx-21.0.9_windows-x64_bin-sdk.zip"
  Download-File $UrlFx $zip $Log
  Extract-7z $zip $ToolsDir $Log
  & $Log "OpenJFX ready."
}

function Ensure-ImageMagick([scriptblock]$Log) {
  if (Test-Path $MagickDir) { & $Log "ImageMagick OK ($MagickDir)"; return }
  Ensure-7Zip $Log
  $arc = Join-Path $ToolsDir "ImageMagick-7.1.2-11-portable-Q16-x64.7z"
  Download-File $UrlMagick $arc $Log

  # bat踏襲: 展開先フォルダ名は固定
  Extract-7z $arc $MagickDir $Log
  & $Log "ImageMagick ready."
}

function Ensure-Fonts([scriptblock]$Log) {
  Ensure-Dir $FontsDir
  $jp = Join-Path $FontsDir "NotoSansJP-Medium.ttf"
  if (Test-Path $jp) { & $Log "Fonts OK ($FontsDir)"; return }

  foreach ($f in $Fonts) {
    $out = Join-Path $FontsDir $f.Name
    Download-File $f.Url $out $Log
  }
  & $Log "Fonts ready."
}

# ----------------------------
# 実行
# ----------------------------
function Run-App([scriptblock]$Log) {
  if (-not (Test-Path $JarPath)) {
    throw "Jar not found: $JarPath"
  }
  $java = Join-Path $JdkDir "bin\java.exe"
  if (-not (Test-Path $java)) {
    throw "java.exe not found: $java"
  }
  $fxLib = Join-Path $FxDir "lib"
  if (-not (Test-Path $fxLib)) {
    throw "JavaFX lib not found: $fxLib"
  }

  & $Log "Starting..."
  & $Log "$java --module-path `"$fxLib`" --add-modules javafx.controls,javafx.fxml -jar `"$JarPath`""

  Start-Process -FilePath $java -ArgumentList @(
    "--module-path", $fxLib,
    "--add-modules", "javafx.controls,javafx.fxml",
    "-jar", $JarPath
  ) -WorkingDirectory $Root
}

function Setup-All([scriptblock]$Log, [scriptblock]$SetProgress) {
  Ensure-Dir $ToolsDir
  Ensure-Dir $FontsDir

  # 大まかな進捗
  & $SetProgress 3
  Ensure-SystemJava21Plus $Log

  & $SetProgress 15
  Ensure-7Zip $Log

  & $SetProgress 35
  Ensure-Jdk $Log

  & $SetProgress 55
  Ensure-JavaFX $Log

  & $SetProgress 75
  Ensure-ImageMagick $Log

  & $SetProgress 90
  Ensure-Fonts $Log

  & $SetProgress 100
  & $Log "Setup completed."
}

# ----------------------------
# GUI (WinForms)
# ----------------------------
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$form = New-Object System.Windows.Forms.Form
$form.Text = "VRCVideoLogViewer Launcher"
$form.Size = New-Object System.Drawing.Size(820, 520)
$form.StartPosition = "CenterScreen"

$btnSetup = New-Object System.Windows.Forms.Button
$btnSetup.Text = "セットアップ（DL/展開）"
$btnSetup.Size = New-Object System.Drawing.Size(180, 34)
$btnSetup.Location = New-Object System.Drawing.Point(20, 20)

$btnRun = New-Object System.Windows.Forms.Button
$btnRun.Text = "起動"
$btnRun.Size = New-Object System.Drawing.Size(120, 34)
$btnRun.Location = New-Object System.Drawing.Point(220, 20)

$progress = New-Object System.Windows.Forms.ProgressBar
$progress.Location = New-Object System.Drawing.Point(20, 64)
$progress.Size = New-Object System.Drawing.Size(760, 18)
$progress.Minimum = 0
$progress.Maximum = 100
$progress.Value = 0

$logBox = New-Object System.Windows.Forms.TextBox
$logBox.Multiline = $true
$logBox.ReadOnly = $true
$logBox.ScrollBars = "Vertical"
$logBox.WordWrap = $false
$logBox.Font = New-Object System.Drawing.Font("Consolas", 10)
$logBox.Location = New-Object System.Drawing.Point(20, 94)
$logBox.Size = New-Object System.Drawing.Size(760, 360)

function Append-Log([string]$msg) {
  $ts = (Get-Date).ToString("HH:mm:ss")
  $logBox.AppendText("[$ts] $msg`r`n")
  $logBox.SelectionStart = $logBox.Text.Length
  $logBox.ScrollToCaret()
  [System.Windows.Forms.Application]::DoEvents() | Out-Null
}

$Log = { param($m) Append-Log $m }
$SetProgress = {
  param($v)
  $progress.Value = [Math]::Max(0, [Math]::Min(100, [int]$v))
  [System.Windows.Forms.Application]::DoEvents() | Out-Null
}

$btnSetup.Add_Click({
  try {
    $btnSetup.Enabled = $false
    $btnRun.Enabled = $false
    & $Log "Setup start..."
    & $SetProgress 0
    Setup-All $Log $SetProgress
  } catch {
    & $Log "ERROR: $($_.Exception.Message)"
    & $Log $_.Exception.ToString()
  } finally {
    $btnSetup.Enabled = $true
    $btnRun.Enabled = $true
  }
})

$btnRun.Add_Click({
  try {
    $btnSetup.Enabled = $false
    $btnRun.Enabled = $false

    # 足りなければセットアップ
    if (-not (Test-Path $JdkDir) -or -not (Test-Path $FxDir)) {
      & $Log "Tools not ready. Running setup first..."
      & $SetProgress 0
      Setup-All $Log $SetProgress
    } else {
      # system java が古くても事故りにくくする（要件）
      Ensure-SystemJava21Plus $Log
    }

    Run-App $Log
  } catch {
    & $Log "ERROR: $($_.Exception.Message)"
    & $Log $_.Exception.ToString()
  } finally {
    $btnSetup.Enabled = $true
    $btnRun.Enabled = $true
  }
})

$form.Controls.Add($btnSetup)
$form.Controls.Add($btnRun)
$form.Controls.Add($progress)
$form.Controls.Add($logBox)

# 初期表示
Append-Log "Root:  $Root"
Append-Log "tools: $ToolsDir"
Append-Log "fonts: $FontsDir"
Append-Log "Jar:   $JarPath"
Append-Log "Ready."

[void]$form.ShowDialog()
