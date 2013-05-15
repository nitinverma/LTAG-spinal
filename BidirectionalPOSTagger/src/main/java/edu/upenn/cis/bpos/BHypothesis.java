package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.BHypothesis.java

import java.util.Vector;

public class BHypothesis
{

    public BHypothesis(BLinIsland blinisland, SLabel slabel)
    {
        isgold = false;
        mistake = 0;
        island = blinisland;
        lastLabel = slabel;
        features = new Vector();
    }

    public void compLblTtlScores(SFeatLib sfeatlib)
    {
        labelScore = sfeatlib.getScore(features);
        hypoScore = labelScore + contextScore;
    }

    public void setContextScore(double d)
    {
        contextScore = d;
    }

    public double getLabelScore()
    {
        return labelScore;
    }

    public double getHypoScore()
    {
        return hypoScore;
    }

    public double getLabelScoreMGN()
    {
        if(training)
        {
            double d = (island.rightBoundPosi - island.leftBoundPosi) + 1;
            return labelScore + MARGIN_RATE;
        } else
        {
            return labelScore;
        }
    }

    public double getHypoScoreMGN()
    {
        if(training)
        {
            double d = (island.rightBoundPosi - island.leftBoundPosi) + 1;
            return hypoScore + MARGIN_RATE * ((double)mistake + (double)mistake / d);
        } else
        {
            return hypoScore;
        }
    }

    public static double MARGIN_RATE;
    public static boolean training = false;
    public BLinIsland island;
    public SLabel lastLabel;
    public int socketIDFromLeft;
    public int socketIDFromRight;
    private double labelScore;
    private double contextScore;
    private double hypoScore;
    public boolean isgold;
    public int mistake;
    public Vector features;

}
