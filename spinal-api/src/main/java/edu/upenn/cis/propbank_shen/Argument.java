package edu.upenn.cis.propbank_shen;
import java.util.*;

/**
   This class represents an "argument" in a "predicate argument
   structure".  As such, on a logical/mathematical level, one
   may construe an "argument" as a projection of tuple, where
   the tuple is an element of a relation "defined" by the verb
   in question.  For example, the sentence
<p><center>
        John gave Sue a Penny.  
    </center><p>

   may be viewed as denoting, amongst other
   things, a predicate "gave" with three "arguments", as in
   gave(John,Sue,Penny).
    <p>
   In the propbank, each of these arguments is constituted by 
   up to three things (two of which arg optional):
   
   <ol>
   <li> an argument label, as defined by the class ArgLabel
   <li> an optional modifying label, as defined by the class ModLabel
   <li>an optional argument location, as defined by the class ArgLoc
   </ol>
   
   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.ArgLabel
   @see edu.upenn.cis.propbank_shen.ModLabel
   @see edu.upenn.cis.propbank_shen.ArgLoc
   
 */
public class Argument implements Comparable{

    /** the primary argument label associated with this Argument */
    public ArgLabel arg_label;
    /** the secondary label associated with this argument, or null */
    public ModLabel mod_label; // can be null
    /** the edu.upenn.cis.treebank location associated with this argument, or null */
    public ArgLoc   location;  // can be null

    /** the separator for the parts for one-line at a time representation */
    public static final String sep="-";

    /** create an Argument from just a label */
    public Argument(ArgLabel albl) {
        arg_label = albl;
        location = null;
        mod_label = null;
    }

    /** create an Argument from an argument label and a modifiying label */
    public Argument(ArgLabel albl, ModLabel mlbl) {
        arg_label = albl;
        mod_label = mlbl;
        location = null;
    }

    /** create an Argument from an argument label and a location */
    public Argument(ArgLabel albl, ArgLoc aloc) {
        arg_label = albl;
        mod_label = null;
        location = aloc;
    }

    /** create an Argument from an argument label, a modifying label, and a location */
    public Argument(ArgLabel albl, ModLabel mlbl, ArgLoc aloc) {
        arg_label = albl;
        mod_label = mlbl;
        location = aloc;
    }
    
    public int compareTo(Object o) {
	Argument arg = (Argument) o;
	int locComparison = this.location.compareTo(arg.location);
	if (locComparison != 0) return locComparison;
	String labelString = arg_label.toString();
	if (mod_label != null) labelString += mod_label.toString();
	String labelString2 = arg.arg_label.toString();
	if (arg.mod_label != null) labelString2 += arg.mod_label.toString();
	return labelString.compareTo(labelString2);
    }

    /** set the argument's location to loc
        @param loc the new argument location
    */
    public void setLocation(ArgLoc loc) { location = loc;}
    
    
    public ArgLoc getLocation() { return this.location; }
    
    /**
       set the arguments modifying label.
       @param ml  the new modifying label
    */
    public void setMod(ModLabel ml) { mod_label = ml; }
    
    /**
       set the argument's primary label.
       @param al the new primary label
     */
    public void setLabel(ArgLabel al) { arg_label = al; }
    
    /** produce a canonical string from the Argument */
    public String toString() {
        String locstr;
        if (location == null) { locstr = "?:?"; } 
        else { locstr = location.toString();  }
        
        String modstr;
        if (mod_label == null) { modstr = "";}
        else { modstr = sep + mod_label.toString(); }
        
        String astr = arg_label.toString();
        
        return locstr + sep + astr + modstr;
    }

    public boolean equals (Object o) {
        if (!(o instanceof Argument))
            return false;
        Argument a = (Argument)o;
        return 
            ((a.arg_label == arg_label) ||
             (a.arg_label != null && a.arg_label.equals(arg_label))) &&
            ((a.mod_label == mod_label) ||
             (a.mod_label != null && a.mod_label.equals(mod_label))) &&
            ((a.location == location) ||
             (a.location != null && a.location.equals(location)));
    }

//     public int hashCode () {
//         return arg_label.hashCode() + 19 * mod_label.hashCode() +
//            23 * location.hashCode();
//     }

    /**
       convert a string into an "argument".
       
       @param s  the string to be converted.
     */
    public static Argument ofString(String s) throws CorruptDataException {
        StringTokenizer tok = new StringTokenizer(s, sep);
        int nelts = tok.countTokens();
        if (nelts < 2 || nelts > 3) {
            throw new CorruptDataException("invalid argument string, too few or too many parts: " + s);
        }
        String loctok  = tok.nextToken();
        ArgLoc aloc = null;
        if (loctok == "?:?") {
            aloc = null;
        } else {
            aloc = ArgLoc.ofString(loctok);
        }
        ArgLabel albl = ArgLabel.ofString(tok.nextToken());
        if (nelts == 3) {
            ModLabel mlbl = ModLabel.ofString(tok.nextToken());
            return new Argument(albl, mlbl, aloc);
        } else {
            return new Argument(albl, aloc);
        }
    }
}

