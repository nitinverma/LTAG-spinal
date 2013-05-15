package edu.upenn.cis.propbank_shen;


// these are done sortof like enumerations, ick.
/** 
    An Argument Label represents the information ascribed to an argument in 
   a predicate-argument structure.  A Basic Argument label just ascribes
   a number, such as Arg0, Arg1, .... There is also ArgM for a modifying
   argument, and ArgA for a causative agent.

   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.PAStruct
*/
public final class ArgLabel {
    
    private String name;
    private int number;
    public static String prefix = "ARG";

    /**
       This private constructor is creates numbered arguments

       @param nm the name of the label, eg "Arg"
       @param num the number of the label.
    */
    private ArgLabel(int num)  {
        name = prefix;
        number=num;
    }

    /**
       This private constructor creates non-numbered arguments
       
       @param nm the name of the label, eg "ArgM"
     */
    private ArgLabel(String nm) {
        name = prefix + nm;
        number = -1;
    }
    


    /**
       create a string representation of the argument label.
     */
    public String toString() {  
        String result;
        if (name.endsWith("REL")) {
            return "rel";
        }
        if (name.endsWith("TBERR")) {
            return "TBERR";
        }
        if (number < 0) {
            return name;
        } else {
            Integer i = new Integer(number);
            return name + i.toString();
        }
    }

    /**
       create an argument from a string of the form
       "Arg<bf>X</bf>" where <bf>X</bf> is either "M", "A", or an integer.
       
       @param s the string from which the resulting arg label is made
     */
    public static ArgLabel ofString(String s) throws CorruptDataException {
        if (s.toUpperCase().equals("REL")) {
            return ArgLabel.REL;
        }
        if (s.toUpperCase().equals("TBERR")) {
            return ArgLabel.TBERR;
        }
        if (!s.toUpperCase().startsWith("ARG")) {
            throw new CorruptDataException("invalid argument label: " + s);
        }
        if (s.length() == 4) {
            if (s.toUpperCase().endsWith("A")) {
                return ArgLabel.ARGA;
            }
            else if (s.toUpperCase().endsWith("M")) {
                return ArgLabel.ARGM;
            }
        }
        try {
            Integer n = Integer.decode(s.substring(3, s.length()));
            return numberedLabels[n.intValue()];
        } catch (Exception e) {
            throw new CorruptDataException("invalid argument label: " + s);
        }
    }
    
    public boolean isRel() {
        return this.equals(REL);
    }
    
    public boolean isArgM() {
        return this.equals(ARGM);
    }
    
    public boolean isArgA() {
        return this.equals(ARGA);
    }
    
    public boolean isNumbered() {
        return number != -1;
    }
    
    public boolean isArgument() {
        return this.isNumbered() || this.isArgA();
    }    
    

    
    /** get the numbner associated with the label, if
        the number is meaningless, return -1 */
    public int getNum() { return number; }


    /**
       get the name associated with the label
       all labels have names.
    */
    public String getName() { return name; }

    /** Modifying argument -- an adjunct in the argument/adjuct "distinction"*/
    public static final ArgLabel ARGM=new ArgLabel("M");
    /** Causative agent.  A Causative agent is an extra agent, such as
     * the general in the sentence 
     *  <center>
     *  The general marches the soldiers around the barracks.
     *  </center>
     * */
    public static final ArgLabel ARGA=new ArgLabel("A");

    /**
       this label is used for the predicating verb.  It is present
       not so much as an argument as to provide a slot in which
       to identify the location of the verb, which can be tricky
       when there are phrasals and what not.
     */
    public static final ArgLabel REL=new ArgLabel("REL");

    /**
       This label is used by our annotators whenever they feel the
       predicate argument structure cannot be annotated correctly
       due to an error in the treebank.
     */
    public static final ArgLabel TBERR = new ArgLabel("TBERR");

    /**
     * Numbered arguments
     * */
    public static final ArgLabel ARG0=new ArgLabel(0);
    public static final ArgLabel ARG1=new ArgLabel(1);
    public static final ArgLabel ARG2=new ArgLabel(2);
    public static final ArgLabel ARG3=new ArgLabel(3);
    public static final ArgLabel ARG4=new ArgLabel(4);
    public static final ArgLabel ARG5=new ArgLabel(5);
    public static final ArgLabel ARG6=new ArgLabel(6);
    public static final ArgLabel ARG7=new ArgLabel(7);
    public static final ArgLabel ARG8=new ArgLabel(8);
    public static final ArgLabel ARG9=new ArgLabel(9);

    private static ArgLabel[] numberedLabels = {ARG0, ARG1, ARG2, ARG3, ARG4, 
                                                ARG5, ARG6, ARG7, ARG8, ARG9};

    public boolean equals (Object o) {
        if (!(o instanceof ArgLabel))
            return false;
        ArgLabel al = (ArgLabel)o;
        return al.number == number && al.name.equals(name);
    }
    
//     public int hashCode () {
//         return number + name.hashCode();
//     }
}
