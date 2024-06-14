package com.example.remoteappdemo

import android.R.attr
import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.Socket
import javax.net.ssl.SSLSocketFactory


class Connection(private val context: Context) {
/*    fun connectToDevice(host: String, port: Int): SSLSocket? {
        try {
            // Load the custom KeyStore from the raw resources
            val trustedKeyStore = KeyStore.getInstance("PKCS12")
            val keyStoreInputStream: InputStream = context.resources.openRawResource(R.raw.keystore)
            trustedKeyStore.load(keyStoreInputStream, "passw".toCharArray())
            keyStoreInputStream.close()

            // Initialize KeyManagerFactory with the loaded KeyStore
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(trustedKeyStore, "passw".toCharArray())

            // Initialize SSLContext to use TLS v1.2
            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(keyManagerFactory.keyManagers, null, null)

            // Retrieve the SSLSocketFactory from the SSLContext
            val customFactory = sslContext.socketFactory

            // Create an SSL socket using the custom factory
            return customFactory.createSocket(host, port) as SSLSocket
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }*/

  fun connectToDevice(host: String, port: Int) {
      try {
          val socketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
          val socket = socketFactory.createSocket(host, port)
          if (socket.isConnected) {
              // Connection established!
              // ... (your data transfer code)
              try {
                  // Send command to TV
                  val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                  writer.write("power")
                  writer.flush()
              } catch (e: Exception) {
                  e.printStackTrace()
              }
          } else {
              // Connection failed!
              Log.e("Socket connection", "Failed to connect to device")
          }

          // ... (your data transfer and close logic)
      } catch (e: IOException) {
          Log.e("Socket connection", "Error creating socket: " + e.message)
      }
  }

}
