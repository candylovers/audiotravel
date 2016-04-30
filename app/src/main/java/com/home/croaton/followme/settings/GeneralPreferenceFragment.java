package com.home.croaton.followme.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;

import com.home.croaton.followme.R;
import com.home.croaton.followme.activities.SettingsActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        // Hack for saving map from settings
        Preference preference = findPreference(getString(R.string.settings_send_my_map));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File toFile;
                try
                {
                    toFile = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS), "tmp.xml");
                    FileOutputStream to = new FileOutputStream(toFile);

                    FileInputStream from = new FileInputStream(getActivity().getFilesDir().getAbsolutePath() + "/excursions/gamlastan/gamlastan.xml");
                    copyFile(from, to);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                    return false;
                }

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("vnd.android.cursor.dir/email");

                String to[] = {"dmytro.shervarly@gmail.com"};
                emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(toFile));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Try this map!");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));

                return false;
            }

            public void copyFile(FileInputStream src, FileOutputStream dst)
            {
                FileChannel inChannel = src.getChannel();
                FileChannel outChannel = dst.getChannel();
                try
                {
                    long a = inChannel.size();
                    long transfered = inChannel.transferTo(0, inChannel.size(), outChannel);
                    Log.d("auudiotravel", "copying");
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    try
                    {
                        if (inChannel != null)
                            inChannel.close();
                        if (outChannel != null)
                            outChannel.close();
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Activity a = getActivity();
            startActivity(new Intent(a, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}