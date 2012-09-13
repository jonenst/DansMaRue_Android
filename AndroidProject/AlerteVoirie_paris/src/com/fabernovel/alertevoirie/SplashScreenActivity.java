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
package com.fabernovel.alertevoirie;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.fabernovel.alertevoirie.entities.Constants;
import com.fabernovel.alertevoirie.entities.JsonData;
import com.fabernovel.alertevoirie.webservice.AVService;
import com.fabernovel.alertevoirie.webservice.AVServiceErrorException;
import com.fabernovel.alertevoirie.webservice.RequestListener;

import fr.paris.android.signalement.R;

public class SplashScreenActivity extends Activity implements RequestListener {

    protected static final long SPLASH_DURATION = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                init();

                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime < SPLASH_DURATION) {
                    try {
                        sleep(SPLASH_DURATION - elapsedTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                next();
            }
        }.start();
    }

    private void init() {

        try {
            SharedPreferences sp = getSharedPreferences(JsonData.PARAM_CATEGORIES, Context.MODE_PRIVATE);
            JSONObject categoriesRequest = new JSONObject().put(JsonData.PARAM_REQUEST, JsonData.VALUE_REQUEST_GET_CATEGORIES)
                                                           .put(JsonData.PARAM_CURRENT_VERSION, sp.getString(JsonData.PARAM_CURRENT_VERSION, "0.0"));

            AVService.getInstance(this).postJSON(new JSONArray().put(categoriesRequest), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void next() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
            }
        });
    }

    @Override
    public void onRequestcompleted(int requestCode, Object result) {
        if (requestCode == AVService.REQUEST_JSON && result != null) {
            try {
                Log.d("DEBUG", "result=" + result);

                JSONObject rootObj = new JSONObject((String) result);
                JSONObject answerObj = (JSONObject) rootObj.get(JsonData.PARAM_ANSWER);

                // Version
                String version = answerObj.getString(JsonData.PARAM_VERSION);
                SharedPreferences sp = getSharedPreferences(JsonData.PARAM_CATEGORIES, Context.MODE_PRIVATE);

                if (!sp.getString(JsonData.PARAM_CURRENT_VERSION, "1.0").equals(version)) {

                    Editor e = sp.edit();
                    e.putString(JsonData.PARAM_CURRENT_VERSION, version);
                    e.commit();

                    // Categories
                    JSONObject categories = (JSONObject) rootObj.get(JsonData.PARAM_CATEGORIES);
                    try {
                        FileOutputStream fos = getApplicationContext().openFileOutput("categories.json", Context.MODE_PRIVATE);
                        Writer out = new OutputStreamWriter(fos);
                        out.write(categories.toString());
                        out.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                Log.e(Constants.PROJECT_TAG, "Erreur dde recuperation des categories", e);
            }/*
              * catch (FileNotFoundException e) {
              * // TODO Auto-generated catch block
              * Log.e(Constants.PROJECT_TAG,"File not found",e);
              * }
              */

        } else if (requestCode == AVService.REQUEST_ERROR) {
            AVServiceErrorException error = null;
            if (result instanceof JSONException) {
                error = new AVServiceErrorException(7);
                Toast.makeText(getApplicationContext(), "Erreur serveur : mauvais flux JSON", Toast.LENGTH_LONG).show();
            } else {
                error = (AVServiceErrorException) result;
            }
            String errorString = null;
            switch (error.errorCode) {
                case 19:
                    // already invalidated
                    errorString = getString(R.string.error_already_invalidated);
                    break;

                default:
                    errorString = getString(R.string.server_error);
                    break;
            }

            new AlertDialog.Builder(this).setTitle(R.string.error_popup_title)
                                         .setMessage(errorString)
                                         .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                             @Override
                                             public void onClick(DialogInterface dialog, int which) {
                                                 finish();
                                             }
                                         })
                                         .show();
        }

    }
}