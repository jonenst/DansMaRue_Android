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

import org.json.JSONObject;

public class Last_Location {
    public static double     latitude  = Constants.DEFAULT_LAT * 0.000001;
    public static double     longitude = Constants.DEFAULT_LON * 0.000001;
    public static String     number    = "";
    public static String     street    = "";
    public static String     postcode  = "";
    public static String     town      = "";
    public static String     country   = "";
    public static JSONObject Incidents;
}
