
package com.example.dev_mobile.network

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Gestionnaire de cookies persistant pour le mode Offline First.
 */
class AuthCookieJar(context: Context) : CookieJar {

    // stocker dans le disque dur pour pouvoir rallumer l'app sans se connecter à nouveau
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_cookies_storage", Context.MODE_PRIVATE)
    private val gson = Gson()
    // stocker dans la Ram (c plus rapide)
    private val cookies = mutableListOf<Cookie>()

    init {
        // AU DÉMARRAGE : On recharge les cookies depuis le disque
        val json = prefs.getString("cookies_json", null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<SerializableCookie>>() {}.type
                val savedList: List<SerializableCookie> = gson.fromJson(json, type)
                cookies.addAll(savedList.map { it.toCookie() })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        synchronized(this) {
            cookies.forEach { newCookie ->
                this.cookies.removeAll { it.name == newCookie.name }
                this.cookies.add(newCookie)
            }
            // SAUVEGARDE SUR LE DISQUE
            val json = gson.toJson(this.cookies.map { SerializableCookie(it) })
            prefs.edit().putString("cookies_json", json).apply()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        // On ne renvoie que les cookies valides (non expirés)
        return cookies.filter { it.matches(url) && it.expiresAt > now }
    }

    /**
     * APPELÉ AU LOGOUT : Efface tout en mémoire ET sur le disque.
     */
    fun clearAll() {
        synchronized(this) {
            cookies.clear()
            prefs.edit().remove("cookies_json").apply()
        }
    }

    fun hasSession(): Boolean {
        val now = System.currentTimeMillis()
        return cookies.any { it.name == "access_token" && it.expiresAt > now }
    }
}

/**
 * Classe utilitaire pour stocker les cookies OkHttp en JSON.
 */
data class SerializableCookie(
    val name: String, val value: String, val expiresAt: Long,
    val domain: String, val path: String, val secure: Boolean,
    val httpOnly: Boolean, val hostOnly: Boolean
) {
    constructor(c: Cookie) : this(c.name, c.value, c.expiresAt, c.domain, c.path, c.secure, c.httpOnly, c.hostOnly)

    fun toCookie(): Cookie = Cookie.Builder()
        .name(name).value(value).expiresAt(expiresAt).domain(domain).path(path)
        .let { if (secure) it.secure() else it }
        .let { if (httpOnly) it.httpOnly() else it }
        .build()
}
