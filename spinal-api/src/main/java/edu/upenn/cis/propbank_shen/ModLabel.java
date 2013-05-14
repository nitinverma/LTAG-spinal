
package edu.upenn.cis.propbank_shen;

/**
   A class emulating an enumeration of labels representing 
   "modifiers" in the propbank.  Even though there is obviously
   a vast amount of overlap between the denotation of the labels,
   a single modifier is chosen as the appropriate annotation
   if it is deemed "the most" appropriate.  I don't think this
   distinction is at all well defined, but it is common practice
   in this field and what the linguists want.

    <ul>
    <li> EXT  extent
    <li> LOC  location
    <li> DIR  direction
    <li> NEG  negation  (not in PREDITOR)
    <li> MOD  general modification
    <li> ADV  adverbial modification
    <li> MNR  manner
    <li> PRD  secondary predication
    <li> REC  recipricol (eg herself, etc)
    <li> TMP  temporal
    <li> PRP  purpose - deprecated !!!
    <li> PNC  purpose no cause
    <li> CAU  cause
    <li> STR  stranded
    </ul>
   @author Scott Cotton
*/


public final class ModLabel {

    private String label_name;
    private String label_descr;

    // private constructor
    private ModLabel(String nm, String descr) {
        label_name = nm;
        label_descr = descr;
    
    }

    public final ModLabel prepMod(String prep) {
        return new ModLabel(prep, "preposition");
    }

    /**
       Convert a ModLabel to a string
     */
    public final String toString() { return label_name; }
    
    /**
       Given a string, return the associated ModLabel

       @param s the string to be converted to a ModLabel
     */
    public static final ModLabel ofString(String s) {
        if (s.equals("EXT")) {  return ModLabel.EXT; }
        else if (s.equals("LOC")) { return ModLabel.LOC; }
        else if (s.equals("MOD")) { return ModLabel.MOD; }
        else if (s.equals("ADV")) { return ModLabel.ADV; }
        else if (s.equals("MNR")) { return ModLabel.MNR; }
        else if (s.equals("PRD")) { return ModLabel.PRD; }
        else if (s.equals("REC")) { return ModLabel.REC; }
        else if (s.equals("TMP")) { return ModLabel.TMP; }
        else if (s.equals("PRP")) { return ModLabel.PRP; }
        else if (s.equals("PNC")) { return ModLabel.PNC; }
        else if (s.equals("CAU")) { return ModLabel.CAU; }
        else if (s.equals("STR")) { return ModLabel.STR; }
        else {
            return new ModLabel(s, "preposition");
        }
    }

    public boolean equals (Object label) {
        if (this == label)
            return true;
        if (!(label instanceof ModLabel))
            return false;
        ModLabel m = (ModLabel)label;
        return label_name.equals(m.label_name) && 
            label_descr.equals(m.label_descr);
    }

//     public int hashCode () {
//         return 7 * label_name.hashCode() + label_descr.hashCode();
//     }

    /**
       Return a brief english description of the associated label
     */
    public final String getDescription() { return label_descr; }


    public static final ModLabel EXT = new ModLabel("EXT", "extent");
    public static final ModLabel LOC = new ModLabel("LOC", "location");
    public static final ModLabel DIR = new ModLabel("DIR", "direction");
    public static final ModLabel MOD = new ModLabel("MOD", "modal");
    public static final ModLabel ADV = new ModLabel("ADV", "adverbial");
    public static final ModLabel MNR = new ModLabel("MNR", "manner");
    public static final ModLabel PRD = new ModLabel("PRD", "secondary predication");
    public static final ModLabel REC = new ModLabel("REC", "recipricol");
    public static final ModLabel TMP = new ModLabel("TMP", "temporal");
    public static final ModLabel PRP = new ModLabel("PRP", "purpose (deprecated)");
    public static final ModLabel PNC = new ModLabel("PNC", "purpose, not the cause");
    public static final ModLabel CAU = new ModLabel("CAU", "cause");
    public static final ModLabel STR = new ModLabel("STR", "stranded");
}
