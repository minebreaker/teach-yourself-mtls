package rip.deadcode.tymtls.server

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.SecureRequestCustomizer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.nio.file.Paths
import java.security.KeyStore


private const val trustStore = "clientca.keystore"
//private const val trustStorePass = "hogehoge"
private const val keyStore = "server.keystore"
private const val keyStorePass = "hogehoge"

fun main() {

    val server = Server()

    val sslContextFactory = SslContextFactory.Server().also {
        it.trustStore = KeyStore.getInstance(Paths.get(trustStore).toFile(), null as CharArray?)
//        it.setTrustStorePassword(trustStorePass)
        it.keyStore = KeyStore.getInstance(Paths.get(keyStore).toFile(), null as CharArray?)
        it.keyManagerPassword = keyStorePass
        it.needClientAuth = true
    }

    val httpConnectionFactory = HttpConnectionFactory(HttpConfiguration().also {
        it.addCustomizer(SecureRequestCustomizer())
    })

    val connector = ServerConnector(server, sslContextFactory, httpConnectionFactory).also {
        it.port = 8080
    }

    server.addConnector(connector)
    server.handler = object : AbstractHandler() {
        override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse,
        ) {
            response.writer.println("hello, world")
            response.writer.close()
        }
    }

    server.start()
}
