package com.reelvideos.app.base;


import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import static com.reelvideos.app.config.Constants.PREF_LOCALE_NAME;

abstract public class BaseActivity extends AppCompatActivity {
    abstract public void initComponents();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppLocale(CoreApp.getInstance().getSharedPreferences().getString(PREF_LOCALE_NAME, "en"));

    }

    public void setAppLocale(String localeCode){
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.setLocale(new Locale(localeCode.toLowerCase()));
        resources.updateConfiguration(config, dm);
    }

}
