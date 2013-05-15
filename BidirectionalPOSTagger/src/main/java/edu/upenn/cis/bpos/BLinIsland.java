package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.BLinIsland.java

import java.util.*;

public class BLinIsland
{

    public String toString()
    {
        if(leftBoundPosi != rightBoundPosi)
            return (new StringBuilder()).append("(").append(leftBoundPosi).append(",").append(lastPosi).append("(").append(sen.words[lastPosi]).append("),").append(rightBoundPosi).append(")").toString();
        else
            return (new StringBuilder()).append("").append(lastPosi).append("(").append(sen.words[lastPosi]).append(")").toString();
    }

    public BLinIsland(BLinTagSample blintagsample, int i, boolean flag)
    {
        sen = blintagsample;
        lastPosi = i;
        leftBoundPosi = i;
        rightBoundPosi = i;
        HashSet hashset = new HashSet();
        HashSet hashset1 = new HashSet();
        Hashtable hashtable = new Hashtable();
        for(int j = 0; j < SLabelLib.target.size(); j++)
        {
            SLabel slabel = (SLabel)SLabelLib.target.get(j);
            genSktHypo(slabel, null, null, -1, 0.0D, null, null, -1, 0.0D, hashset, hashset1, hashtable, flag, true, 0);
        }

        keepKBest(hashset, hashset1, hashtable, flag);
        setTopHypo();
        if(flag)
        {
            int k = 0;
            do
            {
                if(k >= leftBoundSocket.size())
                    break;
                if(((BSocket)leftBoundSocket.get(k)).sktLabel.firstElement() == sen.tags[i])
                {
                    goldLeftBoundSktID = k;
                    break;
                }
                k++;
            } while(true);
            k = 0;
            do
            {
                if(k >= rightBoundSocket.size())
                    break;
                if(((BSocket)rightBoundSocket.get(k)).sktLabel.firstElement() == sen.tags[i])
                {
                    goldRightBoundSktID = k;
                    break;
                }
                k++;
            } while(true);
        }
    }

    public BLinIsland(BLinTagSample blintagsample, int i, BLinIsland blinisland, BLinIsland blinisland1, boolean flag)
    {
        sen = blintagsample;
        lastPosi = i;
        if(blinisland == null)
            leftBoundPosi = i;
        else
            leftBoundPosi = blinisland.leftBoundPosi;
        if(blinisland1 == null)
            rightBoundPosi = i;
        else
            rightBoundPosi = blinisland1.rightBoundPosi;
        if(i < leftBoundPosi || i > rightBoundPosi)
            System.err.println("Error in creating edu.upenn.cis.bpos.BLinIsland");
        islandFromLeft = blinisland;
        islandFromRight = blinisland1;
        HashSet hashset = new HashSet();
        HashSet hashset1 = new HashSet();
        Hashtable hashtable = new Hashtable();
label0:
        for(int j = 0; j < SLabelLib.target.size(); j++)
        {
            SLabel slabel = (SLabel)SLabelLib.target.get(j);
            boolean flag1 = true;
            if(islandFromLeft == null && islandFromRight == null)
                genSktHypo(slabel, null, null, -1, 0.0D, null, null, -1, 0.0D, hashset, hashset1, hashtable, flag, true, 0);
            if(islandFromLeft != null && islandFromRight == null)
            {
                for(int l = 0; l < islandFromLeft.rightBoundSocket.size(); l++)
                {
                    BSocket bsocket = (BSocket)islandFromLeft.rightBoundSocket.get(l);
                    for(int k1 = 0; k1 < islandFromLeft.leftBoundSocket.size(); k1++)
                    {
                        BSocket bsocket3 = (BSocket)islandFromLeft.leftBoundSocket.get(k1);
                        BHypothesis bhypothesis = islandFromLeft.hypo[k1][l][0];
                        if(bhypothesis != null)
                        {
                            double d = bhypothesis.getHypoScore();
                            flag1 = flag1 && bhypothesis.isgold;
                            genSktHypo(slabel, bsocket3, bsocket, l, d, null, null, -1, 0.0D, hashset, hashset1, hashtable, flag, flag1, bhypothesis.mistake);
                        }
                    }

                }

            }
            if(islandFromLeft == null && islandFromRight != null)
            {
                for(int i1 = 0; i1 < islandFromRight.leftBoundSocket.size(); i1++)
                {
                    BSocket bsocket1 = (BSocket)islandFromRight.leftBoundSocket.get(i1);
                    for(int l1 = 0; l1 < islandFromRight.rightBoundSocket.size(); l1++)
                    {
                        BSocket bsocket4 = (BSocket)islandFromRight.rightBoundSocket.get(l1);
                        BHypothesis bhypothesis1 = islandFromRight.hypo[i1][l1][0];
                        if(bhypothesis1 != null)
                        {
                            double d1 = bhypothesis1.getHypoScore();
                            flag1 = flag1 && bhypothesis1.isgold;
                            genSktHypo(slabel, null, null, -1, 0.0D, bsocket4, bsocket1, i1, d1, hashset, hashset1, hashtable, flag, flag1, bhypothesis1.mistake);
                        }
                    }

                }

            }
            if(islandFromLeft == null || islandFromRight == null)
                continue;
            int j1 = 0;
            do
            {
                if(j1 >= islandFromLeft.rightBoundSocket.size())
                    continue label0;
                BSocket bsocket2 = (BSocket)islandFromLeft.rightBoundSocket.get(j1);
label1:
                for(int i2 = 0; i2 < islandFromLeft.leftBoundSocket.size(); i2++)
                {
                    BSocket bsocket5 = (BSocket)islandFromLeft.leftBoundSocket.get(i2);
                    BHypothesis bhypothesis2 = islandFromLeft.hypo[i2][j1][0];
                    if(bhypothesis2 == null)
                        continue;
                    double d2 = bhypothesis2.getHypoScore();
                    flag1 = flag1 && bhypothesis2.isgold;
                    int j2 = 0;
                    do
                    {
                        if(j2 >= islandFromRight.leftBoundSocket.size())
                            continue label1;
                        BSocket bsocket6 = (BSocket)islandFromRight.leftBoundSocket.get(j2);
                        for(int k2 = 0; k2 < islandFromRight.rightBoundSocket.size(); k2++)
                        {
                            BSocket bsocket7 = (BSocket)islandFromRight.rightBoundSocket.get(k2);
                            BHypothesis bhypothesis3 = islandFromRight.hypo[j2][k2][0];
                            if(bhypothesis3 != null)
                            {
                                double d3 = bhypothesis3.getHypoScore();
                                flag1 = flag1 && bhypothesis3.isgold;
                                genSktHypo(slabel, bsocket5, bsocket2, j1, d2, bsocket7, bsocket6, j2, d3, hashset, hashset1, hashtable, flag, flag1, bhypothesis2.mistake + bhypothesis3.mistake);
                            }
                        }

                        j2++;
                    } while(true);
                }

                j1++;
            } while(true);
        }

        keepKBest(hashset, hashset1, hashtable, flag);
        setTopHypo();
        if(flag)
        {
            int k = 0;
            do
            {
                if(k >= leftBoundSocket.size())
                    break;
                if(((BSocket)leftBoundSocket.get(k)).sktLabel.firstElement() == sen.tags[i])
                {
                    goldLeftBoundSktID = k;
                    break;
                }
                k++;
            } while(true);
            k = 0;
            do
            {
                if(k >= rightBoundSocket.size())
                    break;
                if(((BSocket)rightBoundSocket.get(k)).sktLabel.firstElement() == sen.tags[i])
                {
                    goldRightBoundSktID = k;
                    break;
                }
                k++;
            } while(true);
        }
    }

    private void genSktHypo(SLabel slabel, BSocket bsocket, BSocket bsocket1, int i, double d, BSocket bsocket2, 
            BSocket bsocket3, int j, double d1, HashSet hashset, HashSet hashset1, Hashtable hashtable, 
            boolean flag, boolean flag1, int k)
    {
        BHypothesis bhypothesis = new BHypothesis(this, slabel);
        bhypothesis.socketIDFromLeft = i;
        bhypothesis.socketIDFromRight = j;
        Vector vector = new Vector();
        if(bsocket1 == null)
        {
            vector.add(slabel);
            if(bsocket3 != null)
                vector.add(bsocket3.sktLabel.firstElement());
        } else
        {
            vector.add(bsocket.sktLabel.firstElement());
            if(bsocket.sktLabel.size() > 1)
                vector.add(bsocket.sktLabel.get(1));
            else
                vector.add(slabel);
        }
        BSocket bsocket4 = new BSocket(vector);
        hashset.add(bsocket4);
        vector = new Vector();
        if(bsocket3 == null)
        {
            if(bsocket1 != null)
                vector.add(bsocket1.sktLabel.lastElement());
            vector.add(slabel);
        } else
        {
            if(bsocket2.sktLabel.size() > 1)
                vector.add(bsocket2.sktLabel.get(0));
            else
                vector.add(slabel);
            vector.add(bsocket2.sktLabel.lastElement());
        }
        BSocket bsocket5 = new BSocket(vector);
        hashset1.add(bsocket5);
        Vector vector1 = new Vector();
        vector1.add(bsocket4);
        vector1.add(bsocket5);
        Vector vector2 = (Vector)hashtable.get(vector1);
        if(vector2 == null)
        {
            vector2 = new Vector();
            hashtable.put(vector1, vector2);
        }
        vector2.add(bhypothesis);
        genCandFeat(bhypothesis, sen, lastPosi, slabel, bsocket1, bsocket3);
        bhypothesis.setContextScore(d + d1);
        bhypothesis.compLblTtlScores(BLinTagLearn.feat);
        if(flag)
            if(flag1 && slabel == sen.tags[lastPosi])
            {
                bhypothesis.isgold = true;
                bhypothesis.mistake = 0;
                goldHypo = bhypothesis;
            } else
            {
                bhypothesis.isgold = false;
                if(slabel == sen.tags[lastPosi])
                    bhypothesis.mistake = k;
                else
                    bhypothesis.mistake = k + 1;
            }
    }

    public void setTopHypo()
    {
        double d = (-1.0D / 0.0D);
        for(int i = 0; i < leftBoundSocket.size(); i++)
        {
            for(int j = 0; j < rightBoundSocket.size(); j++)
            {
                BHypothesis bhypothesis = hypo[i][j][0];
                if(bhypothesis == null)
                    continue;
                double d1 = bhypothesis.getHypoScoreMGN();
                if(d1 > d)
                {
                    d = d1;
                    topOpHypo = bhypothesis;
                    topLeftBoundSktID = i;
                    topRightBoundSktID = j;
                    topHypoID = 0;
                }
            }

        }

    }

    public void keepKBest(HashSet hashset, HashSet hashset1, Hashtable hashtable, boolean flag)
    {
        Vector vector = new Vector();
        Vector vector1 = new Vector();
        Enumeration enumeration = hashtable.keys();
        do
        {
            if(!enumeration.hasMoreElements())
                break;
            Vector vector2 = (Vector)enumeration.nextElement();
            Vector vector3 = (Vector)hashtable.get(vector2);
            double d = ((BHypothesis)vector3.firstElement()).getHypoScoreMGN();
            for(int l = 1; l < vector3.size(); l++)
            {
                double d1 = ((BHypothesis)vector3.get(l)).getHypoScoreMGN();
                if(d1 > d)
                    d = d1;
            }

            boolean flag1 = false;
            int i1 = 0;
            do
            {
                if(i1 >= vector1.size())
                    break;
                if(d > ((Double)vector1.get(i1)).doubleValue())
                {
                    vector1.insertElementAt(Double.valueOf(d), i1);
                    vector.insertElementAt(vector2, i1);
                    flag1 = true;
                    break;
                }
                i1++;
            } while(true);
            if(vector1.size() > BLinTagLearn.KSOCKET)
            {
                vector1.setSize(BLinTagLearn.KSOCKET);
                vector.setSize(BLinTagLearn.KSOCKET);
            } else
            if(!flag1 && vector1.size() < BLinTagLearn.KSOCKET)
            {
                vector1.add(Double.valueOf(d));
                vector.add(vector2);
            }
        } while(true);
        leftBoundSocket = new Vector();
        rightBoundSocket = new Vector();
        for(int i = 0; i < vector.size(); i++)
        {
            BSocket bsocket = (BSocket)((Vector)vector.get(i)).firstElement();
            BSocket bsocket2 = (BSocket)((Vector)vector.get(i)).lastElement();
            addSocket(bsocket, leftBoundSocket);
            addSocket(bsocket2, rightBoundSocket);
        }

        hypo = new BHypothesis[leftBoundSocket.size()][rightBoundSocket.size()][BLinTagLearn.KHYPO];
        for(int j = 0; j < leftBoundSocket.size(); j++)
        {
            BSocket bsocket1 = (BSocket)leftBoundSocket.get(j);
            for(int k = 0; k < rightBoundSocket.size(); k++)
            {
                BSocket bsocket3 = (BSocket)rightBoundSocket.get(k);
                Vector vector4 = new Vector();
                vector4.add(bsocket1);
                vector4.add(bsocket3);
                Vector vector5 = (Vector)hashtable.get(vector4);
                if(vector5 != null)
                    topKHypo(hypo[j][k], vector5);
            }

        }

    }

    private void addSocket(BSocket bsocket, Vector vector)
    {
        boolean flag = false;
        int i = 0;
        do
        {
            if(i >= vector.size())
                break;
            if(bsocket.equals(vector.get(i)))
            {
                flag = true;
                break;
            }
            i++;
        } while(true);
        if(!flag)
            vector.add(bsocket);
    }

    private void topKHypo(BHypothesis abhypothesis[], Vector vector)
    {
        Vector vector1 = new Vector();
        for(int i = 0; i < vector.size(); i++)
        {
            boolean flag = false;
            double d = ((BHypothesis)vector.get(i)).getHypoScoreMGN();
            int k = 0;
            do
            {
                if(k >= vector1.size())
                    break;
                double d1 = ((BHypothesis)vector1.get(k)).getHypoScoreMGN();
                if(d > d1)
                {
                    vector1.insertElementAt(vector.get(i), k);
                    flag = true;
                    if(vector1.size() > BLinTagLearn.KHYPO)
                        vector1.setSize(BLinTagLearn.KHYPO);
                    break;
                }
                k++;
            } while(true);
            if(!flag && vector1.size() < BLinTagLearn.KHYPO)
                vector1.add(vector.get(i));
        }

        for(int j = 0; j < vector1.size(); j++)
            abhypothesis[j] = (BHypothesis)vector1.get(j);

    }

    private void genCandFeat(BHypothesis bhypothesis, BLinTagSample blintagsample, int i, SLabel slabel, BSocket bsocket, BSocket bsocket1)
    {
        genAdwaitFeat(bhypothesis, blintagsample, i, slabel, bsocket, bsocket1);
        genExtraFeat(bhypothesis, blintagsample, i, slabel, bsocket, bsocket1);
    }

    private void genAdwaitFeat(BHypothesis bhypothesis, BLinTagSample blintagsample, int i, SLabel slabel, BSocket bsocket, BSocket bsocket1)
    {
        String s = (new StringBuilder()).append("").append(slabel).toString();
        String s1 = (new StringBuilder()).append("|X:").append(blintagsample.words[i].word.toLowerCase()).toString();
        bhypothesis.features.add((new StringBuilder()).append(s).append(s1).toString());
        String s2 = blintagsample.words[i].word;
        String s3 = s2.toLowerCase();
        byte abyte0[] = s3.getBytes();
        String s4 = "";
        String s5 = "";
        byte byte0 = 4;
        byte0 = 9;
        for(int j = 0; j < byte0; j++)
        {
            int k = j;
            if(k < abyte0.length)
            {
                s4 = (new StringBuilder()).append(s4).append((char)abyte0[k]).toString();
                bhypothesis.features.add((new StringBuilder()).append(s).append("|P:").append(s4).toString());
            }
            int l = abyte0.length - 1 - j;
            if(l >= 0)
            {
                s5 = (new StringBuilder()).append(s5).append((char)abyte0[l]).toString();
                bhypothesis.features.add((new StringBuilder()).append(s).append("|S:").append(s5).toString());
            }
        }

        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;
        abyte0 = s2.getBytes();
        for(int i1 = 0; i1 < abyte0.length; i1++)
        {
            char c = (char)abyte0[i1];
            flag |= Character.isDigit(c);
            if(i > 0 || i1 > 0)
                flag1 |= Character.isUpperCase(c);
            flag2 |= c == '-';
        }

        if(flag)
            bhypothesis.features.add((new StringBuilder()).append(s).append("|NM").toString());
        if(flag1)
            bhypothesis.features.add((new StringBuilder()).append(s).append("|UP").toString());
        if(flag2)
            bhypothesis.features.add((new StringBuilder()).append(s).append("|HF").toString());
        Vector vector = new Vector();
        if(bsocket != null)
        {
            for(int j1 = 0; j1 < bsocket.sktLabel.size(); j1++)
                vector.add(((SLabel)bsocket.sktLabel.get(j1)).lbl);

            if(vector.size() < 2 && i - vector.size() == 0)
                vector.insertElementAt("@", 0);
        } else
        if(i == 0)
            vector.add("@");
        Vector vector1 = new Vector();
        if(bsocket1 != null)
        {
            for(int k1 = 0; k1 < bsocket1.sktLabel.size(); k1++)
                vector1.add(((SLabel)bsocket1.sktLabel.get(k1)).lbl);

            if(vector1.size() < 2 && i + vector1.size() == blintagsample.words.length - 1)
                vector1.add("@");
        } else
        if(i == blintagsample.words.length - 1)
            vector1.add("@");
        if(vector.size() > 0)
        {
            String s6 = s;
            int i2 = 0;
            for(int l2 = vector.size() - 1; l2 >= 0; l2--)
            {
                i2++;
                s6 = (new StringBuilder()).append(s6).append("|L").append(i2).append(":").append((String)vector.get(l2)).toString();
                bhypothesis.features.add(s6);
            }

        }
        if(vector1.size() > 0)
        {
            String s7 = s;
            int j2 = 0;
            for(int i3 = 0; i3 < vector1.size(); i3++)
            {
                j2++;
                s7 = (new StringBuilder()).append(s7).append("|R").append(j2).append(":").append((String)vector1.get(i3)).toString();
                bhypothesis.features.add(s7);
            }

        }
        if(vector.size() > 0 && vector1.size() > 0)
        {
            String s8 = (new StringBuilder()).append("|L1:").append((String)vector.lastElement()).append("|R1:").append((String)vector1.firstElement()).toString();
            bhypothesis.features.add((new StringBuilder()).append(s).append(s8).toString());
        }
        for(int l1 = -2; l1 <= 2; l1++)
        {
            if(l1 == 0)
                continue;
            int k2 = i + l1;
            String s9 = "@";
            if(k2 >= 0 && k2 < blintagsample.words.length)
                s9 = blintagsample.words[k2].word.toLowerCase();
            else
            if(k2 == -2 || k2 == blintagsample.words.length + 1)
                continue;
            String s10 = "|L";
            int j3 = 0 - l1;
            if(l1 > 0)
            {
                s10 = "|R";
                j3 = l1;
            }
            bhypothesis.features.add((new StringBuilder()).append(s).append(s10).append(j3).append("X:").append(s9).toString());
        }

    }

    private void genExtraFeat(BHypothesis bhypothesis, BLinTagSample blintagsample, int i, SLabel slabel, BSocket bsocket, BSocket bsocket1)
    {
        String s = (new StringBuilder()).append("").append(slabel).toString();
        String s1 = (new StringBuilder()).append("|X:").append(blintagsample.words[i].word.toLowerCase()).toString();
        Vector vector = new Vector();
        if(bsocket != null)
        {
            for(int j = 0; j < bsocket.sktLabel.size(); j++)
                vector.add(((SLabel)bsocket.sktLabel.get(j)).lbl);

            if(vector.size() < 2 && i - vector.size() == 0)
                vector.insertElementAt("@", 0);
        } else
        if(i == 0)
            vector.add("@");
        Vector vector1 = new Vector();
        if(bsocket1 != null)
        {
            for(int k = 0; k < bsocket1.sktLabel.size(); k++)
                vector1.add(((SLabel)bsocket1.sktLabel.get(k)).lbl);

            if(vector1.size() < 2 && i + vector1.size() == blintagsample.words.length - 1)
                vector1.add("@");
        } else
        if(i == blintagsample.words.length - 1)
            vector1.add("@");
        if(vector.size() > 0)
        {
            String s2 = s;
            int l = 0;
            for(int j1 = vector.size() - 1; j1 >= 0; j1--)
            {
                l++;
                s2 = (new StringBuilder()).append(s2).append("|L").append(l).append(":").append((String)vector.get(j1)).toString();
                bhypothesis.features.add((new StringBuilder()).append(s2).append(s1).toString());
                if(l > 1)
                {
                    bhypothesis.features.add((new StringBuilder()).append(s).append("|L").append(l).append(":").append((String)vector.get(j1)).toString());
                    bhypothesis.features.add((new StringBuilder()).append(s).append("|L").append(l).append(":").append((String)vector.get(j1)).append(s1).toString());
                }
            }

        }
        if(vector1.size() > 0)
        {
            String s3 = s;
            int i1 = 0;
            for(int k1 = 0; k1 < vector1.size(); k1++)
            {
                i1++;
                s3 = (new StringBuilder()).append(s3).append("|R").append(i1).append(":").append((String)vector1.get(k1)).toString();
                bhypothesis.features.add((new StringBuilder()).append(s3).append(s1).toString());
                if(i1 > 1)
                {
                    bhypothesis.features.add((new StringBuilder()).append(s).append("|R").append(i1).append(":").append((String)vector1.get(k1)).toString());
                    bhypothesis.features.add((new StringBuilder()).append(s).append("|R").append(i1).append(":").append((String)vector1.get(k1)).append(s1).toString());
                }
            }

        }
        if(vector.size() > 0 && vector1.size() > 0)
        {
            String s4 = (new StringBuilder()).append("|L1:").append((String)vector.lastElement()).append("|R1:").append((String)vector1.firstElement()).toString();
            bhypothesis.features.add((new StringBuilder()).append(s).append(s4).append(s1).toString());
        }
        String s5 = "@";
        if(i - 1 >= 0)
            s5 = blintagsample.words[i - 1].word.toLowerCase();
        bhypothesis.features.add((new StringBuilder()).append(s).append(s1).append("|L1X:").append(s5).toString());
        String s6 = "@";
        if(i + 1 < blintagsample.words.length)
            s6 = blintagsample.words[i + 1].word.toLowerCase();
        bhypothesis.features.add((new StringBuilder()).append(s).append(s1).append("|R1X:").append(s6).toString());
    }

    public void display(StringBuffer stringbuffer)
    {
        BLinTagSample blintagsample = new BLinTagSample(sen.words);
        retrieve(blintagsample, topLeftBoundSktID, topRightBoundSktID);
        blintagsample.display(stringbuffer);
    }

    public void retrieve(BLinTagSample blintagsample, int i, int j)
    {
        BHypothesis bhypothesis = hypo[i][j][0];
        blintagsample.tags[lastPosi] = bhypothesis.lastLabel;
        if(islandFromLeft != null)
        {
            int k = bhypothesis.socketIDFromLeft;
            int i1 = getCompLeftSktID(islandFromLeft.leftBoundSocket, (BSocket)leftBoundSocket.get(i));
            islandFromLeft.retrieve(blintagsample, i1, k);
        }
        if(islandFromRight != null)
        {
            int l = bhypothesis.socketIDFromRight;
            int j1 = getCompRightSktID(islandFromRight.rightBoundSocket, (BSocket)rightBoundSocket.get(j));
            islandFromRight.retrieve(blintagsample, l, j1);
        }
    }

    private int getCompLeftSktID(Vector vector, BSocket bsocket)
    {
        for(int i = 0; i < vector.size(); i++)
        {
            BSocket bsocket1 = (BSocket)vector.get(i);
            boolean flag = true;
            int j = 0;
            do
            {
                if(j >= bsocket1.sktLabel.size())
                    break;
                if(bsocket1.sktLabel.get(j) != bsocket.sktLabel.get(j))
                {
                    flag = false;
                    break;
                }
                j++;
            } while(true);
            if(flag)
                return i;
        }

        System.err.println((new StringBuilder()).append("LEFT LINK BROKEN at ").append(lastPosi).toString());
        return -1;
    }

    private int getCompRightSktID(Vector vector, BSocket bsocket)
    {
        for(int i = 0; i < vector.size(); i++)
        {
            BSocket bsocket1 = (BSocket)vector.get(i);
            boolean flag = true;
            int j = 0;
            do
            {
                if(j >= bsocket1.sktLabel.size())
                    break;
                if(bsocket1.sktLabel.get(bsocket1.sktLabel.size() - 1 - j) != bsocket.sktLabel.get(bsocket.sktLabel.size() - 1 - j))
                {
                    flag = false;
                    break;
                }
                j++;
            } while(true);
            if(flag)
                return i;
        }

        System.err.println((new StringBuilder()).append("RIGHT LINK BROKEN at ").append(lastPosi).toString());
        return -1;
    }

    public BLinTagSample sen;
    public int lastPosi;
    public int leftBoundPosi;
    public int rightBoundPosi;
    public BLinIsland islandFromLeft;
    public BLinIsland islandFromRight;
    public Vector leftBoundSocket;
    public Vector rightBoundSocket;
    public BHypothesis hypo[][][];
    public BHypothesis topOpHypo;
    public int topLeftBoundSktID;
    public int topRightBoundSktID;
    public int topHypoID;
    public BHypothesis goldHypo;
    public int goldLeftBoundSktID;
    public int goldRightBoundSktID;
}
