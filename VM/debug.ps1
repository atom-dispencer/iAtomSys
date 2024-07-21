# Build and test using a default version.
# This is replaced with a live version in the GitHub actions CI/CD pipeline
echo "Cleaning Gradle build directory..."
./gradlew clean

$VERSION = "DEV"
echo "Building with version '$VERSION'"
./gradlew build test -Pversion="$VERSION"

# Start the jar with a debugger
$JAR = ".\build\libs\vm-$VERSION.jar"
echo "Starting '$JAR'"
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar "$JAR"