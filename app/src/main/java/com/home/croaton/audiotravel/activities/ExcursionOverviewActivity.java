package com.home.croaton.audiotravel.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.domain.Excursion;
import com.home.croaton.audiotravel.domain.ExcursionBrief;
import com.home.croaton.audiotravel.download.IExcursionDownloader;
import com.home.croaton.audiotravel.download.S3ExcursionDownloader;

public class ExcursionOverviewActivity extends AppCompatActivity {

    private ExcursionBrief excursion;

    private Button openButton;
    private Button loadButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion_overview);

        Intent intent = getIntent();
        excursion = intent.getParcelableExtra(IntentNames.SELECTED_EXCURSION_BRIEF);

        initUI();
    }

    private void initUI() {
        openButton = (Button) findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExcursionOverviewActivity.this, MapsActivity.class);
                intent.putExtra(IntentNames.SELECTED_EXCURSION_NAME, excursion.getName());
                startActivity(intent);
            }
        });

        loadButton = (Button) findViewById(R.id.load_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadExcursionTask downloadTask = new DownloadExcursionTask(ExcursionOverviewActivity.this);
                downloadTask.execute(excursion);
            }
        });

        progressDialog = new ProgressDialog(ExcursionOverviewActivity.this);
        progressDialog.setMessage("A message");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
    }

    private class DownloadExcursionTask extends AsyncTask<ExcursionBrief, Integer, Excursion> {
        private final Context context;

        public DownloadExcursionTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Excursion doInBackground(ExcursionBrief... inputs) {
            IExcursionDownloader downloader = new S3ExcursionDownloader(context);
            return downloader.downloadExcursion(inputs[0]);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Excursion result) {
            progressDialog.dismiss();
        }
    }
}
