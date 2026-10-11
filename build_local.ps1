$customJavaHome = "C:\Users\mohdr\.vscode\extensions\redhat.java-1.52.0-win32-x64\jre\21.0.9-win32-x86_64"
$customMavenHome = "C:\Users\mohdr\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798"

Write-Host "Setting JAVA_HOME to: $customJavaHome"
$env:JAVA_HOME = $customJavaHome
$env:Path = "$customJavaHome\bin;$env:Path"

Write-Host "Using Maven from: $customMavenHome"
$mvnCmd = "$customMavenHome\bin\mvn.cmd"

# Verify tools
& "$customJavaHome\bin\java.exe" -version
& $mvnCmd -version

# Run Build
Write-Host "`nStarting Build..."
& $mvnCmd clean package
