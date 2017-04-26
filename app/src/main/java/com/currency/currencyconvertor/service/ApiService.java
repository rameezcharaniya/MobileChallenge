package com.currency.currencyconvertor.service;

import com.currency.currencyconvertor.model.CurrencyResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author Rameez.
 */

public interface ApiService {
    @GET("latest")
    Call<CurrencyResponseModel> getLatestCurrencies();
}
