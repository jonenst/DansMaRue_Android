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

/*
 * ----------------------------------------------------------------------------------
 * WARNING : YOU NEED TO RENAME THIS CLASS TO COMPILE.
 * Class has to be renamed into "Constants.java" and fill it with your own server URL
 * ----------------------------------------------------------------------------------
 */
public final class ConstantsTemplate {

    public static final boolean DEBUGMODE                            = true;
    public static final boolean USE_HOCKEYAPP                        = DEBUGMODE;                                                    // to remove for

    public static int           DEFAULT_LON                          = 48869881;
    public static int           DEFAULT_LAT                          = 2353171;

    // publish build

    public static final String  HOCKEY_APP_ID                        = "xxx";

    public final static String  HTTPHEADERKEY_APP_AVAILABLE_VERSION  = "X-App-Available-Version";
    public final static String  HTTPHEADERKEY_APP_X_APP_FORCE_UPDATE = "X-App-Force-Update";
    public static final String  KEY_NEW_VERSION_MSG                  = "KEY_NEW_VERSION_MSG";

    public static final String  PROJECT_TAG                          = "DEBUG";
    public static final String  RESOURCES_PACKAGE                    = "com.fabernovel.alertevoirie";
    public static final int     PICTURE_PREFERED_WIDTH               = 640;
    public static final String  NEW_REPORT                           = "NewReport";
    public static final String  SDCARD_PATH                          = "/Android/data/" + RESOURCES_PACKAGE;
    public static final long    TIMEOUT                              = 30000;

    // Ville de Paris ==========================================================

    public static final String  CATEGORY_PROVIDER_AUTHORITY          = "fr.paris.android.signalement.dataprovider.advice";
    private static final String AV_URL_PROD                          = "http://r57-sira-ws.apps.paris.fr/sira/rest/signalement/api/";

    public static final String  AV_URL                               = AV_URL_PROD;
}
