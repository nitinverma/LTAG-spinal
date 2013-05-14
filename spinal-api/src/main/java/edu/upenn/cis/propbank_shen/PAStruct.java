package edu.upenn.cis.propbank_shen;

import java.util.*;

/**
 *
 * A class representing an instance of predicate argument structure.
 *
 * @author Scott Cotton
 * @see edu.upenn.cis.propbank_shen.Argument
*/
public class PAStruct {
    
    /** 
     * the arguments associated with the structure, including the verb itself 
     */
    protected List arguments;
    /** the predicate itself, i.e., the root form of the verb in question */
    protected String lemma;
   
    /** construct a PAStruct object from the lemma */
    public PAStruct(String lem) {
        lemma = lem;
        arguments = new ArrayList();
    }
    
    // getters
    /**
     * return the lemma, eg the root form of the associated verb.
     */
    public String getLemma() { return lemma; }
    /** 
     *  return a list of arguments, including the verb itself
     * @see edu.upenn.cis.propbank_shen.Argument
     */
    public List getArgs() { return arguments; }
    
    /* 
       some wrapper functions around the list functionality
       
       XXX should we go ahead and implement the enumeration 
       interface?
    */

    /**
     *  Add a single argument to the arguments associated with this PA structures.
     */
    public void addArg(Argument a) {
        arguments.add((Object) a);
	Collections.sort(arguments);
    }
    
    /**
     * get the nth argument
     *
     * @param n the n'th argument
     */
    public Argument nthArg(int n) {
        return (Argument) arguments.get(n);
    }
   
    /**
     * remove the argument at position i.
     */
    public void removeArg(int i) {
        arguments.remove(i);
    }
   
    /**
     * remove the argument that is the same as a
     * @param a  argument to be removed
     */
    public void removeArg(Argument a) {
        arguments.remove(a);
    }
    
    /**
     * The number of arguments
     */
    public int size() {
        return arguments.size();
    }
    
    /*
      to/from storage
     */
    /**
     * convert the predicate argument structure to a canonical string.
     */
    public String toString() {
        String res = "";
        if (arguments.size() > 0)
            res += arguments.get(0).toString();
        for(int i=1; i<arguments.size(); i++) {
            res += " " + arguments.get(i);
        }
        return res;
    }
    
    /**
     * create a predicate argument structure from a canonical string
     *
     * @param s the canonical string of the argument.
     * @throws CorruptDataException
     */
    public static PAStruct ofString(String s) throws CorruptDataException {
        StringTokenizer stok = new StringTokenizer(s);
        int nelts = stok.countTokens();
        if (nelts < 2) {
            throw new CorruptDataException("invalid PAStruct string: " + s);
        }
        String lem = stok.nextToken();
        PAStruct pas = new PAStruct(lem);
        while (stok.hasMoreTokens()) {
            Argument arg = Argument.ofString(stok.nextToken());
            pas.addArg(arg);
        }
        return pas;
    }
    /**
     * Checks whether the PAstructs are equivalent.  Ignores the order in which the arguments are stored.
     */
    public boolean equals (Object o) {
        if (!(o instanceof PAStruct))
            return false;
        PAStruct p = (PAStruct)o;

        return this.lemma.equals(p.lemma) 
	    && (this.arguments).equals(p.arguments);
    }

    public int hashCode () {
        return lemma.hashCode() + 13 * arguments.hashCode();
    }
}
