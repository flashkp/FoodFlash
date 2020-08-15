package com.kp.FoodFlash.activity

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kp.FoodFlash.R
import com.kp.FoodFlash.adapter.DashboardAdapter
import com.kp.FoodFlash.model.RestaurantMenu
import com.kp.FoodFlash.utils.ConnectionManager
import org.json.JSONException
import com.kp.FoodFlash.adapter.MenuAdapter
import com.kp.FoodFlash.database.ResDatabase
import com.kp.FoodFlash.database.ResEntity
import com.kp.FoodFlash.model.Restaurant

lateinit var toolbar3: Toolbar
lateinit var recyclerView: RecyclerView
lateinit var layoutManager: RecyclerView.LayoutManager
lateinit var menuAdapter: MenuAdapter
lateinit var resId: String
lateinit var resName: String
lateinit var resRating:String
lateinit var cost_for_one:String
lateinit var restaurantImage:String
lateinit var proceedToCartLayout: RelativeLayout
lateinit var btnProceedToCart: Button
lateinit var activity_Progress: RelativeLayout
lateinit var textViewFavourite:TextView
var restaurantMenuList = arrayListOf<RestaurantMenu>()
var restaurantInfoList = arrayListOf<Restaurant>()

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        textViewFavourite=findViewById(R.id.textViewfav)
        proceedToCartLayout = findViewById(R.id.relativeLayoutProceedToCart)
        btnProceedToCart = findViewById(R.id.buttonProceedToCart)
        activity_Progress = findViewById(R.id.menu_Progress)
        toolbar3 = findViewById(R.id.toolBar)
        resId = intent.getStringExtra("restaurantId")
        resName = intent.getStringExtra("restaurantName")
        resRating = intent.getStringExtra("resRating")
        cost_for_one = intent.getStringExtra("cost")
        restaurantImage = intent.getStringExtra("image")
        val resEntity = ResEntity(
            resId,
            resName,
            resRating,
            cost_for_one,
            restaurantImage
        )
        setSupportActionBar(toolbar3)
        supportActionBar?.title = resName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        layoutManager = LinearLayoutManager(this@MenuActivity)
        recyclerView = findViewById(R.id.recyclerViewMenu)
        val checkFav = DashboardAdapter.DBAsyncTask(this@MenuActivity, resEntity, 1).execute()
        val isFav = checkFav.get()
        if (isFav) {
            textViewFavourite.setTag("liked")
            textViewFavourite.setBackgroundResource(R.drawable.ic_isfav)
        } else {
            textViewFavourite.setTag("unliked")
            textViewFavourite.setBackgroundResource(R.drawable.ic_favourites)
        }
        textViewFavourite.setOnClickListener {
            if (!DashboardAdapter.DBAsyncTask(this@MenuActivity, resEntity, 1).execute().get()) {
                val async = DashboardAdapter.DBAsyncTask(this@MenuActivity, resEntity, 2).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(this@MenuActivity, "Added to Favourites :)", Toast.LENGTH_SHORT).show()
                    textViewFavourite.setTag("liked")
                    textViewFavourite.setBackgroundResource(R.drawable.ic_isfav)
                } else {
                    Toast.makeText(this@MenuActivity, "Some Error Occurred", Toast.LENGTH_LONG).show()
                }
            } else {
                val async = DashboardAdapter.DBAsyncTask(this@MenuActivity, resEntity, 3).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(this@MenuActivity, "Removed from Favourites", Toast.LENGTH_SHORT).show()
                    textViewFavourite.setTag("unliked")
                    textViewFavourite.setBackgroundResource(R.drawable.ic_favourites)
                } else {
                    Toast.makeText(this@MenuActivity as Context, "Some Error Occurred", Toast.LENGTH_LONG)
                        .show()

                }
            }
        }
        if (ConnectionManager().checkConnectivity(this@MenuActivity)) {
            activity_Progress.visibility = View.VISIBLE
            try {
                restaurantMenuList.clear()

                val queue = Volley.newRequestQueue(this@MenuActivity)

                val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {
                        println("Response1 is " + it)
                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")
                        if (success) {
                            val data = responseJsonObjectData.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantObject = RestaurantMenu(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("cost_for_one")

                                )
                                restaurantMenuList.add(restaurantObject)
                                menuAdapter = MenuAdapter(
                                    this@MenuActivity,
                                    resId,
                                    resName,
                                    proceedToCartLayout,
                                    btnProceedToCart,
                                    recyclerView,
                                    restaurantMenuList
                                )
                                recyclerView.adapter = menuAdapter
                                recyclerView.layoutManager = layoutManager
                            }

                        }
                        activity_Progress.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        activity_Progress.visibility = View.INVISIBLE

                        println("errrrror" + it)
                        Toast.makeText(
                            this@MenuActivity,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "4dabd3b987382b"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)
            } catch (e: JSONException) {
                Toast.makeText(
                    this@MenuActivity,
                    "Some Unexpected error occured!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this@MenuActivity)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@MenuActivity)
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }

    }
    override fun onBackPressed() {


        if(menuAdapter.getCount()>0) {


            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("Alert!")
            alterDialog.setMessage("Going back will remove everything from cart")
            alterDialog.setPositiveButton("Okay") { text, listener ->
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
            alterDialog.setNegativeButton("No") { text, listener ->

            }
            alterDialog.show()
        }else{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id=item.itemId

        when(id){
            android.R.id.home->{
                if(menuAdapter.getCount()>0) {

                    val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    alterDialog.setTitle("Alert!")
                    alterDialog.setMessage("Going back will remove everything from cart")
                    alterDialog.setPositiveButton("Okay") { text, listener ->
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }
                    alterDialog.setNegativeButton("No") { text, listener ->

                    }
                    alterDialog.show()
                }else{
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    class DBAsyncTask(val context: Context, val resEntity: ResEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, ResDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val restaurant: ResEntity? =
                        db.resDao().getRestaurantById(resEntity.restaurant_Id.toString())
                    db.close()
                    return restaurant != null
                }
                2 -> {

                    db.resDao().insertRestaurant(resEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.resDao().deleteRestaurant(resEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}