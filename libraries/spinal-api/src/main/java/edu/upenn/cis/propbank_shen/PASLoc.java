package edu.upenn.cis.propbank_shen;

import java.io.File;

//import edu.upenn.cis.treebank.TBNode;
//import edu.upenn.cis.treebank.InvalidAddressException;
//import edu.upenn.cis.treebank.TBFind;


/**
   This class represents the location of a predicate argument structure.  It
   consists of a filename, a sentence number (starting with 0) and a terminal
   number in the sentence (starting with 0).
   
   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.PBConfig

 */
public class PASLoc {

    /** the filename of the associated PAS. */
    String path;
    /** the sentence number of the sentence, starting with 0. */
    int sentno;
    /** the terminal number in the sentence, starting with 0. */
    int termno;
    
    /** the treebank node, or null if we haven't looked it up yet */
//    protected TBNode tbnode;
    
    /**
       A constructor -- you supply the pieces, we supply the Object.
     */
    public PASLoc(String p, int sno, int tno) {
        path = p;
        sentno = sno;
        termno = tno;
  //      tbnode = null;
    }


    /**
       return the full path of the file to which this location refers.
     */
    public File getPath()
    {
        return new File(PBConfig.TreeBankDir(), path);
    }


    
//    
//    /**
//       get the treebank node associated with this predicate argument
//       structure location.
//
//       @see edu.upenn.cis.treebank.TBNode
//     */
//    public TBNode getTBNode() throws InvalidAddressException
//    {
//        if (tbnode != null) {
//            return tbnode;
//        }
//        tbnode = TBFind.nodeAt(getPath().toString(), sentno, termno);
//        return tbnode;
//    }

    /**
       construct a canonical string from the object.
     */
    public String toString() {
        return path + " " + (new Integer(sentno)).toString() + " " 
            + (new Integer(termno)).toString();
    }

    /**
       given a canonical string representing a location of 
       a predicate, return a corresponding PASLoc object.

       @param s the canonical string representation of a PAS location
     */
    public static PASLoc ofString(String s) throws CorruptDataException {
        String parts[] = s.split(" ");
        if (parts.length != 3) { 
            throw new CorruptDataException("Invalid location of a predicate argument structure: " + s);
        }
        try {
            int sn = Integer.decode(parts[1]).intValue();
            int tn = Integer.decode(parts[2]).intValue();
            return new PASLoc(parts[0], sn, tn);
        } catch (NumberFormatException e) { // XXX what's this exception?
            throw new CorruptDataException("Invalid location of a predicate argument structure: " + s);
        }
    }

    /**
     *  return true iff o is the same PASLoc as this object
     */
    public boolean equals (Object o) {
        if (!(o instanceof PASLoc))
            return false;
        PASLoc p = (PASLoc)o;
        return path.equals(p.path) && sentno == p.sentno && termno == p.termno;
    }

    /**
     * Produce a hash code for this instance
     */
    public int hashCode () {
        return this.toString().hashCode();
// TODO apply this neat trick elsewhere
    }
}

