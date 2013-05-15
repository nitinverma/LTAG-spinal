package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.BLinTagSample.java

import java.util.Vector;

public class BLinTagSample extends BSample
{

    public BLinTagSample(Vector vector)
    {
        words = new SWord[vector.size()];
        for(int i = 0; i < words.length; i++)
            words[i] = SWordLib.getSWord((String)vector.get(i));

    }

    public BLinTagSample(Vector vector, Vector vector1)
    {
        words = new SWord[vector.size()];
        tags = new SLabel[vector1.size()];
        for(int i = 0; i < words.length; i++)
        {
            words[i] = SWordLib.getSWord((String)vector.get(i));
            tags[i] = SLabelLib.getSLabel((String)vector1.get(i));
        }

    }

    public BLinTagSample(SWord asword[])
    {
        words = asword;
        tags = new SLabel[asword.length];
    }

    public void display(StringBuffer stringbuffer)
    {
        for(int i = 0; i < words.length; i++)
        {
            if(i > 0)
                stringbuffer.append(" ");
            stringbuffer.append((new StringBuilder()).append(words[i]).append("(").append(tags[i]).append(")").toString());
        }

    }

    public void displayConll()
    {
        for(int i = 0; i < words.length; i++)
            System.out.println((new StringBuilder()).append(words[i]).append("\t\t").append(tags[i]).toString());

        System.out.println("");
    }

    public SWord words[];
    public SLabel tags[];
}
