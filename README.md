
# react-native-cellular-info

Detects type of cellular connection you are using.

Documentation TBA -- sorry!

## Getting started

`$ npm install react-native-cellular-info --save`

### Mostly automatic installation

`$ react-native link react-native-cellular-info`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-cellular-info` and add `RNCellularInfo.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNCellularInfo.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNCellularInfoPackage;` to the imports at the top of the file
  - Add `new RNCellularInfoPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-cellular-info'
  	project(':react-native-cellular-info').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-cellular-info/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-cellular-info')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNCellularInfo.sln` in `node_modules/react-native-cellular-info/windows/RNCellularInfo.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Com.Reactlibrary.RNCellularInfo;` to the usings at the top of the file
  - Add `new RNCellularInfoPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNCellularInfo from 'react-native-cellular-info';

// TODO: What to do with the module?
RNCellularInfo;
```
  