package com.home.croaton.followme.domain;

import java.util.ArrayList;

public class Excursion {
    private ArrayList<AudioTrack> tracks;
    private ExcursionBrief excursionBrief;
    private Route route;

    public Excursion(){
        this(null);
    }

    public Excursion(ExcursionBrief brief) {
        excursionBrief = brief;
    }

    public ArrayList<AudioTrack> getTracks(){
        return tracks;
    }

    public String getKey() {
        if (excursionBrief == null)
            throw new UnsupportedOperationException("Called get key on empty excursion");

        return excursionBrief.getKey();
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    // ToDo: move audio list on excursion level
    public String[] getAudioFileNames() {
        return route.getAudioFileNames();
    }
}
