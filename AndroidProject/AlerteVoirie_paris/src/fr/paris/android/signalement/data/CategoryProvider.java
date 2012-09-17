/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.paris.android.signalement.data;

import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.fabernovel.alertevoirie.entities.Constants;
import com.fabernovel.alertevoirie.entities.JsonData;
import com.fabernovel.alertevoirie.utils.JSONCursor;

public class CategoryProvider extends ContentProvider {

    private static final int  CATEGORIES    = 1;
    private static final int  CATEGORIES_ID = 2;
    private static final int  CATEGORY_ID   = 3;

    private static UriMatcher uriMatcher    = null;

    // public static final String AUTHORITY = "fr.paris.android.signalement.dataprovider.advice";

    public static JSONObject  categories;

    @Override
    public boolean onCreate() {

        return true;
    }

    public void laodDataIfNeeded() {

        if (categories == null) {
            categories = new JSONObject();
            // load json data
            // DataInputStream in = new DataInputStream(getContext().getResources().openRawResource(fr.paris.android.signalement.R.raw.categories));
            try {
                // open categorie file located in the internal memory
                FileInputStream in = getContext().openFileInput("categories.json");
                byte[] buffer = new byte[in.available()];
                in.read(buffer);
                String bufferStr = new String(buffer);
                Log.d(Constants.PROJECT_TAG, "<--- Read from file = " + bufferStr);
                categories = (JSONObject) new JSONTokener(bufferStr).nextValue();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e("Alerte Voirie", "JSON error", e);
            } catch (ClassCastException e) {
                Log.e("Alerte Voirie", "JSON error", e);
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read only provider");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Read only provider");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        laodDataIfNeeded();

        JSONArray array = new JSONArray();
        try {
            String categoryId = JsonData.VALUE_NULL;
            switch (uriMatcher.match(uri)) {
                case CATEGORIES_ID:
                    categoryId = uri.getLastPathSegment();
                    //$FALL-THROUGH$
                case CATEGORIES:
                    JSONObject parent = categories.getJSONObject(categoryId);
                    if (parent.has(JsonData.PARAM_CATEGORY_CHILDREN)) {
                        JSONArray subset = parent.getJSONArray(JsonData.PARAM_CATEGORY_CHILDREN);
                        for (int i = 0; i < subset.length(); i++) {
                            JSONObject obj = categories.getJSONObject(subset.getString(i));
                            obj.put(BaseColumns._ID, (long) Integer.valueOf(subset.getString(i)));
                            array.put(obj);
                        }
                    }
                    break;

                case CATEGORY_ID:
                    categoryId = uri.getLastPathSegment();
                    array.put(categories.getJSONObject(categoryId));
                    Log.d(Constants.PROJECT_TAG, "category returned = " + categories.getJSONObject(categoryId));
                    break;

                default:
                    return null;
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Cursor c = new JSONCursor(array, projection);
        if (c != null) c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read only provider");
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Constants.CATEGORY_PROVIDER_AUTHORITY, "categories", CATEGORIES);
        uriMatcher.addURI(Constants.CATEGORY_PROVIDER_AUTHORITY, "categories/*", CATEGORIES_ID);
        uriMatcher.addURI(Constants.CATEGORY_PROVIDER_AUTHORITY, "category/*", CATEGORY_ID);
    }
}
