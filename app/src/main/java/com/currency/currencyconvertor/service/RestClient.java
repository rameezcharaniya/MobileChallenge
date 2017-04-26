package com.currency.currencyconvertor.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Rameez.
 */

public class RestClient {

    private Gson gson;
    private ApiService apiService;

    public RestClient() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .create();

        apiService = createAPIService();
    }

    public ApiService getAPIService() {
        return apiService;
    }

    private ApiService createAPIService() {
        String BASE_URL = "http://api.fixer.io";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);


        OkHttpClient client = httpClient.build();
        Retrofit retrofitAPI = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofitAPI.create(ApiService.class);
    }
}
