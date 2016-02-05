package com.home.croaton.audiotravel.instrumentation;

public interface IObservable<T>
{
    void subscribe(IObserver<T> observer);
    void unSubscribe(IObserver<T> observer);
}
