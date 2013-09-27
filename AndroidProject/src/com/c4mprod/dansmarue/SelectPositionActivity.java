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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.TextView;

import com.c4mprod.dansmarue.entities.Constants;
import com.c4mprod.dansmarue.entities.IntentData;
import com.c4mprod.dansmarue.utils.LocationHelper;
import com.c4mprod.dansmarue.utils.Utils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import fr.paris.android.signalement.R;

public class SelectPositionActivity extends MapActivity implements LocationListener {

    private Location                         currentBestLocation = null;
    private MapView                          map;
    private LocationManager                  locationManager;
    private Geocoder                         geo;
    private boolean                          isInvalidAdress     = true;
    private CursorOveray                     cursorOverlay;
    private AsyncTask<String, Void, Address> adressTask;
    ProgressDialog                           pd;

    private GeoPoint                         mCurrentPoint;
    private String                           mCurrentAdress;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.layout_select_position);

        // init button
        findViewById(R.id.Button_validate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInvalidAdress) {

                    if (Utils.isOnline(getApplicationContext())) {
                        new AddressGetter().execute();
                    } else {
                        displayErrorPopup(R.string.server_error);
                    }

                } else {

                    mCurrentAdress = (((TextView) findViewById(R.id.EditText_address_number)).getText().toString() + " "
                                      + ((TextView) findViewById(R.id.EditText_address_street)).getText().toString() + " \n"
                                      + ((TextView) findViewById(R.id.EditText_address_postcode)).getText().toString() + " " + ((TextView) findViewById(R.id.EditText_address_town)).getText()
                                                                                                                                                                                    .toString()).trim();
                    Intent result = new Intent();
                    result.putExtra(IntentData.EXTRA_ADDRESS, mCurrentAdress);
                    if (mCurrentPoint != null) {
                        result.putExtra(IntentData.EXTRA_LONGITUDE, mCurrentPoint.getLongitudeE6() / 1E6);
                        result.putExtra(IntentData.EXTRA_LATITUDE, mCurrentPoint.getLatitudeE6() / 1E6);
                    }
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        });

        OnFocusChangeListener ofc = new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) enableSearch();

            }
        };

        TextWatcher twatch = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                enableSearch();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSearch();
            }
        };

        ((Button) findViewById(R.id.ButtonMapPosition)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!("-").equals(((Button) v).getTag())) {
                    ((Button) v).setTag("-");
                    ((Button) v).setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_bg_diminuer));
                    SelectPositionActivity.this.findViewById(R.id.TextViewpos02).setVisibility(View.GONE);
                    SelectPositionActivity.this.findViewById(R.id.Layoutpos01).setVisibility(View.GONE);
                    SelectPositionActivity.this.findViewById(R.id.TextViewpos03).setVisibility(View.GONE);
                } else {
                    ((Button) v).setTag("+");
                    ((Button) v).setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_bg_agrandir));
                    SelectPositionActivity.this.findViewById(R.id.TextViewpos02).setVisibility(View.VISIBLE);
                    SelectPositionActivity.this.findViewById(R.id.TextViewpos03).setVisibility(View.VISIBLE);
                    SelectPositionActivity.this.findViewById(R.id.Layoutpos01).setVisibility(View.VISIBLE);
                }

            }
        });

        // focus
        // ((TextView) findViewById(R.id.EditText_address_number)).setOnFocusChangeListener(ofc);
        // ((TextView) findViewById(R.id.EditText_address_street)).setOnFocusChangeListener(ofc);
        // ((TextView) findViewById(R.id.EditText_address_postcode)).setOnFocusChangeListener(ofc);
        // ((TextView) findViewById(R.id.EditText_address_town)).setOnFocusChangeListener(ofc);

        // text modification
        ((TextView) findViewById(R.id.EditText_address_number)).addTextChangedListener(twatch);
        ((TextView) findViewById(R.id.EditText_address_street)).addTextChangedListener(twatch);
        ((TextView) findViewById(R.id.EditText_address_postcode)).addTextChangedListener(twatch);
        ((TextView) findViewById(R.id.EditText_address_town)).addTextChangedListener(twatch);

        geo = new Geocoder(this);

        map = (MapView) findViewById(R.id.MapView_map);
        map.setBuiltInZoomControls(true);
        map.getController().setZoom(18);
        map.setSatellite(true);

        findViewById(R.id.Button_validate).setEnabled(false);

    }

    @Override
    protected void onResume() {

        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider != null) {
            if (!provider.contains("gps")) {
                // Notify users and show settings if they want to enable GPS
                new AlertDialog.Builder(SelectPositionActivity.this).setMessage(getString(R.string.activate_gps))
                                                                    .setPositiveButton("Activer", new DialogInterface.OnClickListener() {

                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            // TODO Auto-generated method stub
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
                searchForLocation();
            }
        }
        super.onResume();
    }

    private void searchForLocation() {

        if (Utils.isOnline(getApplicationContext())) {
            pd = new ProgressDialog(this);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setIndeterminate(true);
            pd.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    locationManager.removeUpdates(SelectPositionActivity.this);
                    if (pd.isShowing()) pd.dismiss();
                }
            });
            pd.setMessage(getString(R.string.search_position));
            pd.show();

            // Acquire a reference to the system Location Manager
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            currentBestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            // Register the listener with the Location Manager to receive location
            // updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);

            if (currentBestLocation != null) {
                cursorOverlay = new CursorOveray(getResources().getDrawable(R.drawable.map_cursor));
                cursorOverlay.setGeopoint(new GeoPoint((int) (currentBestLocation.getLatitude() * 1000000),
                                                       (int) (currentBestLocation.getLongitude() * 1000000)));
                map.getOverlays().add(cursorOverlay);
                map.invalidate();
            } else {
                cursorOverlay = new CursorOveray(getResources().getDrawable(R.drawable.map_cursor));
                cursorOverlay.setGeopoint(new GeoPoint(Constants.DEFAULT_LON, Constants.DEFAULT_LAT));
                map.getOverlays().add(cursorOverlay);
                map.invalidate();
            }

            if (currentBestLocation != null) {
                handleNewLocation(currentBestLocation);
            }
        } else {

            cursorOverlay = new CursorOveray(getResources().getDrawable(R.drawable.map_cursor));
            cursorOverlay.setGeopoint(new GeoPoint(Constants.DEFAULT_LON, Constants.DEFAULT_LAT));
            map.getOverlays().add(cursorOverlay);
            map.invalidate();

            displayErrorPopup(R.string.server_error);
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
                        searchForLocation();
                        break;
                }
            }
        } else {
            // the user did not enable his GPS
        }
    }

    protected void enableSearch() {
        if (findViewById(R.id.Layoutpos01).getVisibility() == View.VISIBLE) {
            ((Button) findViewById(R.id.Button_validate)).setText(R.string.address_search);
            findViewById(R.id.Button_validate).setEnabled(true);
            isInvalidAdress = true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Called when a new location is found by the location provider.
        if (LocationHelper.isBetterLocation(location, currentBestLocation)) {
            handleNewLocation(location);

            // disable GPS if accurate enough
            if (location.getAccuracy() <= 80) {
                locationManager.removeUpdates(this);
                if (pd != null && pd.isShowing()) pd.dismiss();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void handleNewLocation(Location location) {
        currentBestLocation = location;
        GeoPoint newGeo = LocationHelper.geoFromLocation(location);
        map.getController().animateTo(newGeo);
        setMarker(newGeo);
    }

    private void setMarker(GeoPoint newGeo) {

        if (newGeo != null) {
            if (cursorOverlay != null) {
                cursorOverlay.setGeopoint(newGeo);
            } else {
                cursorOverlay = new CursorOveray(getResources().getDrawable(R.drawable.map_cursor));
                cursorOverlay.setGeopoint(newGeo);
                map.getOverlays().add(cursorOverlay);
                map.getController().animateTo(newGeo);
            }
            mCurrentPoint = newGeo;

            // ((TextView) findViewById(R.id.TextView_address)).setText(null);
            // ((TextView) findViewById(R.id.EditText_address_number)).setText(null);

            if (Utils.isOnline(getApplicationContext())) {
                // Log.d(Constants.PROJECT_TAG, "Position: " + newGeo.getLatitudeE6() / 1E6 + " / " + newGeo.getLongitudeE6() / 1E6);
                new AddressGetter().execute(newGeo.getLatitudeE6() / 1E6, newGeo.getLongitudeE6() / 1E6);
            }
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private class CursorOveray extends ItemizedOverlay<OverlayItem> {
        GeoPoint p;

        public CursorOveray(Drawable defaultMarker) {
            super(boundCenterBottom(defaultMarker));
            // TODO Auto-generated constructor stub
        }

        public void setGeopoint(GeoPoint geo) {
            p = geo;
            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            OverlayItem item = new OverlayItem(p, "", "");
            return item;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView) {

            // Log.d("DEBUG", "onTap p: lat=" + p.getLatitudeE6() + " lon=" + p.getLongitudeE6());
            if (locationManager != null) {
                locationManager.removeUpdates(SelectPositionActivity.this);
            }
            setMarker(p);
            return true;
        }

    }

    private class AddressGetter extends AsyncTask<Double, Void, String[]> {

        protected GeoPoint geopoint;

        @Override
        protected String[] doInBackground(Double... params) {

            String[] result = new String[4];
            try {
                List<Address> addr = new ArrayList<Address>();

                if (params.length == 2) {

                    Log.d("DEBUG", "GET LOCATION FROM LAT LON : " + params[0] + " " + params[1]);

                    addr = geo.getFromLocation(params[0], params[1], 1);
                    // try {
                    // addr = getStringFromLocation(params[0], params[1]);
                    // } catch (JSONException e) {
                    // e.printStackTrace();
                    // }

                    Log.d("DEBUG", "addr size : " + addr.size());

                } else {

                    String address = ((TextView) findViewById(R.id.EditText_address_number)).getText().toString() + " "
                                     + ((TextView) findViewById(R.id.EditText_address_street)).getText().toString() + " , "
                                     + ((TextView) findViewById(R.id.EditText_address_postcode)).getText().toString() + " "
                                     + ((TextView) findViewById(R.id.EditText_address_town)).getText().toString() + " , " + "France";

                    Log.d("DEBUG", "GET LOCATION FROM ADDR : " + address);
                    addr = geo.getFromLocationName(address, 1);

                    Log.d("DEBUG", "addr size : " + addr.size());
                    if (addr.size() > 0) {
                        geopoint = new GeoPoint((int) (addr.get(0).getLatitude() * 1E6), (int) (addr.get(0).getLongitude() * 1E6));

                    }
                }

                if (addr.size() > 0) {

                    result[0] = addr.get(0).getAddressLine(0);
                    result[2] = addr.get(0).getPostalCode();
                    result[3] = addr.get(0).getLocality();

                    // num and street mixed
                    Pattern p = Pattern.compile("^[\\d\\-]+");
                    Matcher m = p.matcher(result[0]);
                    if (m.find()) {
                        result[1] = result[0].replace(m.group(), "");
                        result[0] = m.group();
                    } else {
                        result[1] = result[0];
                        result[0] = "";
                    }

                    // in case street = city
                    result[1] = result[1] == null ? "" : result[1].trim();
                    if (result[1].equals(result[3])) {
                        result[1] = "";
                    }

                    // normalisation
                    result[0] = result[0] == null ? "" : result[0].trim();
                    if (result[0].length() > 3) result[0] = "";
                    result[1] = result[1] == null ? "" : result[1].trim();
                    result[2] = result[2] == null ? "" : result[2].trim();
                    result[3] = result[3] == null ? "" : result[3].trim();
                }
            } catch (IOException e) {
                Log.e("DEBUG", "Address Error : ", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {

            if (geopoint != null) {
                setMarker(geopoint);
                map.getController().animateTo(geopoint);
                geopoint = null;
            }

            if (locationManager != null) locationManager.removeUpdates(SelectPositionActivity.this);
            if (pd != null && pd.isShowing()) pd.dismiss();

            // Log.d(Constants.PROJECT_TAG, "n° : " + result[0]);
            // Log.d(Constants.PROJECT_TAG, "Rue : " + result[1]);
            // Log.d(Constants.PROJECT_TAG, "CP : " + result[2]);
            // Log.d(Constants.PROJECT_TAG, "Ville : " + result[3]);

            String number = "", street = "", postcode = "", town = "";

            number = result[0];
            street = result[1];
            postcode = result[2];
            town = result[3];

            // Log.d("DEBUG", "RECEIVED LOCATION number:'" + number + "' street:'" + street + "' postcode:'" + postcode + "' town:'" + town + "'");

            if (("".equals(number) || number == null) && ("".equals(street) || "France".equals(street) || street == null)
                && ("".equals(postcode) || postcode == null) && ("".equals(town) || town == null)) {

                displayErrorPopup(R.string.no_adress_found);

                // Log.d("DEBUG", "---1");
                findViewById(R.id.Button_validate).setEnabled(false);

            } else {

                // Log.d("DEBUG", "---2");
                ((TextView) findViewById(R.id.EditText_address_street)).setText(street != null ? street : "");
                ((TextView) findViewById(R.id.EditText_address_number)).setText(number != null ? number : "");
                ((TextView) findViewById(R.id.EditText_address_postcode)).setText(postcode);
                ((TextView) findViewById(R.id.EditText_address_town)).setText(town);

                if (!((TextView) findViewById(R.id.EditText_address_street)).getText().toString().equals("")
                    && !((TextView) findViewById(R.id.EditText_address_postcode)).getText().toString().equals("")
                    && !((TextView) findViewById(R.id.EditText_address_town)).getText().toString().equals("")) {
                    findViewById(R.id.Button_validate).setEnabled(true);
                } else
                    findViewById(R.id.Button_validate).setEnabled(false);
            }

            ((Button) findViewById(R.id.Button_validate)).setText(R.string.select_position_btn_validate);
            isInvalidAdress = false;

            // Log.d(Constants.PROJECT_TAG, "Number : " + number);
            // Log.d(Constants.PROJECT_TAG, "Street : " + street);
            // Log.d(Constants.PROJECT_TAG, "Postcode : " + postcode);
            // Log.d(Constants.PROJECT_TAG, "Town : " + town);
        }
    }

    public void displayErrorPopup(int idMessage) {
        new AlertDialog.Builder(SelectPositionActivity.this).setTitle(R.string.error_popup_title)
                                                            .setMessage(idMessage)
                                                            .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                }
                                                            })
                                                            .show();
    }

    // CUSTOM REVERSE GEOCODER

    public static List<Address> getStringFromLocation(double lat, double lng) throws ClientProtocolException, IOException, JSONException {

        String address = String.format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
                                                       + Locale.getDefault().getCountry(), lat, lng);

        // Log.d("DEBUG", "getStringFromLocation address : " + address);
        HttpGet httpGet = new HttpGet(address);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        List<Address> retList = null;

        response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();
        int b;
        while ((b = stream.read()) != -1) {
            stringBuilder.append((char) b);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject = new JSONObject(stringBuilder.toString());

        retList = new ArrayList<Address>();

        if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
            JSONArray results = jsonObject.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String indiStr = result.getString("formatted_address");
                Address addr = new Address(Locale.getDefault());
                addr.setAddressLine(0, indiStr);
                retList.add(addr);
            }
        }

        return retList;
    }

}
