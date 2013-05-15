package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.SLabelLib.java

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class SLabelLib
{

    public SLabelLib()
    {
    }

    public static void init()
    {
        label2id = new Hashtable(LABEL_HASH_INIT);
        id2label = new Vector();
    }

    public static int getLabelID(String s)
    {
        Integer integer = (Integer)label2id.get(s);
        if(integer != null)
        {
            return integer.intValue();
        } else
        {
            Integer integer1 = new Integer(id2label.size());
            label2id.put(s, integer1);
            id2label.add(new SLabel(s, integer1.intValue()));
            return integer1.intValue();
        }
    }

    public static SLabel getSLabel(String s)
    {
        return (SLabel)id2label.get(getLabelID(s));
    }

    public static SLabel getSLabel(int i)
    {
        return (SLabel)id2label.get(i);
    }

    public static int getSize()
    {
        return id2label.size();
    }

    public static String listAll()
    {
        StringBuffer stringbuffer = new StringBuffer();
        for(int i = 0; i < id2label.size(); i++)
            stringbuffer.append((new StringBuilder()).append("").append(i).append(" ").append(id2label.get(i)).append("\n").toString());

        return stringbuffer.toString();
    }

    public static void saveLabels(String s)
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileOutputStream(s));
            for(int i = 0; i < id2label.size(); i++)
                printwriter.println((new StringBuilder()).append("").append(id2label.get(i)).toString());

            printwriter.close();
        }
        catch(FileNotFoundException filenotfoundexception)
        {
            System.err.println(filenotfoundexception.toString());
        }
    }

    public static void loadLabels(String s)
    {
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(s));
            System.err.println((new StringBuilder()).append("Open Label File : ").append(s).toString());
            for(String s1 = bufferedreader.readLine(); s1 != null; s1 = bufferedreader.readLine())
            {
                String s2 = s1.trim();
                String as[] = s2.split("\\s+");
                getSLabel(as[0]);
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
    }

    public static void initTargetWithLabel()
    {
        target = new Vector();
        for(int i = 0; i < id2label.size(); i++)
            target.add(id2label.get(i));

    }

    private static int LABEL_HASH_INIT = 50;
    public static Hashtable label2id;
    public static Vector id2label;
    public static Vector target;

}
