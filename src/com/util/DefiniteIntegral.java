package com.util;
/*
需求：编译一个自动计算定积分的函数
方法：根据定义，求曲线面积，分成n个区间，即n个矩形，由于每个区间差都是一样的，
    可作为一个矩形的宽，矩形的长为每个区间的中点对应的函数，长和宽的乘积就是
    其中一个小矩形的面积，将n个小矩形的面积相加就是，该被积函数的积分。
步骤：1：定义被积函数，可以修改,需要计算什么函数的积分，可以自己设置
    2：定义第i个区间的中点值方法，即定义积分变量
    3：定义每个小区间的间隔差方法，即将范围分成n个等区间
*/
import java.util.*;

public class DefiniteIntegral {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);


        // 提示用户输入积分上下限
        //System.out.println("请输入积分上下限：");

        // 定义积分上下限a,b,有控制台输入
        //double a = input.nextDouble();
        //double b = input.nextDouble();
        double a = 0;
        double b = 1;

        double sum = 0;
        // 求出区间差，分成10000个区间，区间越小，误差越小
        double e = cha(a, b, 10000.0);

        // 求和，循环从第一个区间叠加到第10000个
        for (int j = 1; j <= 10000; j++) {
            double x = zhongjian(a, b, 10000.0, j);
            sum = sum + f(x);
        }
        System.out.print("定积分结果为：");
        System.out.println(sum * e);

    }

    // 定义被积函数，可以修改
    public static double f(double x) {
        //return 2*x*x+x;
        return x * (1 - x);
    }

    // 定义第i个区间的中点值，即定义积分变量
    public static double zhongjian(double a, double b, double n, int i) {
        return a + i * (b - a) / n;
    }

    // 定义每个小区间的间隔差，即将范围分成n个等区间
    public static double cha(double a, double b, double n) {
        return (b - a) / n;
    }
}
