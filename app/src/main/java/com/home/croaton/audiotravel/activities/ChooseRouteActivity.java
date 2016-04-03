package com.home.croaton.audiotravel.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.settings.SettingsActivity;

public class ChooseRouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_route);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Choose route");
        setSupportActionBar(toolbar);

        configureRoutList();
    }

    private void configureRoutList()
    {
        View route1 = findViewById(R.id.route_demo);
        View route2 = findViewById(R.id.route_abrahamsberg);
        View route3 = findViewById(R.id.route_gamlastan);

        final Context self = this;
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(self, MapsActivity.class);

                intent.putExtra(getString(R.string.route_name), v.getId());

                startActivity(intent);
            }
        };

        route1.setOnClickListener(onClick);
        route2.setOnClickListener(onClick);
        route3.setOnClickListener(onClick);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_button_id:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
