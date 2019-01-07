package com.hongmingwei.rxjava.util;

/**
 * Created by Hongmingwei on 2018/5/21.
 * Email: 648600445@qq.com
 */

public class TcpBase {

    public static byte[] byteReversed(byte[] res) {
        byte[] b = new byte[res.length];
        for (int i = 0; i < res.length; i++) {
            b[res.length-1-i] = res[i];
        }
        return b;
    }

    public static byte[] intToBytes2(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[4-1-i] = (byte) (n >> (24 - i * 8));
        }
        return b;
    }
}
