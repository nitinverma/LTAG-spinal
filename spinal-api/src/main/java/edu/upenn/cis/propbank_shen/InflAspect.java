
package edu.upenn.cis.propbank_shen;

import java.io.*;

/**
   the "aspect" part of inflectional information.  we represent aspect 
   as either "perfect", "progressive" or "perfect progressive". 

   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.Inflection
 */
public final class InflAspect {

    private String srep;
    /** use a private constructor so that only static members
        can be referenced.  This helps us emulate an enumeration */
    private InflAspect(String s) {srep = s;}


    /** the Perfect voice */
    public static final InflAspect Perfect = new InflAspect("p");
    /** the progressive voice */
    public static final InflAspect Progressive = new InflAspect("o");
    /** the perfect progressive voice */
    public static final InflAspect Both = new InflAspect("b");
    /** no aspect specified */
    public static final InflAspect NoAspect = new InflAspect("-");

    public String toString() { return srep; }
    public static InflAspect ofString(String s) 
    {
        if (s.equals("p")) { return InflAspect.Perfect; }
        else if (s.equals("o")) { return InflAspect.Progressive; }
        else if (s.equals("b")) { return InflAspect.Both; }
        else if (s.equals("-")) { return InflAspect.NoAspect; }
        else {
            System.err.println("invalid inflection.aspect string: "
                               + s
                               + ", defaulting to NoAspect.");
            return InflAspect.NoAspect;
        }
    }
}
    
