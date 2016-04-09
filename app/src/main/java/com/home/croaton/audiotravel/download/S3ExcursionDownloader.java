package com.home.croaton.audiotravel.download;

import android.content.Context;
import android.os.Environment;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.home.croaton.audiotravel.domain.Excursion;
import com.home.croaton.audiotravel.domain.ExcursionBrief;

import java.io.File;
import java.io.InputStream;

public class S3ExcursionDownloader implements IExcursionDownloader {
    private static final String COGNITO_POOL_ID = "us-east-1:ddabcbf7-9b32-47a4-a958-d9475c989850";
    private static final String BUCKET_NAME = "followme";

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
    public Excursion downloadExcursion(ExcursionBrief brief) {
        AmazonS3Client s3Client = getS3Client(context);
        S3Object object = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, "abrahamsberg.xml"));
        InputStream objectData = object.getObjectContent();
        return new Excursion();
    }
}
