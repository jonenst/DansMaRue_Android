package com.fabernovel.alertevoirie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

import com.fabernovel.alertevoirie.entities.Constants;

import fr.paris.android.signalement.R;

public class NewVersionDialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Bundle bundleExtra = getIntent().getExtras();
        String title = getString(R.string.new_version_dialog_title);
        Boolean force = bundleExtra.getBoolean(Constants.KEY_NEW_VERSION_MSG);
        OnClickListener ok_listener = new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String url_string = "http://market.android.com/details?id=fr.paris.android.signalement";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_string));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        break;
                }
                finish();
            }

        };
        if (force) {
            new AlertDialog.Builder(this).setTitle(title)
                                         .setIcon(R.drawable.icon)
                                         .setMessage(R.string.une_nouvelle_version_est_disponible)
                                         .setCancelable(false)
                                         .setPositiveButton(getText(R.string.new_version_dialog_ok), ok_listener)
                                         .show();
        } else {
            new AlertDialog.Builder(this).setTitle(title)
                                         .setIcon(R.drawable.icon)
                                         .setMessage(R.string.une_nouvelle_version_est_disponible)
                                         .setPositiveButton(getText(R.string.new_version_dialog_ok), ok_listener)
                                         .setNegativeButton(getText(R.string.new_version_dialog_cancel), ok_listener)
                                         .show();

        }
    }

}
