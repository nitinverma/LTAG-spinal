package edu.upenn.cis.propbank_shen;

import java.io.*;

/**
   The "person" part of the inflectional information for a predicate argument
   structure.  We mark the person as "3rd" person in the case that the 3rd
   person marker is present morphologically (Such as "goes"). Otherwise, the
   argument structure is marked with "NoPerson".

   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.Inflection
 */
public final class InflPerson {
    private String srep;
    /** use a private constructor so that only static members
        can be referenced.  This helps us emulate an enumeration */
    private InflPerson(String s) {srep = s;}

    /** the third person inflection */
    public static final InflPerson Third = new InflPerson("3");
    /** no inflection noted */
    public static final InflPerson NoPerson = new InflPerson("-");

    /** create a string representation of the person inflectional info. */
    public String toString() { return srep; }
    /** 
        return the person inflectional information from a string.
        If the string doesn't make sense, print an error and 
        return the default "NoPerson".

        @return an InflPerson instance
    */
    public static InflPerson ofString(String s) 
    {
        if (s.equals("3")) { return InflPerson.Third; }
        else if (s.equals("-")) { return InflPerson.NoPerson; }
        else {
            System.err.println("invalid inflection.person string: " 
                               + s 
                               + ", defaulting to NoPerson");
            return InflPerson.NoPerson;
        }
    }
}
