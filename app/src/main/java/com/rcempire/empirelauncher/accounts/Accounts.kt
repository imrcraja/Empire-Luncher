package com.rcempire.empirelauncher.accounts

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

enum class AccountType { MICROSOFT, OFFLINE, ELY_BY, AUTHLIB_INJECTOR }

data class Account(
    val id: String, val username: String, val uuid: String,
    val type: AccountType, val accessToken: String = "", val refreshToken: String = ""
)

object OfflineAccounts {
    /** Same algorithm vanilla servers use for offline-mode UUIDs. */
    fun create(username: String): Account {
        require(username.matches(Regex("^[A-Za-z0-9_]{3,16}$"))) { "Username must be 3-16 chars: letters, digits, _" }
        val uuid = UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray(Charsets.UTF_8)).toString()
        return Account(uuid, username, uuid, AccountType.OFFLINE)
    }
}

/** Tokens are stored encrypted (AES-256) via the Android Keystore. */
class SecureAccountStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context, "empire_accounts",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    fun save(a: Account) = prefs.edit()
        .putString(a.id, listOf(a.username, a.uuid, a.type.name, a.accessToken, a.refreshToken).joinToString("\u0001"))
        .apply()
    fun all(): List<Account> = prefs.all.mapNotNull { (k, v) ->
        val p = (v as? String)?.split("\u0001") ?: return@mapNotNull null
        if (p.size < 5) null else Account(k, p[0], p[1], AccountType.valueOf(p[2]), p[3], p[4])
    }
    fun remove(id: String) = prefs.edit().remove(id).apply()
}
