package edu.upenn.cis.bpos;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   edu.upenn.cis.bpos.BSocket.java

import java.util.Vector;

public class BSocket
{

    public BSocket(Vector vector)
    {
        sktLabel = vector;
    }

    public boolean equals(Object obj)
    {
        BSocket bsocket = (BSocket)obj;
        if(sktLabel.size() != bsocket.sktLabel.size())
            return false;
        for(int i = 0; i < sktLabel.size(); i++)
            if(sktLabel.get(i) != bsocket.sktLabel.get(i))
                return false;

        return true;
    }

    public int hashCode()
    {
        int i = 0;
        for(int j = 0; j < sktLabel.size(); j++)
            i = i * 64 + ((SLabel)sktLabel.get(j)).id;

        return i;
    }

    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer("<");
        for(int i = 0; i < sktLabel.size(); i++)
        {
            if(i > 0)
                stringbuffer.append(", ");
            stringbuffer.append(sktLabel.get(i));
        }

        stringbuffer.append(">");
        return stringbuffer.toString();
    }

    public Vector sktLabel;
}
