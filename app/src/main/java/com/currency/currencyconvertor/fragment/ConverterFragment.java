package com.currency.currencyconvertor.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.currency.currencyconvertor.CurrencyApplication;
import com.currency.currencyconvertor.R;
import com.currency.currencyconvertor.StorageClass;
import com.currency.currencyconvertor.model.CurrencyResponseModel;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Rameez.
 */

public class ConverterFragment extends Fragment {

    private CurrencyResponseModel currencyResponse;
    private RecyclerView currencyRecyclerView;
    private Spinner currencySelectionSpinner;
    public static final int TIME_30_MINUTES = 30 * 60 * 1000;
    public static final String TIME_PREFERENCE = "time-preference";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        currencyRecyclerView = (RecyclerView) view.findViewById(R.id.currency_recycler_view);
        final EditText currencyValueEditText = (EditText) view.findViewById(R.id.currency_value_edit_text);
        currencySelectionSpinner = (Spinner) view.findViewById(R.id.currency_selection_spinner);
        currencyValueEditText.setText("1");
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        currencyRecyclerView.setLayoutManager(layoutManager);

        currencySelectionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUI(currencySelectionSpinner.getSelectedItem().toString(), BigDecimal.valueOf(Double.parseDouble(currencyValueEditText.getText().toString())));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        currencyValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    currencyValueEditText.setText("1");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (currencySelectionSpinner.getSelectedItem() != null && s.toString().trim().length() > 0) {
                    updateUI(currencySelectionSpinner.getSelectedItem().toString(), BigDecimal.valueOf(Double.parseDouble(s.toString())));
                }
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long time = preferences.getLong(TIME_PREFERENCE, 0);
        long remainingTime = TIME_30_MINUTES - (System.currentTimeMillis() - time);
        if (remainingTime < 0) {
            doApiCall();
        } else {
            processData(new Gson().fromJson(StorageClass.readFromFile(getActivity()), CurrencyResponseModel.class));
            TimerTask task = new scheduledTask();
            Timer myTimer = new Timer(true);
            myTimer.schedule(task, remainingTime);
        }
        return view;
    }

    private class scheduledTask extends TimerTask {
        @Override
        public void run() {
            doApiCall();
        }
    }

    private void updateUI(String selectedCurrency, BigDecimal amount) {
        Map<String, BigDecimal> originalMap = currencyResponse.getRates();
        List<String> keyList = new ArrayList<>();
        keyList.addAll(originalMap.keySet());
        Map<String, BigDecimal> tempMap = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            BigDecimal originalValue = originalMap.get(keyList.get(i));
            BigDecimal updatedValue = (originalValue.divide(currencyResponse.getRates().get(selectedCurrency), 4, RoundingMode.HALF_UP)).multiply(amount);
            tempMap.put(keyList.get(i), updatedValue);
        }
        currencyResponse.setRates(tempMap);
        currencyRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private void processData(CurrencyResponseModel response) {
        currencyResponse = response;
        Map<String, BigDecimal> tempCurrencyMap = currencyResponse.getRates();
        tempCurrencyMap.put(currencyResponse.getBase(), new BigDecimal(1.0));
        tempCurrencyMap.putAll(currencyResponse.getRates());
        currencyResponse.setRates(tempCurrencyMap);
        List<String> list = new ArrayList<>();
        list.addAll(currencyResponse.getRates().keySet());
        if (getActivity() != null) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySelectionSpinner.setAdapter(dataAdapter);
            currencySelectionSpinner.setSelection(list.size() - 1);
            CurrencyListAdapter currencyListAdapter = new CurrencyListAdapter();
            currencyRecyclerView.setAdapter(currencyListAdapter);
            currencyRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void doApiCall() {
        Call<CurrencyResponseModel> call = CurrencyApplication.getRestClient().getAPIService().getLatestCurrencies();
        call.enqueue(new Callback<CurrencyResponseModel>() {
            @Override
            public void onResponse(Call<CurrencyResponseModel> call, Response<CurrencyResponseModel> response) {
                if (response.isSuccessful()) {
                    if (getActivity() != null) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                        editor.putLong(TIME_PREFERENCE, System.currentTimeMillis());
                        editor.apply();
                        StorageClass.writeToFile(new Gson().toJson(response.body()), getActivity());
                        TimerTask task = new scheduledTask();
                        Timer myTimer = new Timer(true);
                        myTimer.schedule(task, TIME_30_MINUTES);
                    }
                    processData(response.body());
                }
            }

            @Override
            public void onFailure(Call<CurrencyResponseModel> call, Throwable t) {
            }
        });
    }

    private class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyViewHolder> {
        private ArrayList<String> keyList;

        private CurrencyListAdapter() {
            keyList = new ArrayList<>();
            keyList.addAll(currencyResponse.getRates().keySet());
        }

        @Override
        public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            return new CurrencyViewHolder(layoutInflater.inflate(R.layout.layout_currency_item, parent, false));
        }

        @Override
        public void onBindViewHolder(CurrencyViewHolder holder, int position) {
            holder.countryName.setText(keyList.get(position));
            holder.convertedValue.setText(currencyResponse.getRates().get(keyList.get(position)).toString());
        }

        @Override
        public int getItemCount() {
            return currencyResponse != null ? currencyResponse.getRates().size() : 0;
        }
    }

    private class CurrencyViewHolder extends ViewHolder {
        TextView countryName;
        TextView convertedValue;

        CurrencyViewHolder(View itemView) {
            super(itemView);
            countryName = (TextView) itemView.findViewById(R.id.country_name);
            convertedValue = (TextView) itemView.findViewById(R.id.converted_value);
        }
    }
}
