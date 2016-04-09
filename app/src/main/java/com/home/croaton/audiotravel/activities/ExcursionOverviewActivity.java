package com.home.croaton.audiotravel.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.domain.ExcursionBrief;

public class ExcursionOverviewActivity extends AppCompatActivity {

    private ExcursionBrief excursion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion_overview);

        Intent intent = getIntent();
        excursion = intent.getParcelableExtra(IntentNames.SELECTED_EXCURSION_BRIEF);

        initUI();
    }

    private void initUI()
    {
        Button openButton = (Button) findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExcursionOverviewActivity.this, MapsActivity.class);
                intent.putExtra(IntentNames.SELECTED_EXCURSION_NAME, excursion.getName());
                startActivity(intent);
            }
        });
    }
}
