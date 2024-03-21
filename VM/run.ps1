# Get an up-to-date build
./gradlew build --warning-mode all

# Launch non-blocking
Start-Process "java" -ArgumentList {
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
        "-jar", ".\build\libs\iAtomSysVM-1.jar"
    }

# Wait 5 seconds for the debug server to start
Start-Sleep -Seconds 5;