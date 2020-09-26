set -e
echo "Start"
./../gradlew verifyAll
./../gradlew verifyReleaseAndroidTestScreenshotTest -PtestBuildType=release
./../gradlew verifyUnspecifiedAndroidTestScreenshotTest -PtestBuildType=unspecified
