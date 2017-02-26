package com.likhil.rec;

/**
 * Created by likhilm on 05/12/16.
 */
class FunData {
    int line;
    String name;
    String nameRaw;

    public FunData(int no, String substring, String nat) {
        line = no;
        name = substring;
        nameRaw = nat;

    }

    public void print() {
        System.out.println("Function : " + name + " Line : " + line + "\n Raw : " + nameRaw);

         /**/
    }
}