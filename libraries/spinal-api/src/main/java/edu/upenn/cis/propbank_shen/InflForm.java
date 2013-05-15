package edu.upenn.cis.propbank_shen;

import java.io.*;

/**
   This represents the overall "form" of the inflection.  We note
   whether each instance is an Infinitive, a gerund, a participle,
   or in regular (finite, full) form.

   <p>
   Sometimes, no form is specified because this annotation isn't 
   very complete, so we have NoForm as well.

   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.Inflection
 */
public final class InflForm {

    private String srep;

    /** use a private constructor so that only static members
        can be referenced.  This helps us emulate an enumeration */
    private InflForm(String s) {srep = s;}
    
    public static final InflForm Infinitive = new InflForm("i");
    public static final InflForm Gerund = new InflForm("g");
    public static final InflForm Participle = new InflForm("p");
    public static final InflForm Full = new InflForm("v");
    public static final InflForm NoForm = new InflForm("-");

    public String toString() { return srep; }

    /** 
        return the "form" inflectional information from a string.
        If the string doesn't make sense, print an error and 
        return the default "NoForm".

        @return an InflForm instance
    */
    public static InflForm ofString(String s) 
    {
        if (s.equals("i")) { return InflForm.Infinitive; }
        else if (s.equals("g")) { return InflForm.Gerund; }
        else if (s.equals("p")) { return InflForm.Participle; }
        else if (s.equals("v")) { return InflForm.Full; }
        else if (s.equals("-")) { return InflForm.NoForm; }
        else {
            System.err.println("invalid string for inflection.form: "
                               + s
                               + ", defaulting to NoForm");
            return InflForm.NoForm;
        }
    }
}
