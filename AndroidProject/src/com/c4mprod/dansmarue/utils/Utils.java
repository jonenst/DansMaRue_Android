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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.c4mprod.dansmarue.NewVersionDialogActivity;
import com.c4mprod.dansmarue.entities.Constants;

public class Utils {
    public static String getUdid(Context c) {
        // TelephonyManager tel = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        // return tel.getDeviceId();

        String deviceIdentifier = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceIdentifier;
    }

    public static void fromInputToOutput(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    public static void handeResponseHeaders(Context _context, HttpResponse response, boolean checkForUpdate) {
        Header[] response_headers = response.getAllHeaders();
        for (int i = 0; i < response_headers.length; i++) {
            Header header = response_headers[i];
            // Log.i(Constants.PROJECT_TAG, "RESPONSE HEADERS          : " + header.getName() + ":" + header.getValue());

        }

        Header update_force_header = null;
        Header update_available_header = null;

        Header[] update_available_headers = response.getHeaders(Constants.HTTPHEADERKEY_APP_AVAILABLE_VERSION);
        if (update_available_headers != null && update_available_headers.length >= 1) {
            update_available_header = update_available_headers[0];

        }

        Header[] update_force_headers = response.getHeaders(Constants.HTTPHEADERKEY_APP_X_APP_FORCE_UPDATE);
        if (update_force_headers != null && update_force_headers.length >= 1) {
            update_force_header = update_force_headers[0];

        }

        if (update_available_header != null && update_force_header != null) {
            final Boolean force_update = Boolean.valueOf(update_force_header.getValue());
            if (force_update || checkForUpdate) {
                try {
                    final int currentVersionCode = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0).versionCode;
                    final String latestVersionCodeString = update_available_header.getValue();
                    Integer latestVersionCode = Integer.valueOf(latestVersionCodeString);
                    if (currentVersionCode < latestVersionCode) {
                        showNewVersionDialog(_context, force_update);
                    }

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

    }

    private static void showNewVersionDialog(Context ctx, boolean forceUpdate) {
        Intent intent = new Intent(ctx, NewVersionDialogActivity.class);
        intent.putExtra(Constants.KEY_NEW_VERSION_MSG, forceUpdate);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
