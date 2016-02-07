package com.home.croaton.audiotravel.audio;

public enum AudioServiceCommand
{
    Play(       1<<0),
    Pause(      1<<1),
    Stop(       1<<2),
    LoadTracks( 1<<3),
    ReverseState(  1<<4);


    private final int value;
    private AudioServiceCommand(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
