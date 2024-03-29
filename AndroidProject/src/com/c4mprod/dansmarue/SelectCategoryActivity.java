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

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.c4mprod.dansmarue.entities.Category;
import com.c4mprod.dansmarue.entities.Constants;
import com.c4mprod.dansmarue.entities.IntentData;

import fr.paris.android.signalement.R;

public class SelectCategoryActivity extends ListActivity {
    private static final String[] PROJECTION   = new String[] { BaseColumns._ID, Category.NAME, Category.CHILDREN };

    private Cursor                categoryCursor;
    public static final String    TYPEDRAWABLE = "drawable";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.layout_select_category);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_nouveau_rapport);

        long categoryId = getIntent().getLongExtra(IntentData.EXTRA_CATEGORY_ID, 0);
        categoryCursor = managedQuery(ContentUris.withAppendedId(Category.CHILDREN_CONTENT_URI, categoryId), PROJECTION, null, null, null);
        setListAdapter(new catAdapter(this, R.layout.cell_text, categoryCursor, new String[] { Category.NAME }, new int[] { R.id.TextView_text }));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        categoryCursor.moveToPosition(position);

        //Log.d(Constants.PROJECT_TAG, "setResult? " + categoryCursor.isNull(categoryCursor.getColumnIndex(Category.CHILDREN)));
        if (categoryCursor.isNull(categoryCursor.getColumnIndex(Category.CHILDREN))) {
            Intent result = new Intent();
            result.putExtra(IntentData.EXTRA_CATEGORY_ID, id);
            setResult(RESULT_OK, result);
            finish();
        } else {
            Intent i = new Intent(this, SelectCategoryActivity.class);
            i.putExtra(IntentData.EXTRA_CATEGORY_ID, id);
            startActivityForResult(i, 0);
        }
    }

    private class catAdapter extends SimpleCursorAdapter {

        public catAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            // TODO Auto-generated constructor stub
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View v = super.getView(position, convertView, parent);
            Cursor cur = (Cursor) getItem(position);
            int d = getResources().getIdentifier("pictocat" + cur.getLong(cur.getColumnIndex(BaseColumns._ID)), TYPEDRAWABLE, R.class.getPackage().getName());

            ((ImageView) v.findViewById(R.id.ImageViewCat)).setImageResource(d);

            return v;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }
}