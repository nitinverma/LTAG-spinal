package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.SWord.java


public class SWord
{

    public SWord(String s)
    {
        word = new String(s);
    }

    public SWord(String s, int i)
    {
        word = new String(s);
        id = i;
    }

    public String toString()
    {
        return word;
    }

    public String word;
    public int id;
}
