package com.currency.currencyconvertor.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

import com.currency.currencyconvertor.R;
import com.currency.currencyconvertor.fragment.ConverterFragment;

public class ConverterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        FragmentManager fragmentManager = getFragmentManager();
        ConverterFragment fragment = (ConverterFragment) fragmentManager.findFragmentById(R.id.id_fragment_converter);
        if (fragment == null) {
            fragment = new ConverterFragment();
            fragmentManager.beginTransaction().replace(R.id.id_fragment_converter, fragment).commit();
        }
    }
}
