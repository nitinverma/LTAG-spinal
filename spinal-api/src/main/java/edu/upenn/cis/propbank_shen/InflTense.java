package edu.upenn.cis.propbank_shen;

import java.io.*;

/**
   We represent the future, past, and present tenses with the 
   propbank inflectional information.

   <p>

   Sometimes, no tense is specified because this annotation isn't 
   very complete, so we have NoTense as well.

   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.Inflection

*/
public final class InflTense {

    private String srep;
    /** use a private constructor so that only static members
        can be referenced.  This helps us emulate an enumeration */
    private InflTense(String s) {srep = s;}

    /** the future tense */
    public static final InflTense Future = new InflTense("f");
    /** the past tense */
    public static final InflTense Past = new InflTense("p");
    /** the present tense */
    public static final InflTense Present = new InflTense("n");
    /** no tense specified */
    public static final InflTense NoTense = new InflTense("-");

    /** create a canonical string representation of the InflTense instance */
    public String toString() { return srep; }
    /** return an InflTense instance from a canonical string.
        print an error on stderr if the string doesn't make sense, and
        use the default "NoTense" in that case"
    */
    public static InflTense ofString(String s) 
    {
        if (s.equals("f")) { return InflTense.Future; }
        else if (s.equals("p")) { return InflTense.Past; }
        else if (s.equals("n")) { return InflTense.Present; }
        else if (s.equals("-")) { return InflTense.NoTense; }
        else {
            System.err.println("invalid inflection.tense string: " + s);
            return InflTense.NoTense;
        }
            
    }
}
