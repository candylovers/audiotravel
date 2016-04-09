package com.home.croaton.audiotravel.download;

import com.home.croaton.audiotravel.domain.Excursion;
import com.home.croaton.audiotravel.domain.ExcursionBrief;

public interface IExcursionDownloader {
    Excursion downloadExcursion(ExcursionBrief brief);
}
