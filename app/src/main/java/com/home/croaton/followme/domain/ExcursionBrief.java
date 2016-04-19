package com.home.croaton.followme.domain;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

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

    @ElementList(name="content", inline = true)
    private List<ExcursionBriefContent> contentByLanguage;

    @Attribute(name="id")
    private String id;

    @Attribute(name="name")
    private String name;

    @Attribute(name="thumbnailFilePath")
    private String thumbnailFilePath;

    @Element(name="cost")
    private double cost;

    @Element(name="length")
    private double length;

    @Element(name="duration")
    private double duration;

    public ExcursionBrief(){
        this("", "", "", 0.0, 0.0, 0.0, new ArrayList<ExcursionBriefContent>());
    }

    public ExcursionBrief(String id, String name, String thumbnailFilePath, double cost,
                          double length, double duration, List<ExcursionBriefContent> contentByLanguage) {
        this.id = id;
        this.name = name;
        this.thumbnailFilePath = thumbnailFilePath;
        this.cost = cost;
        this.length = length;
        this.duration = duration;
        this.contentByLanguage = contentByLanguage;
    }

    private ExcursionBrief(Parcel in) {
        id = in.readString();
        name = in.readString();
        thumbnailFilePath = in.readString();
        cost = in.readDouble();
        length = in.readDouble();
        duration = in.readDouble();

        contentByLanguage = new ArrayList<>();
        in.readList(contentByLanguage, ExcursionBriefContent.class.getClassLoader());
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

    public double getCost() {
        return cost;
    }

    public ExcursionBriefContent getContentByLanguage(String language) {
        for(ExcursionBriefContent content : contentByLanguage)
        {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(thumbnailFilePath);
        out.writeDouble(cost);
        out.writeDouble(length);
        out.writeDouble(duration);
        out.writeList(contentByLanguage);
    }
}
