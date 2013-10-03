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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.c4mprod.dansmarue.entities.IntentData;
import com.c4mprod.dansmarue.utils.LongPressMapView;
import com.c4mprod.dansmarue.utils.LongPressMapView.OnLongpressListener;
import com.c4mprod.dansmarue.utils.Utils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import fr.paris.android.signalement.R;

public class FullscreenSelectPositionActivity extends MapActivity {

    private Geocoder                                     mGeocoder;
    private CursorOverlay                                mCursorOverlay;
    private GeoPoint                                     mCurrentPoint;
    private String                                       mCurrentAdress;
    private boolean                                      isIncidentLocalized;
    private boolean                                      isUserLocalized;
    private Location                                     mUserLocation;

    private com.c4mprod.dansmarue.utils.LongPressMapView mMapView;
    private LinearLayout                                 mBottomBar;
    private AutoCompleteTextView                         mSearchBar;
    private Button                                       mClearSearch;
    private TextView                                     mStreetView;
    private TextView                                     mCityView;
    private ImageButton                                  mMyposition;
    private TextView                                     mHintView;

    Handler                                              mAutoCompleteHandler  = new Handler();
    Runnable                                             mAutoCompleteRunnable = new Runnable() {
                                                                                   @Override
                                                                                   public void run() {
                                                                                       new AutoCompleteAddressGetter().execute();
                                                                                   }
                                                                               };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_fullscreen_select_position);

        // init views
        mMyposition = (ImageButton) findViewById(R.id.btn_my_position);
        mStreetView = (TextView) findViewById(R.id.tv_street);
        mCityView = (TextView) findViewById(R.id.tv_city);
        mHintView = (TextView) findViewById(R.id.tv_hint);
        mBottomBar = (LinearLayout) findViewById(R.id.layout_bottom_bar);
        mBottomBar.setVisibility(View.GONE);
        mSearchBar = (AutoCompleteTextView) findViewById(R.id.et_search);

        mClearSearch = (Button) findViewById(R.id.btn_clear);
        mClearSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchBar.setText("");
            }
        });

        mMyposition.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserLocation != null) {
                    mMapView.getController().animateTo(new GeoPoint((int) (mUserLocation.getLatitude() * 1E6), (int) (mUserLocation.getLongitude() * 1E6)));
                }
            }
        });

        // init next button
        ImageButton nextBtn = (ImageButton) findViewById(R.id.btn_next);
        nextBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isIncidentLocalized) {
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
        mSearchBar.setAdapter(new ArrayAdapter<String>(FullscreenSelectPositionActivity.this.getApplicationContext(),
                                                       android.R.layout.simple_dropdown_item_1line, new ArrayList<String>()));
        mSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.toString().length() >= 2) {
                    mAutoCompleteHandler.removeCallbacks(mAutoCompleteRunnable);
                    mAutoCompleteHandler.postDelayed(mAutoCompleteRunnable, 2000);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        mSearchBar.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mCurrentAdress = v.getText().toString();
                if (Utils.isOnline(getApplicationContext())) {
                    new AddressGetter().execute();
                } else {
                    displayErrorPopup(R.string.server_error);
                }
                return false;
            }
        });

        // Maps
        mMapView = (LongPressMapView) findViewById(R.id.MapView_map);
        mMapView.setBuiltInZoomControls(false);
        mMapView.getController().setZoom(18);
        mMapView.setSatellite(true);

        CustomMyLocationOverlay myLocationOverlay = new CustomMyLocationOverlay(this, mMapView);
        myLocationOverlay.enableMyLocation();
        mMapView.getOverlays().add(myLocationOverlay);

        mMapView.setOnLongpressListener(new OnLongpressListener() {
            @Override
            public void onLongpress(MapView view, GeoPoint p) {
                setMarker(p);
            }
        });

        mGeocoder = new Geocoder(this);
    }

    @Override
    protected void onResume() {

        // Activate the GPS in the settings
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider != null) {
            if (!provider.contains("gps")) {
                // Notify users and show settings if they want to enable GPS
                new AlertDialog.Builder(FullscreenSelectPositionActivity.this).setMessage(getString(R.string.activate_gps))
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
            }
            // else {
            // searchForLocation();
            // }
        }
        super.onResume();
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
                        // searchForLocation();
                        break;
                }
            }
        } else {
            // the user did not enable his GPS
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setMarker(GeoPoint p) {

        if (p != null) {
            if (mCursorOverlay != null) {
                mCursorOverlay.setGeopoint(p);
            } else {
                mCursorOverlay = new CursorOverlay(getResources().getDrawable(R.drawable.map_cursor));
                mCursorOverlay.setGeopoint(p);
                mMapView.getOverlays().add(mCursorOverlay);
                mMapView.getController().animateTo(p);
            }
            mCurrentPoint = p;

            if (Utils.isOnline(getApplicationContext())) {
                new AddressGetter().execute(p.getLatitudeE6() / 1E6, p.getLongitudeE6() / 1E6);
            }
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private class CustomMyLocationOverlay extends MyLocationOverlay {

        public CustomMyLocationOverlay(Context context, MapView map) {
            super(context, map);
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView) {

            // Log.d("DEBUG", "onTap p: lat=" + p.getLatitudeE6() + " lon=" + p.getLongitudeE6());
            // setMarker(p);

            return true;
        }

        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);

            if (!isUserLocalized) {
                mUserLocation = location;
                mMapView.getController().animateTo(new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
                isUserLocalized = true;
            }
        }
    }

    private class CursorOverlay extends ItemizedOverlay<OverlayItem> {
        GeoPoint p;

        public CursorOverlay(Drawable defaultMarker) {
            super(boundCenterBottom(defaultMarker));
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
            // setMarker(p);

            if (mBottomBar.getVisibility() == View.VISIBLE) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mBottomBar.setVisibility(View.GONE);
                    }
                });
                mBottomBar.startAnimation(animation);
            }
            return true;
        }
    }

    //
    // ADRESS GETTER
    //

    private class AutoCompleteAddressGetter extends AsyncTask<Double, Void, Integer> {

        private final List<String> mAddresses = new ArrayList<String>();

        @Override
        protected Integer doInBackground(Double... arg0) {

            String address = mSearchBar.getText().toString() + " , Paris, France";

            // Log.d("DEBUG", "AutoCompleteAddressGetter ->>>address: " + address);
            List<Address> addresses = new ArrayList<Address>();

            int i = 0;
            try {
                addresses = mGeocoder.getFromLocationName(address, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                // Log.d("DEBUG", "AutoCompleteAddressGetter <<<-addresses: " + addresses);

                mAddresses.clear();
                for (Address a : addresses) {
                    mAddresses.add(a.getAddressLine(0) + " " + (a.getPostalCode() != null ? a.getPostalCode() : "") + " "
                                   + (a.getLocality() != null ? a.getLocality() : ""));
                }
            } else {
                mAddresses.clear();
            }
            return i;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer result) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> newAdaptor = new ArrayAdapter<String>(FullscreenSelectPositionActivity.this.getApplicationContext(),
                                                                               android.R.layout.simple_dropdown_item_1line, mAddresses);
                    mSearchBar.setAdapter(null);
                    mSearchBar.setAdapter(newAdaptor);
                    mSearchBar.showDropDown();
                }
            });
        }
    }

    private class AddressGetter extends AsyncTask<Double, Void, String[]> {

        protected GeoPoint geopoint;

        @Override
        protected String[] doInBackground(Double... params) {

            String[] result = new String[4];
            try {
                List<Address> addr = new ArrayList<Address>();

                if (mGeocoder != null) {
                    if (params.length == 2) {

                        // Log.d("DEBUG", "GET LOCATION FROM LAT LON : " + params[0] + " " + params[1]);

                        // http://stackoverflow.com/questions/7109240/service-not-available-geocoder-android
                        // Google issue : https://code.google.com/p/android/issues/detail?id=38009
                        addr = mGeocoder.getFromLocation(params[0], params[1], 1);

                        // workaround:
                        // try {
                        // addr = getStringFromLocation(params[0], params[1]);
                        // } catch (JSONException e) {
                        // e.printStackTrace();
                        // }
                        // Log.d("DEBUG", "addr size : " + addr.size());

                    } else {

                        String address = mSearchBar.getText().toString() + " , " + "France";

                        // Log.d("DEBUG", "GET LOCATION FROM ADDR : " + address);
                        addr = mGeocoder.getFromLocationName(address, 1);

                        // Log.d("DEBUG", "addr size : " + addr.size());
                        if (addr.size() > 0) {
                            geopoint = new GeoPoint((int) (addr.get(0).getLatitude() * 1E6), (int) (addr.get(0).getLongitude() * 1E6));

                        }
                    }
                }

                if (addr != null && addr.size() > 0) {

                    result[0] = addr.get(0).getAddressLine(0);
                    result[2] = addr.get(0).getPostalCode();
                    result[3] = addr.get(0).getLocality();

                    // num and street mixed
                    result[1] = result[0];
                    result[0] = "";
                    Pattern p = Pattern.compile("^[\\d\\-]+");
                    if (result[0] != null) {
                        Matcher m = p.matcher(result[0]);
                        if (m.find()) {
                            result[1] = result[0].replace(m.group(), "");
                            result[0] = m.group();
                        }
                    }

                    // in case street = city
                    if (result[1] != null) {
                        result[1] = result[1] == null ? "" : result[1].trim();
                        if (result[1].equals(result[3])) {
                            result[1] = "";
                        }
                    }

                    // normalisation
                    result[0] = (result[0] == null ? "" : result[0].trim());
                    if (result[0].length() > 3) result[0] = "";
                    result[1] = (result[1] == null ? "" : result[1].trim());
                    result[2] = (result[2] == null ? "" : result[2].trim());
                    result[3] = (result[3] == null ? "" : result[3].trim());
                }
            } catch (IOException e) {
                Log.e("DEBUG", "Address Error : ", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {

            if (mMapView != null && geopoint != null) {
                setMarker(geopoint);
                mMapView.getController().animateTo(geopoint);
                geopoint = null;
            }

            String number = "", street = "", postcode = "", town = "";
            number = result[0];
            street = result[1];
            postcode = result[2];
            town = result[3];

            // Log.d("DEBUG", "RECEIVED LOCATION number:'" + number + "' street:'" + street + "' postcode:'" + postcode + "' town:'" + town + "'");

            if (("".equals(number) || number == null) && ("".equals(street) || "France".equals(street) || street == null)
                && ("".equals(postcode) || postcode == null) && ("".equals(town) || town == null)) {

                displayErrorPopup(R.string.no_adress_found);
                // findViewById(R.id.btn_next).setEnabled(false);
            } else {

                if (mStreetView != null && mCityView != null && mHintView != null) {
                    mStreetView.setText((number != null ? number : "") + " " + (street != null ? street : ""));
                    mCityView.setText(postcode + " " + town);
                    mHintView.setVisibility(View.GONE);
                }

                mCurrentAdress = (number != null ? number : "") + " " + (street != null ? street : "") + " " + postcode + " " + town;

                if (mBottomBar != null && mBottomBar.getVisibility() == View.GONE) {
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                    animation.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                            mBottomBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }
                    });
                    mBottomBar.startAnimation(animation);
                }

            }

            isIncidentLocalized = true;
        }
    }

    public void displayErrorPopup(int idMessage) {
        new AlertDialog.Builder(FullscreenSelectPositionActivity.this).setTitle(R.string.error_popup_title)
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
                JSONArray components = result.getJSONArray("address_components");
                Address addr = new Address(Locale.getDefault());
                for (int c = 0; c < components.length(); c++) {
                    JSONObject comp = components.getJSONObject(c);
                    JSONArray types = comp.getJSONArray("types");
                    for (int t = 0; t < types.length(); t++) {
                        if ("locality".equals(types.getString(t))) {
                            addr.setLocality(comp.getString("long_name"));
                        } else if ("postal_code".equals(types.getString(t))) {
                            addr.setPostalCode(comp.getString("long_name"));
                        } else if ("route".equals(types.getString(t))) {
                            addr.setAddressLine(0, comp.getString("long_name"));
                        }
                    }
                }

                retList.add(addr);
            }
        }

        return retList;
    }
}
