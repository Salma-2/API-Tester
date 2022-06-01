package com.salma.apitester

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val testBtn = findViewById<Button>(R.id.test_btn)
        val radioGroup = findViewById<RadioGroup>(R.id.rb_group)
        val errorTv = findViewById<TextView>(R.id.errors_tv)
        val dataEt = findViewById<EditText>(R.id.data_et)

        radioGroup.setOnCheckedChangeListener { _, id ->
            Log.d(TAG, "setOnCheckedChangeListener")
            if (id == R.id.post_rb) {
                Log.d(TAG, "POST Checked")
                dataEt.text.clear()
                dataEt.hint = getString(R.string.enter_request_body)
            } else {
                Log.d(TAG, "GET Checked")
                dataEt.text.clear()
                dataEt.hint = getString(R.string.enter_query_params)
            }
        }
        testBtn.setOnClickListener {
            val selectedRequest = radioGroup.checkedRadioButtonId
            resetTextViews()

            if (checkInternetConnection()) {
                if (selectedRequest == R.id.post_rb) {
                    if (dataEt.text.toString() != "") {
                        postRequest()
                    } else {
                        errorTv.text = getString(R.string.alert)
                    }
                } else if (selectedRequest == R.id.get_rb) {
                    getRequest()
                }
            } else {
                resetTextViews()
                Log.d(TAG, "no internet connection")
                errorTv.text = getString(R.string.no_internet_error)
            }

        }
    }

    private fun getRequest() {
        Log.d(TAG, "---GET Request---")
        /* ------- Get reference to the views ------- */
        // Inputs
        val urlInput = findViewById<EditText>(R.id.url_et).text.toString()
        val headerInput = findViewById<EditText>(R.id.header_et).text.toString()
        val queryInput = findViewById<EditText>(R.id.data_et).text.toString()


        // Create Background Thread
        val runnable = Runnable {
            //Create HTTP connection
            var conn: HttpURLConnection? = null
            var query = ""
            try {
                if (queryInput != "") {
                    val params = JSONObject(queryInput)
                    val builder: Uri.Builder = Uri.Builder()
                    for (param in params.keys()) {
                        builder.appendQueryParameter(param, params.get(param).toString())
                    }
                    query = builder.build().encodedQuery!!
                }

                val url = URL("$urlInput?$query")
                Log.d("url", url.toString())
                conn = url.openConnection() as HttpURLConnection
                //Add headers
                if (headerInput != "") {
                    val headers = JSONObject(headerInput)
                    for (headerKey in headers.keys()) {
                        conn.setRequestProperty(headerKey, headers.get(headerKey).toString())
                    }
                }
                val reqHeaders = conn.requestProperties
                val responseCode = conn.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {  // read the response
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val header = conn.headerFields

                    //For Logging
                    val reqInfo = buildString {
                        append("URL: ")
                        append(url)
                        append("\n")
                        append("Response Code: ")
                        append(responseCode)
                        append("\n")
                        append("Request Header: ")
                        append(reqHeaders)
                    }
                    Log.d("GET Request Success", reqInfo)

                    //update UI
                    handler.post {
                        updateUI(responseCode.toString(), header.toString(), response)
                    }

                } else { //an error occurred
                    val resMsg = conn.responseMessage
                    val errorMessage = buildString {
                        append("Did not receive successful HTTP response, status code = ")
                        append(responseCode.toString())
                        append(", status message: [")
                        append(resMsg)
                        append("]")
                    }
                    // Update UI
                    handler.post {
                        updateUI(errorMessage = errorMessage, responseCode = responseCode.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                handler.post {
                    updateUI(errorMessage = e.message.toString())
                }
            } finally {
                conn?.disconnect()
            }

        }
        val thread = Thread(runnable)
        thread.start()
    }

    private fun postRequest() {
        Log.d(TAG, "---POST Request---")
        /* ------- Get reference to the views ------- */
        // Inputs
        val urlInput = findViewById<EditText>(R.id.url_et).text.toString()
        val headerInput = findViewById<EditText>(R.id.header_et).text.toString()
        val bodyInput = findViewById<EditText>(R.id.data_et).text.toString()

        //Create Background thread
        val runnable = Runnable {
            // Create Http Connection
            var conn: HttpURLConnection? = null
            try {
                val url = URL(urlInput)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                // to be able to write content to the connection output stream
                conn.doOutput = true
                conn.useCaches = true

                //Add headers
                conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )  //to send the request body in JSON format
                conn.setRequestProperty("Accept", "application/json")
                if (headerInput != "") {
                    val headers = JSONObject(headerInput)
                    for (headerKey in headers.keys()) {
                        conn.setRequestProperty(headerKey, headers.get(headerKey).toString())
                    }
                }

                val reqHeaders = conn.requestProperties
                Log.d("Req Header", reqHeaders.toString())

                //send body request
                val jsonObject = JSONObject(bodyInput)
                val wr = DataOutputStream(conn.outputStream)
                wr.writeBytes(jsonObject.toString())
                wr.flush()
                wr.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val header = conn.headerFields

                    //For Logging
                    val reqInfo = buildString {
                        append("Response Code: ")
                        append(responseCode)
                        append("\n")
                        append("Request Header: ")
                        append(reqHeaders)
                        append("\n")
                        append("Request Body: ")
                        append(jsonObject)
                    }
                    Log.d("POST Request Success", reqInfo)

                    handler.post {
                        updateUI(responseCode.toString(), header.toString(), response)
                    }

                } else {
                    val resMsg = conn.responseMessage
                    val errorMessage = buildString {
                        append("Did not receive successful HTTP response, status code = ")
                        append(responseCode.toString())
                        append(", status message: [")
                        append(resMsg)
                        append("]")
                    }
                    // Update UI
                    handler.post {
                        updateUI(errorMessage = errorMessage, responseCode = responseCode.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                handler.post { updateUI(errorMessage = e.message.toString()) }

            } finally {
                conn?.disconnect()
            }
        }
        val thread = Thread(runnable)
        thread.start()
    }

    private fun resetTextViews() {
        val responseCodeTv = findViewById<TextView>(R.id.responsecode_tv)
        val headerTv = findViewById<TextView>(R.id.header_tv)
        val bodyTv = findViewById<TextView>(R.id.body_tv)
        val errorTv = findViewById<TextView>(R.id.errors_tv)

        responseCodeTv.text = ""
        headerTv.text = ""
        bodyTv.text = ""
        errorTv.text = ""
    }

    private fun checkInternetConnection(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork != null
    }

    private fun updateUI(
        responseCode: String = "",
        header: String = "",
        response: String = "",
        errorMessage: String = ""
    ) {
        val responseCodeTv = findViewById<TextView>(R.id.responsecode_tv)
        val headerTv = findViewById<TextView>(R.id.header_tv)
        val bodyTv = findViewById<TextView>(R.id.body_tv)
        val errorTv = findViewById<TextView>(R.id.errors_tv)

        responseCodeTv.text = responseCode
        headerTv.text = header
        bodyTv.text = response
        errorTv.text = errorMessage
    }
}