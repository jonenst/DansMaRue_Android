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
package com.c4mprod.dansmarue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.c4mprod.dansmarue.entities.Category;
import com.c4mprod.dansmarue.entities.Constants;
import com.c4mprod.dansmarue.entities.Incident;
import com.c4mprod.dansmarue.entities.IntentData;
import com.c4mprod.dansmarue.entities.JsonData;
import com.c4mprod.dansmarue.entities.Last_Location;
import com.c4mprod.dansmarue.utils.JSONAdapter;
import com.c4mprod.dansmarue.webservice.AVService;
import com.c4mprod.dansmarue.webservice.RequestListener;
import com.c4mprod.utils.ImageDownloader;

import fr.paris.android.signalement.R;

public class ExistingIncidentsActivity extends ListActivity implements RequestListener, LocationListener {
    private static final int      DIALOG_PROGRESS = 0;
    private final ImageDownloader imageDownloader = new ImageDownloader();
    private ProgressDialog        mPd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.layout_existing_incidents);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_nouveau_rapport);

        // init button
        findViewById(R.id.Button_validate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ExistingIncidentsActivity.this, SelectCategoryActivity.class), 0);
            }
        });
    }

    @Override
    protected void onResume() {
        try {

            showDialog(DIALOG_PROGRESS);
            JSONObject request = new JSONObject().put(JsonData.PARAM_REQUEST, JsonData.VALUE_REQUEST_GET_INCIDENTS_BY_POSITION)
                                                 /* .put(JsonData.PARAM_UDID, Utils.getUdid(this)) */
                                                 .put(JsonData.PARAM_RADIUS, JsonData.VALUE_RADIUS_CLOSE)
                                                 .put(JsonData.PARAM_POSITION,
                                                      new JSONObject().put(JsonData.PARAM_POSITION_LONGITUDE, Last_Location.longitude)
                                                                      .put(JsonData.PARAM_POSITION_LATITUDE, Last_Location.latitude));

            AVService.getInstance(this).postJSON(new JSONArray().put(request), this);

        } catch (JSONException e) {
            // Log.e(Constants.PROJECT_TAG, "Error loading existing incidents", e);
        }
        super.onResume();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                mPd = new ProgressDialog(this);
                mPd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mPd.setIndeterminate(true);
                mPd.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        removeDialog(DIALOG_PROGRESS);
                    }
                });
                mPd.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        AVService.getInstance(ExistingIncidentsActivity.this).cancelTask();
                        finish();
                    }
                });
                mPd.setMessage(getString(R.string.ui_message_loading));
                return mPd;

            default:
                return super.onCreateDialog(id);
        }
    }

    @Override
    public void onRequestcompleted(int requestCode, Object result) {
        try {
            JSONArray responses;
            responses = new JSONArray((String) result);
            JSONObject response = responses.getJSONObject(0);

            if (requestCode == AVService.REQUEST_JSON) {
                if (JsonData.VALUE_REQUEST_GET_INCIDENTS_BY_POSITION.equals(response.getString(JsonData.PARAM_REQUEST))) {
                    JSONArray items = response.getJSONObject(JsonData.PARAM_ANSWER).getJSONArray(JsonData.PARAM_CLOSEST_INCIDENTS);

                    JSONArray items2 = new JSONArray();

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject jsonObj = items.getJSONObject(i);
                        // //Log.d("DEBUG", "--->>jsonObj=" + jsonObj.toString());
                        Incident inc = Incident.fromJSONObject(this, jsonObj);
                        if (inc.state != 'R') items2.put(jsonObj);
                    }
                    setListAdapter(new MagicAdapter(this, items2, R.layout.cell_report, new String[] { JsonData.PARAM_INCIDENT_DESCRIPTION },
                                                    new int[] { R.id.TextView_text }));
                }
            }
        } catch (JSONException e) {
            // Log.e(Constants.PROJECT_TAG, "JSONException", e);

        } catch (ClassCastException e) {
            // Log.e(Constants.PROJECT_TAG, "Can't read response. trying to cast " + result.getClass() + " into String", e);

        }

        if (mPd != null) {
            mPd.dismiss();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (getIntent().getBooleanExtra(Constants.NEW_REPORT, false)) {
                Intent i = new Intent(this, ReportDetailsActivity.class);
                i.putExtra(IntentData.EXTRA_CATEGORY_ID, data.getLongExtra(IntentData.EXTRA_CATEGORY_ID, -1));
                startActivity(i);
            }
            // Log.d(Constants.PROJECT_TAG, "Result: " + data.getLongExtra(IntentData.EXTRA_CATEGORY_ID, -1));
            finish();
        }
    }

    class MagicAdapter extends JSONAdapter {
        public MagicAdapter(Context context, JSONArray data, int cellLayout, String[] from, int[] to) {
            super(context, data, cellLayout, from, to, null);
        }

        @Override
        protected String getCategoryOfItem(int itemId) {
            return super.getCategoryOfItem(itemId).substring(0, 10);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            ImageView icone = (ImageView) v.findViewById(R.id.ImageView_icon);

            String imgName = null;
            try {

                Incident incident = Incident.fromJSONObject(getApplicationContext(), ((JSONObject) getItem(position)));

                // Log.i(Constants.PROJECT_TAG, "getView : incident" + incident);

                JSONArray imgarr = incident.pictures_far;

                if (imgarr.length() > 0) {
                    imgName = imgarr.getString(0);
                }

                if (imgName == null) {
                    imgarr = incident.pictures_close;

                    if (imgarr.length() > 0) {
                        imgName = imgarr.getString(0);
                    }
                }

                // Log.d(Constants.PROJECT_TAG, "" + imgName);
                if (imageDownloader.getDefault_img() == null) {
                    imageDownloader.setDefault_img(icone.getDrawable());
                }
                imageDownloader.download(imgName, icone);

                char state = incident.state;

                ((ImageView) v.findViewById(R.id.ImageView_icn)).setImageDrawable(incident.invalidations > 0 ? getResources().getDrawable(R.drawable.icn_incident_nonvalide)
                                                                                                            : incident.confirms > 0 ? getResources().getDrawable(R.drawable.icn_incident_confirme)
                                                                                                                                   : state == 'R' ? getResources().getDrawable(R.drawable.icn_incident_resolu2)
                                                                                                                                                 : getResources().getDrawable(R.drawable.icn_creer));

                if (incident.invalidations > 0) {
                    v.findViewById(R.id.Arrow_details).setVisibility(View.GONE);
                } else {
                    v.findViewById(R.id.Arrow_details).setVisibility(View.VISIBLE);
                }

                float[] distances = new float[3];

                Location.distanceBetween(Last_Location.latitude, Last_Location.longitude, incident.latitude, incident.longitude, distances);

                ((TextView) v.findViewById(R.id.TextView_distance)).setText(getString(R.string.address_distance).replace("x",
                                                                                                                         Integer.toString((int) distances[0])));
                ((TextView) v.findViewById(R.id.TextView_distance)).setVisibility(View.VISIBLE);
                // ImageManager.fetchDrawableOnThread(imgName, icone, icone.getDrawable());

                // get category
                long catId = incident.categoryId;

                String category = null;
                // Log.d(Constants.PROJECT_TAG, "Cat id = " + catId);

                Cursor c = getContentResolver().query(ContentUris.withAppendedId(Category.CONTENT_URI, catId), new String[] { Category.PARENT, Category.NAME },
                                                      null, null, null);

                if (c.moveToFirst()) {
                    category = c.getString(c.getColumnIndex(Category.NAME));
                }
                c.close();

                ((TextView) v.findViewById(R.id.TextView_title)).setText(category);

                // HACK MDP
                if (catId < 0) {
                    v.findViewById(R.id.Arrow_details).setVisibility(View.GONE);
                    v.findViewById(R.id.ImageView_icn).setVisibility(View.GONE);
                    v.findViewById(R.id.ImageView_icon).setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                // Log.e(Constants.PROJECT_TAG, ((JSONObject) getItem(position)).toString(), e);
            } catch (ClassCastException e) {
                // Log.e(Constants.PROJECT_TAG, getItem(position).getClass().toString(), e);
            }

            if (getItemViewType(position) == TYPE_ITEM) {
                // TODO get incident photo
            }
            return v;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // //Log.d(Constants.PROJECT_TAG, "onListItemClick : "+l.getAdapter().getItem(position));
        Intent i = new Intent(this, ReportDetailsActivity.class);
        i.putExtra("existing", true);
        i.putExtra("event", l.getAdapter().getItem(position).toString());

        try {
            Incident incident = Incident.fromJSONObject(this, new JSONObject(l.getAdapter().getItem(position).toString()));
            if (incident.state == 'R' || incident.invalidations > 0) return;
            if (incident.categoryId < 0) return; // HACK MDP
        } catch (JSONException e) {
            // Log.e(Constants.PROJECT_TAG, "JSONException in onListItemClick", e);
        }
        startActivity(i);
        // super.onListItemClick(l, v, position, id);
    }

}
