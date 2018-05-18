package com.loqoursys.meko.ui.fragments

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.loqoursys.meko.R
import com.loqoursys.meko.data.FoodItem
import com.loqoursys.meko.listener.MainClickListener
import com.loqoursys.meko.listener.OnFoodItemSelected
import com.loqoursys.meko.ui.DrinksAdapter
import com.loqoursys.meko.ui.view_model.FoodViewModel
import utils.DateTimeTemplate
import utils.mekoFoods
import java.util.*

/**
 * Created by root on 5/14/18 for LoqourSys
 */
class DrinksFragment : Fragment(), MainClickListener {

    private lateinit var listener: OnFoodItemSelected
    private lateinit var drinksAdapter: DrinksAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(FoodViewModel::class.java)
        viewModel.getFood(false).reObserve(this, observer)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFoodItemSelected) {
            listener = context
        } else {
            throw ClassCastException("${context.toString()} must implement OnFoodItemSelected")
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.content_food, container, false)
        initFragment(view)
        return view
    }

    private fun initFragment(view: View) {
        val cal = Calendar.getInstance()
        val recycler = view.findViewById<RecyclerView>(R.id.meko_recycler)
        val today = view.findViewById<TextView>(R.id.dash_date)
        recycler?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,
                false)

        val date = DateTimeTemplate.format(cal, "%DDD%, %MMM% %dd%")
        today?.text = "What will you drink $date"
        drinksAdapter = DrinksAdapter(activity!!, mekoFoods, this)
        recycler.adapter = drinksAdapter
    }


    private val observer = Observer<ArrayList<FoodItem>> {
        if (it != null) {
            drinksAdapter.swapData(it)
        }
    }

    private fun <T> LiveData<T>.reObserve(owner: LifecycleOwner, observer: Observer<T>) {
        removeObserver(observer)
        observe(owner, observer)
    }

    override fun onItemClick(pos: Int) {
        listener.onItemSelected(pos)
    }

    companion object {
        fun newInstance(): DrinksFragment {
            return DrinksFragment()
        }
    }
}
