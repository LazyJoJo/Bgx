package com.stock.fund.test;

import java.util.ArrayList;
import java.util.List;

public class StringTest {
    public static void main(String[] args) {
        // 测试字符串分割
        String input = "a,,a";
        String[] ary = split(input,",,");
        for (String string : ary) {
            System.out.println(string);
        }
        System.out.println(input);
        

        // 转成32位字符串
        String ip = "212.225.121.2";
        String[] ips = split(ip,".");
        StringBuffer sb = new StringBuffer();
        for (String num : ips) {
            int i = Integer.valueOf(num);
            String a = changeTo(i);
            sb.append(a);
        }
        System.out.println(sb.toString());

        

    }

    public static String[] split(String input, String pre){
        int l = pre.length();
        List<String> list = new ArrayList<>();
        while(input.indexOf(pre)!=-1){
            int end = input.indexOf(pre);
            list.add(input.substring(0, end));
            input = input.substring(end+l, input.length());
        }
        list.add(input);
        return list.toArray(new String[list.size()]);
    }
    public static String changeTo(int input){
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i<8;i++){
            int nowNum = input%2;
            input = input/2;
            sb.append(String.valueOf(nowNum));
        }
        return sb.reverse().toString();
    }
}
