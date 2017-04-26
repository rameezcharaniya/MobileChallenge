package com.currency.currencyconvertor.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author Rameez.
 */

public class CurrencyResponseModel implements Parcelable {
    private String base;
    private Date date;
    private Map<String, BigDecimal> rates;

    private CurrencyResponseModel(Parcel in) {
        base = in.readString();
        long date1 = in.readLong();
        date = date1 < 0 ? null : new java.util.Date(date1);
    }

    public static final Creator<CurrencyResponseModel> CREATOR = new Creator<CurrencyResponseModel>() {
        @Override
        public CurrencyResponseModel createFromParcel(Parcel in) {
            return new CurrencyResponseModel(in);
        }

        @Override
        public CurrencyResponseModel[] newArray(int size) {
            return new CurrencyResponseModel[size];
        }
    };

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    public void setRates(Map<String, BigDecimal> rates) {
        this.rates = rates;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(base);
        dest.writeLong(date == null ? -1 : date.getTime());
    }
}
