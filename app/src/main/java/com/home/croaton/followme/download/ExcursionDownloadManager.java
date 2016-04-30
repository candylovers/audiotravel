package com.home.croaton.followme.download;

import android.content.Context;
import android.text.TextUtils;

import com.home.croaton.followme.domain.ExcursionBrief;
import com.home.croaton.followme.domain.Route;
import com.home.croaton.followme.domain.RouteSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ExcursionDownloadManager {

    private static final String LOCAL_EXCURSIONS_DIR = "excursions";
    private static final CharSequence FOLDER_SEPARATOR = "/";
    private static final String XML_EXTENSION = ".xml";
    private static final String MP3_EXTENSION = ".mp3";
    private static final String POINT_NAMES_SUFFIX = "_point_names";
    private static final String LOCAL_AUDIO_FOLDER_NAME = "audio";

    private final ExcursionBrief excursionBrief;
    private final Context context;
    private String language;

    public ExcursionDownloadManager(Context context, ExcursionBrief brief, String currentLanguage) {
        this.excursionBrief = brief;
        this.language = currentLanguage;
        this.context = context;
    }

    public boolean excursionIsLoaded() {
        File routeFile = new File(getRouteFileName());
        File pointNamesFile = new File(getPointNamesFileName());

        if (!routeFile.exists() || !pointNamesFile.exists())
            return false;

        Route route = loadRoute();
        return audiosAlreadyLoaded(route);
    }

    private Route loadRoute() {
        try {
            return RouteSerializer.deserializeFromFile(new FileInputStream(getRouteFileName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean audiosAlreadyLoaded(Route route) {
        for(String filename : route.getAudioFileNames())
        {
            File file = new File(TextUtils.join(FOLDER_SEPARATOR, new String[]{ getAudioLocalDir(), filename + MP3_EXTENSION}));
            if (!file.exists())
                return false;
        }

        return true;
    }

    public String getExcursionLocalDir()
    {
        return TextUtils.join(FOLDER_SEPARATOR, new String[]{context.getFilesDir().getAbsolutePath(),
                LOCAL_EXCURSIONS_DIR, excursionBrief.getKey() });
    }

    public String getAudioLocalDir()
    {
        return TextUtils.join(FOLDER_SEPARATOR, new String[] {context.getFilesDir().getAbsolutePath(),
                LOCAL_AUDIO_FOLDER_NAME, excursionBrief.getKey(), language});
    }

    public String getRouteFileName() {
        String routeName = getExcursionLocalDir();
        return TextUtils.join(FOLDER_SEPARATOR, new String[]{ routeName, excursionBrief.getKey() + XML_EXTENSION });
    }

    public String getPointNamesFileName() {
        String routeName = getExcursionLocalDir();
        return TextUtils.join(FOLDER_SEPARATOR, new String[]{ routeName, excursionBrief.getKey() + POINT_NAMES_SUFFIX + XML_EXTENSION });
    }

}
