package com.home.croaton.audiotravel.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.home.croaton.audiotravel.R;

import org.osmdroid.DefaultResourceProxyImpl;

public class CustomResourceProxy extends DefaultResourceProxyImpl {

    private final Context mContext;
    public CustomResourceProxy(Context pContext) {
        super(pContext);
        mContext = pContext;
    }

    @Override
    public Bitmap getBitmap(final bitmap pResId) {
        switch (pResId){
            case marker_default:
                return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.play);
        }
        return super.getBitmap(pResId);
    }

    @Override
    public Drawable getDrawable(final bitmap pResId) {
        switch (pResId){
            case marker_default:
                return mContext.getResources().getDrawable(R.drawable.play);
        }
        return super.getDrawable(pResId);
    }
}