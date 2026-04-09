package com.example.dev_mobile.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// Android bloque la connexion car il ne peut pas verifier l'identité du serveur et des certificats autosignés., c pour ca on utilise trustManager
// qui accept toutes les connexions et certificats
private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
})

object RetrofitClient {

    private const val BASE_URL = "https://162.38.111.34:4000/"

    private var _cookieJar: AuthCookieJar? = null

    /**
     * Initialise le client avec le contexte de l'application.
     * Appelé dans MainActivity pour permettre la sauvegarde des tokens.
     */
    fun init(context: Context) {
        if (_cookieJar == null) {
            _cookieJar = AuthCookieJar(context)
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // accepte la connexion meme si le nom de l'url c pas le meme que celui dans les certif
            .cookieJar(_cookieJar ?: throw IllegalStateException("RetrofitClient non initialisé. Appelez init(context) dans MainActivity."))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getCookieJar(): AuthCookieJar = _cookieJar ?: throw IllegalStateException("RetrofitClient non initialisé")
}