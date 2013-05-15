package edu.upenn.cis.propbank_shen;

import java.util.*;
//import edu.upenn.cis.treebank.TBNode;
//import edu.upenn.cis.treebank.LabelMatcher;

import edu.upenn.cis.spinal.*;


/**
 *   This class represents a location of an argument in the text.
 *   <p>
 *   Basically, locations just represent spans in the text, but there
 *   are some complications.  For example, sometimes arguments are associated
 *   with more than one span, such as the "utterance" argument
 *   of the verb "say" in the sentence "'I'm going home', John said, 'before 10PM'."
 *   <p>
 *   Here, 
 *   "I'm going home ... before 10PM" is not a continuous span,
 *   so the <code>ArgLoc</code> will consist of two spans.
 *   <p>
 *   The second situation is for resolving empty constituents and sentence
 *   local anaphora.  We consider the sentence "John is going to swim".  The
 *   syntactic representation of this sentence adds a pseudo subject to the
 *   infinitive "to swim", with a pointer to "John".  However,
 *   since WH-movement was not explicitly marked in the Penn treebank, we have marked
 *   this situation as well in the form of declaring that a set of nodes 
 *   have equivalent semantic content.   For example, in the sentence 
 *    <center>
 *       "The man who swam the mile died"
 *   </center>
 *    Our treebank would declare that "who" is the subject of "swam", but not
 *    that "who" refered to the same thing as "the man".  So we mark all the nodes,
 *    the node containing "who", the node containing "the man", and the associated
 *    empty constituent as nodes which denote the same thing.
 *    <p>
 * 
 *    Thus in short, we may view the data structure for a location in the text
 *    associated with an argument in a predicate argument structure as exactly one
 *    of the following three cases:
 *    <ol>
 *        <li> a singleton span
 *        <li> a set of spans arranged together
 *        <li>a set of spans referring to the same semantic entity.
 *    </ol>
 * @author Scott Cotton
 * @see WordSpan
 */
public class ArgLoc implements Comparable {

    public static final int SINGLE=0;
    public static final int CONCAT=1 << 0;
    public static final int EQUIVA=1 << 1;

    protected int loc_type;
    protected List locs;
    protected WordSpan ta;

    /** construct an argument location from a basic argument location */
    public ArgLoc(WordSpan ta) {
        loc_type=SINGLE;
        locs = null;
        this.ta = ta;
    }
    
    /** 
	construct an argument location from a loc_type and a list
        of other ArgLoc objects. List must contain at least two
	elements, and ltype must be CONCAT or EQUIVA.
    */
    public ArgLoc(int ltype, List alocs) {
	if (alocs.size() < 2) 
	    throw new IllegalArgumentException("Illegal call to ArgLoc constructor: "+
					       "length of list muse be >= 2");
	Iterator iter = alocs.iterator();
	while (iter.hasNext()) { 
	    if (!(iter.next() instanceof ArgLoc))
		throw new IllegalArgumentException("Illegal call to ArgLoc constructor: "+
						   "list must only contain ArgLoc objects");
	}
	if (ltype!=CONCAT && ltype!=EQUIVA)
	    throw new IllegalArgumentException("Illegal call to ArgLoc constructor: "+
					       "ltype must be one of EQUIV or CONCAT");
	loc_type = ltype;
	//Collections.sort(alocs);
	locs = alocs;
	ta = null;
    }

    /** return true if this location consists of a single word span. */
    public boolean isSingle() { return loc_type == SINGLE;  }

    /** return true if this location consists of several word spans. */
    public boolean isConcat() { return loc_type == CONCAT; }

    /** return true if this location consists of a trace chain */
    public boolean isTraceChain() { return loc_type == EQUIVA; }

    
    
    /**
     * Returns the unique <code>WordSpan</code> if this location consists of just one span.
     * Otherwise, returns null.
     */
    public WordSpan getWordSpan() { return ta; }

    /**
     * Returns a list of other ArgLoc objects if this location consists of multiple nodes.
     * Otherwise, return null.
     */
    public List getLocList() { return locs; }

    /** 
     * return a canoncial string for a location type: the empty string
     * for SINGLE, a comma for CONCAT, an asterisk for EQUIVA. These strings
     * are used as separators in the representation of word spans.
     */
    public String locTypeToString() {
        switch (loc_type) {
        case SINGLE: return "";
        case CONCAT: return ",";
        case EQUIVA: return "*";
        }
        return "compiler needs this return statement because it is stupid.";
    }

    /** create a loc_type from a canoncial string or throw CorruptDataException if
        this is an invalid string */
    public static int locTypeOfstring(String s) throws CorruptDataException {
        if (s == ",") { return ArgLoc.CONCAT; }
        if (s == "*") { return ArgLoc.EQUIVA; }
        throw new CorruptDataException("invalid string for location type of an argument: " + s);
    }

    /**
       Compares argument locations.
       We compare by the earliest node anywhere in the (possible nested) structure of the ArgLoc.
    */
    public int compareTo(Object o) {
        if (!(o instanceof ArgLoc)) {
            throw new IllegalArgumentException("invalid call to ArgLoc.compareTo(), object not" +
                                               " an ArgLoc instance.");
        }
        ArgLoc al = (ArgLoc) o;
	
	List alAddresses = al.getAllWordSpans();
	List thisAddresses = this.getAllWordSpans();
	
	Collections.sort(alAddresses);
	Collections.sort(thisAddresses);
	WordSpan thisFirst = (WordSpan) thisAddresses.get(0);
	WordSpan alFirst = (WordSpan) alAddresses.get(0);
	int firstComparison =  thisFirst.compareTo(alFirst);
	if (firstComparison != 0) return firstComparison;
	if (this.equals(al))
	    return 0;
	else
	    return this.toString().compareTo(al.toString());
    }
    
    /**
     * returns a flat list of all WordSpans contained somewhere 
     * (possibly deeply nested) in this ArgLoc
     */
    public List getAllWordSpans() {
	List res = new LinkedList();
	if (isSingle()) 
	    res.add(ta);
	else 
	    for (int i=0; i < locs.size(); i++) 
		res.addAll(((ArgLoc)locs.get(i)).getAllWordSpans());
	
	return res;
    }

    /**
       create a string representation of the argument location.
     */
    public String toString() {
	if (isSingle()) return ta.toString();  //base case
	
	String lts = locTypeToString();
	ArgLoc fst = (ArgLoc) locs.get(0);
	String result = fst.toString();
	for (int i=1; i < locs.size(); i++) {
	    ArgLoc loc = (ArgLoc) locs.get(i);
	    result += lts + loc.toString();    //recursive call to elements in list
	}
	return result;
    }
    
    /**
       convert a string to an ArgLoc if possible, otherwise throw
       CorruptDataException.

       @param s  the string to be converted.
     */
    public static ArgLoc ofString(String s) throws CorruptDataException {
	//Note from Ben:  
	// Since we are checking for the presence of '*' first, this means that
	// a string consisting of both '*' and ',' (e.g. 4:5*2:3,1:0) will be considered
	// a trace chain (one of whose elements is a split arg).  In other words,
	// the ',' operator has precedence over the '*' operator.

        int idx;
        idx = s.indexOf('*');
        if (idx != -1) {
            StringTokenizer tok = new StringTokenizer(s, "*");
            LinkedList l = new LinkedList();
            while (tok.hasMoreElements()) {
                ArgLoc loc = ofString(tok.nextToken());
                l.add(loc);
            }
            return new ArgLoc(ArgLoc.EQUIVA, l);
        }
        idx = s.indexOf(',');
        if (idx != -1) {
            StringTokenizer tok = new StringTokenizer(s, ",");
            LinkedList l = new  LinkedList();
            while(tok.hasMoreElements()) {
                ArgLoc loc = ofString(tok.nextToken());
                l.add(loc);
            }
            return new ArgLoc(ArgLoc.CONCAT, l);
        }
        WordSpan ta = WordSpan.ofString(s);
        return new ArgLoc(ta);
    }


    public boolean equals (Object o) {
        if (!(o instanceof ArgLoc))
            return false;
        ArgLoc al = (ArgLoc)o;

        return al.loc_type == this.loc_type 
	    && ((al.locs == null && this.locs == null) || al.locs.equals(this.locs)) 
	    && ((al.ta == null && ta == null) || al.ta.equals(ta));
    }
    
//     public int hashCode () {
// 	if (ta == null)
// 	    return 19 * loc_type + locs.hashCode();
// 	else
// 	    return 19 * loc_type + ta.hashCode();
//     }
    
//    //auxiliary method used in 'getUniqueSurfaceConstituent()'
//    private Integer getRank(TBNode root) {
//	if (isTraceChain()) return null;
//	else if (isConcat()) {
//	    TreeSet ranks = new TreeSet();
//	    Iterator iter = getLocList().iterator();
//	    while (iter.hasNext()) 
//		ranks.add(((ArgLoc)iter.next()).getRank(root));
//	    if (ranks.size() != 1) return null;   //not all constituents have same rank - undefined!
//	    else return (Integer) ranks.first();
//	}
//	else {
//	    WordSpan treeAddress = getWordSpan();
//	    assert treeAddress != null;
//	    TBNode tbnode = treeAddress.addressedNode(root);
//	    if (tbnode.isEmpty()) return new Integer(0);       //It's an empty constituent - rank 0
//	    String label = tbnode.getLabel().getPrimary();
//	    if (label.startsWith("W") ||
//		(label.equals("IN") &&
//		 tbnode.leftSibling()==null &&
//		 tbnode.rightSibling()==null &&
//		 tbnode.getParent().getLabel().getPrimary().startsWith("WH")))
//		return new Integer(1);                        //It's a WH constituent - rank 1
//	    else return new Integer(2);                       //anything else - rank 2
//	}
//    }


//    // possibly null if none can be found
//    // TODO compare with WordSpan.getSubTree() - are these the same semantics?
//    
//    public ElemTree getDominatingNode(Sentence s) {
//        
//        if (this.isTraceChain()) {
//            return this.getUniqueSurfaceConstituent(s);
//        } else if (this.isConcat()) {
//            // we know that there are no deeply nested WordSpans 
//            // (see propbank README.txt case 3)
//            return WordSpan.merge(this.getAllWordSpans().iterator())
//            .getSubTree(s); // possibly null
//        } else {
//            assert this.isSingle();
//            return this.getWordSpan().getSubTree(s);
//        }
//    }
//    
//    /**
//     * 
//     * @param s
//     * @return
//     */
//    public ElemTree getUniqueSurfaceConstituent(Sentence s) {
//        if (this.isSingle()) throw new RuntimeException("Implement this case");
//        if (this.isConcat()) throw new RuntimeException("Implement this case");
//        assert this.isTraceChain();
//        
//        Iterator chain = this.getAllWordSpans().iterator();
//        
//        ElemTree e = null;
//        while (chain.hasNext()) {
//            e = ((WordSpan) chain.next()).
//                    getSubTree(s);
//            if (e == null) // no dominating node could be found
//                continue;
//            if (e.isEmptyElement()) continue;
//        }
//        if (e == null) {
//            throw new IllegalStateException("trace chain without a surface constituent"
//                    + this.toString());
//        }
//        return e;
//
//    }
    
   
    
//    /**
//     * Attempts to find and return a unique surface constituent ArgLoc if this is a trace chain.
//     * If this is not a trace chain, or if a unique surface constituent ArgLoc cannot be found,
//     * returns null.
//     *
//     * @deprecated -- only here so code compiles
//     */
//    public ArgLoc getUniqueSurfaceConstituent(TBNode verbnode) {
//	TBNode root = verbnode.getRoot();
//	Vector[] rankArray = new Vector[3];
//	Iterator locIterator = getLocList().iterator();
//	ArgLoc constituent;
//	Vector rankVector;
//
//	if (!isTraceChain()) return null;
//	for (int i=0; i<rankArray.length; i++) {
//	    rankArray[i] = new Vector();
//	}
//	while (locIterator.hasNext()) {
//	    constituent = (ArgLoc) locIterator.next();	    
//	    Integer rank = constituent.getRank(root);
//	    if (rank == null) return null;         //undefined rank!
//	    rankArray[rank.intValue()].add(constituent);
//	}
//	for (int i=rankArray.length-1; i>=0; i--) {
//	    rankVector = rankArray[i];
//	    if (rankVector.size() == 0) return null;  //no unique surface element!
//	    else return (ArgLoc) rankVector.get(0);
//	}
//	assert false: "shouldn't get here!";
//	return null;
//    }
    
//    /**
//     * given the node associated with the verb in a sentence, 
//     * make this ArgLoc object contain TreeAddresss in the order    
//     * from source to target through a trace.
//     * 
//     * we use the following algorithm: find all empty consituents which 
//     * are not wh consituents, sort them by proximity to verb.  find all wh
//     * constiuents (who, which, etc), and sort them by proximity to verb
//     * .  Find everything else, sort them by proximity to verb, and then
//     * the resulting locs are in the order empty constituents, wh constituents,
//     * everything-else.
//     * 
//     * @param verbnode a TBNode object representing the verb for this annotation. 
//     * @see edu.upenn.cis.treebank.TBNode
//     */
//    // XXX this could be made even better if we separated out empty constituents 
//    // into those which satisfy tbnode.isTrace() and those which don't.
//    // -scott
//    public void sortMotion(TBNode verbnode)
//    {
//        // this doesn't make any sense unless this a "trace" or movement
//        // argument location type.
//        if (loc_type != EQUIVA) {
//            return;
//        }      
//        //
//        // first we grab the TBNodes we need, and record which ones are empty
//        // and which ones have WH labels and which one is closest to the verb
//        //
//	// Note from Ben:  In cases where an element in the trace chain itself
//	// contains multiple nodes (i.e. a split argument), we will just use 
//	// the earliest of those nodes. (But now we have a new problem....
//	// how do we put humpty dumpy back together again?  Let's use a hashMap
//	// to map TreeAddresses to ArgLoc constituents)
//	//
//	HashMap map = new HashMap();
//        LinkedList tbnodes = new LinkedList();
//        Iterator lociter = locs.iterator();
//        BitSet isempty = new BitSet();
//        BitSet iswh = new BitSet();
//        LabelMatcher whmatch = new LabelMatcher("^WH");
//        int current_index = 0;
//        TBNode root = verbnode.getRoot();
//	List addressList = new LinkedList();
//        while(lociter.hasNext()) {
//	    ArgLoc loc = (ArgLoc)lociter.next();
//	    List addresses = loc.getAllWordSpans();
//	    Collections.sort(addresses);
//	    WordSpan treeAddress = (WordSpan)addresses.get(0);
//	    assert !map.containsKey(treeAddress); 
//	    map.put(treeAddress, loc);
//	    addressList.add(treeAddress);
//            TBNode tbn = treeAddress.addressedNode(root);
//            if (tbn.isEmpty()) {
//                isempty.set(current_index);
//            }
//            if (tbn.findFirst(whmatch) != null) {
//                iswh.set(current_index);
//            }
//            current_index++;
//        }
//        //
//        // find empty consitutents first, then sort them by proximity to verb
//        //
//        LinkedList empty_locs = new LinkedList();
//        for (int i=0; i<isempty.length(); ++i) {
//            if (isempty.get(i) && !iswh.get(i)) {
//                empty_locs.add(addressList.get(i));
//            }
//        }
//        final WordSpan verbloc = new WordSpan(verbnode.getTerminalNum(), 
//                                                    verbnode.getHeight());
//        Comparator verbprox = new Comparator() {
//                public int compare(Object o1, Object o2) {
//                    WordSpan aloc1 = (WordSpan) o1;
//                    WordSpan aloc2 = (WordSpan) o2;
//                    int dist1 = Math.abs(verbloc.compareTo(aloc1));
//                    int dist2 = Math.abs(verbloc.compareTo(aloc2));
//                    return dist1 - dist2;
//                }
//            };
//        Collections.sort(empty_locs, verbprox);
//        //
//        // find wh consituents and sort them by proximity to verb
//        //
//        LinkedList wh_locs = new LinkedList();
//        for (int j=0; j<iswh.length(); ++j) {
//            if (iswh.get(j)) {
//                wh_locs.add(addressList.get(j));
//            }
//        }
//        Collections.sort(wh_locs, verbprox);
//        LinkedList non_empty_non_wh = new LinkedList();
//        for (int k=0; k<locs.size(); ++k) {
//            if (!iswh.get(k) && !isempty.get(k)) {
//                non_empty_non_wh.add(addressList.get(k));
//            }
//        }
//        Collections.sort(non_empty_non_wh, verbprox);
//        //
//        // put it all back together, first empty, then wh, then everything else
//        //
//        LinkedList new_locs = new LinkedList();
//        new_locs.addAll(empty_locs);
//        new_locs.addAll(wh_locs);
//        new_locs.addAll(non_empty_non_wh);
//        locs.clear();
//	for (int i=0; i<new_locs.size(); i++) 
//	    locs.add(map.get((WordSpan)new_locs.get(i)));
//    }

//    /**
//       a simple unit test, not interesting for use.
//     */
//    public static void main(String args[]) throws CorruptDataException {
//	ArgLoc loc1 = ofString("1:2,2:3");
//	ArgLoc loc2 = ofString("1:2*2:3");
//	ArgLoc loc3 = ofString("1:2*2:3,3:4");
//	ArgLoc loc4 = ofString("1:2,2:3*3:4");
//	ArgLoc loc5 = ofString("1:2,2:3*3:4");
//	System.out.println(loc1);
//	System.out.println(loc2);
//	System.out.println(loc3);
//	System.out.println(loc4);
//	
//	assert loc5.equals(loc4);
//
//    }
}

