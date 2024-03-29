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

import android.net.Uri;

public interface Category {
    public static final Uri    CONTENT_URI          = Uri.parse("content://" + Constants.CATEGORY_PROVIDER_AUTHORITY + "/category");
    public static final Uri    CHILDREN_CONTENT_URI = Uri.parse("content://" + Constants.CATEGORY_PROVIDER_AUTHORITY + "/categories");
    public static final String NAME                 = "name";
    public static final String CHILDREN             = "children_id";
    public static final String PARENT               = "parent_id";

}
