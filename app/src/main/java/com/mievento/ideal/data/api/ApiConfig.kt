package com.mievento.ideal.data.api

object ApiConfig {
    // 🔥 TUS IPs DESPLEGADAS
    private const val API_SERVER_IP = "3.208.254.174"

    // URLs principales
    const val BASE_URL = "http://$API_SERVER_IP/api/"
    const val UPLOAD_URL = "http://$API_SERVER_IP/api/events/upload-image"
    const val IMAGES_BASE_URL = "http://$API_SERVER_IP"

    // Si usas HTTPS, cambia a:
    // const val BASE_URL = "https://$API_SERVER_IP/api/"
    // const val UPLOAD_URL = "https://$API_SERVER_IP/api/events/upload-image"
    // const val IMAGES_BASE_URL = "https://$API_SERVER_IP"

    // Para desarrollo local (comentado)
    // const val BASE_URL = "http://10.0.2.2:3000/api/"
    // const val UPLOAD_URL = "http://10.0.2.2:3000/api/events/upload-image"
}