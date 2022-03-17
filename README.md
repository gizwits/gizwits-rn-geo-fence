
# react-native-gizwits-rn-geofence

## Getting started

`$ npm install react-native-gizwits-rn-geofence --save`

### Mostly automatic installation

`$ react-native link react-native-gizwits-rn-geofence`

## Usage
```javascript
import RNGizwitsRnGeofence from 'react-native-gizwits-rn-geofence';

// TODO: What to do with the module?
RNGizwitsRnGeofence;
```
  
## 注意事项
原本手动取消是没有回调的，也就是说await会一直卡住
3.0.18开始，手动取消有回调，错误码6