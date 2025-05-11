set -e
echo "Start"
./../gradlew verifyAll --continue
./../gradlew :example-resources-order:verifyLocalCanaryAndroidTestScreenshotTest -PtestBuildType=canary
./../gradlew verifyReleaseAndroidTestScreenshotTest -PtestBuildType=release
./../gradlew verifyUnspecifiedAndroidTestScreenshotTest -PtestBuildType=unspecified
