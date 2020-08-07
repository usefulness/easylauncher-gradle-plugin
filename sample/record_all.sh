# utility script
# I can't make the library work with multiple flavors :/ And as that temporairly solves my problem I'm going to leave it as it is

set -e
function addToIndex {
  git status | grep "modified:" | awk '{print $2}' | xargs git add
}

echo "Record Start"
./../gradlew :example-simple:recordDebugAndroidTestScreenshotTest
addToIndex
echo "Simple completed"
./../gradlew :example-custom:recordLocalDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordProductionDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordQaDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-custom:recordStagingDebugAndroidTestScreenshotTest
addToIndex
echo "Custom completed"
./../gradlew :example-vector:recordTwoVectorsDebugAndroidTestScreenshotTest
addToIndex
./../gradlew :example-vector:recordWrongLabelPositionDebugAndroidTestScreenshotTest

