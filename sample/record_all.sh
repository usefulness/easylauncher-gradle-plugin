# utility script
# I can't make the library work with multiple flavors :/ And as that temporarily solves my problem I'm going to leave it as it is

set -e
function addToIndex {
  git status | grep "modified:" | awk '{print $2}' | xargs git add
  # git status | grep "screenshots" | grep -v "new file:"  | grep -v "deleted:" | awk '{print $1}' | xargs git add
  git commit -m "screenshots" --allow-empty
}

addToIndex
./../gradlew --stop

echo "Multi Activity Alias start"
./../gradlew :example-activity-alias:recordDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-activity-alias:recordUnspecifiedAndroidTestScreenshotTest \-PtestBuildType=unspecified --no-configuration-cache
addToIndex
./../gradlew :example-activity-alias:recordReleaseAndroidTestScreenshotTest \-PtestBuildType=release --no-configuration-cache
addToIndex

echo "Custom Start"
./../gradlew :example-custom:recordLocalAaaDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-custom:recordProductionAaaDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-custom:recordQaAaaDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-custom:recordStagingAaaDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-custom:recordLocalBbbDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-custom:recordProductionBbbDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-custom:recordQaBbbDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-custom:recordStagingBbbDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex

echo "Icon round Start"
./../gradlew :example-icon-round:recordKeepDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-icon-round:recordIgnoreDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex

echo "Library Start"
./../gradlew :example-library:recordDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex

echo "Manifest Placeholder Start"
./../gradlew :example-manifest-placeholder:recordDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex

echo "Scripted Start"
./../gradlew :example-scripted:recordDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex

echo "Simple Start"
./../gradlew :example-simple:recordDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex

echo "Vector start"
./../gradlew :example-vector:recordTwoVectorsDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordWrongLabelPositionDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordChromeLikeDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordChromeLikeCustomizedDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordChromeLikeTopDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordAlphaDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordCustomFontDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordCustomFontByFileDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordCustomFontByOtfDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
./../gradlew :example-vector:recordUsingAndroidVersionDebugAndroidTestScreenshotTest --no-configuration-cache
addToIndex
echo "Completed"
