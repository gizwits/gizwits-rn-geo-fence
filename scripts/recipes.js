const defaultContext = {
  gaodeKey: '<Your gaode appKey>',
  googleKey: '<Your gaode appKey>'

}

/**
 * @param {object} [ctx]
 * @return {{ [string]: (Recipe|Recipe[]) }}
 */
module.exports = (ctx = defaultContext) => ({
  'android/settings.gradle': {
    pattern: `rootProject.name.*`,
    patch: `
    include ':react-native-gizwits-rn-geofence'
    project(':react-native-gizwits-rn-geofence').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-gizwits-rn-geofence/android')
    `
  },

  'android/**/AndroidManifest.xml': {
    pattern: `</activity>`,
    patch: `
    <meta-data 
          android:name="com.amap.api.v2.apikey" 
          android:value="${ctx.gaodeKey}"/>
    <meta-data
          android:name="com.google.android.maps.v2.API_KEY"
          android:value="${ctx.googleKey}"/>
    `
  }

})
