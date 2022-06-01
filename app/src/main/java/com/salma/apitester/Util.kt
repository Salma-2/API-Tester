package com.salma.apitester

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class Util {
    companion object {
        fun headerBuilder(str: String): HashMap<String, String> {
            val headers = HashMap<String, String>()
            val pattern = "([\\w-]+): (.*)"
            val headersList = str.split(",,").filter { item -> item != "" }

            for (header in headersList) {
                val isValid = Regex(pattern).matches(header)
                Log.d("HeaderBuilder", isValid.toString())
                if (isValid) {
                    val (key, value) = Regex(pattern).find(header)!!.destructured
                    headers[key] = value
                }
            }

            // TODO: Check that all headers are valid
            return headers
        }

        fun responseBuilder(input: InputStream): StringBuilder {
            val response = StringBuilder()
            val inputStream = InputStreamReader(input)
            val br = BufferedReader(inputStream)
            var responseLine = br.readLine()
            while (responseLine != null) {
                response.append(responseLine)
                responseLine = br.readLine()
            }
            inputStream.close()
            br.close()
            return response
        }

        fun checkInternetConnection(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetwork != null
        }
    }
}
