package com.salma.apitester

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val testBtn = findViewById<Button>(R.id.test_btn)
        val radioGroup = findViewById<RadioGroup>(R.id.rb_group)
        radioGroup.setOnCheckedChangeListener { _, id ->
            Log.d(TAG, "setOnCheckedChangeListener")
            val bodyEt= findViewById<EditText>(R.id.body_et)
            if (id == R.id.post_rb) {
                Log.d(TAG, "POST Checked")
                bodyEt.visibility= View.VISIBLE
            } else {
                Log.d(TAG, "GET Checked")
                bodyEt.visibility= View.INVISIBLE
            }
        }
        testBtn.setOnClickListener {
            val selectedRequest = radioGroup.checkedRadioButtonId
            resetViews()
            if (selectedRequest == R.id.post_rb){
                postRequest()
            }
            else{
                getRequest()
            }
        }
    }

    private fun getRequest(){
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

        if(checkInternetConnection()){
            val url = URL(urlInput)
            //Create HTTP connection
            val conn = url.openConnection() as HttpURLConnection

            // Create Background Thread
            val runnable = Runnable{

            }
            val thread= Thread(runnable)
            thread.start()

        }
        else{
            Log.d(TAG, "no internet connection")
            errorTv.text= getString(R.string.no_internet_error)
        }
    }

    private fun postRequest(){
        Log.d(TAG, "---POST Request---")
    }

    private fun checkInternetConnection(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork != null
    }

    private fun resetViews(){
        val responseCodeTv = findViewById<TextView>(R.id.responsecode_tv)
        val headerTv = findViewById<TextView>(R.id.header_tv)
        val bodyTv = findViewById<TextView>(R.id.body_tv)
        val errorTv = findViewById<TextView>(R.id.errors_tv)

        responseCodeTv.text = ""
        headerTv.text = ""
        bodyTv.text = ""
        errorTv.text = ""

    }

    companion object {
        private const val TAG = "MainActivity"
    }
}