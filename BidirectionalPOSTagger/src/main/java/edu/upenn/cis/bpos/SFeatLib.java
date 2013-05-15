package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.SFeatLib.java

import java.io.*;
import java.util.*;

public class SFeatLib
{

    public SFeatLib()
    {
        init();
    }

    public SFeatLib(SFeatLib sfeatlib)
    {
        init();
        for(int i = 0; i < sfeatlib.id2feat.size(); i++)
        {
            SFeat sfeat = (SFeat)sfeatlib.id2feat.get(i);
            id2feat.add(new SFeat(sfeat));
            feat2id.put(sfeat.featstr, new Integer(i));
        }

    }

    public void init()
    {
        feat2id = new Hashtable(FEAT_HASH_INIT);
        id2feat = new Vector(FEAT_HASH_INIT);
    }

    public void loadFeatTable(String s)
    {
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(s));
            System.err.println((new StringBuilder()).append("Open Feature Table : ").append(s).toString());
            for(String s1 = bufferedreader.readLine(); s1 != null; s1 = bufferedreader.readLine())
            {
                String as[] = s1.split(" ");
                int i = regFeat(as[as.length - 2]);
                setWeight(i, Double.parseDouble(as[as.length - 1]));
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
        id2feat.trimToSize();
        System.err.println((new StringBuilder()).append("").append(id2feat.size()).append(" features loaded").toString());
    }

    private void setWeight(int i, double d)
    {
        SFeat sfeat = (SFeat)id2feat.get(i);
        sfeat.weight = d;
    }

    public void regFeat(Vector vector)
    {
        for(int i = 0; i < vector.size(); i++)
            regFeat((String)vector.get(i));

    }

    public int regFeat(String s)
    {
        Integer integer = (Integer)feat2id.get(s);
        if(integer != null)
        {
            int i = integer.intValue();
            ((SFeat)id2feat.get(i)).freq++;
            return i;
        } else
        {
            Integer integer1 = new Integer(id2feat.size());
            feat2id.put(s, integer1);
            SFeat sfeat = new SFeat(s);
            sfeat.freq = 1;
            id2feat.add(sfeat);
            return integer1.intValue();
        }
    }

    private int getFeatID(String s)
    {
        Integer integer = (Integer)feat2id.get(s);
        if(integer != null)
            return integer.intValue();
        else
            return -1;
    }

    public double getWeight(String s)
    {
        int i = getFeatID(s);
        if(i == -1)
            return 0.0D;
        else
            return ((SFeat)id2feat.get(i)).weight;
    }

    public double getScore(Vector vector)
    {
        double d = 0.0D;
        for(int i = 0; i < vector.size(); i++)
        {
            int j = getFeatID((String)vector.get(i));
            if(j != -1)
                d += ((SFeat)id2feat.get(j)).weight;
        }

        return d;
    }

    public double getVotedScore(Vector vector, int i)
    {
        double d = 0.0D;
        for(int j = 0; j < vector.size(); j++)
        {
            int k = getFeatID((String)vector.get(j));
            if(k != -1)
            {
                SFeat sfeat = (SFeat)id2feat.get(k);
                d += sfeat.updateCmlwt(i);
            }
        }

        return d;
    }

    public void updateFeat(Hashtable hashtable, double d, int i)
    {
        Enumeration enumeration = hashtable.keys();
        do
        {
            if(!enumeration.hasMoreElements())
                break;
            String s = (String)enumeration.nextElement();
            int j = ((Integer)hashtable.get(s)).intValue();
            if(j != 0)
            {
                int k = getFeatID(s);
                if(k == -1)
                    k = regFeat(s);
                SFeat sfeat = (SFeat)id2feat.get(k);
                sfeat.updateCmlwt(i);
                sfeat.weight += (double)j * d;
            }
        } while(true);
    }

    public void updateFeat(Vector vector, double d, int i)
    {
        if(d == 0.0D)
            System.err.println("*** ZERO UPDATING***");
        for(int j = 0; j < vector.size(); j++)
        {
            String s = (String)vector.get(j);
            int k = getFeatID(s);
            if(k == -1)
                k = regFeat(s);
            SFeat sfeat = (SFeat)id2feat.get(k);
            sfeat.updateCmlwt(i);
            sfeat.weight += d;
        }

    }

    public void listWeight()
    {
        System.err.println("list weights :");
        for(int i = 0; i < id2feat.size(); i++)
            System.err.println((new StringBuilder()).append("").append(i).append(" ").append(((SFeat)id2feat.get(i)).featstr).append(" ").append(((SFeat)id2feat.get(i)).weight).toString());

    }

    public void saveWeight(String s, int i)
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileOutputStream(s));
            for(int j = 0; j < id2feat.size(); j++)
                printwriter.println((new StringBuilder()).append("").append(j).append(" ").append(((SFeat)id2feat.get(j)).featstr).append(" ").append(((SFeat)id2feat.get(j)).updateCmlwt(i)).toString());

            printwriter.close();
        }
        catch(FileNotFoundException filenotfoundexception)
        {
            System.err.println(filenotfoundexception.toString());
        }
    }

    public void updateCmlwt(int i)
    {
        for(int j = 0; j < id2feat.size(); j++)
            ((SFeat)id2feat.get(j)).updateCmlwt(i);

    }

    public void useVotedFeat(int i)
    {
        for(int j = 0; j < id2feat.size(); j++)
        {
            SFeat sfeat = (SFeat)id2feat.get(j);
            sfeat.updateCmlwt(i);
            sfeat.weight = sfeat.cmlwt;
        }

    }

    private static int FEAT_HASH_INIT = 0xf4240;
    public Hashtable feat2id;
    public Vector id2feat;

}
