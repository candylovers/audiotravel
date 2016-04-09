package com.home.croaton.audiotravel.domain;

import java.util.ArrayList;

public class Excursion {
    private String name;
    private ArrayList<AudioTrack> tracks;

    public Excursion(){
        this("", new ArrayList<AudioTrack>());
    }

    public Excursion(String name, ArrayList<AudioTrack> tracks){
        this.name = name;
        this.tracks = tracks;
    }

    public String getName(){
        return name;
    }

    public ArrayList<AudioTrack> getTracks(){
        return tracks;
    }
}
