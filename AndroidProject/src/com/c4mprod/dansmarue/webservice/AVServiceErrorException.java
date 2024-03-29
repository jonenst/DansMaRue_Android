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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Alerte Voirie.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.c4mprod.dansmarue.webservice;

public class AVServiceErrorException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 8338905716824467752L;
    public int errorCode;

    public AVServiceErrorException(int code) {
        errorCode = code;
    }
    
    @Override
    public String toString() {
        return this.getClass().getCanonicalName()+" : "+errorCode+" - "+getMessage();
    }

    @Override
    public String getMessage() {
        switch (errorCode) {
            case 1:
                return "Empty JSON request";
            case 2:
                return "Bad JSON request";
            case 3:
                return "Bad request sub-element";
            case 4:
                return "Empty device identifier";
            case 5:
                return "Bad device identifier";
            case 6:
                return "Empty position parameter";
            case 7:
                return "Bad position's parameter";
            case 8:
                return "Bad category id parameter (saveIncident)";
            case 9:
                return "Empty category id parameter (saveIncident)";
            case 10:
                return "Any incident for user (getReport)";
            case 11:
                return "Empty incident identifier (updateIncident)";
            case 12:
                return "Bad incident identifier (updateIncident)";
            case 13:
                return "Bad user identifier (updateIncident)";
            case 14:
                return "Empty user identifier (updateIncident)";
            case 15:
                return "Empty incident identifier (getIncidentById)";
            case 16:
                return "Bad incident identifier(getIncidentById)";
            case 17:
                return "The incident is already put to status \"resolved\" by the same user";
            case 18:
                return "The user already confirm this incident";
            case 19:
                return "The user already invalidate this incident";
            case 20:
                return "The authentication failed: bad login";
            case 21:
                return "Bad user login (userAuthentication)";
            case 22:
                return "Empty user login (userAuthentication)";
            case 23:
                return "Bad password (userAuthentication)";
            case 24:
                return "Empty password (userAuthentication)";
            case 25:
                return "Bad authentToken (userAuthentication)";
            case 26:
                return "Empty  authentToken (userAuthentication)";
            case 27:
                return "Bad radius (getIncidentByPosition)";
            case 28:
                return "Empty radius (getIncidentByPosition)";
            case 29:
                return "Bad incident picture content";
            case 30:
                return "Impossible to read the incident picture from the temp file";
            case 31:
                return "Bad json request token from application";


            default:
                return "Unexpected error";
        }
    }
}
