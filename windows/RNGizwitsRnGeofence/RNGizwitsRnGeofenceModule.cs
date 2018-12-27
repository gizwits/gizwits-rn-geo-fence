using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Gizwits.Rn.Geofence.RNGizwitsRnGeofence
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNGizwitsRnGeofenceModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNGizwitsRnGeofenceModule"/>.
        /// </summary>
        internal RNGizwitsRnGeofenceModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNGizwitsRnGeofence";
            }
        }
    }
}
