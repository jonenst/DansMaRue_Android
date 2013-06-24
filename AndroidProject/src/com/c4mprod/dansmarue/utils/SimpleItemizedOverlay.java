/**
 * This file is part of the Alerte Voirie project.
 * 
 * Copyright (C) 2010-2011 C4M PROD
 * 
 * Alerte Voirie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alerte Voirie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Alerte Voirie. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.c4mprod.dansmarue.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.c4mprod.dansmarue.ReportDetailsActivity;
import com.c4mprod.dansmarue.entities.Incident;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;

import fr.paris.android.signalement.R;

@SuppressWarnings("rawtypes")
public class SimpleItemizedOverlay extends ItemizedOverlay {
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private View                   mBubbleView;
    private final Context          c;
    private final MapView          mMapView;

    public SimpleItemizedOverlay(Drawable defaultMarker, Context context, MapView mapview, ArrayList<OverlayItem> overlayItems) {
        super(defaultMarker);
        boundCenterBottom(defaultMarker);
        c = context;
        this.mMapView = mapview;

        mOverlays = overlayItems;
        populate();
    }

    // public void addOverlayItem(OverlayItem overlay) {
    // mOverlays.add(overlay);
    // populate();
    // }

    @Override
    protected OverlayItem createItem(int i) {
        OverlayItem overlayItem = mOverlays.get(i);
        boundCenterBottom(overlayItem.getMarker(0));
        // overlayItem.setMarker(defaultMarker);
        return overlayItem;
    }

    @Override
    public int size() {
        return mOverlays.size();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);
    }

    @Override
    protected boolean onTap(int index) {
        Incident tappedIncident = (Incident) getItem(index);
        if (tappedIncident != null) {
            OverlayItem tapped = getItem(index);
            MapView.LayoutParams params = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, tapped.getPoint(),
                                                                   LayoutParams.BOTTOM_CENTER);
            params.mode = MapView.LayoutParams.MODE_MAP;
            if (mBubbleView == null) {
                mBubbleView = ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_map_bubble, null);
            }

            //Log.d("AlerteVoirie_PM", "state of incident : " + tappedIncident.state);
            
            if (tappedIncident.invalidations == 0 && tappedIncident.state != 'R') {
                mBubbleView.findViewById(R.id.Bubble_arrow).setVisibility(View.VISIBLE);
                
                if (tappedIncident.categoryId >= 0) // HACK MDP
                {
                    mBubbleView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            Intent i = new Intent(c, ReportDetailsActivity.class);
                            i.putExtra("existing", true);
                            i.putExtra("event", (String) mBubbleView.getTag());
                            c.startActivity(i);
                            clearBubble();
                        }
                    });
                }
            } else {
                mBubbleView.findViewById(fr.paris.android.signalement.R.id.Bubble_arrow).setVisibility(View.GONE);
                mBubbleView.setOnClickListener(null);
            }
            TextView title = (TextView) mBubbleView.findViewById(R.id.TextView_title);
            TextView subtitle = (TextView) mBubbleView.findViewById(R.id.TextView_subtitle);

            title.setText(tappedIncident.description);
            subtitle.setText(tappedIncident.address);
            //Log.d("AlerteVoirie_PM", "tapped incident " + tappedIncident.toString());
            mBubbleView.setTag(tappedIncident.toString());
            mMapView.addView(mBubbleView, params);
            mMapView.getController().animateTo(tapped.getPoint());
            return true;
        }
        return super.onTap(index);
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        clearBubble();// View(mBubbleView);
        return super.onTap(p, mapView);
    }

    public void clearBubble() {
        mMapView.removeAllViews();
    }
}
