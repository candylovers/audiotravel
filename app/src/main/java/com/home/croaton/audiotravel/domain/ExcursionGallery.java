package com.home.croaton.audiotravel.domain;

import java.util.ArrayList;
import java.util.UUID;

public class ExcursionGallery {
    public ArrayList<ExcursionBrief> getAvailableExcursions(){
        ArrayList<ExcursionBrief> excursions = new ArrayList<ExcursionBrief>();
        excursions.add(new ExcursionBrief(UUID.randomUUID(), "Gamlastan", "gamlastan"));
        excursions.add(new ExcursionBrief(UUID.randomUUID(), "Abrahamsberg", "abrahamsberg"));
        excursions.add(new ExcursionBrief(UUID.randomUUID(), "Gamlastan", "gamlastan"));

        return excursions;
    }
}
