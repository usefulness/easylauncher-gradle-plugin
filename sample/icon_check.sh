set -e
echo "Start"
./../gradlew verifyAll --continue --no-configuration-cache -PagpVersion=7.3.0-beta05
./../gradlew verifyReleaseAndroidTestScreenshotTest -PtestBuildType=release --no-configuration-cache -PagpVersion=7.3.0-beta05
./../gradlew verifyUnspecifiedAndroidTestScreenshotTest -PtestBuildType=unspecified --no-configuration-cache -PagpVersion=7.3.0-beta05
