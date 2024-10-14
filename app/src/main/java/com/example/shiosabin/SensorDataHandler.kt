package com.example.shiosabin

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object SensorDataHandler {
    suspend fun fetchFromApi(urlString: String, context: Context): List<String> {
        // SharedPreferencesを取得
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("SensorPrefs", Context.MODE_PRIVATE)
        // センサーIDを読み取る
        val sensorID = sharedPreferences.getString("SENSOR_ID", null)

        return try {
            withContext(Dispatchers.IO) {
                val url = URL(urlString + sensorID)
                val con = url.openConnection() as HttpURLConnection
                con.connectTimeout = 30_000
                con.readTimeout = 30_000
                con.requestMethod = "GET"
                con.connect()

                if (con.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext listOf("Error: Response Code ${con.responseCode}")
                }

                val str = con.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val jsonArray = JSONArray(str)

                // JSONArray の内容を List<String> に変換
                val resultList = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    resultList.add(jsonArray.get(i).toString())
                }

                resultList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listOf("Error: ${e.message}")
        }
    }
}
