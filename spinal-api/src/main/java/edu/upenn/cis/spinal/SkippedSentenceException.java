package edu.upenn.cis.spinal;

/**
 * Exception thrown when a method is called on an object representing 
 * a skipped sentence in the LTAG-spinal treebank.
 *
 * @author Lucas Champollion
 */
public class SkippedSentenceException extends IllegalStateException {
    
    private Sentence source = null;
    
    /** Creates a new instance of <code>SkippedSentenceException</code>. */
    public SkippedSentenceException() {
    }
    
    /** 
     * Creates a new instance of <code>SkippedSentenceException</code> and records the 
     * sentence in question. 
     * 
     * @param source the sentence that caused this exception
     */
    public SkippedSentenceException(Sentence source) {
        this.source = source;
    }
    
    /**
     * Returns the skipped sentence that caused this exception.
     * 
     * @return a <code>Sentence</code> object
     */
    public Sentence getSource() {
        return this.source;
    }
}
