package com.home.croaton.followme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.home.croaton.followme.R;
import com.home.croaton.followme.domain.ExcursionBrief;
import com.home.croaton.followme.domain.ExcursionRepository;

public class ChooseRouteActivity extends AppCompatActivity {
    private final ExcursionRepository excursionRepository = new ExcursionRepository(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_route);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.stockholm_excursions);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.excursion_gallery);
        ExcursionBriefAdapter adapter = new ExcursionBriefAdapter(
                this,
                R.layout.excursion_brief_item,
                excursionRepository.getGallery().getAvailableExcursions());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExcursionBrief excursion = (ExcursionBrief) parent.getItemAtPosition(position);

                Intent intent = new Intent(ChooseRouteActivity.this, ExcursionOverviewActivity.class);
                intent.putExtra(IntentNames.SELECTED_EXCURSION_BRIEF, excursion);
                startActivity(intent);
            }
        });
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
                Intent intent = new Intent(ChooseRouteActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
