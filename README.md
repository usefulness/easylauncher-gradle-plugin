# Easylauncher gradle plugin for Android

[![codecov](https://codecov.io/gh/usefulness/easylauncher-gradle-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/usefulness/easylauncher-gradle-plugin)
&nbsp;[![build](https://github.com/usefulness/easylauncher-gradle-plugin/workflows/Build%20project/badge.svg)](https://github.com/usefulness/easylauncher-gradle-plugin/actions)
&nbsp;[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

[![version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/project/starter/easylauncher/maven-metadata.xml?label=gradle)](https://plugins.gradle.org/search?term=com.starter)

Modify the launcher icon of each of your app-variants using simple gradle rules. Add ribbons of any color, overlay your own images, change the colors of the icon, ...

This is a rework of original _Easylauncher_ library, which supports modern build tools and **Vector icons**

![](icons/ic_launcher_debug.png) ![](icons/ic_launcher_staging.png) ![](icons/ic_launcher_variant.png) ![](icons/ic_launcher_beta.png)
 
![](icons/ic_launcher_grayscale.png) ![](icons/customColorRibbonTopRight.png) ![](icons/customColorRibbonTop.png) ![](icons/customColorRibbonBottom.png)

## Usage

### Basic usage

```groovy
// in app/build.gradle
plugins {
  id "com.starter.easylauncher" version "${{version}}"
}
```
see [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.starter.easylauncher) for details

### Advanced usage

You can customize the filters applied to each type, flavor and variant of your app.  

Imagine these are the type and flavors of your app:

```groovy
// in app/build.gradle
android {
    buildTypes {
        debug {
            //Debuggable, will get a default ribbon in the launcher icon
        }
        beta {
            //Debuggable, will get a default ribbon in the launcher icon
            debuggable true
        }
        canary {
            //Non-debuggable, will not get any default ribbon
            debuggable false
        }
        release {
            //Non-debuggable, will not get any default ribbon
        }
    }
    productFlavors {
        local {}
        qa {}
        staging {}
        production {}
    }
}
```


You could customize the plugin's behaviour like this: 


```groovy
easylauncher {
    defaultFlavorNaming = true // Use flavor name for default ribbon, instead of the type name
    
    productFlavors {
        local {}
        qa {
            // Add one more filter to all `qa` variants
            filters = redRibbonFilter()
        }
        staging {}
        production {}
    }
    
    buildTypes {
        beta {
            // Add two more filters to all `beta` variants
            filters = [
                    customRibbon(ribbonColor: "#0000FF"),
                    overlayFilter(new File("example-custom/launcherOverlay/beta.png"))
            ]
        }
        canary {
            // Remove ALL filters to `canary` variants
            enable false
        }
        release {}
    }
    
    variants {
        productionDebug {
            // OVERRIDE all previous filters defined for `productionDebug` variant
            filters = orangeRibbonFilter("custom")
        }
    }
}
```


## Available filters

## Grayscale filter

| Command | Result |
| - | - |
| `grayscaleFilter()` | ![](icons/grayscale.png) |
_Note: It doesn't work with vector images yet_


## Overlay filter

| Command | Result |
| - | - |
| `overlayFilter(new File("example-custom/launcherOverlay/beta.png"))` | ![](icons/overlay.png) |

## Ribbon filters

| Filter | Command | Result |
| - | - | - |
| Gray ribbon | `grayRibbonFilter()` | ![](icons/grayRibbon.png) |
| Green ribbon | `greenRibbonFilter()` | ![](icons/greenRibbon.png) |
| Yellow ribbon | `yellowRibbonFilter()` | ![](icons/yellowRibbon.png) |
| Orange ribbon | `orangeRibbonFilter()` | ![](icons/orangeRibbon.png) |
| Red ribbon | `redRibbonFilter()` | ![](icons/redRibbon.png) |
| Blue ribbon | `blueRibbonFilter()` | ![](icons/blueRibbon.png) |

## Advanced Ribbon filter

| Description | Command | Result |
| - | - | - |
| Custom background color  | `customRibbon(ribbonColor: "#6600CC")` | ![](icons/customColorRibbon.png) |
| Custom label | `customRibbon(label: "label", ribbonColor: "#DCDCDC")` | ![](icons/customColorRibbon2.png) |
| Custom text color | `customRibbon(label: "label", ribbonColor: "#DCDCDC", labelColor: "#000000")` | ![](icons/customColorRibbon3.png) |
| Custom gravity - Top | `customRibbon(label: "custom", position: "top")` | ![](icons/customColorRibbonTop.png) |
| Custom gravity - Bottom | `customRibbon(position: "bottom")` | ![](icons/customColorRibbonBottom.png) |
| Custom gravity - TopLeft | `customRibbon(position: "topLeft")` | ![](icons/customColorRibbonTopLeft.png) |
| Custom gravity - TopRight | `customRibbon(position: "topRight")` | ![](icons/customColorRibbonTopRight.png) |
| Custom text size (relative to the icon size) | `customRibbon(position: "bottom", textSizeRatio: 0.2)` | ![](icons/customColorRibbonBottomSize.png) |

## Available options

 - `defaultFlavorNaming`: use _flavor_ name in default ribbons, instead of using _buildType_
 - `enable`: enable/disable **all** filters for a flavor/type/variant
 - `productFlavors`: define filters for flavors (will be **added** to filters defined for types)
 - `buildTypes`: define filters for types (will be **added** to filter defined for flavors)
 - `variants`: define filters for variants (will **override** any other filters)


## Project Structure

```
easylauncher/   - Gradle plugin
sample/         - root directory of supported Android applications which serve as test projects
```

## Credits
Credits to [Akaita's easylauncher plugin](https://github.com/akaita/easylauncher-gradle-plugin) which this project heavily relies on
