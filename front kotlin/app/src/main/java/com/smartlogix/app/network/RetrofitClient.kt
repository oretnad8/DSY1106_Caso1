package com.smartlogix.app.network

import com.smartlogix.app.SmartLogixApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Cliente Retrofit centralizado para todos los microservicios
 * Gestiona la configuración de red, interceptores y autenticación
 */
object RetrofitClient {

    // URLs base de los microservicios
    private const val BASE_URL_AUTH = "http://dantero.ddns.net:8081/"
    private const val BASE_URL_USUARIOS = "http://dantero.ddns.net:8082/"
    private const val BASE_URL_PRODUCTOS = "http://dantero.ddns.net:8083/"
    private const val BASE_URL_UBICACIONES = "http://dantero.ddns.net:8084/"
    private const val BASE_URL_APROBACIONES = "http://dantero.ddns.net:8085/"

    /**
     * Interceptor para agregar el token de autenticación a todas las peticiones
     * Lee directamente de SharedPreferences para garantizar que siempre se usa el último token
     */
    private val authInterceptor = Interceptor { chain: Interceptor.Chain ->
        val context = SmartLogixApplication.instance
        val prefs = context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val url = chain.request().url.toString()

        android.util.Log.d("RetrofitClient", "--> Interceptando: $url")
        if (token == null) {
            android.util.Log.w("RetrofitClient", "!!! TOKEN ES NULL para: $url")
        } else {
            android.util.Log.d("RetrofitClient", "Token encontrado (length: ${token.length})")
        }

        val requestBuilder = chain.request().newBuilder()
        
        // Agregar token si existe
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        
        // Agregar Content-Type
        requestBuilder.addHeader("Content-Type", "application/json")
        
        val response = chain.proceed(requestBuilder.build())

        android.util.Log.d("RetrofitClient", "<-- Codigo: ${response.code} para: $url")

        // Manejar errores de autenticación globalmente (401 Unauthorized)
        if (response.code == 401 && token != null) {
            android.util.Log.e("RetrofitClient", "SESIÓN EXPIRADA (401) en $url - Limpiando token")
            val app = SmartLogixApplication.instance
            app.clearAuthToken()
            
            // Notificar al sistema que la sesión expiró para redirigir al login
            CoroutineScope(Dispatchers.Main).launch {
                app.emitSessionExpired()
            }
        } else if (response.code == 403) {
            android.util.Log.w("RetrofitClient", "ACCESO PROHIBIDO (403) en $url - Verifique permisos del rol")
        }

        response
    }

    /**
     * Interceptor para logging (solo en debug)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP compartido con interceptores
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Crea una instancia de Retrofit para una URL base específica
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ========== SERVICIOS API ==========

    val authService: AuthApiService by lazy {
        createRetrofit(BASE_URL_AUTH).create(AuthApiService::class.java)
    }

    val usuariosService: UsuariosApiService by lazy {
        createRetrofit(BASE_URL_USUARIOS).create(UsuariosApiService::class.java)
    }

    val productosService: ProductosApiService by lazy {
        createRetrofit(BASE_URL_PRODUCTOS).create(ProductosApiService::class.java)
    }

    val ubicacionesService: UbicacionesApiService by lazy {
        createRetrofit(BASE_URL_UBICACIONES).create(UbicacionesApiService::class.java)
    }

    val aprobacionesService: AprobacionesApiService by lazy {
        createRetrofit(BASE_URL_APROBACIONES).create(AprobacionesApiService::class.java)
    }

    private const val BASE_URL_PEDIDOS = "http://dantero.ddns.net:8086/"

    val pedidosService: PedidosApiService by lazy {
        createRetrofit(BASE_URL_PEDIDOS).create(PedidosApiService::class.java)
    }
}


