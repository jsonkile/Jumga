package com.bigheadapps.monkee.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.DELIVERY_FEE
import com.bigheadapps.monkee.models.Order
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class MerchantOrdersAdapter : RecyclerView.Adapter<MerchantOrdersAdapter.ViewHolder>() {
    private var orders = emptyList<Order>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameQuantity.text =
            "(${orders[position].quantity}) ${orders[position].productName.trim()}"
        holder.cost.text =
            "$${orders[position].totalCost - DELIVERY_FEE} + $$DELIVERY_FEE Delivery Fee"
        holder.merchantCut.text = "You received $${orders[position].merchantCut}"

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        holder.dateTime.text =
            "Ordered on ${simpleDateFormat.format(orders[position].paidAt.toDate())}"

        Glide.with(holder.itemView.context).load(orders[position].image)
            .placeholder(R.drawable.placeholder_image).centerCrop().into(holder.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.merchant_order_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    internal fun inflate(orders: List<Order>) {
        this.orders = orders
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameQuantity: TextView = itemView.findViewById(R.id.name_quantity)
        val cost: TextView = itemView.findViewById(R.id.cost)
        val image: ImageView = itemView.findViewById(R.id.image)
        val merchantCut: TextView = itemView.findViewById(R.id.merchant_cut)
        val dateTime: TextView = itemView.findViewById(R.id.date_time)
    }
}
