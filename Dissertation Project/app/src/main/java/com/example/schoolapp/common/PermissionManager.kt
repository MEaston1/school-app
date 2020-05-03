package com.example.schoolapp.common

import com.example.schoolapp.common.CacheManager.CURRENT_USER

object PermissionManager {
    val isLoggedIn: Boolean
        get() = CURRENT_USER.isNotEmpty()

    fun canOnlyRead(): Boolean {
        return if (!isLoggedIn) false else CURRENT_USER != Constants.ADMIN_EMAIL
                || CURRENT_USER != Constants.EDITOR_1_EMAIL
    }

    fun canPublishNews(): Boolean {
        return if (!isLoggedIn) false else CURRENT_USER === Constants.ADMIN_EMAIL
    }
    fun canEditNews(): Boolean {
        return if (!isLoggedIn) false else CURRENT_USER === Constants.ADMIN_EMAIL
                || CURRENT_USER === Constants.EDITOR_1_EMAIL
                || CURRENT_USER === Constants.EDITOR_1_EMAIL
    }
    fun canDeleteNews(): Boolean {
        return if (!isLoggedIn) false else CURRENT_USER === Constants.ADMIN_EMAIL
    }
}