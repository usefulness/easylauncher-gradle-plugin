set -e
echo "Start"
./../gradlew verifyAll --continue
./../gradlew verifyReleaseAndroidTestScreenshotTest -PtestBuildType=release
./../gradlew verifyUnspecifiedAndroidTestScreenshotTest -PtestBuildType=unspecified
