$customJavaHome = "C:\Users\mohdr\.vscode\extensions\redhat.java-1.52.0-win32-x64\jre\21.0.9-win32-x86_64"
$customMavenHome = "C:\Users\mohdr\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798"

$env:JAVA_HOME = $customJavaHome
$env:Path = "$customJavaHome\bin;$env:Path"
$mvnCmd = "$customMavenHome\bin\mvn.cmd"

Write-Host "Running AuthServiceTest..."
& $mvnCmd -Dtest=AuthServiceTest test | Out-File -Encoding utf8 test_output.txt
