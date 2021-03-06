package com.home.croaton.followme.domain;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "excursionBrief")
public class ExcursionBrief implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ExcursionBrief createFromParcel(Parcel in) {
            return new ExcursionBrief(in);
        }

        public ExcursionBrief[] newArray(int size) {
            return new ExcursionBrief[size];
        }
    };

    @ElementList(name = "content", inline = true)
    private List<ExcursionBriefContent> contentByLanguage;

    @Attribute(name = "key")
    private String key;

    @Attribute(name = "thumbnailFilePath")
    private String thumbnailFilePath;

    @Element(name = "cost")
    private double cost;

    @Element(name = "length")
    private double length;

    @Element(name = "duration")
    private double duration;

    @ElementList(name = "area")
    private List<SerializableGeoPoint> area;

    public ExcursionBrief() {
        this("", "", 0.0, 0.0, 0.0, new ArrayList<ExcursionBriefContent>(), new ArrayList<SerializableGeoPoint>());
    }

    public ExcursionBrief(
            String key,
            String thumbnailFilePath,
            double cost,
            double length,
            double duration,
            List<ExcursionBriefContent> contentByLanguage,
            List<SerializableGeoPoint> area) {
        this.key = key;
        this.thumbnailFilePath = thumbnailFilePath;
        this.cost = cost;
        this.length = length;
        this.duration = duration;
        this.contentByLanguage = contentByLanguage;
        this.area = area;
    }

    private ExcursionBrief(Parcel in) {
        key = in.readString();
        thumbnailFilePath = in.readString();
        cost = in.readDouble();
        length = in.readDouble();
        duration = in.readDouble();

        contentByLanguage = new ArrayList<>();
        in.readList(contentByLanguage, ExcursionBriefContent.class.getClassLoader());

        area = new ArrayList<>();
        in.readList(area, GeoPoint.class.getClassLoader());
    }

    public String getKey() {
        return key;
    }

    public String getThumbnailFilePath() {
        return thumbnailFilePath;
    }

    public double getCost() {
        return cost;
    }

    public ExcursionBriefContent getContentByLanguage(String language) {
        for (ExcursionBriefContent content : contentByLanguage) {
            if (content.getLang().equals(language))
                return content;
        }

        return null;
    }

    public double getLength() {
        return length;
    }

    public double getDuration() {
        return duration;
    }

    public List<SerializableGeoPoint> getArea() {
        return area;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(key);
        out.writeString(thumbnailFilePath);
        out.writeDouble(cost);
        out.writeDouble(length);
        out.writeDouble(duration);
        out.writeList(contentByLanguage);
        out.writeList(area);
    }
}
