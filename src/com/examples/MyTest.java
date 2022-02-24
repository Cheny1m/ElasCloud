package com.examples;

import java.util.Map;
import java.util.Properties;

/**
 * @业务描述：
 * @package_name： com.ratel.mongo
 * @project_name： springboot-mongo
 * @author： ratelfu@qq.com
 * @create_time： 2020-08-30 10:11
 * @copyright (c) ratelfu 版权所有
 */
public class MyTest {
    //*program arguments 其实就是对应的args参数
    public static void main(String[] args) {
        //获取当前堆的大小 byte 单位
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println(heapSize/8/1024/1024);

        //获取堆的最大大小byte单位
        //超过将抛出 OutOfMemoryException
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        System.out.println(heapMaxSize/8/1024/1024);

        //获取当前空闲的内存容量byte单位
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println(heapFreeSize);
    }

}
