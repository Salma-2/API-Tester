package com.salma.apitester

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
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

        radioGroup.setOnCheckedChangeListener { _, id ->
            Log.d(TAG, "setOnCheckedChangeListener")
            val bodyEt = findViewById<EditText>(R.id.body_et)
            if (id == R.id.post_rb) {
                Log.d(TAG, "POST Checked")
                bodyEt.visibility = View.VISIBLE
            } else {
                Log.d(TAG, "GET Checked")
                bodyEt.visibility = View.INVISIBLE
            }
        }
        testBtn.setOnClickListener {
            val selectedRequest = radioGroup.checkedRadioButtonId
            resetViews()

            if (Util.checkInternetConnection(this)) {
                if (selectedRequest == R.id.post_rb) {
                    postRequest()
                } else if (selectedRequest == R.id.get_rb) {
                    getRequest()
                }
            } else {
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
        // Build Headers
        val headers = Util.headerBuilder(headerInput)
        // Create Background Thread
        val runnable = Runnable {
            //Create HTTP connection
            var conn: HttpURLConnection? = null
            try {
                val url = URL(urlInput)
                conn = url.openConnection() as HttpURLConnection

                //Add headers
                for (headerKey in headers.keys) {
                    conn.setRequestProperty(headerKey, headers[headerKey])
                }
                val reqHeaders = conn.requestProperties
                Log.d(TAG, "Request Header: $reqHeaders")

                val responseCode = conn.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {  // read the response
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val header = conn.headerFields

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
                        updateUI(errorMessage = errorMessage)
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
        val bodyInput = findViewById<EditText>(R.id.body_et).text.toString()
        // Build Headers
        val headers = Util.headerBuilder(headerInput)
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

                //Add headers
                conn.setRequestProperty("Content-Type", "application/json")  //to send the request body in JSON format
                conn.setRequestProperty("Accept", "application/json")
                for (header in headers.keys) {
                    conn.setRequestProperty(header, headers[header])
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
                        updateUI(errorMessage = errorMessage)
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

    private fun resetViews() {
        val responseCodeTv = findViewById<TextView>(R.id.responsecode_tv)
        val headerTv = findViewById<TextView>(R.id.header_tv)
        val bodyTv = findViewById<TextView>(R.id.body_tv)
        val errorTv = findViewById<TextView>(R.id.errors_tv)

        responseCodeTv.text = ""
        headerTv.text = ""
        bodyTv.text = ""
        errorTv.text = ""
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

        responseCodeTv.text = responseCode.toString()
        headerTv.text = header
        bodyTv.text = response
        errorTv.text = errorMessage
    }
}