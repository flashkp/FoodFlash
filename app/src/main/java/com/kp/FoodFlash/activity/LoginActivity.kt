package com.kp.FoodFlash.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kp.FoodFlash.R
import org.json.JSONException
import org.json.JSONObject
import com.android.volley.Request
import com.kp.FoodFlash.utils.ConnectionManager

class LoginActivity : AppCompatActivity() {
    lateinit var etMobile: EditText
    lateinit var etPassword: EditText
    lateinit var loginBtn: Button
    lateinit var forgotPassword: TextView
    lateinit var txtRegister: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        setContentView(R.layout.activity_login)
        print(isLoggedIn)
        if (isLoggedIn) {
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        title = "Log In"
        etMobile = findViewById(R.id.etMobile)
        etPassword = findViewById(R.id.etPass)
        loginBtn = findViewById(R.id.btnLogIn)
        forgotPassword = findViewById(R.id.txtForgetPassword)
        txtRegister = findViewById(R.id.txtRegister)
        loginBtn.setOnClickListener {
            loginBtn.visibility=View.INVISIBLE
            if (etMobile.text.isBlank()) {
                etMobile.setError("Mobile Number Missing")
                loginBtn.visibility=View.VISIBLE
            }
            else {
                if (etPassword.text.isBlank()) {
                    loginBtn.visibility = View.VISIBLE
                    etPassword.setError("Missing Password")
                } else {
                    if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
                        try {
                            val MobileNumber = etMobile.text.toString()
                            val password = etPassword.text.toString()
                            val queue = Volley.newRequestQueue(this@LoginActivity)
                            val url = "http://13.235.250.119/v2/login/fetch_result"
                            val jsonParams = JSONObject()
                            jsonParams.put("mobile_number", MobileNumber)
                            jsonParams.put("password", password)
                            val jsonRequest = object :
                                JsonObjectRequest(Request.Method.POST, url, jsonParams,
                                    Response.Listener {
                                        val res = it.getJSONObject("data")
                                        val success: Boolean = res.getBoolean("success")
                                        if (success) {
                                            val foodJSONObject = res.getJSONObject("data")
                                            var user_id = foodJSONObject.getString("user_id")
                                            var name = foodJSONObject.getString("name")
                                            var email = foodJSONObject.getString("email")
                                            var address = foodJSONObject.getString("address")
                                            sharedPreferences.edit().putBoolean("isLoggedIn", true)
                                                .apply()
                                            sharedPreferences.edit().putString("user_id", user_id)
                                                .apply()
                                            sharedPreferences.edit().putString("user_name", name)
                                                .apply()
                                            sharedPreferences.edit().putString("email", email)
                                                .apply()
                                            sharedPreferences.edit().putString("address", address)
                                                .apply()
                                            sharedPreferences.edit()
                                                .putString("mobile", MobileNumber)
                                                .apply()
                                            val intent =
                                                Intent(this@LoginActivity, HomeActivity::class.java)
                                            startActivity(intent)
                                            finishAffinity()
                                        } else {
                                            Toast.makeText(
                                                this@LoginActivity as Context,
                                                "Login Failed. Check Mobile Number or Password",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }, Response.ErrorListener {

                                    }) {
                                override fun getHeaders(): MutableMap<String, String> {
                                    val headers = HashMap<String, String>()
                                    headers["Content-Type"] = "application/json"
                                    headers["token"] = "4dabd3b987382b"
                                    return headers
                                }
                            }
                            queue.add(jsonRequest)
                        } catch (e: JSONException) {

                            Toast.makeText(
                                this@LoginActivity,
                                "Some unexpected error occured!!!",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    } else {
                        val alterDialog =
                            androidx.appcompat.app.AlertDialog.Builder(this@LoginActivity)
                        alterDialog.setTitle("No Internet")
                        alterDialog.setMessage("Internet Connection can't be establish!")
                        alterDialog.setPositiveButton("Open Settings") { text, listener ->
                            val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                            startActivity(settingsIntent)
                        }

                        alterDialog.setNegativeButton("Exit") { text, listener ->
                            ActivityCompat.finishAffinity(this@LoginActivity)
                        }
                        alterDialog.setCancelable(false)

                        alterDialog.create()
                        alterDialog.show()
                    }
                }
            }
        }
        forgotPassword.setOnClickListener(View.OnClickListener {
            val fp = Intent(this@LoginActivity, ForgotActivity::class.java)
            startActivity(fp)

        })


        txtRegister.setOnClickListener(View.OnClickListener {
            val tr = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(tr)

        })
    }
}