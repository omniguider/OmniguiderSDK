package com.omni.omninavi.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.omni.omninavi.R;

/**
 * Created by wiliiamwang on 20/02/2017.
 */

public class OmniClusterRender extends DefaultClusterRenderer<OmniClusterItem> {

    private IconGenerator mClusterIconGenerator;
    private Context mContext;

    public OmniClusterRender(Context context, GoogleMap map, ClusterManager<OmniClusterItem> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(OmniClusterItem item, MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getIconRes()));
        markerOptions.title(item.getTitle());
        markerOptions.snippet(item.getSnippet());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<OmniClusterItem> cluster, MarkerOptions markerOptions) {
        final Drawable clusterIcon = mContext.getResources().getDrawable(R.mipmap.icon_select);
        clusterIcon.setColorFilter(mContext.getResources().getColor(android.R.color.holo_orange_light), PorterDuff.Mode.SRC_ATOP);

        if (mClusterIconGenerator == null) {
            mClusterIconGenerator = new IconGenerator(mContext);
        }
        mClusterIconGenerator.setBackground(clusterIcon);

        //modify padding for one or two digit numbers
        if (cluster.getSize() < 10) {
            mClusterIconGenerator.setContentPadding(30, 10, 0, 0);
        } else if (cluster.getSize() > 100) {
            mClusterIconGenerator.setContentPadding(5, 10, 0, 0);
        } else {
            mClusterIconGenerator.setContentPadding(15, 10, 0, 0);
        }

        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

}
