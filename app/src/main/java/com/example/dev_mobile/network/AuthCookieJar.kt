
package com.example.dev_mobile.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class AuthCookieJar : CookieJar {

    private val cookies = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { newCookie ->
            this.cookies.removeAll { it.name == newCookie.name }
            this.cookies.add(newCookie)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies.toList()

    fun clearAll() = cookies.clear()

    fun hasSession(): Boolean = cookies.any { it.name == "access_token" }
}