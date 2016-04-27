package com.home.croaton.followme.download;

import android.content.Context;
import android.text.TextUtils;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.home.croaton.followme.domain.Excursion;
import com.home.croaton.followme.domain.ExcursionBrief;
import com.home.croaton.followme.domain.RouteSerializer;
import com.home.croaton.followme.instrumentation.ZipUnZip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class S3ExcursionDownloader implements IExcursionDownloader {
    private static final String COGNITO_POOL_ID = "us-east-1:ddabcbf7-9b32-47a4-a958-d9475c989850";
    private static final String BUCKET_NAME = "followme";
    private static final String EXCURSION_FOLDER_NAME = "excursions";
    private static final String AUDIO_FOLDER_NAME = "audio";
    private static final CharSequence FOLDER_SEPARATOR = "/";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String MP3_EXTENSION = ".mp3";
    private static final String XML_EXTENSION = ".xml";

    private static CognitoCachingCredentialsProvider sCredProvider;
    private static AmazonS3Client sS3Client;

    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    COGNITO_POOL_ID,
                    Regions.US_EAST_1);

        }
        return sCredProvider;
    }

    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            System.setProperty(SDKGlobalConfiguration.ENFORCE_S3_SIGV4_SYSTEM_PROPERTY, "true");
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
            sS3Client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
        }
        return sS3Client;
    }

    private final Context context;

    public S3ExcursionDownloader(Context context){
        this.context = context;
    }

    @Override
    public Excursion downloadExcursion(ExcursionBrief brief, String language) {

        Excursion excursion = new Excursion(brief);

        String excursionKey = brief.getKey().toLowerCase();
        downloadAndSavePackage(getExcursionPackageDir(excursionKey));

        loadRoute(excursion);
        if (audiosAlreadyLoaded(excursion, language))
            return excursion;

        downloadAndSavePackage(getAudioPackageDir(excursionKey, language));

        return new Excursion();
    }

    private boolean audiosAlreadyLoaded(Excursion excursion, String language) {
        String audioFolder = getAudioPackageDir(excursion.getKey(), language).replaceAll(ZIP_EXTENSION, "");

        for(String filename : excursion.getAudioFileNames())
        {
            File file = new File(TextUtils.join(FOLDER_SEPARATOR, new String[]{context.getFilesDir().getAbsolutePath(), audioFolder, filename + MP3_EXTENSION}));
            if (!file.exists())
                return false;
        }

        return true;
    }

    private void loadRoute(Excursion excursion) {
        String routeFileName = getRouteFileName(excursion);

        try {
            excursion.setRoute(RouteSerializer.deserializeFromFile(new FileInputStream(routeFileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void downloadAndSavePackage(String packageDir) {
        InputStream packageData = downloadPackage(packageDir);

        String[] pathSegments = packageDir.replaceAll(ZIP_EXTENSION, "").split(FOLDER_SEPARATOR.toString());

        File localPackageDir = context.getFilesDir();
        for(int i = 0; i < pathSegments.length; i++)
        {
            localPackageDir = new File(localPackageDir, pathSegments[i]);
            if (!localPackageDir.exists())
                localPackageDir.mkdir();
        }

        ZipUnZip.unzip(packageData, localPackageDir.getAbsolutePath());
    }

    private InputStream downloadPackage(String packageDir)
    {
        AmazonS3Client s3Client = getS3Client(context);
        S3Object object = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, packageDir));
        return object.getObjectContent();
    }

    private String getRouteFileName(Excursion excursion) {
        String routeName = getExcursionPackageDir(excursion.getKey()).replaceAll(ZIP_EXTENSION, "");
        return TextUtils.join(FOLDER_SEPARATOR, new String[]{context.getFilesDir().getAbsolutePath(), routeName, excursion.getKey() + XML_EXTENSION});
    }

    private String getExcursionPackageDir(String excursionKey)
    {
        return TextUtils.join(FOLDER_SEPARATOR, new String[] { EXCURSION_FOLDER_NAME, excursionKey + ZIP_EXTENSION });
    }

    private String getAudioPackageDir(String excursionKey, String language)
    {
        return TextUtils.join(FOLDER_SEPARATOR, new String[] { AUDIO_FOLDER_NAME, excursionKey, language + ZIP_EXTENSION});
    }
}
