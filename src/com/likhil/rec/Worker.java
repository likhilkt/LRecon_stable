package com.likhil.rec;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by likhilm on 30/12/16.
 */
public class Worker {
    static int OSTART = 0, OEND = 1, MIDCHAR = 2, NSTART = 3, NEND = 4;
    //static String ptrn = "(static\\s+)*([\\w\\d_])+\\s+([\\w\\d_])+\\s*\\(.*\\)\\s*\\{";
    //static String ptrn = "static?([a-zA-Z_]+\\s+)*[a-zA-Z_]+\\s*\\(+((const)*\\s*[a-zA-Z_]+[0-9a-zA-Z_]*\\**\\s+\\**[a-zA-Z_]+[0-9a-zA-Z_]*\\**\\s*,*\\s*)*\\)\\s*\\{";
    //static String ptrn ="(?!\\b(if|while|for)\\b)\\b\\w+(?=\\s*\\()";

    /* Patter to match functions
       Accuracy is less.
     */
    static String ptrn = "((static)?\\s*" +
            "[\\w]+\\s*" +
            "\\*?\\s*[\\w]+\\s*" +
            "\\(\\s*" +
            "((\\w?\\s*\\**\\w+\\**\\s+\\**\\w+\\**[\\[\\]]*\\s*,*\\s*)*)?" +
            "(\\s*(void)\\s*)?" +
            "\\)" +
            "\\s*" +
            "\\{)";
    //static String ptrn ="(static{0,2}\\s+)*([a-zA-Z]+\\s+)*[a-zA-Z]+\\s*\\(+((const)*\\s*[a-zA-Z_]+[0-9a-zA-Z_]*\\**\\s+\\**[a-zA-Z_]+[0-9a-zA-Z_]*\\**\\s*\\,*\\s*)*\\)\\s*\\{";

    /*  Process the lower version file. Find all the functions
        in lower version and insert it to Array list. Object of
        FunData will be inserted in the Array.
     */
    static void processOldFile(String fileName) throws IOException {
        int no = 0, ct = 0, ln = 0, pr = -1;
        boolean start = false;
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer sb = new StringBuffer();
        String line;
        //Core Regex
        //String ptrn = "(static\\s+)*([\\w\\d_])+\\s+([\\w\\d_])+\\s*\\(.*\\)\\s*\\{";
        //String ptrn = "(static{0,2}\\s+)*([a-zA-Z_]+\\s+)*[a-zA-Z_]+\\s*\\(+((const)*\\s*[a-zA-Z_]+[0-9a-zA-Z_]*\\**\\s+\\**[a-zA-Z_]+[0-9a-zA-Z_]*\\**\\s*,*\\s*)*\\)\\s*\\{";

        Pattern p = Pattern.compile(ptrn, Pattern.MULTILINE);//. represents single character
        Matcher m;// = p.matcher("");
        Helper.dec();
        Helper.LOG.append("Functions in lower version");
        Helper.dec();

        /*  Pattern matching using entire file can take a lot of time as my pattern is not efficient.  the number of { and } will
            be same in a function / src code (Assumption) At the end of a function , diff b/w { and } will be zero, and at start,
            count of { will be one. So pattern matching done only for the chars between end of function and start of function
            Pattern matching needed because, there may be a lot of strings between end of one function and start of next function.

            beg - beginning of file.
            start - start of a function
         */
        boolean beg = true; // trial - error.
        while ((line = br.readLine()) != null) {

            Helper.oLineIndex.put(no, ln);

            ct += (Helper.count(line, "{") - (Helper.count(line, "}")));

            if (ct == 0 && pr > 0) {
                start = true;
            }
            if (start || beg)
                sb.append(line).append("\n");

            if(ct==1)
                beg = false;

            if (ct == 1 && pr == 0) {
                String ts = sb.toString();

                m = p.matcher(ts);
                if (m.find()) {
                    Helper.LOG.append("\n").append(no).append(" : ").append(ts.substring(m.start(), m.end()));
                    Helper.oFunData.add(new FunData(no, ts.substring(m.start(), m.end()), ts.substring(m.start(), m.end()).replaceAll("\\s+", "")));
                }
                sb = new StringBuffer();
                start = false;
            }
            pr = ct;

            Helper.oldFile.append(line).append("\n");
            //Main.oldFile.add(line);
            ln += line.length() + 1;
            //Helper.dec();
            no++;
        }
        Helper.oLineIndex.put(no, ln);
        //Helper.p(ln+"");
        br.close();
        fr.close();
        String result = sb.toString();
        sb = null;

    }

    /*  run unix command 'diff' to get difference between lower version and higher version.
        create object of DiffOutData using diff output and insert this object into Array List.
     */
    static void processDiff(String file1, String file2) throws IOException {
        String dargs = "diff -Biw " + file1 + " " + file2;
        //Call the Legend diff <3
        Process proc = Runtime.getRuntime().exec(dargs);
        BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String i;
        while ((i = is.readLine()) != null) {
            StringBuffer locBuf = new StringBuffer();
            if (!(i.startsWith(">") || i.startsWith("<") || i.startsWith("-") || i.startsWith("\\"))) {
                DiffOutData temp = new DiffOutData();
                temp.lineDesc = populateLineDesc(i);

                FunData tfun = getFunction(temp.lineDesc[OEND]);
                if (tfun != null) {
                    temp.setFunData(tfun);
                    //Helper.p(tfun.name+"\n"+tfun.nameRaw+"\n"+temp.lineDesc[OEND]);
                    Helper.LOG.append("\n\nFun Line : ").append(temp.funData.name).append(" ").append(temp.funData.line);
                } else {
                    Helper.LOG.append("\n===========================\nSkipped below changes..\n===========================\n");
                    continue;
                }
                if (temp.lineDesc[MIDCHAR] == 'a') {
                    int j = 0;
                    temp.ns += "\n";
                    do {
                        //temp.newLines.add(is.readLine().substring(1));
                        temp.ns += is.readLine().substring(1) + "\n";
                        locBuf.append("\n").append(temp.ns);
                        j++;
                    } while (j <= temp.lineDesc[NEND] - temp.lineDesc[NSTART] && temp.lineDesc[NSTART] != -1);
                    temp.added = j;
                    temp.adjLinesAbove = Helper.getBackmySafe(tfun.line,temp.lineDesc[OEND], Helper.CHAR);
                    //Helper.p("In Add : " + temp.adjLinesAbove.replaceAll("\\s+", ""));
                } else if (temp.lineDesc[MIDCHAR] == 'c') {
                    int k = 0;

                    do {
                        //temp.oldLines.add(is.readLine().substring(1));
                        temp.os += is.readLine().substring(1) + "\n";
                        locBuf.append("\n").append(temp.os);
                        k++;
                    } while (k <= temp.lineDesc[OEND] - temp.lineDesc[OSTART] && temp.lineDesc[OSTART] != -1);
                    temp.deleted = k;
                    while (!is.readLine().startsWith("--")) {
                    }
                    int l = 0;
                    temp.ns += "\n";
                    do {
                        //temp.newLines.add(is.readLine().substring(1));
                        temp.ns += is.readLine().substring(1) + "\n";
                        locBuf.append("\n").append(temp.ns);
                        l++;
                    } while (l <= temp.lineDesc[NEND] - temp.lineDesc[NSTART] && temp.lineDesc[NSTART] != -1);
                    temp.added = l;

                    int l_start = (temp.lineDesc[OSTART] != -1 ? temp.lineDesc[OSTART] : temp.lineDesc[OEND]) - 1;
                    /*to prevent reading lines above specific function*/
                    temp.adjLinesAbove = Helper.getBackmySafe(tfun.line,l_start, Helper.CHAR);
                    //temp.adjLinesAbove = Helper.getBackmy(l_start, Helper.CHAR);
                    //Helper.p("Mod : " + temp.adjLinesAbove.replaceAll("\\s+", ""));

                } else if (temp.lineDesc[MIDCHAR] == 'd') {
                    int m = 0;
                    do {
                        //temp.oldLines.add(is.readLine().substring(1));
                        temp.os += is.readLine().substring(1) + "\n";
                        locBuf.append("\n").append(temp.os);
                        m++;
                    } while (m <= temp.lineDesc[OEND] - temp.lineDesc[OSTART] && temp.lineDesc[OSTART] != -1);
                    temp.deleted = m;
                    int l_start = (temp.lineDesc[OSTART] != -1 ? temp.lineDesc[OSTART] : temp.lineDesc[OEND]) - 1;
                    /*to prevent reading lines above specific function*/
                    temp.adjLinesAbove = Helper.getBackmySafe(tfun.line,l_start, Helper.CHAR);
                    //temp.adjLinesAbove = Helper.getBackmy(l_start, Helper.CHAR);
                    //Helper.p("Del : " + temp.adjLinesAbove.replaceAll("\\s+", ""));
                }
                if (temp.lineDesc[OSTART] != -1 && tfun != null) {

                }
                if (tfun != null) {
                    //temp.adj = temp.getAdjLinesAbove()[0] + "\n" + temp.getAdjLinesAbove()[1];
                    temp.diffFromStart -= temp.funData.line;
                    Helper.list.add(temp);
                } else {
                    Helper.LOG.append("\n").append(locBuf.toString()).append("\n").append((char) temp.lineDesc[MIDCHAR]).append("\n").append(temp.oldLines).append("\n").append(temp.newLines);
                }
            }
        }
    }


    static void rabbitRec(String file) throws IOException {
        int mIndex = -1,
                size = Helper.list.size(), vno = 1;
        read(file);
        //Helper.p(Helper.newFile.length() + " ");
        while (size > 0) {
            size--;
            Helper.dec();
            Helper.LOG.append("Change No : ").append(vno++);
            Helper.dec();
            DiffOutData data = Helper.list.get(size);
            int nextFunstart = -1;
            int functionIndex[] = findFunLine(Helper.newFile, data.getFunData().name);
            //System.out.println(data.getFunData().name+" ");

            if (functionIndex != null && functionIndex[0] >= 0) {
                nextFunstart = functionIndex[1] + findNextfun(Helper.newFile.substring(functionIndex[1]));
                //Helper.p(nextFunstart + " next " + functionIndex[0] + " nnn " + functionIndex[1] + " " + Helper.newFile.substring(nextFunstart, nextFunstart + 50));
                Helper.LOG.append("1 : ").append(functionIndex[0]).append(" 2 : ").append(nextFunstart);
                Helper.LOG.append("Trying to add changes near function - ").append(data.getFunData().name);
                int locIndex = -1;
                //int len = -1;
                if (nextFunstart == functionIndex[1] - 1) {
                    nextFunstart = -1;
                } else {
                    nextFunstart--;
                }
                //System.out.println(functionIndex[0]+ " LIKHIL  " + nextFunstart);
                switch (data.lineDesc[MIDCHAR]) {
                    case 'a':
                        locIndex = findAbove(functionIndex[0], Helper.newFile, data.getAdjLinesAbove(), nextFunstart);
                        if (locIndex >= 0) {
                            locIndex += functionIndex[0] - 1;
                            if (findRemoChars(locIndex, Helper.newFile, data.ns, nextFunstart) == null) {
                                Helper.newFile.insert(locIndex, data.ns);
                                Helper.LOG.append("\nAdded below lines \nFrom : \n").append(data.ns);
                            } else {
                                Helper.LOG.append("\nbelow lines are already present in file\n").append(data.ns);
                            }
                        } else {
                            Helper.LOG.append("\nAddition of line '" + data.ns + "' SKIPPED!!!\nChange is below the following line, but same is not present in new file\n"+data.getAdjLinesAbove());
                        }
                        Helper.LOG.append("Near Line no : ").append(Helper.lineNo(locIndex));
                        //Case A
                        break;
                    case 'c':
                        locIndex = findAbove(functionIndex[0], Helper.newFile, data.getAdjLinesAbove(), nextFunstart);
                        if (locIndex >= 0) {
                            locIndex += functionIndex[0] - 1;
                            //find chars to delete.
                            int rm[] = findRemoChars(locIndex, Helper.newFile, data.os, nextFunstart);
                            if (rm != null) {
                                Helper.newFile.delete(rm[0], rm[1]);
                                Helper.newFile.insert(rm[0], data.ns);
                                Helper.LOG.append("\nChanged below lines \nFrom : \n").append(data.os).append("\nTo : \n").append(data.ns);
                                Helper.LOG.append("Near Line no : ").append(Helper.lineNo(locIndex)).append("\nBelow the following line \n").append(data.getAdjLinesAbove());
                                ;
                            } else {
                                Helper.LOG.append("\nChanging of below lines SKIPPED!!!\nFrom : ").append(data.os).append("\nTo : ").append(data.ns).append("\nReason : cant find line to be changed :- \n").append(data.os);
                                continue;
                            }

                        } else {
                            Helper.LOG.append("\nChanging of below lines SKIPPED!!!\nFrom : ").append(data.os).append("\nTo : ").append(data.ns)
                            .append("\nChange is below the following line, but same is not present in new file "+data.getAdjLinesAbove());
                            continue;
                        }
                        break;
                    case 'd':
                        locIndex = findAbove(functionIndex[0], Helper.newFile, data.getAdjLinesAbove(), nextFunstart);
                        if (locIndex >= 0) {
                            locIndex += functionIndex[0] - 1;
                            int rm[] = findRemoChars(locIndex, Helper.newFile, data.os, nextFunstart);
                            if (rm != null) {
                                Helper.newFile.delete(rm[0], rm[1]);
                                Helper.LOG.append("\nRemoved below lines \n").append(data.os);
                                Helper.LOG.append("Near Line no : ").append(Helper.lineNo(locIndex)).append("\nBelow the following line \n").append(data.getAdjLinesAbove());
                            } else {
                                Helper.LOG.append("\n11Deleting of below lines SKIPPED!!!\nFrom : ").append(data.os);
                            }
                        } else {
                            Helper.LOG.append("\n22Deleting of below lines SKIPPED!!!\nFrom : ").append(data.os);
                        }
                        break;
                }
            } else {
                Helper.LOG.append("\n33SKIPPED changes From :").append(data.os).append("\nTo :").append(data.ns+"ff "+functionIndex);
            }
        }

        reCreateNewFileFromLine(Helper.newFile, "output.txt");
        reCreateNewFileFromLine(Helper.LOG, "log.log");


    }


    static int[] populateLineDesc(String str) {
        int ret[] = {-1, -1, -1, -1, -1};
        int len = str.length();
        StringBuffer sb = new StringBuffer();
        char c;

        //Helper.p(str + " ");
        int mid = str.indexOf('c') + str.indexOf('d') + str.indexOf('a') + 2;
        ret[MIDCHAR] = str.charAt(mid);
        if (Helper.count(str.substring(0, mid), ",") == 0) {
            ret[OEND] = Integer.parseInt(str.substring(0, mid));
        } else {
            ret[OSTART] = Integer.parseInt(str.substring(0, str.indexOf(',')));
            ret[OEND] = Integer.parseInt(str.substring(str.indexOf(',') + 1, mid));
        }
        if (Helper.count(str.substring(mid + 1), ",") == 0) {
            ret[NEND] = Integer.parseInt(str.substring(mid + 1));
        } else {
            ret[NSTART] = Integer.parseInt(str.substring(mid + 1, str.indexOf(',', mid)));
            ret[NEND] = Integer.parseInt(str.substring(str.indexOf(',', mid) + 1));
        }
        return ret;
    }


    private static FunData getFunction(int line) {
        int size = Helper.oFunData.size() - 1;
        for (int i = size; i >= 0; i--) {
            if (Helper.oFunData.get(i).line < line)
                return Helper.oFunData.get(i);
        }
        return null;
    }

    private static int findNextfun(String s) {
        //String ptrn = "(static\\s+)*([\\w\\d_])+\\s+([\\w\\d_])+\\s*\\((const*\\s*[\\w\\d_]+\\s+[\\w\\d_]+,*)*\\)\\s*\\{";
        //String ptrn = "(static\\s+)*([\\w\\d_])+\\s+([\\w\\d_])+\\s*\\(.*\\)\\s*\\{";
        Pattern p = Pattern.compile(ptrn, Pattern.DOTALL);//. represents single character
        Matcher m = p.matcher(s);
        if (m.find())
            return m.start();
        return -1;

    }

    private static int[] findFunLine(StringBuffer newFileInput, String name) {
        //return patternMakerForAbove(name,newFileInput.toString());//+fi+2+adj.length();
        return patternMaker(name, newFileInput.toString());
    }

    private static int findAbove(int fi, StringBuffer newFileInput, String adj, int len) {
        //System.out.println(" adj : "+adj);
        fi=fi==0?1:fi;
        if (len == -1)
            return patternMakerForAbove(adj, newFileInput.substring(fi - 1));//+fi+2+adj.length();
        else
            return patternMakerForAbove(adj, newFileInput.substring(fi - 1, len));
        //return patternMaker(adj,newFileInput.substring(fi-1))+fi+2+adj.length();
    }

    private static int[] findRemoChars(int fi, StringBuffer sb, String lines, int len) {
        //Helper.p(sb.length() + "LLLL" + len + " ff : " + fi);
        if (len == -1)
            return patternMakerForRemo(lines, sb.substring(fi - 1), fi - 1);
        else
            return patternMakerForRemo(lines, sb.substring(fi - 1, len), fi - 1);
    }


    private static int[] patternMaker(String ip, String input) {
        ip = Helper.formRegex(ip);
        //System.out.println(ip);
        Pattern p = Pattern.compile(ip);//. represents single character
        Matcher m = p.matcher(input);

        if (m.find()) {
            //System.out.println(m.start());
            return new int[]{m.start(), m.end()};
        }
        return null;

    }

    private static int patternMakerForAbove(String ip, String input) {
        ip = Helper.formRegex(ip);
        //System.out.println(ip);
        Pattern p = Pattern.compile(ip);//. represents single character
        Matcher m = p.matcher(input);
        if (m.find()) {
            return m.end();
        }
        return -1;

    }

    private static int[] patternMakerForRemo(String ip, String input, int i) {
        ip = Helper.formRegex(ip);
        //System.out.println("rem" + ip);
        Pattern p = Pattern.compile(ip);//. represents single character
        Matcher m = p.matcher(input);

        if (m.find()) {
            //System.out.println(m.start());
            //System.out.println(input.substring(m.start(), m.end()));
            return new int[]{m.start() + i, m.end() + i};
        }
        return null;

    }

    private static void read(String fileName) throws IOException {
        int i = 0;
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            Helper.oIndexLine.add(i);
            Helper.newFile.append(line).append("\n");
            i += line.length() + 1;
        }
    }

    static int reCreateNewFileFromLine(StringBuffer lines, String name) throws IOException {
        File f = new File(name);
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        String arr[] = lines.toString().split("\n");
        int li = arr.length;
        for (int i = 0; i < li; i++) {
            bw.write(arr[i] + "\n");
            //bw.write(lines.toString());
        }
        bw.flush();

        return 0;
    }

}
