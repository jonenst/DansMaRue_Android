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
import android.os.Handler;

import com.c4mprod.dansmarue.entities.JsonData;
import com.c4mprod.dansmarue.utils.Utils;
import com.c4mprod.dansmarue.webservice.AVService;
import com.c4mprod.dansmarue.webservice.AVServiceErrorException;
import com.c4mprod.dansmarue.webservice.RequestListener;

import fr.paris.android.signalement.R;

public class SplashScreenActivity extends Activity implements RequestListener {

    protected static final long SPLASH_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                init();
            }
        }, SPLASH_DURATION);
    }

    private void init() {

        if (Utils.isOnline(getApplicationContext())) {
            try {
                SharedPreferences sp = getSharedPreferences(JsonData.PARAM_CATEGORIES, Context.MODE_PRIVATE);
                JSONObject categoriesRequest = new JSONObject().put(JsonData.PARAM_REQUEST, JsonData.VALUE_REQUEST_GET_CATEGORIES)
                                                               .put(JsonData.PARAM_CURRENT_VERSION, sp.getString(JsonData.PARAM_CURRENT_VERSION, "0"));

                AVService.getInstance(this).postJSON(new JSONArray().put(categoriesRequest), this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            displayNoConnexionPopup();
        }

    }

    private void next() {
        // runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
        // }
        // });
    }

    @Override
    public void onRequestcompleted(int requestCode, Object result) {
        if (requestCode == AVService.REQUEST_JSON && result != null) {
            try {
                // //Log.d(Constants.PROJECT_TAG, "downloaded =" + result);

                JSONArray rootArr = new JSONArray((String) result);
                JSONObject rootObj = rootArr.optJSONObject(0);
                JSONObject answerObj = (JSONObject) rootObj.get(JsonData.PARAM_ANSWER);

                // Version
                String version = answerObj.getString(JsonData.PARAM_VERSION);
                SharedPreferences sp = getSharedPreferences(JsonData.PARAM_CATEGORIES, Context.MODE_PRIVATE);

                if (!sp.getString(JsonData.PARAM_CURRENT_VERSION, "0").equals(version)) {

                    Editor e = sp.edit();
                    e.putString(JsonData.PARAM_CURRENT_VERSION, version);
                    e.commit();

                    // Categories
                    JSONObject categories = (JSONObject) answerObj.get(JsonData.PARAM_CATEGORIES);
                    getApplicationContext().deleteFile("categories.json");
                    try {
                        FileOutputStream fos = getApplicationContext().openFileOutput("categories.json", Context.MODE_PRIVATE);
                        Writer out = new OutputStreamWriter(fos);
                        String strObj = categories.toString();
                        // //Log.d(Constants.PROJECT_TAG, "---> Write to file = "
                        // + strObj);
                        out.write(strObj);
                        out.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                next();

            } catch (JSONException e) {
                e.printStackTrace();
                displayNoConnexionPopup();
            }

        } else if (requestCode == AVService.REQUEST_ERROR) {
            AVServiceErrorException error = null;
            String errorString = null;

            if (result instanceof JSONException) {
                // error = new AVServiceErrorException(7);
                errorString = "Erreur serveur : mauvais flux JSON";
            } else {
                error = (AVServiceErrorException) result;
                if (error != null) {
                    switch (error.errorCode) {
                        case 19:
                            // already invalidated
                            errorString = getString(R.string.error_already_invalidated);
                            break;

                        default:
                            errorString = getString(R.string.server_error);
                            break;
                    }
                } else {
                    errorString = getString(R.string.server_error);
                }
            }

            if (!this.isFinishing()) {
                new AlertDialog.Builder(SplashScreenActivity.this).setTitle(R.string.error_popup_title)
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

    public void displayNoConnexionPopup() {
        new AlertDialog.Builder(SplashScreenActivity.this).setTitle(R.string.error_popup_title)
                                                          .setMessage(R.string.server_error)
                                                          .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                              @Override
                                                              public void onClick(DialogInterface dialog, int which) {
                                                                  finish();
                                                              }
                                                          })
                                                          .show();
    }

}