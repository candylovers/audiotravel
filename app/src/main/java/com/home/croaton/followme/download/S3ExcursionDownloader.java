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

import java.io.InputStream;

public class S3ExcursionDownloader implements IExcursionDownloader {
    private static final String COGNITO_POOL_ID = "us-east-1:ddabcbf7-9b32-47a4-a958-d9475c989850";
    private static final String BUCKET_NAME = "followme";
    private static final String EXCURSION_FOLDER_NAME = "excursions";
    private static final String AUDIO_FOLDER_NAME = "audio";
    private static final CharSequence FOLDER_SEPARATOR = "/";
    private static final String ZIP_EXTENSION = ".zip";

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
        String excursionKey = brief.getKey().toLowerCase();
        downloadAndExtractPackage(getExcursionPackageKey(excursionKey));
        downloadAndExtractPackage(getAudioPackageKey(excursionKey, language));

        return new Excursion();
    }

    private void downloadAndExtractPackage(String packageKey)
    {
        InputStream packageData = downloadPackage(packageKey);
        //unzip package
    }

    private InputStream downloadPackage(String packageKey)
    {
        AmazonS3Client s3Client = getS3Client(context);
        S3Object object = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, packageKey));
        return object.getObjectContent();
    }

    private String getExcursionPackageKey(String excursionKey)
    {
        return TextUtils.join(FOLDER_SEPARATOR, new String[] { EXCURSION_FOLDER_NAME, excursionKey + ZIP_EXTENSION });
    }

    private String getAudioPackageKey(String excursionKey, String language)
    {
        return TextUtils.join(FOLDER_SEPARATOR, new String[]{AUDIO_FOLDER_NAME, excursionKey, language + ZIP_EXTENSION});
    }
}
