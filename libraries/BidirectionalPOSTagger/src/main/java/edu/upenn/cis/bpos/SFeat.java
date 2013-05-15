package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.SFeatLib.java


class SFeat
{

    public SFeat(String s)
    {
        featstr = s;
        weight = 0.0D;
        freq = 0;
        update = 0;
        cmlwt = 0.0D;
    }

    public SFeat(SFeat sfeat)
    {
        featstr = sfeat.featstr;
        weight = sfeat.weight;
        freq = sfeat.freq;
        update = sfeat.update;
        cmlwt = sfeat.cmlwt;
    }

    public double updateCmlwt(int i)
    {
        cmlwt += (double)(i - update) * weight;
        update = i;
        return cmlwt;
    }

    public String featstr;
    public double weight;
    public int freq;
    public int update;
    public double cmlwt;
}
