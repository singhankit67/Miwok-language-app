package com.reelvideos.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.reelvideos.app.base.BaseActivity;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.ActivitySettingsBinding;
import com.reelvideos.app.utils.AppExtensions;

import static com.reelvideos.app.config.Constants.PREF_LOCALE_NAME;

//import com.mukesh.BuildConfig;

public class SettingsActivity extends BaseActivity {

    ActivitySettingsBinding binding;
    SharedPreferences sharedPreferences =
            CoreApp.getInstance().getSharedPreferences();
    String localeNameFinal = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initComponents();
    }

    @Override
    public void initComponents() {

        binding.backBtn.setOnClickListener(view -> goBackToPrev());
        binding.logoutTxt.setOnClickListener(view -> logOutConfirm());
        binding.changeLanguage.setOnClickListener(view -> showLanguagePopUp());
        binding.becomeCreator.setOnClickListener(view -> becomeCreatorPopUp());
        binding.copyright.setOnClickListener(view -> Copyright());
        binding.privacy.setOnClickListener(view -> Privacy());
        binding.loginTerms.setOnClickListener(view -> LoginTerms());
        binding.appVer.setText(getString(R.string.app_ver, BuildConfig.VERSION_NAME));

    }

    private void Copyright() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.copyright_policy_link))));
    }

    private void Privacy() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link))));
    }

    private void LoginTerms() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_and_condition_link))));
    }

    private void becomeCreatorPopUp() {
        //Prefetch Position
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.Creator_tittle))
                .setMessage(R.string.Creator_message)
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.send_email), (dialogInterface, i) -> {

                    /* Create the Intent */
                    final Intent emailIntent = new Intent(Intent.ACTION_SEND);

                    /* Fill it with Data */
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, "creators@lytevideo.com");
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I want to become Creator");
                    //emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");
                    emailIntent.setType("message/rfc822");

                    /* Send it off to the Activity-Chooser */
                    try {
                        this.startActivity(Intent.createChooser(emailIntent, getString(R.string.send_emaill)));
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(this, R.string.no_email_clients, Toast.LENGTH_SHORT).show();
                    }
                    dialogInterface.dismiss();
                })
                .show();
    }

    private void logOutConfirm() {

        new MaterialAlertDialogBuilder(this, R.style.dialogs_custom)
                .setTitle(R.string.confirm_txt)
                .setMessage(R.string.signOutMsg)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    logOut();
                })
                .setNegativeButton(android.R.string.no, null).show();

    }

    public void goBackToPrev() {
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBackToPrev();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AppExtensions.deleteCache(this);
    }

    void logOut() {
        AppExtensions.deleteCache(this);
        GlobalVariables.removeAllPrefs();
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finish();
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }


    private void showLanguagePopUp() {

        String[] locale = new String[]{"English", "Hindi"};
        int selectedItem = 0;

        //Prefetch Position
        String language = sharedPreferences.getString(PREF_LOCALE_NAME, "en");
        if (language.equals("hi")) selectedItem = 1;


        int finalSelectedItem = selectedItem;
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.title_lang))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.confirm_txt), (dialogInterface, i) -> {

                    finish();
                    startActivity(new Intent(SettingsActivity.this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                    dialogInterface.dismiss();
                })
                .setSingleChoiceItems(locale, selectedItem, (dialogInterface, i) -> {
                    if (i != finalSelectedItem) {

                        if (i == 1)
                            localeNameFinal = "hi";
                        sharedPreferences.edit().putString(PREF_LOCALE_NAME, localeNameFinal).apply();
                    }
                }).show();

    }

}