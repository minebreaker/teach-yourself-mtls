package rip.deadcode.tymtls.clinet

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.security.KeyStore


private const val trustStore = "serverca.keystore"
private const val trustStorePass = "hogehoge"
private const val keyStore = "client.keystore"
private const val keyStorePass = "hogehoge"

fun main() {

    val transport = NetHttpTransport.Builder()
        .trustCertificates(
            KeyStore.getInstance(Paths.get(trustStore).toFile(), trustStorePass.toCharArray()),
            KeyStore.getInstance(Paths.get(keyStore).toFile(), keyStorePass.toCharArray()),
            keyStorePass
        )
        .build()

    val response = transport.createRequestFactory()
        .buildGetRequest(GenericUrl("https://localhost:8080"))
        .execute()

    val responseBody = response.content.readAllBytes().toString(StandardCharsets.UTF_8)
    println(responseBody)
}
