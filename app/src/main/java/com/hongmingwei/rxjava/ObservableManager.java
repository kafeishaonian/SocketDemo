package com.hongmingwei.rxjava;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hongmingwei on 2017/6/18.
 * Email: 648600445@qq.com
 */

public class ObservableManager<Param, Result> implements ObservableInterface<Function, Param, Result> {
    /**
     * TAG
     */
    private static final String TAG = ObservableManager.class.getSimpleName();
    /**
     * params
     */
    private HashMap<String, Set<Function>> mapping;
    private final Object lockObj = new Object();
    private static ObservableManager mInstance;

    public ObservableManager(){
        mapping = new HashMap<>();
    }

    public static ObservableManager getInstance(){
        if (mInstance == null){
            mInstance = new ObservableManager();
        }
        return mInstance;
    }

    @Override
    public void registerObserver(String name, Function observer) {
        synchronized (lockObj){
            Set<Function> observers;
            if (!mapping.containsKey(name)){
                observers = new HashSet<>();
                mapping.put(name, observers);
            } else {
                observers = mapping.get(name);
            }
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(String name) {
        synchronized (lockObj){
            mapping.remove(name);
        }
    }

    @Override
    public void removeObserver(Function observer) {
        synchronized (lockObj){
            for (String key : mapping.keySet()){
                Set<Function> observers = mapping.get(key);
                observers.remove(observer);
            }
        }
    }

    @Override
    public void removeObserver(String name, Function observer) {
        synchronized (lockObj){
            if (mapping.containsKey(name)){
                Set<Function> observers = mapping.get(name);
                observers.remove(observer);
            }
        }
    }

    @Override
    public Set<Function> getObserver(String name) {
        Set<Function> observers = null;
        synchronized (lockObj){
            if (mapping.containsKey(name)){
                observers = mapping.get(name);
            }
        }
        return observers;
    }

    @Override
    public void clear() {
        synchronized (lockObj){
            mapping.clear();
        }
    }

    @Override
    public Result notify(String name, Param... p) {
        synchronized (lockObj){
            if (mapping.containsKey(name)){
                Set<Function> observers = mapping.get(name);
                for (Function o : observers){
                    return (Result) o.function(p);
                }
            }
        }
        return null;
    }
}
