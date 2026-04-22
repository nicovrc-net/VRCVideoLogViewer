@echo off

if exist ".\start.ps1" (
    rem found
) else (
    echo "Remove-Item ./start.ps1" >> start.ps1
    echo "Invoke-WebRequest -Uri https://raw.githubusercontent.com/nicovrc-net/VRCVideoLogViewer/refs/heads/release/run-batch/start.ps1 -OutFile ./start.ps1" >> start.ps1
    echo; >> start.ps1
    echo "# SIG # Begin signature block" >> start.ps1
    echo "# MIIIpQYJKoZIhvcNAQcCoIIIljCCCJICAQExCzAJBgUrDgMCGgUAMGkGCisGAQQB" >> start.ps1
    echo "# gjcCAQSgWzBZMDQGCisGAQQBgjcCAR4wJgIDAQAABBAfzDtgWUsITrck0sYpfvNR" >> start.ps1
    echo "# AgEAAgEAAgEAAgEAAgEAMCEwCQYFKw4DAhoFAAQUMCDYTd3PEqF8vNhZ8AcoovFe" >> start.ps1
    echo "# ZSqgggUuMIIFKjCCAxKgAwIBAgIQFu7PAOinJJxLTeEeaGSG+TANBgkqhkiG9w0B" >> start.ps1
    echo "# AQsFADAsMSowKAYDVQQDDCFuaWNvdnJjLm5ldCBPVT1TZWxmLXNpZ25lZCBSb290" >> start.ps1
    echo "# Q0EwIBcNMjYwNDIwMTIxNzA0WhgPMjA5ODEyMzExNTAwMDBaMCwxKjAoBgNVBAMM" >> start.ps1
    echo "# IW5pY292cmMubmV0IE9VPVNlbGYtc2lnbmVkIFJvb3RDQTCCAiIwDQYJKoZIhvcN" >> start.ps1
    echo "# AQEBBQADggIPADCCAgoCggIBALq2vwv7CZYo6E4uRw+dE1maw/ubYxBWjMD67SZU" >> start.ps1
    echo "# b6dHv4wz2h3sNwUoCBi7pemUJ/pGfEfr8Wr/NG3uJwjw9HOJYm12Wu3hjNYVHFwq" >> start.ps1
    echo "# SiEkkYXpfAlSEOPvwuGsINLLkf5TtWGZSr/4NkvbvAVVqWsli1EOYTceOaYmXXQ+" >> start.ps1
    echo "# VXfEaNYhpOX/KmAugCDAfEuAFWZBu0jAoXIC+DvvTn1qiiSCvD4jCbbUB5I1mCIb" >> start.ps1
    echo "# l4B4mHaMpt6GrZE8G5eif5vZXa0ikt+jWuwGCyNApQhfVuDGSZtHpq78PSgbms6D" >> start.ps1
    echo "# LhOhXzwVsgno6RzGpVDeyyBzqYFURup1btajOCH7T+3SQXS6PCEdyRsNnBmRP/WS" >> start.ps1
    echo "# uhD124ElhaHn7k9HxPiaM+Om4BYQJwMvOAcRkNDsBTvLKm7gM53dd+VaMYxLfZvc" >> start.ps1
    echo "# o1cK7zqGMGCqd8V1nOstkUGYSZViaUdvP9yCkO8IqZpJv63iCHlaTTw1RT/xXosx" >> start.ps1
    echo "# RrQCKUX+D5ZKUNnT52vNs6B5W7Ijyuwxdzrfu6g3+d2vVLwBgLlepLR9NeqZgzKF" >> start.ps1
    echo "# g64QzkJ5OQkJsExBEeKeqrVuaL3jQvrI7HRhy1XAMOJlq1gefdC+bQTLEENNrATb" >> start.ps1
    echo "# 02ZhMHvmYFxj6Ce9qXDHdLZxGuVqrYtCUz6f2MFR6Nd/ifTnCDg6Cd5bn+kgwyFz" >> start.ps1
    echo "# kOphAgMBAAGjRjBEMA4GA1UdDwEB/wQEAwIHgDATBgNVHSUEDDAKBggrBgEFBQcD" >> start.ps1
    echo "# AzAdBgNVHQ4EFgQUCQFMhKmdgTdjvy01UeIx8bj4dnQwDQYJKoZIhvcNAQELBQAD" >> start.ps1
    echo "# ggIBAFhcx3r2HS99DtbsnYuVsMMt3BPE9h83dY8o0gta20T7BVgukRMoVCnKVWxT" >> start.ps1
    echo "# cKKn6umGLFptCyQgf1p5MHh+hCOUAK/fu2v6s3WNu5/HW0P/EBGmsptl+ROT8hEc" >> start.ps1
    echo "# U7p2Q9kjMcxP6w7afHm78f8PZe9IXxOf9xL7vUzqTopgOjFArUE97o4LTNTuJP4G" >> start.ps1
    echo "# 1cMJHTKy6ZFRgCpupl2ktEe6/m9HSFz61+3xkotnwZozZ1yPfz/3Knd5QqiNtE+Z" >> start.ps1
    echo "# KdyPocIjxjo6opi9uex6qMPMnXAOLms/w2rlC44bbUF+7NxIBinIS0m1nNp00z0c" >> start.ps1
    echo "# qKVcnlkeaJnfyeQsocJG+/i+EXn7cIEO8+YZJH5bsv+XexiTP8SS4QlainVwb963" >> start.ps1
    echo "# oMCZRplbrMfbWufk9cpUsW8blqJIN4+f9T5hTf8+RaqUXKDmGMPlYpEHTy1mNIke" >> start.ps1
    echo "# B27qph5Cg2R5A/EZqVJyI00Qj8/WrfH/5wJCLB9C0sKovq99iFb/6I7Mo0GNs9rt" >> start.ps1
    echo "# ZoNUZDk5aZUQLQVGRq8kXhS3O7nfF+8FsfjiAHHVf33ioG/3wnDQBZICwn273Tui" >> start.ps1
    echo "# zbZ/fyXvu7mTKsv6SsmTjGoO0ql02ufChDZgD1j8NOGmm0C5l087o4gbUW9Z8hlM" >> start.ps1
    echo "# ctVq3PhmU9UfW/Efepk3tTRRwLde7Brs3PCzw8v1iqWsq9SoMYIC4TCCAt0CAQEw" >> start.ps1
    echo "# QDAsMSowKAYDVQQDDCFuaWNvdnJjLm5ldCBPVT1TZWxmLXNpZ25lZCBSb290Q0EC" >> start.ps1
    echo "# EBbuzwDopyScS03hHmhkhvkwCQYFKw4DAhoFAKB4MBgGCisGAQQBgjcCAQwxCjAI" >> start.ps1
    echo "# oAKAAKECgAAwGQYJKoZIhvcNAQkDMQwGCisGAQQBgjcCAQQwHAYKKwYBBAGCNwIB" >> start.ps1
    echo "# CzEOMAwGCisGAQQBgjcCARUwIwYJKoZIhvcNAQkEMRYEFKxYg+9v49XCib2sE2J2" >> start.ps1
    echo "# SwtQq6VVMA0GCSqGSIb3DQEBAQUABIICAGv7uMalY1vsVDqu2WhzvYtSOpw2sEuV" >> start.ps1
    echo "# sG3x7Myl4kTKMNXu5fw7u2vcBedEXiqjy0BM9RAxCpkyvQQQusXdbk8qtclBqdYQ" >> start.ps1
    echo "# cGl4cRKkiPFQpsOGNpIQj4w7EMGa11kpjZLOuJiUH3EsNGf3hA/NtW7gZswSX3v3" >> start.ps1
    echo "# 0+3Hi/NjMl+YBEwTvIMGd0FH4qNNo5QdHyv0HKY4fTrQaJblBsbAyl66wsylgbXY" >> start.ps1
    echo "# YTkGhHZX7B73BZ/lpOLd2FOcBhOh9YtZ755vGEXBIoP6iSUY4J9DAmOgiFEcMmHL" >> start.ps1
    echo "# HPFCtU3Yj2/vTE5qQKDdVkXYou9t+9QZDiZe1d2+gsl8D4HvBVpFvx9uUY6ESuxP" >> start.ps1
    echo "# 1qoM+s9+ia/0CS5Hz8soXCTU7Ljy8OwK3Wv7tYyRqvWB8o1loMOmiL3b01YZh/9K" >> start.ps1
    echo "# rwqo3mnXysonLXH/2e0l5AqTz04NHLiC0DdCWHiMhEXenZa8LNcso4AygRqGwZa4" >> start.ps1
    echo "# VL0imGb/c+1GhZpIm+xz5dT4/1u4+bG4q6FuZ+o/vl+Q2P+KwEqXnY8SwLhVKmWj" >> start.ps1
    echo "# q7KYlWXcAhHMvoAJzrExULqdKmoCKLMSxLTX97vM52gIWLm7fnvdCPVs4ahN2bMF" >> start.ps1
    echo "# vLDqUpdQG1fYrCkb3vhJ3OcoCCTNKWXX9139R02nygZ2jMjFeQ6qsn1+3ewtx7Dd" >> start.ps1
    echo "# kwteGAWwRBdO" >> start.ps1
    echo "# SIG # End signature block"" >> start.ps1
)

powershell -NoProfile -ExecutionPolicy Unrestricted .\start.ps1
exit