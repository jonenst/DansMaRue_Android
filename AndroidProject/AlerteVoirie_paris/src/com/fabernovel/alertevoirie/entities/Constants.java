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
package com.fabernovel.alertevoirie.entities;

public final class Constants {

    public static final boolean DEBUGMODE                            = false;

    public static final boolean USE_HOCKEYAPP                        = DEBUGMODE;
    public static final String  HOCKEY_APP_ID                        = "b9b8fe70b6839e828c37ac95b8206293";

    public final static String  HTTPHEADERKEY_APP_AVAILABLE_VERSION  = "X-App-Available-Version";
    public final static String  HTTPHEADERKEY_APP_X_APP_FORCE_UPDATE = "X-App-Force-Update";
    public static final String  KEY_NEW_VERSION_MSG                  = "KEY_NEW_VERSION_MSG";

    public static final String  PROJECT_TAG                          = "DEBUG";
    public static final String  RESOURCES_PACKAGE                    = "com.fabernovel.alertevoirie";
    public static final int     PICTURE_PREFERED_WIDTH               = 640;
    public static final String  NEW_REPORT                           = "NewReport";
    public static final String  SDCARD_PATH                          = "/Android/data/" + RESOURCES_PACKAGE;
    public static final long    TIMEOUT                              = 30000;

    // C4M =====================================================================
    /*
     * private static final String AV_URL_PREPROD = "http://alerte-voirie.ppd.c4mprod.com/api/";
     * private static final String AV_URL_PROD = "http://www.alertevoirie.com/api/";
     * private static final String URL_TEST = "http://test.dev.playtomo.com/tools/testpost.php";
     * public static String CATEGORY_PROVIDER_AUTHORITY = "com.fabernovel.alertevoirie.dataprovider.advice";
     * public static final String AV_URL = AV_URL_PROD;
     */

    // Ville de Paris ==========================================================

    private static final String AV_URL_DEV_LUTECE_SIRA_INTEG         = "http://dev.lutece.paris.fr/sira-integ/rest/signalement/api/";
    private static final String AV_URL_DEV_LUTECE_SIRA_R7            = "http://dev.lutece.paris.fr/sira/rest/signalement/api/";
    private static final String AV_URL_DEV_LUTECE_R57_SIRA           = "http://r57-sira-ws.rec.apps.paris.fr/sira/rest/signalement/api/";
    public static final String  CATEGORY_PROVIDER_AUTHORITY          = "fr.paris.android.signalement.dataprovider.advice";

    public static final String  AV_URL                               = DEBUGMODE ? AV_URL_DEV_LUTECE_SIRA_INTEG : AV_URL_DEV_LUTECE_R57_SIRA;
}
