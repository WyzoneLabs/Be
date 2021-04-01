package com.brimbay.be;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import adapter.LocationAdapter;
import model.Locations;
import network.APIClient;
import network.APIInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchLocationActivity extends AppCompatActivity implements LocationAdapter.OnItemClickListener {
    private static final String TAG = "SearchLocation";
    private SearchLocationActivity selfRef;
    //region Views
    private EditText _search;
    private ImageButton _cancel;
    private RecyclerView _recycler;
    //endregion

    //region Vars
    private LocationAdapter adapter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        setContentView(R.layout.activity_search_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _search = findViewById(R.id.search);
        _cancel = findViewById(R.id.cancel);
        _recycler = findViewById(R.id.recycler);

        getLocations();
    }

    public void getLocations() {
        APIInterface mApiInterface = APIClient.getClient(selfRef).create(APIInterface.class);
        Call<List<Locations>> genderCall = mApiInterface.getLocations();
        genderCall.enqueue(new Callback<List<Locations>>() {
            @Override
            public void onResponse(@NotNull Call<List<Locations>> call, @NotNull Response<List<Locations>> response) {
                if (response.code() == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        performSearch(response.body());

                    } else {
                        Log.d(TAG, "Null Locations List");
                    }

                }
            }

            @Override
            public void onFailure(@NotNull Call<List<Locations>> call, @NotNull Throwable t) {
                Log.d(TAG, "Error:" + t.getLocalizedMessage());
            }
        });
    }

    private void performSearch(List<Locations> data) {
        adapter = new LocationAdapter(data);
        _recycler.setHasFixedSize(true);
        _recycler.setLayoutManager(new LinearLayoutManager(selfRef, RecyclerView.VERTICAL, false));
        _recycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(this);

        _search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                _cancel.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s);
            }
        });

        _cancel.setOnClickListener((v) -> _search.setText(""));
    }

    @Override
    public void itemClickListener(Locations location, int i) {
        Intent intent = new Intent();
        intent.setAction("LOCATION");
        intent.putExtra("data", location);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}