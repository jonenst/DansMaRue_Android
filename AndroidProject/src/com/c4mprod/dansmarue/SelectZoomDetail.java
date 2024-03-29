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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.c4mprod.dansmarue.entities.Constants;
import com.c4mprod.dansmarue.entities.PictoView;

import fr.paris.android.signalement.R;

public class SelectZoomDetail extends Activity {

    ImageView photo;
    PictoView picto;
    Bitmap    picture;
    float     photo_width, photo_heigth, layout_with, layout_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.layout_report_picture_zoom);

        photo = ((ImageView) findViewById(R.id.ImageViewPictoBG));
        picto = ((PictoView) findViewById(R.id.ViewPicto));
        final Button okBtn = (Button) findViewById(R.id.ButtonViewPicto);

        setPictureToImageView(ReportDetailsActivity.CAPTURE_FAR, photo);

        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_nouveau_rapport);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LayoutParams params = picto.getLayoutParams();

                params.width = photo.getWidth();
                params.height = photo.getHeight();

                picto.setLayoutParams(params);
                okBtn.setVisibility(View.VISIBLE);
            }
        }, 1000);
        ((EditText) SelectZoomDetail.this.findViewById(R.id.Comment_img)).setText(getIntent().getStringExtra("comment"));

        ((EditText) SelectZoomDetail.this.findViewById(R.id.Comment_img)).setVisibility(View.INVISIBLE);

        okBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    layout_with = picto.getWidth();
                    layout_height = picto.getHeight();

                    // if (Constants.DEBUGMODE) {
                    // Log.d(Constants.PROJECT_TAG, "Margin: " + (((photo.getWidth() + layout_with) / 2) - photo.getWidth()) * photo.getWidth() / photo_width
                    // + "," + (((photo.getHeight() + layout_height) / 2) - photo.getHeight()) * photo.getHeight() / photo_heigth);
                    // Log.d(Constants.PROJECT_TAG, "onClick : Photo widht/height" + photo.getWidth() + "/" + photo.getHeight() + " ImageView: " + photo_width
                    // + "/" + photo_heigth);
                    // }
                    picto.setSupport(picture, photo_width, photo_heigth, getApplicationContext());

                    Intent data = new Intent();
                    data.putExtra("comment", ((EditText) SelectZoomDetail.this.findViewById(R.id.Comment_img)).getText().toString());

                    setResult(RESULT_OK, data);
                    finish();
                } catch (FileNotFoundException e) {
                    Log.e(Constants.PROJECT_TAG, "Picture save error", e);
                }

            }
        });
        okBtn.postInvalidate();
    }

    private void setPictureToImageView(String pictureName, ImageView imageView) {
        picture = null;

        try {
            InputStream in = openFileInput(pictureName);
            picture = BitmapFactory.decodeStream(in);
            in.close();

            if (picture.getWidth() > picture.getHeight()) {
                Matrix m = new Matrix();
                m.postRotate(90);
                picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), m, true);
            }

            photo_width = picture.getWidth();
            photo_heigth = picture.getHeight();

            imageView.setImageBitmap(picture);

            // Log.d(Constants.PROJECT_TAG, picture.getWidth() + "," + picture.getHeight());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}