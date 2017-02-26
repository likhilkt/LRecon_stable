package com.likhil.rec;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    static int OSTART = 0, OEND = 1, MIDCHAR = 2, NSTART = 3, NEND = 4;
    static String status="Init..";
    static boolean TRUE=true;

    public static void main(String[] args) throws IOException {
        // write your code here
        try {
            String fill_l = "", file_h = "", file_rec = "";
            //Read input
            Scanner sc = new Scanner(System.in);
            Helper.p("Enter Lower version file : ");
            fill_l = sc.nextLine();
            Helper.p("Enter Higher version file name : ");
            file_h = sc.nextLine();
            Helper.p("Enter File need to be modified : ");
            file_rec = sc.nextLine();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    StringBuffer sb = new StringBuffer();
                    String ps = "";
                    int i=1;
                    while (TRUE) {
                        if(i%500==0) {
                            System.out.print(".");
                            i = 1;
                        }
                        if(!ps.equals(status)){
                            Helper.p("\nSTATUS : " + status);
                            ps=status;
                        }
                        try {
                            Thread.sleep(1);
                            i++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            Thread t = new Thread(r);
            t.start();

            //Build lineno-index map of old file.
            long t1 = System.currentTimeMillis();
            status = "Processing old file started";
            Worker.processOldFile(fill_l);
            status = "Processing old file completed";
            //Helper.poli(234);
            //Helper.p(Helper.getBackmy(234,2));
            status = "Processing diff output started";
            Worker.processDiff(fill_l, file_h);
            status = "Processing diff output completed";

            status = "Starting Recon";
            Worker.rabbitRec(file_rec);
            status = "Recon completed. please check log.log";
            Helper.p(("\nTIME : " + (System.currentTimeMillis() - t1)));
            TRUE = false;
            Helper.p(status);

        }catch (Exception e){
            status = "Exception in main thread \n"+e.toString()+"\nRecon FAILED!!!";
            TRUE=false;
        }
    }
}
