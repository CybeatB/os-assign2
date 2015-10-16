// Simulator.java
// Operating Systems Assignment 2
// a1646288 - Jonathan Blieschke
// a1645899 - Joshua Schwarz

import java.util.*;
import java.io.*;

public class Simulator {

    // Main
    public static void main(String params[]) {
        // get input file
        if (params.length < 1) {
            System.out.println("No File Specified");
            return;
        }

        // open file
        File infile = new File(params[0]);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(infile));
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }

        // read file
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String parts[] = line.split(" ", 2);
                int frames = Integer.valueOf(parts[0]);
                String actions[] = parts[1].replaceAll("\\}|\\{", "").split(",");

                //System.out.println(line);
                ArrayList<Frame> buffer = new ArrayList<Frame>(frames);
                int faults = 0;
                int index = 0;

                // SCR
                System.out.println("Second-Chance replacement (" + frames + " frames):");
                System.out.println("\tReference Page\tFrames\tReplaced Page");
                int scrtime = 0;
                for (int i = 0; i < actions.length; i++) {
                    String act = actions[i];
                    Frame newframe = null;
                    Frame victim = null;

                    // process new action
                    if (act.contains("m")) {
                        newframe = new Frame(Integer.valueOf(act.replace("m", "")));
                        newframe.setMod(true);
                    } else {
                        newframe = new Frame(Integer.valueOf(act));
                    }
                    newframe.setRef(true);

                    // search buffer
                    for (Frame f : buffer) {
                        if (f.id() == newframe.id()) {
                            buffer.set(buffer.indexOf(f), newframe);
                            break;
                        }
                    }

                    // if not found, add to buffer
                    if (!buffer.contains(newframe)) {
                        scrtime += 1;
                        if (buffer.size() < frames) {
                            buffer.add(newframe);
                        } else {
                            while (!buffer.contains(newframe)) {
                                Frame f = buffer.get(index);
                                if (f.getRef()) {
                                    f.setRef(false);
                                } else {
                                    victim = f;
                                    buffer.set(index, newframe);
                                }
                                index = (index + 1) % frames;
                            }
                        }
                    }

                    // update faults
                    faults += (victim == null ? 0 : 1);
                    scrtime += (victim == null ? 0 : 10);

                    // print action
                    String framelist = "";
                    for (int j = 0; j < frames; j++) {
                        if (buffer.size() > j) {
                            framelist += buffer.get(j);
                        }
                        framelist += ",";
                    }
                    framelist = framelist.replaceFirst(",$", "");
                    System.out.println("\t\t" + newframe + "\t" + framelist + "\t\t" + (victim == null ? "" : victim));
                }
                System.out.println("Total Number of Page Faults = " + faults);
                System.out.println("Total I/O Time Units = " + scrtime);

                // Cleanup
                System.out.println("");
                buffer = new ArrayList<Frame>(frames);
                faults = 0;
                index = 0;

                // SCE1
                System.out.println("Second-Chance replacement extension 1 (" + frames + " frames):");
                System.out.println("\tReference Page\tFrames\tReplaced Page");
                int scetime = 0;
                for (int i = 0; i < actions.length; i++) {
                    String act = actions[i];
                    Frame newframe = null;
                    Frame victim = null;

                    // process new action
                    if (act.contains("m")) {
                        newframe = new Frame(Integer.valueOf(act.replace("m", "")));
                        newframe.setMod(true);
                    } else {
                        newframe = new Frame(Integer.valueOf(act));
                    }
                    newframe.setRef(true);

                    // search buffer
                    for (Frame f : buffer) {
                        if (f.id() == newframe.id()) {
                            if (f.getMod()) {
                                newframe.setMod(true);
                            }
                            buffer.set(buffer.indexOf(f), newframe);
                            break;
                        }
                    }

                    // if not found, add to buffer
                    if (!buffer.contains(newframe)) {
                        scetime += 1;
                        if (buffer.size() < frames) {
                            buffer.add(newframe);
                        } else {
                            Boolean modpass = false;
                            int init = index;
                            int count = 0;
                            while (!buffer.contains(newframe)) {

                                Frame f = buffer.get(index);
                                if (!f.getRef() && !f.getMod()) {
                                    victim = f;
                                    buffer.set(index, newframe);
                                    scetime += (victim.getMod() ? 10 : 0);
                                    faults += 1;
                                } else if (!f.getRef() && f.getMod() && count == 1) {
                                    victim = f;
                                    buffer.set(index, newframe);
                                    scetime += (victim.getMod() ? 10 : 0);
                                    faults += 1;
                                } else if (count == 1) {
                                    f.setRef(false);
                                }

                                index = (index + 1) % frames;
                                if (index == init) {
                                    if (count == 1) {
                                        count = 0;
                                    } else {
                                        count = 1;
                                    }
                                }
                            }
                        }
                    }

                    // print action
                    String framelist = "";
                    for (int j = 0; j < frames; j++) {
                        if (j < buffer.size()) {
                            framelist += buffer.get(j);
                        }
                        framelist += ",";
                    }
                    framelist = framelist.replaceFirst(",$", "");
                    System.out.println("\t\t" + newframe + "\t" + framelist + "\t\t" + (victim == null ? "" : victim));
                }
                System.out.println("Total Number of Page Faults = " + faults);
                System.out.println("Total I/O Time Units = " + scetime);
                System.out.format("SC/SCE1 I/O Time Ratio = %.2f\n", ((float) scetime / (float) scrtime));

                // Cleanup
                System.out.println("");
                buffer = new ArrayList<Frame>(frames);
                faults = 0;
                index = 0;

                // SCE2
                System.out.println("Second-Chance replacement extension 2 (" + frames + " frames):");
                System.out.println("\tReference Page\tFrames\tReplaced Page");
                scetime = 0;
                for (int i = 0; i < actions.length; i++) {
                    String act = actions[i];
                    Frame newframe = null;
                    Frame victim = null;

                    // process new action
                    if (act.contains("m")) {
                        newframe = new Frame(Integer.valueOf(act.replace("m", "")));
                        newframe.setMod(true);
                    } else {
                        newframe = new Frame(Integer.valueOf(act));
                    }
                    newframe.setRef(true);

                    // search buffer
                    for (Frame f : buffer) {
                        if (f.id() == newframe.id()) {
                            if (f.getMod()) {
                                newframe.setMod(true);
                            }
                            buffer.set(buffer.indexOf(f), newframe);
                            break;
                        }
                    }

                    // if not found, add to buffer
                    if (!buffer.contains(newframe)) {
                        scetime += 1;
                        if (buffer.size() < frames) {
                            buffer.add(newframe);
                        } else {
                            boolean refpass = false;
                            boolean modpass = false;
                            int init = index;
                            int count = 0;
                            while (!buffer.contains(newframe)) {

                                Frame f = buffer.get(index);
                                if (!f.getRef() && !f.getMod()) {
                                    victim = f;
                                    buffer.set(index, newframe);
                                    scetime += (victim.getMod() ? 10 : 0);
                                    faults += 1;
                                } else if (f.getRef() && !f.getMod() && count == 1) {
                                    victim = f;
                                    buffer.set(index, newframe);
                                    scetime += (victim.getMod() ? 10 : 0);
                                    faults += 1;
                                } else if (!f.getRef() && f.getMod() && count == 2) {
                                    victim = f;
                                    buffer.set(index, newframe);
                                    scetime += (victim.getMod() ? 10 : 0);
                                    faults += 1;
                                } else if (count >= 1) {
                                    f.setRef(false);
                                }

                                index = (index + 1) % frames;
                                if (index == init) {
                                    if (count == 1) {
                                        count = 2;
                                    } else if (count == 2) {
                                        count = 0;
                                    } else {
                                        count = 1;
                                    }
                                }
                            }
                        }
                    }

                    // print action
                    String framelist = "";
                    for (int j = 0; j < frames; j++) {
                        if (j < buffer.size()) {
                            framelist += buffer.get(j);
                        }
                        framelist += ",";
                    }
                    framelist = framelist.replaceFirst(",$", "");
                    System.out.println("\t\t" + newframe + "\t" + framelist + "\t\t" + (victim == null ? "" : victim));
                }
                System.out.println("Total Number Of Page Faults = " + faults);
                System.out.println("Total I/O Time Units = " + scetime);
                System.out.format("SC/SCE2 I/O Time Ratio = %.2f\n", ((float) scetime / (float) scrtime));
                System.out.println("");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    // Frames
    private static class Frame {

        public Frame(int id) {
            this.id = id;
            this.ref = false;
            this.mod = false;
        }

        public boolean equals(Frame f) {
            return (this.id == f.id());
        }

        public int compareTo(Frame f) {
            return (this.id - f.id());
        }

        public String toString() {
            return String.valueOf(this.id);
        }

        public int id() {
            return this.id;
        }

        public void setRef(boolean r) {
            this.ref = r;
        }

        public boolean getRef() {
            return ref;
        }

        public void setMod(boolean m) {
            this.mod = m;
        }

        public boolean getMod() {
            return this.mod;
        }
        private int id;
        private boolean ref;
        private boolean mod;
    }
}