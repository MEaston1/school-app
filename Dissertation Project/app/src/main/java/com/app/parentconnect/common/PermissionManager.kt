package com.app.parentconnect.common

import com.app.parentconnect.common.CacheManager.CURRENT_USER
import com.app.parentconnect.common.CacheManager.CURRENT_USER_PERMS

object PermissionManager {
    val isLoggedIn: Boolean
        get() = CURRENT_USER.isNotEmpty()

    fun canPublishNews(): Boolean {
        return if (!isLoggedIn) false else CURRENT_USER_PERMS === Constants.ADMIN_USER
                || CURRENT_USER_PERMS === Constants.EDITOR_USER
    }
    fun canDeleteItems(): Boolean {
        return if (!isLoggedIn) false else CURRENT_USER_PERMS === Constants.ADMIN_USER
    }
}

