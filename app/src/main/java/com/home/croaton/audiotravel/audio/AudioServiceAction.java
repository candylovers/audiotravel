package com.home.croaton.audiotravel.audio;

public enum AudioServiceAction
{
    Play(1),
    Pause(2),
    Stop(3);

    private final int value;
    private AudioServiceAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
