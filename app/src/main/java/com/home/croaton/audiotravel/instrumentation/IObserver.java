package com.home.croaton.audiotravel.instrumentation;

public interface IObserver<T>
{
    void notify(T args);
}
