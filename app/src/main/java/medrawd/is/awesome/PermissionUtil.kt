package medrawd.`is`.awesome

import android.content.Context
import android.content.Context.MODE_PRIVATE

object PermissionUtil {
    private val PREFS_FILE_NAME = "preference"

    fun firstTimeAskingPermission(context: Context, permission: String, isFirstTime: Boolean) {
        val sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply()
    }

    fun isFirstTimeAskingPermission(context: Context, permission: String): Boolean {
        val sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
        return sharedPreference.getBoolean(permission, true)
    }
}