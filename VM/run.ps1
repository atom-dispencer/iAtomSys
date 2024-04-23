./gradlew clean build --warning-mode all
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar .\build\libs\iAtomSysVM-1.jar

# # Launch non-blocking
# Start-Process "java" -ArgumentList {
#         "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
#         "-jar", ".\build\libs\iAtomSysVM-1.jar"
#     }
#
#
# # Wait 5 seconds for the debug server to start
# Start-Sleep -Seconds 5;