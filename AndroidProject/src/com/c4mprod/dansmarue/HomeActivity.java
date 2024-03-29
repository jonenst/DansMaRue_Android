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

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.c4mprod.dansmarue.entities.Constants;
import com.c4mprod.dansmarue.entities.JsonData;
import com.c4mprod.dansmarue.entities.Last_Location;
import com.c4mprod.dansmarue.utils.LocationHelper;
import com.c4mprod.dansmarue.webservice.AVService;
import com.c4mprod.dansmarue.webservice.RequestListener;

import fr.paris.android.signalement.R;

public class HomeActivity extends Activity implements OnClickListener, LocationListener, RequestListener {
    private static final int DIALOG_PROGRESS = 0;
    private static final int MENU_CREDITS    = 0;

    private LocationManager  locationManager;
    private Location         lastlocation;
    private boolean          dialog_shown    = false;
    private boolean          hidedialog      = false;
    private final Handler    myHandler       = new Handler();
    private final Runnable   removeUpdate    = new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     AVService.getInstance(HomeActivity.this).toastServerError(HomeActivity.this.getString(R.string.gps_error));
                                                     locationManager.removeUpdates(HomeActivity.this);
                                                     handleNewLocation(lastlocation);
                                                 }
                                             };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_home);

        // init buttons
        findViewById(R.id.Button_news).setOnClickListener(this);
        findViewById(R.id.Button_reports).setOnClickListener(this);
        findViewById(R.id.Button_new_incident).setOnClickListener(this);
        findViewById(R.id.Button_incidents).setOnClickListener(this);

        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider != null) {
            if (!provider.contains("gps")) {
                // Notify users and show settings if they want to enable GPS
                new AlertDialog.Builder(HomeActivity.this).setMessage(getString(R.string.activate_gps))
                                                          .setPositiveButton("Activer", new DialogInterface.OnClickListener() {

                                                              @Override
                                                              public void onClick(DialogInterface dialog, int which) {
                                                                  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                                  startActivityForResult(intent, 5);
                                                              }
                                                          })
                                                          .setNegativeButton("Ignorer", new DialogInterface.OnClickListener() {

                                                              @Override
                                                              public void onClick(DialogInterface dialog, int which) {

                                                              }
                                                          })
                                                          .show();
            } else {
                searchForLocation(savedInstanceState);
            }
        }
        checkForUpdates();
    }

    private void searchForLocation(Bundle savedInstanceState) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        lastlocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (savedInstanceState == null) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 25, this);

            dialog_shown = true;
            showDialog(DIALOG_PROGRESS);

            myHandler.postDelayed(removeUpdate, 30000);
        } else {
            handleNewLocation(lastlocation);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 5 && resultCode == 0) {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (provider != null) {
                switch (provider.length()) {
                    case 0:
                        // GPS still not enabled..
                        break;
                    default:
                        searchForLocation(null);
                        break;
                }
            }
        } else {
            // the user did not enable his GPS
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hidedialog = true;

        retreiveIncidentsStats();
        checkForCrashes();
    }

    @Override
    protected void onPause() {
        this.hidedialog = false;
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Button_news:
                // //Log.d("DEBUG", "Click button news");
                startActivity(new Intent(this, NewsSimpleActivity.class));
                break;
            case R.id.Button_reports:
                // //Log.d("DEBUG", "Click button reports");
                startActivity((new Intent(this, MyIncidentsActivity.class)));
                break;
            case R.id.Button_incidents:
                // //Log.d("DEBUG", "Click button incidents");
                startActivity((new Intent(this, IncidentsActivityMap.class)));
                break;
            case R.id.Button_new_incident:
                // //Log.d("DEBUG", "Click button new incident");
                Intent i = new Intent(this, ExistingIncidentsActivity.class);
                i.putExtra(Constants.NEW_REPORT, true);
                startActivity(i);
                break;

            default:
                break;
        }
        // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (LocationHelper.isBetterLocation(location, lastlocation)) {
            handleNewLocation(location);
            locationManager.removeUpdates(this);
            myHandler.removeCallbacks(removeUpdate);
        }

    }

    private void handleNewLocation(Location location) {
        lastlocation = location;

        try {

            // DEBUG ONLY when not on Paris (2 rue de la cité 75004 Paris)!!
            if (!Constants.DEBUGMODE && location != null) {
                Last_Location.longitude = location.getLongitude();
                Last_Location.latitude = location.getLatitude();
            } else {
                Last_Location.latitude = Constants.DEFAULT_LON * 0.000001;
                Last_Location.longitude = Constants.DEFAULT_LAT * 0.000001;
            }

            retreiveIncidentsStats();

            hidedialog = true;
        } catch (NullPointerException e) {
            // Log.e(Constants.PROJECT_TAG, "Nullpointer Error", e);
        }

    }

    public void retreiveIncidentsStats() {
        JSONObject request;
        try {
            request = new JSONObject().put(JsonData.PARAM_REQUEST, JsonData.VALUE_REQUEST_GET_INCIDENTS_STATS)
                                      /* .put(JsonData.PARAM_UDID, Utils.getUdid(this)) */
                                      .put(JsonData.PARAM_POSITION,
                                           new JSONObject().put(JsonData.PARAM_POSITION_LONGITUDE, Last_Location.longitude)
                                                           .put(JsonData.PARAM_POSITION_LATITUDE, Last_Location.latitude));
            AVService.getInstance(this).postJSON(new JSONArray().put(request), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        AVService.getInstance(HomeActivity.this).toastServerError(HomeActivity.this.getString(R.string.gps_error));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                ProgressDialog pd = new ProgressDialog(this);
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setIndeterminate(true);
                pd.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (dialog_shown) {
                            removeDialog(DIALOG_PROGRESS);
                            dialog_shown = false;
                        }
                    }
                });
                pd.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        AVService.getInstance(HomeActivity.this).cancelTask();
                        finish();
                    }
                });
                pd.setMessage(getString(R.string.ui_message_loading));
                return pd;

            default:
                return super.onCreateDialog(id);
        }
    }

    @Override
    public void onRequestcompleted(int requestCode, Object result) {
        try {
            JSONArray responses;
            // //Log.d(Constants.PROJECT_TAG, (String) result);
            responses = new JSONArray((String) result);

            JSONObject response = responses.getJSONObject(0);

            // final AnimationSet set = new AnimationSet(false);
            //
            // final float centerX = findViewById(R.id.LinearLayout02).getWidth() / 2;
            // final float centerY = findViewById(R.id.LinearLayout02).getHeight() / 2;
            // final Flip3dAnimation animation = new Flip3dAnimation(0, 360, centerX, centerY);
            // animation.setDuration(500);
            // set.setFillAfter(true);
            // set.setFillBefore(true);
            // animation.setInterpolator(new AccelerateInterpolator());
            //
            // set.addAnimation(animation);

            if (requestCode == AVService.REQUEST_JSON) {
                if (JsonData.VALUE_REQUEST_GET_INCIDENTS_STATS.equals(response.getString(JsonData.PARAM_REQUEST))) {
                    Last_Location.Incidents = response.getJSONObject(JsonData.PARAM_ANSWER);
                    int resolved_incidents = response.getJSONObject(JsonData.PARAM_ANSWER).getInt(JsonData.PARAM_RESOLVED_INCIDENTS);
                    int ongoing_incidents = response.getJSONObject(JsonData.PARAM_ANSWER).getInt(JsonData.PARAM_ONGOING_INCIDENTS);
                    int updated_incidents = response.getJSONObject(JsonData.PARAM_ANSWER).getInt(JsonData.PARAM_UPDATED_INCIDENTS);

                    ((TextView) findViewById(R.id.Home_TextView_Solved)).setText(getResources().getQuantityString(R.plurals.home_label_solved,
                                                                                                                  resolved_incidents, resolved_incidents));
                    ((TextView) findViewById(R.id.Home_TextView_Current)).setText(getResources().getQuantityString(R.plurals.home_label_current,
                                                                                                                   ongoing_incidents, ongoing_incidents));
                    ((TextView) findViewById(R.id.Home_TextView_Update)).setText(getResources().getQuantityString(R.plurals.home_label_update,
                                                                                                                  updated_incidents, updated_incidents));
                }
            }

            // findViewById(R.id.LinearLayout02).startAnimation(set);
            // findViewById(R.id.LinearLayout04).startAnimation(set);
            // findViewById(R.id.LinearLayout03).startAnimation(set);
        } catch (JSONException e) {
            // Log.e(Constants.PROJECT_TAG, "JSONException", e);
        } catch (ClassCastException e) {
            // Log.e(Constants.PROJECT_TAG, "Invalid result. Trying to cast " + result.getClass() + "into String", e);
        } finally {
            if (hidedialog && dialog_shown == true) dismissDialog(DIALOG_PROGRESS);
            dialog_shown = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_CREDITS, Menu.FIRST, R.string.menu_credits).setIcon(android.R.drawable.ic_menu_info_details);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CREDITS:
                Intent i = new Intent(this, CreditsActivity.class);
                startActivity(i);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void checkForCrashes() {
        if (Constants.USE_HOCKEYAPP) {
            CrashManager.register(this, Constants.HOCKEY_APP_ID);
        }
    }

    private void checkForUpdates() {
        if (Constants.USE_HOCKEYAPP) {
            UpdateManager.register(this, Constants.HOCKEY_APP_ID);
        }
    }
}
