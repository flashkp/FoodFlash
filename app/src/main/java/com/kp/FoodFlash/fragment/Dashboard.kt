package com.kp.FoodFlash.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kp.FoodFlash.R
import com.kp.FoodFlash.adapter.DashboardAdapter
import com.kp.FoodFlash.model.Restaurant
import com.kp.FoodFlash.utils.ConnectionManager
import kotlinx.android.synthetic.main.sort_radio.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class Dashboard : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var dashboardAdapter: DashboardAdapter
    lateinit var editTextSearch: EditText
    lateinit var radioButtonView: View
    lateinit var dashboard_fragment_Progressdialog: RelativeLayout
    lateinit var dashboard_fragment_cant_find_restaurant: RelativeLayout
    var restaurantInfoList = arrayListOf<Restaurant>()
    var ratingComparator = Comparator<Restaurant> { rest1, rest2 ->
        if (rest1.restaurantRating.compareTo(rest2.restaurantRating, true) == 0) {
            rest1.restaurantName.compareTo(rest2.restaurantName, true)
        } else {
            rest1.restaurantRating.compareTo(rest2.restaurantRating, true)
        }

    }
    var costComparator = Comparator<Restaurant> { rest1, rest2 ->

        rest1.cost_for_one.compareTo(rest2.cost_for_one, true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        layoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recyclerViewDashboard)
        editTextSearch = view.findViewById(R.id.editTextSearch)
        var textViewfavourite: TextView? = view.findViewById(R.id.textViewfavourite)
        dashboard_fragment_Progressdialog =
            view.findViewById(R.id.dashboard_fragment_Progressdialog)
        dashboard_fragment_cant_find_restaurant =
            view.findViewById(R.id.dashboard_fragment_cant_find_restaurant)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            dashboard_fragment_Progressdialog.visibility = View.VISIBLE
            try {

                val queue = Volley.newRequestQueue(activity as Context)

                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

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
                                val restaurantObject = Restaurant(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one"),
                                    restaurantJsonObject.getString("image_url")
                                )
                                val resId = restaurantJsonObject.getString("id")
                                val resName = restaurantJsonObject.getString("name")
                                val resRating = restaurantJsonObject.getString("rating")
                                val restCost = restaurantJsonObject.getString("cost_for_one")
                                val resImage = restaurantJsonObject.getString("image_url")

                                restaurantInfoList.add(restaurantObject)
                                dashboardAdapter = DashboardAdapter(
                                    activity as Context,
                                    restaurantInfoList
                                )
                                recyclerView.adapter = dashboardAdapter
                                recyclerView.layoutManager = layoutManager
                            }

                        }
                        dashboard_fragment_Progressdialog.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        dashboard_fragment_Progressdialog.visibility = View.INVISIBLE

                        println("errrrror" + it)
                        Toast.makeText(
                            activity as Context,
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
                    activity as Context,
                    "Some Unexpected error occured!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }
        fun filterFun(strTyped: String) {
            val filteredList = arrayListOf<Restaurant>()
            for (item in restaurantInfoList) {
                if (item.restaurantName.toLowerCase().contains(strTyped.toLowerCase())) {

                    filteredList.add(item)

                }
            }
            if (filteredList.size == 0) {
                dashboard_fragment_cant_find_restaurant.visibility = View.VISIBLE
            } else {
                dashboard_fragment_cant_find_restaurant.visibility = View.INVISIBLE
            }
            dashboardAdapter.filter(filteredList)
        }
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(strTyped: Editable?) {
                filterFun(strTyped.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        }
        )
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.sort_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        when (id) {

            R.id.sort -> {
                radioButtonView = View.inflate(context, R.layout.sort_radio, null)
                androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                    .setTitle("Sort By?")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") { text, listener ->
                        if (radioButtonView.cost2.isChecked) {
                            Collections.sort(restaurantInfoList, costComparator)
                            restaurantInfoList.reverse()
                            dashboardAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.cost1.isChecked) {
                            Collections.sort(restaurantInfoList, costComparator)
                            dashboardAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_rating.isChecked) {
                            Collections.sort(restaurantInfoList, ratingComparator)
                            restaurantInfoList.reverse()
                            dashboardAdapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton("CANCEL") { text, listener ->

                    }
                    .create()
                    .show()
            }
        }

        return super.onOptionsItemSelected(item)
    }


}
