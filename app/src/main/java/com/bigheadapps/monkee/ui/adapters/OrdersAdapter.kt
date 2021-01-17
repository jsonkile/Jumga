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

class OrdersAdapter : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {
    lateinit var mClickListener: ClickListener
    private var orders = emptyList<Order>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameQuantity.text = "(${orders[position].quantity}) ${orders[position].productName.trim()}"
        holder.cost.text = "$${orders[position].totalCost - DELIVERY_FEE} + $$DELIVERY_FEE Delivery Fee"
        holder.seller.text = "Sold by ${orders[position].sellerName}"

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        holder.dateTime.text = simpleDateFormat.format(orders[position].paidAt.toDate())

        Glide.with(holder.itemView.context).load(orders[position].image)
            .placeholder(R.drawable.placeholder_image).centerCrop().into(holder.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    internal fun inflate(orders: List<Order>) {
        this.orders = orders
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val nameQuantity: TextView = itemView.findViewById(R.id.name_quantity)
        val cost: TextView = itemView.findViewById(R.id.cost)
        val image: ImageView = itemView.findViewById(R.id.image)
        val seller: TextView = itemView.findViewById(R.id.seller)
        val dateTime: TextView = itemView.findViewById(R.id.date_time)

        override fun onClick(v: View) {
            mClickListener.onClick(v, bindingAdapterPosition, orders[bindingAdapterPosition])
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        mClickListener = clickListener
    }

    interface ClickListener {
        fun onClick(view: View, pos: Int, order: Order)
    }
}
