package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.bpos.java

import java.io.*;
import java.util.Vector;

public class bpos
{

    public bpos()
    {
    }

    public static void main(String args[])
    {
        if(args.length != 4)
        {
            System.out.println("Usage: java -classpath edu.upenn.cis.bpos.bpos.jar edu.upenn.cis.bpos.bpos <beam width> <test file> <weights> <lables>");
            System.out.println("For example: java -classpath edu.upenn.cis.bpos.bpos.jar edu.upenn.cis.bpos.bpos 1 data/sample.raw data/k3.fea data/postag.txt > sample.hypo");
            return;
        } else
        {
            SWordLib.init();
            SLabelLib.init();
            BLinTagLearn.KSOCKET = Integer.parseInt(args[0]);
            predict(args[1], args[2], args[3]);
            return;
        }
    }

    public static void reorder_train(String s, String s1)
    {
        SWordLib.init();
        SLabelLib.init();
        int i = Integer.parseInt(s1);
        loadTrainingData(s);
        int j = train.size() % i;
        if(j == 0)
            System.err.println((new StringBuilder()).append("Bad pair (").append(train.size()).append(",").append(i).append(")").toString());
        else
            System.err.println((new StringBuilder()).append("Good pair (").append(train.size()).append(",").append(i).append(")").toString());
        Vector vector = new Vector();
        vector.setSize(train.size());
        for(int k = 0; k < train.size(); k++)
        {
            int i1 = (k * i + i) % train.size();
            BLinTagSample blintagsample = (BLinTagSample)train.get(i1);
            blintagsample.displayConll();
            vector.setElementAt(Integer.valueOf(1), i1);
        }

        for(int l = 0; l < vector.size(); l++)
            if(vector.get(l) == null)
                System.err.println((new StringBuilder()).append("").append(l).append(" is missing").toString());

    }

    public static void learn(String s)
    {
        loadTrainingData(s);
        SLabelLib.initTargetWithLabel();
        SFeatLib sfeatlib = new SFeatLib();
        BLinTagLearn blintaglearn = new BLinTagLearn(proj, train, sfeatlib);
        blintaglearn.train();
        SLabelLib.saveLabels((new StringBuilder()).append(proj).append(".tag").toString());
    }

    public static void predict(String s, String s1, String s2)
    {
        SLabelLib.loadLabels(s2);
        SLabelLib.initTargetWithLabel();
        SFeatLib sfeatlib = new SFeatLib();
        sfeatlib.loadFeatTable(s1);
        Vector vector = new Vector();
        loadTestData(s, vector);
        BLinTagLearn blintaglearn = new BLinTagLearn(proj, vector, sfeatlib);
        blintaglearn.predict();
    }

    public static void evaluate(String s, String s1, String s2)
    {
        SLabelLib.loadLabels(s2);
        SLabelLib.initTargetWithLabel();
        SFeatLib sfeatlib = new SFeatLib();
        sfeatlib.loadFeatTable(s1);
        loadGoldStandard(s);
        BLinTagLearn blintaglearn = new BLinTagLearn(proj, gold, sfeatlib);
        blintaglearn.evaluate();
    }

    public static void learneval(String s, String s1)
    {
        loadTrainingData(s);
        SLabelLib.initTargetWithLabel();
        SFeatLib sfeatlib = new SFeatLib();
        loadGoldStandard(s1);
        BLinTagLearn blintaglearn = new BLinTagLearn(proj, train, sfeatlib);
        blintaglearn.traineval(gold);
        BLinTagLearn _tmp = blintaglearn;
        sfeatlib.saveWeight((new StringBuilder()).append(proj).append(".fea").toString(), BLinTagLearn.inner);
        SLabelLib.saveLabels((new StringBuilder()).append(proj).append(".tag").toString());
    }

    public static void loadTrainingData(String s)
    {
        loadCoNLLTaggedData(s, train);
    }

    public static void loadGoldStandard(String s)
    {
        loadCoNLLTaggedData(s, gold);
    }

    public static void loadTestData(String s, Vector vector)
    {
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(s));
            System.err.println((new StringBuilder()).append("Open Test File : ").append(s).toString());
            for(String s1 = bufferedreader.readLine(); s1 != null; s1 = bufferedreader.readLine())
            {
                s1 = s1.trim();
                String as[] = s1.split(" ");
                Vector vector1 = new Vector();
                vector1.setSize(as.length);
                for(int i = 0; i < as.length; i++)
                    vector1.setElementAt(as[i], i);

                BLinTagSample blintagsample = new BLinTagSample(vector1);
                vector.add(blintagsample);
            }

            bufferedreader.close();
        }
        catch(FileNotFoundException filenotfoundexception)
        {
            System.err.println(filenotfoundexception.toString());
        }
        catch(IOException ioexception)
        {
            System.err.println(ioexception.toString());
        }
        System.err.println((new StringBuilder()).append("").append(vector.size()).append(" samples loaded").toString());
        System.err.println((new StringBuilder()).append("").append(SWordLib.getSize()).append(" words loaded").toString());
        System.err.println((new StringBuilder()).append("").append(SLabelLib.getSize()).append(" labels loaded").toString());
    }

    public static void loadCoNLLTaggedData(String s, Vector vector)
    {
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(s));
            System.err.println((new StringBuilder()).append("Open Tagged File : ").append(s).toString());
            String s1 = bufferedreader.readLine();
            Vector vector1 = new Vector();
            Vector vector2 = new Vector();
            for(; s1 != null; s1 = bufferedreader.readLine())
            {
                s1 = s1.trim();
                if(s1.equals(""))
                {
                    BLinTagSample blintagsample = new BLinTagSample(vector1, vector2);
                    vector.add(blintagsample);
                    vector1.clear();
                    vector2.clear();
                } else
                {
                    String as[] = s1.split("\\s+");
                    vector1.add(as[0]);
                    vector2.add(as[1]);
                }
            }

            if(vector1.size() > 0)
            {
                BLinTagSample blintagsample1 = new BLinTagSample(vector1, vector2);
                vector.add(blintagsample1);
                vector1.clear();
                vector2.clear();
            }
            bufferedreader.close();
        }
        catch(FileNotFoundException filenotfoundexception)
        {
            System.err.println(filenotfoundexception.toString());
        }
        catch(IOException ioexception)
        {
            System.err.println(ioexception.toString());
        }
        System.err.println((new StringBuilder()).append("").append(vector.size()).append(" samples loaded").toString());
        System.err.println((new StringBuilder()).append("").append(SWordLib.getSize()).append(" words loaded").toString());
        System.err.println((new StringBuilder()).append("").append(SLabelLib.getSize()).append(" labels loaded").toString());
    }

    public static Vector train = new Vector();
    public static Vector test = new Vector();
    public static Vector gold = new Vector();
    public static String proj = "proj";

}
