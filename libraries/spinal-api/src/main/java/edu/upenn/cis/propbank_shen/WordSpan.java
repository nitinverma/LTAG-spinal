package edu.upenn.cis.propbank_shen;

import edu.upenn.cis.spinal.*;
//import edu.upenn.cis.treebank.TBNode;

import java.util.*;

/**
   This class represents the span of a propbank argument or of the subtree
   of a sentence
   by a pair of integers indicating the first word of the 
   argument and the first word that is outside the argument.
   It corresponds to the class <code>TreeAddress</code> in the original 
   Propbank API and had to be changed to reflect Libin Shen's change
   to the original propbank as described in his REAMDE file:
 
   "Word IDs are used to represent the phrases, while in the original Propbank, 
   phrases are represented with the root node of the subtree in PTB."	
   
   The numbers start with zero, i.e. the initial word in a sentence is
   the zeroth word.
 
   @author Scott Cotton
*/
public class WordSpan implements Comparable {

    /** the number of the initial word of this span
     */
    private int start;
    /**
       the number of the final word of this span
     */
    private int end;

    private WordSpan(){};

    /** 
     * Construct a word span from a start and an end position
     */
    public WordSpan(int s, int e) {
        start = s;
        end = e;
    }
    
    /**
     * Merge two <code>WordSpan</code>s. Returns the smallest span that contains
     * each of the input spans. Always returns a continuous span.
     * If one of the arguments is null then the other one is returned.
     * If both of the arguments are null then an exception is raised.
     */
    public static WordSpan combine(WordSpan w1, WordSpan w2) {
        
        if (w1 == null && w2 == null) 
            throw new IllegalArgumentException();
        
        if (w1 == null) return w2;
        if (w2 == null) return w1;
        
        WordSpan current=new WordSpan();
        
        if (w1.start() < w2.start()) {
            current.start = w1.start();
        } else {
            current.start = w2.start();
        }
        if (w1.end() > w2.end()) {
            current.end = w1.end();
        } else {
            current.end = w2.end();
        }
        return current;
    }
    
    /**
     * Merge any number of <code>WordSpan</code>s. Returns the smallest span that contains
     * each of the input spans. Always returns a continuous span. Returns 
     * an exception
     * if the iterator argument is empty.
     * @param wordSpans an <code>Iterator</code> containing spans
     * @return a <code>WordSpan</code> that contains each of the input spans
     */
    public static WordSpan merge(Iterator wordSpans) {
        if (!wordSpans.hasNext()) throw new IllegalArgumentException("Attempted" +
                " to merge zero WordSpans");
        WordSpan current=null;
        current = (WordSpan) wordSpans.next();
        int min = current.start();
        int max = current.end();
        int currentMin, currentMax;
        
        while (wordSpans.hasNext()) {
            current = (WordSpan) wordSpans.next();
            currentMin = current.start();
            currentMax = current.end();
            if (currentMin < min) min = currentMin;
            if (currentMax > max) max = currentMax;
        }
        return new WordSpan(min, max);
    }

    /**
     * Returns the node at this address, relative to root (which should
     * probably be the actual root of a tree).
     */
//    public TAGNode addressedNode (TAGNode root) {
//        TAGNode current = (TAGNode)root.getTerminals().get(start);
//        for (int i=0; i < end; i++) {
//            if (current == null)
//                throw new IllegalArgumentException("Terminal "+
//                      root.getTerminals().get(start) + " is only "+
//                      i + " deep, not "+end+".");
//            current = current.getParent();
//        }
//        return current;
//    }
    
//    // TODO replace dummy method which is only there so that the code compiles
//    public TBNode addressedNode(TBNode root) {
//        throw new UnsupportedOperationException
//                ("dummy method which is only there so that the code compiles");
//    }
    
    
    
    
    /** 
     * Attempts to find a subtree whose root ElemTree yields the words
     * described by this span. Returns null otherwise.
     */
    // TODO use span lookup table (subTreeForSpan) in s to do this more quickly
    public ElemTree getSubTree(Sentence s) {
        return s.getSubTree(this);
    }
    

    public boolean equals (Object o) {
        if (!(o instanceof WordSpan))
            return false;
        WordSpan ta = (WordSpan)o;
        return start() == ta.start() && end() == ta.end();
    }

    // Need to override hashCode() because we're implementing a lookup table
    // (a HashMap) for spans in class Sentence.java
     public int hashCode () {
         return this.toString().hashCode();
     }

    /**
       Comparison of node addresses just uses comparison
       of start numbers backing off to end if
       start numbers are equal.
     */
    public int compareTo(Object o) {
        WordSpan oal = (WordSpan) o;
        if (start() != oal.start()) {
            return start() - oal.start();
        } else {
            return end() - oal.end();
        }
    }

    /**
       Return a string of the form &lt;start&gt;_&lt;end&gt;
     */
    public String toString() {
        return start() + "_" + end();
    }



    /**
     * 
     * Creates a WordSpan instance from a string
     * of the form <pre> &lt;terminal&gt;_&lt;end&gt; </pre>
     * @param s
     * 
     */
    public static WordSpan ofString(String s) throws CorruptDataException
    {
        // TODO replace this "magic symbol" by SEPARATOR field
        int colidx = s.indexOf('_');
        if (colidx == -1) {
            throw new CorruptDataException("invalid basic argument location: " + s);
        }
        Integer t = Integer.decode(s.substring(0, colidx));
        Integer h = Integer.decode(s.substring(colidx + 1, s.length()));
        return new WordSpan(t.intValue(), h.intValue());
    }



    public int start() {
        return start;
    }

    public int end() {
        return end;
    }
}
