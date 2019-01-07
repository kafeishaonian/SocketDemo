package com.hongmingwei.rxjava;

import java.util.Set;

/**
 * Created by Hongmingwei on 2017/6/18.
 * Email: 648600445@qq.com
 */

public interface ObservableInterface<T, P, R> {
    /**
     * 根据名称注册观察者
     * @param name
     * @param observer
     */
    void registerObserver(String name, T observer);

    /**
     * 根据名称反注册观察者
     * @param name
     */
    void removeObserver(String name);

    /**
     * 根据观察者反注册
     * @param observer
     */
    void removeObserver(T observer);

    /**
     * 根据名字和观察者反注册
     * @param name
     * @param observer
     */
    void removeObserver(String name, T observer);

    /**
     * 根据名字获取观察者
     * @param name
     * @return
     */
    Set<T> getObserver(String name);

    /**
     * 清除观察者
     */
    void clear();

    /**
     * 通知观察者
     * @param name
     * @param p
     * @return
     */
    R notify(String name, P...  p);
}
