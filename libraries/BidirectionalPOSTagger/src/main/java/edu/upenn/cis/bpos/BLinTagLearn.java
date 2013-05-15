package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.BLinTagLearn.java

import java.util.Vector;

public class BLinTagLearn
{

    public BLinTagLearn(String s, Vector vector, SFeatLib sfeatlib)
    {
        proj = s;
        sample = vector;
        feat = sfeatlib;
    }

    public void train()
    {
        training = true;
        BHypothesis.training = true;
        BHypothesis.MARGIN_RATE = 48D;
        for(int i = 0; i < maxRound; i++)
        {
            for(int j = 0; j < sample.size(); j++)
            {
                System.err.println((new StringBuilder()).append("Sentence ").append(j).toString());
                curSenID = j;
                BLinTagSample blintagsample = (BLinTagSample)sample.get(j);
                Vector vector = new Vector();
                Vector vector1 = new Vector();
                int k = 0;
                int l = -1;
                int i1 = 0;
                initCands(vector1, blintagsample);
                do
                {
                    if(vector1.size() <= 0)
                        break;
                    inner++;
                    if(k == l)
                    {
                        if(++i1 >= 50)
                            break;
                    } else
                    {
                        l = k;
                        i1 = 0;
                    }
                    BLinIsland blinisland = selectCand(vector1);
                    boolean flag = checkCand(blinisland);
                    if(flag)
                    {
                        applyCand(vector, vector1, blinisland, blintagsample);
                        k++;
                    } else
                    {
                        vector1.clear();
                        if(vector.size() == 0)
                            initCands(vector1, blintagsample);
                        else
                            genAllCands(vector1, vector, blintagsample);
                    }
                } while(true);
                if(k < blintagsample.words.length)
                    System.err.println((new StringBuilder()).append("LOOP: ").append(j).toString());
            }

            feat.saveWeight((new StringBuilder()).append(proj).append(".").append(i).append(".fea").toString(), inner);
        }

    }

    public void predict()
    {
        training = false;
        BHypothesis.training = false;
        for(int i = 0; i < sample.size(); i++)
        {
            BLinTagSample blintagsample = (BLinTagSample)sample.get(i);
            Vector vector = new Vector();
            Vector vector1 = new Vector();
            initCands(vector1, blintagsample);
            BLinIsland blinisland;
            for(; vector1.size() > 0; applyCand(vector, vector1, blinisland, blintagsample))
                blinisland = selectCand(vector1);

            BLinIsland blinisland1 = (BLinIsland)vector.firstElement();
            BLinTagSample blintagsample1 = new BLinTagSample(blinisland1.sen.words);
            blinisland1.retrieve(blintagsample1, blinisland1.topLeftBoundSktID, blinisland1.topRightBoundSktID);
            for(int j = 0; j < blintagsample.words.length; j++)
            {
                if(j > 0)
                    System.out.print(" ");
                System.out.print((new StringBuilder()).append("").append(blintagsample.words[j]).append("_").append(blintagsample1.tags[j]).toString());
            }

            System.out.println("");
        }

    }

    public void evaluate()
    {
        training = false;
        BHypothesis.training = false;
        int i = 0;
        int j = 0;
        int k = 0;
        for(int l = 0; l < sample.size(); l++)
        {
            System.err.println((new StringBuilder()).append("Sentence ").append(l).toString());
            BLinTagSample blintagsample = (BLinTagSample)sample.get(l);
            Vector vector = new Vector();
            Vector vector1 = new Vector();
            boolean flag = true;
            initCands(vector1, blintagsample);
            BLinIsland blinisland;
            for(; vector1.size() > 0; applyCand(vector, vector1, blinisland, blintagsample))
                blinisland = selectCand(vector1);

            BLinIsland blinisland1 = (BLinIsland)vector.firstElement();
            BLinTagSample blintagsample1 = new BLinTagSample(blinisland1.sen.words);
            blinisland1.retrieve(blintagsample1, blinisland1.topLeftBoundSktID, blinisland1.topRightBoundSktID);
            j += blintagsample1.words.length;
            for(int i1 = 0; i1 < blintagsample1.words.length; i1++)
                if(blintagsample1.tags[i1] == blintagsample.tags[i1])
                    i++;
                else
                    flag = false;

            if(flag)
                k++;
            StringBuffer stringbuffer = new StringBuffer("GLD: ");
            blintagsample.display(stringbuffer);
            stringbuffer.append("\nTOP: ");
            ((BLinIsland)vector.firstElement()).display(stringbuffer);
            System.err.println(stringbuffer.toString());
        }

        System.out.println((new StringBuilder()).append("Total: ").append(j).toString());
        System.out.println((new StringBuilder()).append("Match: ").append(i).toString());
        double d = (1.0D * (double)i) / (double)j;
        System.out.println((new StringBuilder()).append("Precision: ").append(d).toString());
        System.out.println((new StringBuilder()).append("Sentences: ").append(sample.size()).toString());
        System.out.println((new StringBuilder()).append("Sen Match: ").append(k).toString());
        double d1 = (1.0D * (double)k) / (double)sample.size();
        System.out.println((new StringBuilder()).append("Sen Precistioin: ").append(d1).toString());
    }

    public void traineval(Vector vector)
    {
        for(int i = 0; i < maxRound; i++)
        {
            training = true;
            BHypothesis.training = true;
            BHypothesis.MARGIN_RATE = 48D;
            for(int j = 0; j < sample.size(); j++)
            {
                System.err.println((new StringBuilder()).append("Sentence ").append(j).toString());
                curSenID = j;
                BLinTagSample blintagsample = (BLinTagSample)sample.get(j);
                Vector vector1 = new Vector();
                Vector vector2 = new Vector();
                int k = 0;
                int l = -1;
                int i1 = 0;
                initCands(vector2, blintagsample);
                do
                {
                    if(vector2.size() <= 0)
                        break;
                    inner++;
                    if(k == l)
                    {
                        if(++i1 >= 50)
                            break;
                    } else
                    {
                        l = k;
                        i1 = 0;
                    }
                    BLinIsland blinisland = selectCand(vector2);
                    boolean flag = checkCand(blinisland);
                    if(flag)
                    {
                        applyCand(vector1, vector2, blinisland, blintagsample);
                        k++;
                    } else
                    {
                        vector2.clear();
                        if(vector1.size() == 0)
                            initCands(vector2, blintagsample);
                        else
                            genAllCands(vector2, vector1, blintagsample);
                    }
                } while(true);
                if(k < blintagsample.words.length)
                    System.err.println((new StringBuilder()).append("LOOP: ").append(j).toString());
            }

            training = false;
            BHypothesis.training = false;
            SFeatLib sfeatlib = feat;
            SFeatLib sfeatlib1 = new SFeatLib(feat);
            sfeatlib1.useVotedFeat(inner);
            BLinTagLearn blintaglearn = new BLinTagLearn(proj, vector, sfeatlib1);
            blintaglearn.evaluate();
            feat = sfeatlib;
        }

    }

    public void initCands(Vector vector, BLinTagSample blintagsample)
    {
        for(int i = 0; i < blintagsample.words.length; i++)
        {
            BLinIsland blinisland = new BLinIsland(blintagsample, i, training);
            vector.add(blinisland);
        }

    }

    public BLinIsland selectCand(Vector vector)
    {
        BLinIsland blinisland = null;
        double d = (-1.0D / 0.0D);
        for(int i = 0; i < vector.size(); i++)
        {
            BLinIsland blinisland1 = (BLinIsland)vector.get(i);
            if(blinisland1.topOpHypo.getLabelScoreMGN() > d)
            {
                d = blinisland1.topOpHypo.getLabelScoreMGN();
                blinisland = blinisland1;
            }
        }

        return blinisland;
    }

    public boolean checkCand(BLinIsland blinisland)
    {
        if(blinisland.topOpHypo == blinisland.goldHypo)
        {
            return true;
        } else
        {
            feat.updateFeat(blinisland.goldHypo.features, 1.0D, inner);
            feat.updateFeat(blinisland.topOpHypo.features, -1D, inner);
            return false;
        }
    }

    public void applyCand(Vector vector, Vector vector1, BLinIsland blinisland, BLinTagSample blintagsample)
    {
        if(blinisland.islandFromLeft != null || blinisland.islandFromRight != null)
        {
            for(int i = 0; i < vector.size(); i++)
            {
                BLinIsland blinisland1 = (BLinIsland)vector.get(i);
                if(blinisland1 == blinisland.islandFromLeft || blinisland1 == blinisland.islandFromRight)
                {
                    vector.removeElementAt(i);
                    i--;
                }
            }

        }
        boolean flag = false;
        int j = 0;
        do
        {
            if(j >= vector.size())
                break;
            BLinIsland blinisland2 = (BLinIsland)vector.get(j);
            if(blinisland2.lastPosi > blinisland.lastPosi)
            {
                vector.insertElementAt(blinisland, j);
                flag = true;
                break;
            }
            j++;
        } while(true);
        if(!flag)
            vector.add(blinisland);
        vector1.removeElement(blinisland);
        for(int k = 0; k < vector1.size(); k++)
        {
            BLinIsland blinisland3 = (BLinIsland)vector1.get(k);
            if(blinisland.islandFromLeft != null && blinisland3.islandFromRight == blinisland.islandFromLeft)
            {
                BLinIsland blinisland4 = new BLinIsland(blintagsample, blinisland3.lastPosi, blinisland3.islandFromLeft, blinisland, training);
                vector1.setElementAt(blinisland4, k);
            }
            if(blinisland.islandFromRight != null && blinisland3.islandFromLeft == blinisland.islandFromRight)
            {
                BLinIsland blinisland5 = new BLinIsland(blintagsample, blinisland3.lastPosi, blinisland, blinisland3.islandFromRight, training);
                vector1.setElementAt(blinisland5, k);
            }
            if(blinisland.islandFromLeft == null && blinisland3.lastPosi == blinisland.lastPosi - 1)
            {
                BLinIsland blinisland6 = new BLinIsland(blintagsample, blinisland3.lastPosi, blinisland3.islandFromLeft, blinisland, training);
                vector1.setElementAt(blinisland6, k);
            }
            if(blinisland.islandFromRight == null && blinisland3.lastPosi == blinisland.lastPosi + 1)
            {
                BLinIsland blinisland7 = new BLinIsland(blintagsample, blinisland3.lastPosi, blinisland, blinisland3.islandFromRight, training);
                vector1.setElementAt(blinisland7, k);
            }
        }

    }

    public void genAllCands(Vector vector, Vector vector1, BLinTagSample blintagsample)
    {
        for(int i = 0; i < blintagsample.words.length; i++)
            if(getDomIsland(i, vector1) == null)
            {
                BLinIsland blinisland = getDomIsland(i - 1, vector1);
                BLinIsland blinisland1 = getDomIsland(i + 1, vector1);
                BLinIsland blinisland2 = new BLinIsland(blintagsample, i, blinisland, blinisland1, training);
                vector.add(blinisland2);
            }

    }

    private BLinIsland getDomIsland(int i, Vector vector)
    {
        for(int j = 0; j < vector.size(); j++)
        {
            BLinIsland blinisland = (BLinIsland)vector.get(j);
            if(blinisland.leftBoundPosi <= i && i <= blinisland.rightBoundPosi)
                return blinisland;
        }

        return null;
    }

    public static final boolean EXTENDFEAT = true;
    public static final int NGRAM = 3;
    public static int KSOCKET = 1;
    public static int KHYPO = 1;
    public static final double TAU = 0.029999999999999999D;
    public static final double RADIUS = 40D;
    public static final double MARGIN_RATE = 48D;
    public static final int maxLoop = 50;
    public static int maxRound = 10;
    public static boolean training;
    public static SFeatLib feat;
    public static String proj;
    public Vector sample;
    private static int currentRound = 0;
    public static int inner = 0;
    public static int curSenID = -1;

}
