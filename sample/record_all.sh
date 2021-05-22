# utility script
# I can't make the library work with multiple flavors :/ And as that temporarily solves my problem I'm going to leave it as it is

set -e
function addToIndex {
  git status | grep "modified:" | awk '{print $2}' | xargs git add
  # git status | grep "screenshots" | grep -v "new file:"  | grep -v "deleted:" | awk '{print $1}' | xargs git add
  git commit -m "wip" --allow-empty
}

echo "Mutli Activity Alias start"
./../gradlew :example-activity-alias:recordDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-activity-alias:recordUnspecifiedAndroidTestScreenshotTest \-PtestBuildType=unspecified
addToIndex
./../gradlew :example-activity-alias:recordReleaseAndroidTestScreenshotTest \-PtestBuildType=release
addToIndex

echo "Custom Start"
./../gradlew :example-custom:recordLocalAaaDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordProductionAaaDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordQaAaaDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordStagingAaaDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordLocalBbbDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordProductionBbbDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordQaBbbDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordStagingBbbDebugAndroidTestScreenshotTest
addToIndex

echo "Icon round Start"
./../gradlew :example-icon-round:recordKeepDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-icon-round:recordIgnoreDebugAndroidTestScreenshotTest
addToIndex

echo "Library Start"
./../gradlew :example-library:recordDebugAndroidTestScreenshotTest
addToIndex

echo "Manifest Placeholder Start"
./../gradlew :example-manifest-placeholder:recordDebugAndroidTestScreenshotTest
addToIndex

echo "Scripted Start"
./../gradlew :example-scripted:recordDebugAndroidTestScreenshotTest
addToIndex

echo "Simple Start"
./../gradlew :example-simple:recordDebugAndroidTestScreenshotTest
addToIndex

echo "Vector start"
./../gradlew :example-vector:recordTwoVectorsDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordWrongLabelPositionDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordChromeLikeDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordChromeLikeCustomizedDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordChromeLikeTopDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordAlphaDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordCustomFontDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordCustomFontByFileDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordCustomFontByOtfDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordUsingAndroidVersionDebugAndroidTestScreenshotTest
addToIndex
echo "Completed"

