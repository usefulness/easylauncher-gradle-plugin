set -e
echo "Start"
./../gradlew verifyAll --continue
./../gradlew :example-resources-order:verifyRoborazziLocalCanary
./../gradlew :example-activity-alias:verifyRoborazziRelease
./../gradlew :example-activity-alias:verifyRoborazziUnspecified
