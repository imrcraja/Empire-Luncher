package com.rcempire.empirelauncher.accounts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class DeviceCode(val userCode: String, val deviceCode: String, val verificationUri: String, val intervalSec: Int, val expiresInSec: Int)

/**
 * Microsoft -> Xbox Live -> XSTS -> Minecraft services login (device-code flow).
 * clientId: register an Azure app (public client, "consumers" audience) and set MS_CLIENT_ID.
 */
class MicrosoftAuth(private val http: OkHttpClient, private val clientId: String) {
    private val json = "application/json".toMediaType()
    private val tenant = "https://login.microsoftonline.com/consumers/oauth2/v2.0"

    suspend fun requestDeviceCode(): DeviceCode = withContext(Dispatchers.IO) {
        val body = FormBody.Builder().add("client_id", clientId)
            .add("scope", "XboxLive.signin offline_access").build()
        val o = call(Request.Builder().url("$tenant/devicecode").post(body).build())
        DeviceCode(o.getString("user_code"), o.getString("device_code"),
            o.getString("verification_uri"), o.optInt("interval", 5), o.optInt("expires_in", 900))
    }

    /** Polls until the user finishes sign-in in the browser. Returns a ready Minecraft account. */
    suspend fun pollAndLogin(dc: DeviceCode): Account {
        val deadline = System.currentTimeMillis() + dc.expiresInSec * 1000L
        while (System.currentTimeMillis() < deadline) {
            delay(dc.intervalSec * 1000L)
            val body = FormBody.Builder().add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                .add("client_id", clientId).add("device_code", dc.deviceCode).build()
            val o = withContext(Dispatchers.IO) { callRaw(Request.Builder().url("$tenant/token").post(body).build()) }
            when (o.optString("error")) {
                "authorization_pending" -> continue
                "" -> return minecraftLogin(o.getString("access_token"), o.optString("refresh_token"))
                else -> error("Login failed: ${o.optString("error_description")}")
            }
        }
        error("Login timed out")
    }

    private suspend fun minecraftLogin(msToken: String, refresh: String): Account = withContext(Dispatchers.IO) {
        val xbl = postJson("https://user.auth.xboxlive.com/user/authenticate", JSONObject()
            .put("Properties", JSONObject().put("AuthMethod", "RPS")
                .put("SiteName", "user.auth.xboxlive.com").put("RpsTicket", "d=$msToken"))
            .put("RelyingParty", "http://auth.xboxlive.com").put("TokenType", "JWT"))
        val xblToken = xbl.getString("Token")
        val uhs = xbl.getJSONObject("DisplayClaims").getJSONArray("xui").getJSONObject(0).getString("uhs")

        val xsts = postJson("https://xsts.auth.xboxlive.com/xsts/authorize", JSONObject()
            .put("Properties", JSONObject().put("SandboxId", "RETAIL").put("UserTokens", JSONArray().put(xblToken)))
            .put("RelyingParty", "rp://api.minecraftservices.com/").put("TokenType", "JWT"))

        val mc = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", JSONObject()
            .put("identityToken", "XBL3.0 x=$uhs;${xsts.getString("Token")}"))
        val mcToken = mc.getString("access_token")

        val profile = call(Request.Builder().url("https://api.minecraftservices.com/minecraft/profile")
            .header("Authorization", "Bearer $mcToken").build())
        Account(profile.getString("id"), profile.getString("name"), profile.getString("id"),
            AccountType.MICROSOFT, mcToken, refresh)
    }

    private fun postJson(url: String, body: JSONObject) =
        call(Request.Builder().url(url).header("Accept", "application/json")
            .post(body.toString().toRequestBody(json)).build())

    private fun call(r: Request): JSONObject {
        val o = callRaw(r)
        if (o.has("error") && !o.has("access_token")) error("Auth error: ${o.optString("error")} ${o.optString("errorMessage")}")
        return o
    }
    private fun callRaw(r: Request): JSONObject =
        http.newCall(r).execute().use { JSONObject(it.body?.string().orEmpty().ifBlank { "{}" }) }
}
