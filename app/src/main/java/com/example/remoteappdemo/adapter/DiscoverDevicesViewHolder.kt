package com.example.remoteappdemo.adapter

import android.net.nsd.NsdServiceInfo
import androidx.recyclerview.widget.RecyclerView
import com.example.remoteappdemo.NetWorkDiscoveryInterface
import com.example.remoteappdemo.databinding.ItemDiscoverDevicesBinding

class DiscoverDevicesViewHolder(val itemBinding: ItemDiscoverDevicesBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun onBindView(devices: NsdServiceInfo, actionListener: NetWorkDiscoveryInterface) {
        itemBinding.apply {
            //if(devices.host !=null) {
                tvName.text = "Device name" + " " +devices.serviceName
            itemView.setOnClickListener {
                actionListener.onItemClick(position)
            }
                //tvIp.text = "Device IP "+ " " +devices.host.hostAddress
            //}
        }
    }


}