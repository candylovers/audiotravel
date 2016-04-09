package com.home.croaton.audiotravel.domain;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="excursionBrief")
public class ExcursionBrief {
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getThumbnailFilePath() {
        return thumbnailFilePath;
    }
}
