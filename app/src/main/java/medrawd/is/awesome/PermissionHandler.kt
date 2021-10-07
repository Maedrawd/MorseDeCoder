package medrawd.`is`.awesome

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

enum class CheckPermissionResult {
    PermissionAsk,
    PermissionPreviouslyDenied,
    PermissionDisabled,
    PermissionGranted
}

typealias PermissionCheckCompletion = (CheckPermissionResult) -> Unit


object PermissionHandler {

    private fun shouldAskPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context,
                permission) != PackageManager.PERMISSION_GRANTED
    }

    fun checkPermission(activity: Activity, permission: String, completion: PermissionCheckCompletion) {
        // If permission is not granted
        if (shouldAskPermission(activity, permission)) {
            //If permission denied previously
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (activity.shouldShowRequestPermissionRationale(permission))) {
                completion(CheckPermissionResult.PermissionPreviouslyDenied)
            } else {
                // Permission denied or first time requested
                if (PermissionUtil.isFirstTimeAskingPermission(activity, permission)) {
                    PermissionUtil.firstTimeAskingPermission(activity, permission, false)
                    completion(CheckPermissionResult.PermissionAsk)
                } else {
                    // Handle the feature without permission or ask user to manually allow permission
                    completion(CheckPermissionResult.PermissionDisabled)
                }
            }
        } else {
            completion(CheckPermissionResult.PermissionGranted)
        }
    }
}