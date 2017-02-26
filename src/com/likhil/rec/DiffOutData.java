package com.likhil.rec;

import java.util.ArrayList;

/**
 * Created by likhilm on 05/12/16.
 */
public class DiffOutData {
    int lineDesc[] = {-1, -1, -1, -1, -1};
    ArrayList<String> oldLines = new ArrayList<String>();
    ArrayList<String> newLines = new ArrayList<String>();

    String ns = "", os = "";

    int added = -1, deleted = -1, changed = -1;
    String adjLinesAbove = "";
    String adjLinesBelow[] = new String[2];
    String adj = "";
    FunData funData;
    int diffFromStart = -1;

    public void populateDFS() {
        diffFromStart = (lineDesc[Main.OSTART] == -1 ? lineDesc[Main.OEND] : lineDesc[Main.OSTART]) - funData.line;
    }

    public int[] getLineDesc() {
        return lineDesc;
    }

    public void setLineDesc(int[] lineDesc) {
        this.lineDesc = lineDesc;
    }

    public ArrayList<String> getOldLines() {
        return oldLines;
    }

    public void setOldLines(ArrayList<String> oldLines) {
        this.oldLines = oldLines;
    }

    public ArrayList<String> getNewLines() {
        return newLines;
    }

    public void setNewLines(ArrayList<String> newLines) {
        this.newLines = newLines;
    }

    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getChanged() {
        return changed;
    }

    public void setChanged(int changed) {
        this.changed = changed;
    }

    public String getAdjLinesAbove() {
        return adjLinesAbove;
    }

    public void setAdjLinesAbove(String adjLinesAbove) {
        this.adjLinesAbove = adjLinesAbove;
    }

    public String[] getAdjLinesBelow() {
        return adjLinesBelow;
    }

    public void setAdjLinesBelow(String[] adjLinesBelow) {
        this.adjLinesBelow = adjLinesBelow;
    }

    public FunData getFunData() {
        return funData;
    }

    public void setFunData(FunData funData) {
        this.funData = funData;
    }

    public void print() {

        System.out.println(lineDesc[0] + "," + lineDesc[1] + "," + (char) lineDesc[2] + "," + lineDesc[3] + "," + lineDesc[4]);
        for (int i = 0; i < oldLines.size(); i++) {
            System.out.println(oldLines.get(i));
        }
        for (int i = 0; i < newLines.size(); i++) {
            System.out.println(newLines.get(i));
        }
        funData.print();
        System.out.println("Below");
        System.out.println(adjLinesAbove);

        System.out.println("Above");
        System.out.println(adjLinesBelow[0] + "\n" + adjLinesBelow[1]);


    }
}