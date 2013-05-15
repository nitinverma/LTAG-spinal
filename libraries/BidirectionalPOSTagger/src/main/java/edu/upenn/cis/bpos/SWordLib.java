package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.SWordLib.java

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class SWordLib
{

    public SWordLib()
    {
    }

    public static void init()
    {
        word2id = new Hashtable(WORD_HASH_INIT);
        id2word = new Vector();
    }

    public static int getWordID(String s)
    {
        Integer integer = (Integer)word2id.get(s);
        if(integer != null)
        {
            return integer.intValue();
        } else
        {
            Integer integer1 = new Integer(id2word.size());
            word2id.put(s, integer1);
            id2word.add(new SWord(s));
            return integer1.intValue();
        }
    }

    public static SWord getSWord(String s)
    {
        return (SWord)id2word.get(getWordID(s));
    }

    public static SWord getSWord(int i)
    {
        return (SWord)id2word.get(i);
    }

    public static int getSize()
    {
        return id2word.size();
    }

    public static String listAll()
    {
        StringBuffer stringbuffer = new StringBuffer();
        for(int i = 0; i < id2word.size(); i++)
            stringbuffer.append((new StringBuilder()).append("").append(i).append(" ").append(id2word.get(i)).append("\n").toString());

        return stringbuffer.toString();
    }

    public static void saveWords(String s)
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileOutputStream(s));
            for(int i = 0; i < id2word.size(); i++)
                printwriter.println((new StringBuilder()).append("").append(id2word.get(i)).toString());

            printwriter.close();
        }
        catch(FileNotFoundException filenotfoundexception)
        {
            System.err.println(filenotfoundexception.toString());
        }
    }

    public static void loadWords(String s)
    {
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(s));
            System.err.println((new StringBuilder()).append("Open Word File : ").append(s).toString());
            for(String s1 = bufferedreader.readLine(); s1 != null; s1 = bufferedreader.readLine())
            {
                String s2 = s1.trim();
                getWordID(s2);
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

    private static int WORD_HASH_INIT = 50;
    public static Hashtable word2id;
    public static Vector id2word;

}
