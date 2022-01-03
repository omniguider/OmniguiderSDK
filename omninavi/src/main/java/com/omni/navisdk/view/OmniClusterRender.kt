package com.omni.navisdk.view

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.omni.navisdk.R
import com.omni.navisdk.module.OmniClusterItem
import com.omni.navisdk.tool.Tools.Companion.instance

class OmniClusterRender(private val mContext: Context, map: GoogleMap?, clusterManager: ClusterManager<OmniClusterItem<*>>?) : DefaultClusterRenderer<OmniClusterItem<*>>(mContext, map, clusterManager) {
    private val mClusterIconGenerator: IconGenerator? = null
    override fun onBeforeClusterItemRendered(item: OmniClusterItem<*>, markerOptions: MarkerOptions) {
        if (item.pOI == null) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(item.iconRes))
        } else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(item.iconRes))
        }
        markerOptions.title(item.title)
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<OmniClusterItem<*>>, markerOptions: MarkerOptions) {
        val TextMarkerGen = IconGenerator(mContext)
        val marker = mContext.resources.getDrawable(R.drawable.solid_circle_holo_orange_light)
        TextMarkerGen.setBackground(marker)
        TextMarkerGen.makeIcon(cluster.size.toString() + "")
        TextMarkerGen.setTextAppearance(mContext,
                if (cluster.size < 10) R.style.ClusterViewTextAppearanceBig else R.style.ClusterViewTextAppearanceMedium)
        if (cluster.size >= 10) {
            TextMarkerGen.setContentPadding(instance!!.dpToIntPx(mContext, 10f),
                    instance!!.dpToIntPx(mContext, 8f),
                    instance!!.dpToIntPx(mContext, 10f),
                    instance!!.dpToIntPx(mContext, 8f))
        }
        val icon = BitmapDescriptorFactory.fromBitmap(TextMarkerGen.makeIcon())
        markerOptions.icon(icon)
    }

    override fun onClusterRendered(cluster: Cluster<OmniClusterItem<*>>, marker: Marker) {
        super.onClusterRendered(cluster, marker)
    }

}