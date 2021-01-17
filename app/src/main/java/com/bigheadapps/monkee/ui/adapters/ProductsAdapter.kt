package com.bigheadapps.monkee.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.Functions
import com.bigheadapps.monkee.models.Product
import com.bumptech.glide.Glide

class ProductsAdapter : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {
    lateinit var mClickListener: ClickListener
    private var products = emptyList<Product>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = products[position].name.trim()
        holder.price.text = Functions.dollafy(products[position].price)

        Glide.with(holder.itemView.context).load(products[position].picture).centerCrop().into(holder.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    internal fun inflate(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val name: TextView = itemView.findViewById(R.id.name)
        val price: TextView = itemView.findViewById(R.id.price)
        val image: ImageView = itemView.findViewById(R.id.image)

        override fun onClick(v: View) {
            mClickListener.onClick(v, bindingAdapterPosition, products[bindingAdapterPosition])
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        mClickListener = clickListener
    }

    interface ClickListener {
        fun onClick(view: View, pos: Int, product: Product)
    }
}
