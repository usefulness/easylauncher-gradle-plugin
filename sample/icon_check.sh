set -e
echo "Start"
./../gradlew verifyAll --continue
./../gradlew :example-resources-order:verifyRoborazziLocalCanary -PtestBuildType=canary
./../gradlew :example-activity-alias:verifyRoborazziRelease -PtestBuildType=release
./../gradlew :example-activity-alias:verifyRoborazziUnspecified -PtestBuildType=unspecified
