./gradlew clean build --warning-mode all
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar .\build\libs\vm.jar