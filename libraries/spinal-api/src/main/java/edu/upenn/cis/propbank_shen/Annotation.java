package edu.upenn.cis.propbank_shen;

import java.io.*;
import java.util.*;

//import edu.upenn.cis.treebank.TBNode;
//import edu.upenn.cis.treebank.InvalidAddressException;

import edu.upenn.cis.spinal.*;


/**
   This class represents an annotation in the propbank.  An annotation
   represents a predicate argument structure together with an optional 
   roleset done by some annotator (usually a person) and with some
   inflectional/morphological information.
  
   @author Scott Cotton

   @see edu.upenn.cis.propbank_shen.PAStruct
   @see edu.upenn.cis.propbank_shen.PASLoc
   @see edu.upenn.cis.propbank_shen.Inflection
   @see edu.upenn.cis.propbank_shen.RoleSet
   @see edu.upenn.cis.propbank_shen.Argument
   
 */

public class Annotation {

    /** the predicate argument structure */
    protected PAStruct pas;
    /** the location in the treebank */
    protected PASLoc pasloc;
    /** the annotator who is responsible. */
    protected String annotator;
    /** the inflectional information */
    protected Inflection inflection;
    /** the roleset identifier */
    protected String rolesetid;
    /** the roleset, if identified or null otherwise */
    protected RoleSet roleset;
    
    /** 
     * construct an Annotation from a line of text.
     *
     * Here is an example line of text for input to the constructor.
     * <pre>
     * wsj/00/wsj_0002.mrg 0 16 gold name.01 ----- 16_16-rel 0_14*17_17-ARG1 18_25-ARG2
     * </pre>
     *
     * The first field is the relative path of the Wall Street Journal corpus
     * file.  The second field is the sentence number.  The third field is
     * the number of the terminal (treebank leaf) representing the annotated
     * verb.  The fourth field is the annotator name. 
     * (In Libin Shen's version of the Propbank, this is always either 
     * <code>gold</code> or <code>mimic</code>, depending on whether the origin
     * is the actual Propbank or Libin's automatic annotation for the verb "be".)
     * The fifth field is the
     * roleset identifier (with .XX indicating this identifier is incomplete 
     * and only refers to the verb, not to a particular roleset
     * associated with that verb). The fifth field describes the verb's inflection
     * in the original Propbank, but is left blank (<code>-----</code>) in Libin's
     * versoin.
     * The remaining fields describe the predicate-argument structure. The main 
     * difference to the original Propbank here is that locations are indicated
     * as word spans rather than as nodes in the Penn Treebank annotation. 
     */
    public Annotation (String ln)
        throws CorruptDataException
    {
        String parts[] = ln.trim().split(" ");
        if (parts.length < 7) {
            throw new CorruptDataException("invalid annotation line: " + ln);
        }
        pasloc = PASLoc.ofString(parts[0] + " " + parts[1] + " " +  parts[2]);
        annotator = parts[3];
        rolesetid = parts[4];
        inflection = new Inflection(parts[5]);
        int dotidx = rolesetid.indexOf(".");
        if (dotidx == -1) {
            throw new CorruptDataException("invalid annotation line (bad roleset): " + ln);
        }
        String lemma = rolesetid.substring(0, dotidx);
        pas = new PAStruct(lemma);
        for (int i=6; i < parts.length; i++) {
            pas.addArg(Argument.ofString(parts[i]));
        }
    }

    public Annotation (PASLoc loc, String annotator, RoleSet roleset,
                       Inflection inflection, PAStruct structure) {
        this(loc, annotator, roleset.getId(), inflection, structure);
        this.roleset = roleset;
    }
    
    public Annotation (PASLoc loc, String annotator, String rolesetId,
                       Inflection inflection, PAStruct structure) {
        this.pasloc = loc;
        this.annotator = annotator;
        this.rolesetid = rolesetId;
        this.inflection = inflection;
        this.pas = structure;
    }

    /**
     * Return a canonical string representation of this class, suitable for
     * passing to the class's constructor.
     */
    public String toString ()
    {
        return (pasloc.toString() + " " 
                + annotator + " " 
                + rolesetid + " " 
                + inflection.toString() + " "
                + pas.toString());
    }
    
    /**
     * Return the predicate argument structure portion of the annotation.
     * @see edu.upenn.cis.propbank_shen.PAStruct
     */
    public PAStruct getPAStruct()
    {
        return pas;
    }
    
    /**
     * Return the predicate argument structure location.
     * @see edu.upenn.cis.propbank_shen.PASLoc
     */
    public PASLoc getPASLoc()
    {
        return pasloc;
    }
    
    private Argument relation = null;
    
    
//    // convenience method -- possibly null if none can be found
//    public ElemTree getRelationSubtree(Sentence s) {
//        return this.getRelation().getLocation().getDominatingNode(s);
//    }
    
    public Argument getRelation() {
        if (this.relation != null) return this.relation;
        Iterator args = this.getPAStruct().getArgs().iterator();
        
        while (args.hasNext()) {
            Argument current = (Argument) args.next();
            if (current.arg_label.isRel()) {
                this.relation = current;
                return current;
            }
        }
        // shouldn't get here because every Annotation must have a relation
        assert false;
        throw new IllegalStateException("Bad annotation -- doesn't have a relation: "
                + this.toString());
        
    }
    

    
    /**
     * Return the inflection part of the annotation.
     * @see edu.upenn.cis.propbank_shen.Inflection
     */
    public Inflection getInflection()
    {
        return inflection;
    }
    
    /**
     * Return the RoleSet of the annotation, if disambiguated, otherwise
     * return <code>null</code>.
     * @see edu.upenn.cis.propbank_shen.RoleSet
     * @throws edu.upenn.cis.propbank_shen.CorruptDataException if Roleset xml file is bad
     */
    public RoleSet getRoleSet() throws CorruptDataException
    {
        if (roleset != null) {
            return roleset;
        }
        if (rolesetid.endsWith(".XX")) {
            return null;
        } else {
            roleset = RoleSet.ofId(rolesetid);
            return roleset;
        }
    }

    /**
     * Return the ID of the roleset, such as run.02 or find.XX.
     */
    public String getRoleSetId () {
  return rolesetid;
    }
    
    /**
     * Return the lemma of the annotation
     */
    public String getLemma()
    {
        return pas.getLemma();
    }

    /** 
     * Return the annotator
     */
    public String getAnnotator()
    {
        return annotator;
    }

//    /**
//     * Return the edu.upenn.cis.treebank node associated with this 
//     * annotation -- the node representing the main predicate.
//     * @see edu.upenn.cis.treebank.TBNode
//     */
//    public TBNode getTBNode() throws InvalidAddressException
//    {
//        return pasloc.getTBNode();
//    }

    /**  a unit test */
    public static void main(String args[]) 
        throws IOException
        //, InvalidAddressException
    {
        if (args.length != 1) {
            System.err.println("error: give me a file like prop-all.idx!");
            System.exit(1);
        }
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        Annotation a;
        while (true) {
            String ln = in.readLine();
            if (ln == null) { break; }
            try {
                a = new Annotation(ln);
            //    TBNode tbn = a.getTBNode();
                PAStruct pas = a.getPAStruct();
                Iterator argiter = pas.getArgs().iterator();
                while (argiter.hasNext()) {
                    Argument arg = (Argument) argiter.next();
                    if (arg.location.loc_type == ArgLoc.EQUIVA) {
              //        arg.location.sortMotion(tbn);
                      System.out.println(a.getPASLoc() + " " + arg.location);
                    }
                }
            } catch (CorruptDataException cd) {
                System.err.println(cd);
            }
        }
    }
}
