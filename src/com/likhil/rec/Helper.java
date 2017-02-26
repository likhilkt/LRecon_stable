package com.likhil.rec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by likhilm on 30/12/16.
 */
public class Helper {
    public static ArrayList<DiffOutData> list = new ArrayList<DiffOutData>();
    static int CHAR = 73;
    static HashMap<Integer, Integer> oLineIndex = new HashMap<Integer, Integer>();
    static ArrayList<Integer> oIndexLine = new ArrayList<Integer>();
    static ArrayList<FunData> oFunData = new ArrayList<FunData>();
    static StringBuffer oldFile = new StringBuffer();
    static StringBuffer newFile = new StringBuffer();
    static StringBuffer LOG = new StringBuffer();

    static int lineNo(int index) {
        return Math.abs(Collections.binarySearch(oIndexLine, index));
    }

    static void dec() {
        LOG.append("\n========================================================================================\n");
    }

    static void p(String s) {
        System.out.println(s);
    }

    static int count(String str, String c) {
        return str.length() - str.replace(c, "").length();
    }

    static void poli(int line) {
        p(line + " index is : " + oLineIndex.get(line));
    }

    static int getIndex(int line) {
        return oLineIndex.get(line);
    }

    static String getBackmy(int line, int cnt) {
        return oldFile.substring((getIndex(line) - cnt) < 0 ? 0 : getIndex(line) - cnt, getIndex(line));
    }

    static String getBackmySafe(int funLine, int line, int cnt) {
        int fIndex = getIndex(funLine);
        int start = (getIndex(line) - cnt) < 0 ? 0 : getIndex(line) - cnt;
        start = start < fIndex ? fIndex : start;
        return oldFile.substring(start, getIndex(line));
    }

    static String formRegex(String ip) {
        return ip.replaceAll("\\\\", "\\\\\\" + "\\")
                .replaceAll("\\*", "\\\\\\" + "*")
                .replaceAll("\\+", "\\\\\\" + "+")
                .replaceAll("\\.", "\\\\\\" + ".")
                .replaceAll("\\s+", "\\\\\\" + "s+")
                .replaceAll("\\{", "\\\\\\" + "{")
                .replaceAll("\\(", "\\\\\\" + "(")
                .replaceAll("\\)", "\\\\\\" + ")")
                .replaceAll("\\}", "\\\\\\" + "}")
                .replaceAll("-", "\\\\\\" + "-")
                .replaceAll("\\[", "\\\\\\" + "[")
                .replaceAll("\\]", "\\\\\\" + "]")
                .replaceAll("\\?", "\\\\\\" + "?")
                .replaceAll("\\|", "\\\\\\" + "|");
    }

}
