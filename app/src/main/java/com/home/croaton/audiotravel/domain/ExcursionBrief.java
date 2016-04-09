package com.home.croaton.audiotravel.domain;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="excursionBrief")
public class ExcursionBrief implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ExcursionBrief createFromParcel(Parcel in) {
            return new ExcursionBrief(in);
        }
        public ExcursionBrief[] newArray(int size) {
            return new ExcursionBrief[size];
        }
    };

    @Attribute(name="id")
    private String id;

    @Attribute(name="name")
    private String name;

    @Attribute(name="thumbnailFilePath")
    private String thumbnailFilePath;

    public ExcursionBrief(){
        this("", "", "");
    }

    public ExcursionBrief(String id, String name, String thumbnailFilePath) {
        this.id = id;
        this.name = name;
        this.thumbnailFilePath = thumbnailFilePath;
    }

    private ExcursionBrief(Parcel in) {
        id = in.readString();
        name = in.readString();
        thumbnailFilePath = in.readString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getThumbnailFilePath() {
        return thumbnailFilePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(thumbnailFilePath);
    }
}
