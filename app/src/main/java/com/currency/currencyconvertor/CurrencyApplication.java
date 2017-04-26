package com.currency.currencyconvertor;

import android.app.Application;

import com.currency.currencyconvertor.service.RestClient;

/**
 * @author Rameez.
 */

public class CurrencyApplication extends Application {
    private static RestClient restClient;

    @Override
    public void onCreate() {
        super.onCreate();
        restClient = new RestClient();
    }

    public static RestClient getRestClient() {
        return restClient;
    }
}
