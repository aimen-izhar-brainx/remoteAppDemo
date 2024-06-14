package com.example.remoteappdemo.adapter

import android.net.nsd.NsdServiceInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.remoteappdemo.NetWorkDiscoveryInterface
import com.example.remoteappdemo.databinding.ItemDiscoverDevicesBinding

class DiscoverDevicesAdapter(
    private val actionListener: NetWorkDiscoveryInterface
) : RecyclerView.Adapter<DiscoverDevicesViewHolder>() {
    private val records : ArrayList<NsdServiceInfo> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiscoverDevicesViewHolder {
        return DiscoverDevicesViewHolder(ItemDiscoverDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = records.size

    override fun onBindViewHolder(holder: DiscoverDevicesViewHolder, position: Int) {
        holder.onBindView(records[position],actionListener)
    }

    fun setData(records:ArrayList<NsdServiceInfo>) {
        this.records.clear()
        this.records.addAll(records)
    }



    //endregion
}