set -e
echo "Start"
./../gradlew verifyAll --continue --no-configuration-cache
./../gradlew verifyReleaseAndroidTestScreenshotTest -PtestBuildType=release --no-configuration-cache
./../gradlew verifyUnspecifiedAndroidTestScreenshotTest -PtestBuildType=unspecified --no-configuration-cache
