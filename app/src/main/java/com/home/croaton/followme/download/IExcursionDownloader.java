package com.home.croaton.followme.download;

import com.home.croaton.followme.domain.Excursion;
import com.home.croaton.followme.domain.ExcursionBrief;

public interface IExcursionDownloader {
    Excursion downloadExcursion(ExcursionBrief brief, String language);
}
