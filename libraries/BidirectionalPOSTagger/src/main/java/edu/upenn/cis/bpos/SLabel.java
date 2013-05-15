package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.SLabel.java


public class SLabel
{

    public SLabel(String s, int i)
    {
        lbl = new String(s);
        id = i;
    }

    public String toString()
    {
        return lbl;
    }

    public SLabel(String s)
    {
        lbl = new String(s);
        id = -1;
    }

    public String lbl;
    public int id;
}
