package com.hongmingwei.rxjava;

/**
 * Created by Hongmingwei on 2017/6/18.
 * Email: 648600445@qq.com
 */

public interface Function<Result, Param> {

    Result function(Param... params);
}
