package com.example.ezhr.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ItemAdapter(val itemClick: (position: Int, item: ChartItem) -> Unit) :
    RecyclerView.Adapter<ItemViewHolder>() {
    private val TAG = "ChartAdapter"

    // Data needed for the chart
    data class ChartItem(
        val id: Int,
        val title: String,
        val available: Double,
        val used: Double
    )

    private var items: List<ChartItem> = listOf()

    /**
     * Create a new view
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chart_list_item, parent, false)
        )

    /**
     * Replace content of the view
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            Log.d(TAG, "Clicked")
            itemClick(position, items[position])
            val data = items[position]
            Log.d(TAG, "ID: ${data.id}")
            when (data.id) {
                0 -> {
                    Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_balanceLeaveFragment)
                        .onClick(holder.itemView)
                }
                1 -> {
                    Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_claimsBalanceFragment)
                        .onClick(holder.itemView)
                }
            }
        }

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * Return the size of your dataset (invoked by the layout manager)
     *
     * @return The total number of items in this adapter.
     *
     */
    override fun getItemCount() = items.size

    // Add the Chart Items
    fun setItems(newItems: List<ChartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = "ChartAdapter"
    // This is to bind the Items in the ViewHolder
    fun bind(item: ItemAdapter.ChartItem) {
        var pieChartGeneric = view.findViewById<PieChart>(R.id.pieChartGeneric)
        val viewMoreButton = view.findViewById<Button>(R.id.buttonViewMore)
        // Event handler for viewMore Button
        viewMoreButton.setOnClickListener {
            // Bind to an action for an item
            Log.d(TAG, "Item: $item")
            Log.d(TAG, "ID: ${item.id}")
            when (item.id) {
                0 -> {
                    Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_balanceLeaveFragment)
                        .onClick(view)
                }
                1 -> {
                    Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_claimsBalanceFragment)
                        .onClick(view)
                }
            }
        }
        // Initialise and show the pie chart
        pieChartGeneric = initPieChart(pieChartGeneric, item.title)
        showPieChart(pieChartGeneric, item.available, item.used)
    }
}

/**
 *   TThis is to initialize the pie chart using MPAndroidChart
 *   @param pieChart : PieChart , text : String
 *   @return PieChart
 */
private fun initPieChart(pieChart: PieChart, text: String): PieChart {
    //using percentage as values instead of amount
    pieChart.setUsePercentValues(false)

    //remove the description label on the lower left corner, default true if not set
    pieChart.description.isEnabled = false

    //enabling the user to rotate the chart, default true
    pieChart.isRotationEnabled = false

    //setting the first entry start from right hand side, default starting from top
    pieChart.rotationAngle = 270f

    //highlight the entry when it is tapped, default true if not set
    pieChart.isHighlightPerTapEnabled = false

    pieChart.setCenterTextSize(08.0F)
    pieChart.centerText = text

    //Set Hollow Radius
    pieChart.holeRadius = 80F

    // Enable interaction
    pieChart.isClickable = true
    // Remove Description
    pieChart.setDrawSliceText(false)
    // Remove Legend
    val leg: Legend = pieChart.legend
    leg.isEnabled = false
    return pieChart
}


/**
 *   TThis is to show the pie chart generated using MPAndroidChart
 *   @param pieChart : PieChart , available : Int, used : Int
 *   @return PieChart
 */
private fun showPieChart(pieChart: PieChart, available: Double, used: Double): PieChart {
    val pieEntries: ArrayList<PieEntry> = ArrayList()
    val label = "type"

    //initializing data
    val typeAmountMap: MutableMap<String, Double> = HashMap()
    typeAmountMap["Available"] = available
    typeAmountMap["Used"] = used

    //initializing colors for the entries
    val colors: ArrayList<Int> = ArrayList()
    colors.add(Color.parseColor("#FFFFFFFF"))
    colors.add(Color.parseColor("#1976d2"))


    //input data and fit data into pie chart entry
    for (type in typeAmountMap.keys) {
        pieEntries.add(PieEntry(typeAmountMap[type]!!.toFloat(), type))
    }

    //collecting the entries with label name
    val pieDataSet = PieDataSet(pieEntries, label)
    //setting text size of the value
    pieDataSet.valueTextSize = 12f
    //providing color list for coloring different entries
    pieDataSet.colors = colors
    //grouping the data set from entry to chart
    val pieData = PieData(pieDataSet)
    //showing the value of the entries, default true if not set
    pieData.setDrawValues(true)
    pieChart.data = pieData
    // Remove value display
    pieData.setDrawValues(false)
    // Set label color to black
    pieChart.setEntryLabelColor(Color.BLACK)
    pieChart.invalidate()
    return pieChart
}