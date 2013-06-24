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
package com.c4mprod.dansmarue.entities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.c4mprod.dansmarue.utils.Utils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Incident extends OverlayItem {
    public Incident(GeoPoint point, String title, String snippet) {
        super(point, title, snippet);
        // TODO Auto-generated constructor stub
    }

    public Incident() {
        this(null, null, null);
    }

    public static final char STATUS_ONGOING  = 'O';
    public static final char STATUS_RESOLVED = 'R';
    public static final char STATUS_UPDATED  = 'U';

    public String            address         = "";
    public String            description     = "";
    public long              categoryId      = 0;
    public double            latitude        = 0;
    public double            longitude       = 0;
    public String            date            = "";
    public char              state           = 0;
    public int               confirms        = 0;
    public long              id              = 0;
    public JSONArray         pictures_far    = new JSONArray();
    public JSONArray         pictures_close  = new JSONArray();
    public int               invalidations   = 0;
    public JSONObject        json;
    public int               priority        = 3;
    public String            email           = "";

    public JSONObject getNewIncidentRequest(Context c) {
        try {
            return new JSONObject().put(JsonData.PARAM_REQUEST, JsonData.VALUE_REQUEST_NEW_INCIDENT)
                                   .put(JsonData.PARAM_UDID, c != null ? Utils.getUdid(c) : "0000")
                                   .put(JsonData.PARAM_EMAIL, email)
                                   .put(JsonData.PARAM_INCIDENT,
                                        new JSONObject().put(JsonData.PARAM_INCIDENT_CATEGORY, categoryId)
                                                        .put(JsonData.PARAM_INCIDENT_ADDRESS, address)
                                                        .put(JsonData.PARAM_INCIDENT_DESCRIPTION, description)
                                                        .put(JsonData.PARAM_INCIDENT_PRIORITY, priority))
                                   .put(JsonData.PARAM_POSITION,
                                        new JSONObject().put(JsonData.PARAM_POSITION_LATITUDE, latitude).put(JsonData.PARAM_POSITION_LONGITUDE, longitude));
        } catch (JSONException e) {
            // Log.e(Constants.PROJECT_TAG, "Error creating new incident", e);
            return null;
        }
    }

    public JSONObject getChangeIncidentRequest(Context c) {
        try {
            return new JSONObject().put(JsonData.PARAM_REQUEST, JsonData.VALUE_REQUEST_CHANGE_INCIDENT)
                                   .put(JsonData.PARAM_IMAGES_INCIDENT_ID, id)
                                   .put(JsonData.PARAM_UDID, c != null ? Utils.getUdid(c) : "0000")
                                   .put(JsonData.PARAM_INCIDENT_CATEGORY, categoryId)
                                   .put(JsonData.PARAM_INCIDENT_ADDRESS, address)
                                   .put(JsonData.PARAM_POSITION,
                                        new JSONObject().put(JsonData.PARAM_POSITION_LATITUDE, latitude).put(JsonData.PARAM_POSITION_LONGITUDE, longitude));
        } catch (JSONException e) {
            // Log.e(Constants.PROJECT_TAG, "Error creating new incident", e);
            return null;
        }
    }

    public JSONObject getUpdateIncidentRequest(Context c, String status) {

        if (Constants.DEBUGMODE) {
            Log.d(Constants.PROJECT_TAG, "updateIncidentRequest : ");
        }
        try {
            return new JSONObject().put(JsonData.PARAM_REQUEST, JsonData.VALUE_REQUEST_UPDATE_INCIDENT)
                                   .put(JsonData.PARAM_UPDATE_INCIDENT_LOG,
                                        new JSONObject().put(JsonData.ANSWER_INCIDENT_ID, id)
                                                        .put(JsonData.PARAM_UDID, Utils.getUdid(c))
                                                        .put(JsonData.PARAM_STATUS, status));
        } catch (JSONException e) {
            // Log.e(Constants.PROJECT_TAG, "Error updating incident", e);
            return null;
        }
    }

    public static Incident fromJSONObject(Context c, JSONObject obj) {
        try {
            // Log.d(Constants.PROJECT_TAG, obj.toString());

            double latitude = obj.getDouble(JsonData.PARAM_INCIDENT_LATITUDE);
            double longitude = obj.getDouble(JsonData.PARAM_INCIDENT_LONGITUDE);

            String name = "";
            if (obj.has(JsonData.PARAM_INCIDENT_DESCRIPTION)) {
                name = obj.getString(JsonData.PARAM_INCIDENT_DESCRIPTION);
            }

            Incident result = new Incident(new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6)), name, "snippet");
            result.json = obj;
            result.latitude = latitude;
            result.longitude = longitude;
            if (obj.has(JsonData.PARAM_INCIDENT_ADDRESS)) {
                result.address = obj.getString(JsonData.PARAM_INCIDENT_ADDRESS);
            }
            result.description = name;

            if (obj.has(JsonData.PARAM_INCIDENT_DATE)) {
                result.date = obj.getString(JsonData.PARAM_INCIDENT_DATE);
            } else {
                result.date = "";
            }

            if (obj.has(JsonData.PARAM_INCIDENT_STATUS)) {
                result.state = obj.getString(JsonData.PARAM_INCIDENT_STATUS).charAt(0);
            }
            result.categoryId = obj.getLong(JsonData.PARAM_INCIDENT_CATEGORY);

            result.confirms = obj.getInt(JsonData.PARAM_INCIDENT_CONFIRMS);
            result.invalidations = obj.getInt(JsonData.PARAM_INCIDENT_INVALIDATION);
            result.id = obj.getLong(JsonData.PARAM_INCIDENT_ID);

            if (obj.has(JsonData.PARAM_INCIDENT_PICTURES)) {
                result.pictures_far = obj.getJSONObject(JsonData.PARAM_INCIDENT_PICTURES).getJSONArray(JsonData.PARAM_INCIDENT_PICTURES_FAR);
                // Log.d("DEBUG", "--->>result.pictures_far=" + result.pictures_far.toString());
            } else {
                result.pictures_far = new JSONArray();
                // Log.d("DEBUG", "--->>result.pictures_far empty");
            }

            if (obj.has(JsonData.PARAM_INCIDENT_PICTURES)) {
                result.pictures_close = obj.getJSONObject(JsonData.PARAM_INCIDENT_PICTURES).getJSONArray(JsonData.PARAM_INCIDENT_PICTURES_CLOSE);
            } else {
                result.pictures_close = new JSONArray();
            }

            if (obj.has(JsonData.PARAM_INCIDENT_PRIORITY)) {
                result.priority = obj.getInt(JsonData.PARAM_INCIDENT_PRIORITY);
            }

            if (obj.has(JsonData.PARAM_INCIDENT_EMAIL)) {
                result.email = obj.getString(JsonData.PARAM_INCIDENT_EMAIL);
            }

            result.setMarker(c.getResources().getDrawable(fr.paris.android.signalement.R.drawable.map_cursor));
            return result;
        } catch (JSONException e) {
            // Log.e(Constants.PROJECT_TAG, "Can't create Incident", e);
            return null;
        }
    }

    public static JSONObject toJSONObject(Incident input) {

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(JsonData.PARAM_INCIDENT_LATITUDE, input.latitude);
            jsonObj.put(JsonData.PARAM_INCIDENT_LONGITUDE, input.longitude);
            jsonObj.put(JsonData.PARAM_INCIDENT_ADDRESS, input.address);
            jsonObj.put(JsonData.PARAM_INCIDENT_DESCRIPTION, input.description);
            jsonObj.put(JsonData.PARAM_INCIDENT_DATE, input.date);
            jsonObj.put(JsonData.PARAM_INCIDENT_STATUS, input.state);
            jsonObj.put(JsonData.PARAM_INCIDENT_CATEGORY, input.categoryId);
            jsonObj.put(JsonData.PARAM_INCIDENT_CONFIRMS, input.confirms);
            jsonObj.put(JsonData.PARAM_INCIDENT_INVALIDATION, input.invalidations);
            jsonObj.put(JsonData.PARAM_INCIDENT_ID, input.id);
            JSONObject pictures = new JSONObject();
            pictures.put(JsonData.PARAM_INCIDENT_PICTURES_FAR, input.pictures_far);
            pictures.put(JsonData.PARAM_INCIDENT_PICTURES_CLOSE, input.pictures_close);
            jsonObj.put(JsonData.PARAM_INCIDENT_PICTURES, pictures);
            jsonObj.put(JsonData.PARAM_INCIDENT_PRIORITY, input.priority);
            jsonObj.put(JsonData.PARAM_INCIDENT_EMAIL, input.email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return json.toString();
    }
}
