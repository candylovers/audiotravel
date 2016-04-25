package com.home.croaton.followme.domain;

import java.util.ArrayList;

public class Excursion {
    private ArrayList<AudioTrack> tracks;
    private ExcursionBrief excursionBrief;

    public Excursion(){
        this("", new ArrayList<AudioTrack>());
    }

    public Excursion(String name, ArrayList<AudioTrack> tracks){
        this.tracks = tracks;
    }

    public ArrayList<AudioTrack> getTracks(){
        return tracks;
    }
}
