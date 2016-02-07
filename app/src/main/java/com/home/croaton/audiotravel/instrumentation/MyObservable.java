package com.home.croaton.audiotravel.instrumentation;

import java.util.ArrayList;

public class MyObservable<T> implements IObservable<T>
{
    private final ArrayList<IObserver<T>> _observers;

    // This hack is to notify observers from the class that creates this one.
    public MyObservable()
    {
        _observers = new ArrayList<>();
    }

    @Override
    public void subscribe(IObserver<T> observer)
    {
        if (observer == null)
            throw new IllegalArgumentException("The observer is null.");

        synchronized(_observers)
        {
            if (_observers.contains(observer))
                throw new IllegalStateException("Observer " + observer + " is already registered.");

            _observers.add(observer);
        }
    }

    @Override
    public void unSubscribe(IObserver<T> observer)
    {
        if (observer == null)
            throw new IllegalArgumentException("The observer is null.");

        synchronized(_observers)
        {
            int index = _observers.indexOf(observer);
            if (index == -1)
                throw new IllegalStateException("Observer " + observer + " was not registered.");

            _observers.remove(index);
        }
    }

    public void notifyObservers(T newValue)
    {
        for(IObserver<T> observer : _observers)
        {
            observer.notify(newValue);
        }
    }
}
