# utility script
# I can't make the library work with multiple flavors :/ And as that temporarily solves my problem I'm going to leave it as it is

set -e
function addToIndex {
  git status | grep "modified:" | awk '{print $2}' | xargs git add
  git status | grep "screenshots" | grep -v "new file:"  | grep -v "deleted:" | awk '{print $1}' | xargs git add
}

#echo "Record Start"
#./../gradlew :example-simple:recordDebugAndroidTestScreenshotTest
#addToIndex
echo "Simple completed"
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
echo "Custom completed"
./../gradlew :example-vector:recordTwoVectorsDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordWrongLabelPositionDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordChromelikeDebugAndroidTestScreenshotTest

