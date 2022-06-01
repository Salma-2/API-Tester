package com.salma.apitester

import android.content.Context
import android.net.ConnectivityManager
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
        // ---get reference to the views ---
        //Edit Text
        val urlInput = findViewById<EditText>(R.id.url_et).text.toString()
        val headerInput = findViewById<EditText>(R.id.header_et).text.toString()
        //Text Views
        val responseCodeTv = findViewById<TextView>(R.id.responsecode_tv)
        val headerTv = findViewById<TextView>(R.id.header_tv)
        val bodyTv = findViewById<TextView>(R.id.body_tv)
        val errorTv = findViewById<TextView>(R.id.errors_tv)

        val headers = Util.headerBuilder(headerInput)

        // Create Background Thread
        val runnable = Runnable {
            //Create HTTP connection

//              TODO -> isValidURL try/catch/finally(disconnect)

            var conn:HttpURLConnection? = null
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
                    val response = Util.responseBuilder(conn.inputStream)
                    val header = conn.headerFields
                    //update UI
                    handler.post {
                        responseCodeTv.text = responseCode.toString()
                        headerTv.text = header.toString()
                        bodyTv.text = response
                    }

                } else { //an error occurred
                    // Update UI
                    handler.post {
                        responseCodeTv.text = responseCode.toString()
                        errorTv.text =
                            buildString {
                                append("Did not receive successful HTTP response, status code = ")
                                append(responseCodeTv.text)
                                append(", status message: [")
                                append(conn.responseMessage)
                                append("]")
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                handler.post{
                    errorTv.text = e.message.toString()
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

        //Create Background thread
        val runnable = Runnable {
            //
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


}