package com.salma.apitester

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup

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
    }

    private fun postRequest(){
        Log.d(TAG, "---POST Request---")
    }
    companion object {
        private const val TAG = "MainActivity"
    }
}