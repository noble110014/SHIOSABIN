package com.example.shiosabin

import android.os.Handler
import android.os.Looper
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale

class ApiHandler(private val urlGetText: String) {

    interface ApiCallback {
        fun onSuccess(result: String)
        fun onError(e: Exception)
    }

    private val handler = Handler(Looper.getMainLooper())

    fun fetchApiData(callback: ApiCallback) {
        Thread {
            var resultText = ""
            try {
                val response = getAPI()
                val rootJSON = JSONObject(response)
                resultText = rootJSON.toString()
                handler.post {
                    callback.onSuccess(resultText)
                }
            } catch (e: JSONException) {
                handler.post {
                    callback.onError(e)
                }
            } catch (e: Exception) {
                handler.post {
                    callback.onError(e)
                }
            }
        }.start()
    }

    private fun getAPI(): String {
        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var str = ""
        try {
            val url = URL(urlGetText)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 10000
            urlConnection.readTimeout = 10000
            urlConnection.addRequestProperty("User-Agent", "Android")
            urlConnection.addRequestProperty("Accept-Language", Locale.getDefault().toString())
            urlConnection.requestMethod = "GET"
            urlConnection.doInput = true
            urlConnection.doOutput = false
            urlConnection.connect()

            if (urlConnection.responseCode == 200) {
                inputStream = urlConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream, "utf-8"))
                var result = bufferedReader.readLine()
                while (result != null) {
                    str += result
                    result = bufferedReader.readLine()
                }
                bufferedReader.close()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            urlConnection?.disconnect()
        }
        return str
    }
}
