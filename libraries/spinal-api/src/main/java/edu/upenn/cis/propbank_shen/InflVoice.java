package edu.upenn.cis.propbank_shen;

import java.io.*;

/**
   This class represents the voice part of the inflection of
   a verb.  A voice is either active or passive or "NoVoice"
   in the case that it just hasn't been specified or doesn't
   make sense, such as when the form is infinitival.

   @author Scott Cotton
   @see Inflection
 */
public final class InflVoice {

    private String srep;
    /** use a private constructor so that only static members
        can be referenced.  This helps us emulate an enumeration */
    private InflVoice(String s) {srep = s;}

    /** the active inflection */
    public static final InflVoice Active = new InflVoice("a");
    /** the passive inflection */
    public static final InflVoice Passive = new InflVoice("p");
    /** none specified */
    public static final InflVoice NoVoice = new InflVoice("-");

    /** convert the voide to a string */
    public String toString() { return srep; }

    /** convert a string to a InflVoice instance */
    public static InflVoice ofString(String s) 
    {
        if (s.equals("a")) { return InflVoice.Active; }
        else if (s.equals("p")) { return InflVoice.Passive; }
        else if (s.equals("-")) { return InflVoice.NoVoice; }
        else {
            System.err.println("invalid string for inflection.voice: "
                               + s
                               + ", defaulting to NoVoice.");
            return InflVoice.NoVoice;
        }
    }
}
