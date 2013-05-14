/**
 * LTAG-spinal API, an interface to the treebank format introduced by Libin Shen.
 * Copyright (C) 2007  Lucas Champollion
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package edu.upenn.cis.spinal;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.upenn.cis.propbank_shen.*;

/**
 * Represents a "spinal" elementary tree in Libin's LTAG
 * treebank. A typical elementary tree (taken from p. 73
 * of his thesis) looks like this:
 *
 * <code>
 * #3 failed
 * spine: a_( S ( VP VBD^ ) )
 * att #0, on 0, slot 0, order 0
 * att #2, on 0, slot 0, order 1
 * att #6, on 0.0, slot 1, order 0
 * </code>
 *
 * or like this (a structure for predicate coordination):
 *
 * <code>
 * &20
 * spine: c_( S S S )
 * crd #3, on 0.0
 * att #9, on 0, slot 1, order 0
 * crd #10, on 0.1
 * </code>
 *
 * @author Lucas Champollion
 * @author Ryan Gabbard
 */
public class ElemTree implements Serializable {

    /**
     * Designates a field (tree, slot, etc.) of unknown type.
     */
    public static final int UNKNOWN = -1; 
    
    /**
     * Designates an initial tree.
     */
    public static final int INITIAL = 0;
    
    /**
     * Designates an auxiliary tree.
     */
    public static final int AUXILIARY = 1;
    
    /**
     * Designates a coordination tree -- these trees are special constructs and not anchored in a lexical item.
     */
    public static final int COORD = 2;
    
    /**
     * A return value designating a special case when the current tree that has no parent.
     */
    public static final int ROOT = 3;
    
    /**
     * Designates a tree combined with its parent by an attachment operation,
     * indicated by the keyword "att".
     */
    public static final int ATTACH = 4;

    /**
     * Designates a tree combined with its parent by an adjunction operation,
     * indicated by the keyword "adj".
     */
    public static final int ADJOIN = 5;

    /**
     * Designates a tree combined with its parent by an attachment operation,
     * indicated by the keyword "crd" in the output of the incremental parser
     * and in the LTAG-spinal treebank.
     */
    public static final int CONJUNCT = 6;

    /**
     * Designates a tree combined with its parent by a conjunction or an attachment operation,
     * indicated by the keyword "con" in the output of the incremental parser
     * and in the LTAG-spinal treebank.
     * 
     * In the treebank and in the output of the incremental parser, "crd" 
     * is used for conjuncts and "att" is used for
     * connectives. In the output of the bidirectional parser, we don't distinguish 
     * conjuncts from connectives, in order to reduce an operation in parsing, so "con" is used
     * to represent both.
     */
    public static final int CONJUNCT_OR_CONNECTIVE = 7;
    
    /**
     * Designates a tree that attaches/adjoins on the left of its parent.
     */
    public static final int LEFT = 8;
    
    /**
     * Designates a tree that attaches/adjoins on the right of its parent.
     */
    public static final int RIGHT = 9;
    
    private static final int ATT_TYPE_OFFSET = 2;
    private static final String newline = System.getProperty("line.separator");
    private Sentence containingSentence;
    int number = -1;
    String terminal = "";
    private int type = UNKNOWN;
    private SpinalNode rootOfSpine = null;
    
    /**
     * Indicates that the input only specified the part of speech and not the 
     * spine, as is the case for the output of Shen's bidirectional parser.
     */
    private boolean bidirectionalParserOutput = false;
    private String pos = null;
    private ArrayList attachments = new ArrayList();
    /**
     * Null iff this tree is the root of the derivation tree.
     */
    private ElemTree parent;
    /**
     * the attachment by which this <code>ElemTree</code>
     * attaches to its parent; null iff this is root of derivation tree
     */
    private TAGAttachment attachmentToParent = null;
    /**
     * the yield of the subtree rooted by this ElemTree
     */
    private WordSpan span;
    
    /**
     * Represents to which side this tree attaches to its parent.
     */
    private int slot = UNKNOWN;
    
    private boolean completed = false;
    private SpinalNode anchor;
    private SpinalNode foot;
    
    private static final Pattern posPattern = Pattern.compile("\\s*pos: (.*)");
    private static final Pattern spinePattern = Pattern.compile("\\s*spine: (.*)");
    private static final Pattern coordPattern = Pattern.compile("\\s*coord\\s*");
    
    
    /**
     * Creates an <code>ElemTree</code> from a string representation and attaches it
     * into the provided derivation tree (Sentence). We assume that the nodes are 
     * numbered in ascending order of linear precedence of their anchors, as 
     * is the case in the LTAG-spinal treebank.
     * @param container the <code>Sentence</code> to which this 
     * <code>ElemTree</code> is to be added
     * @param representation the string that is to be parsed into an 
     * <code>ElemTree</code>
     * @throws edu.upenn.cis.spinal.ElemTreeFormatException if the
     * string representation is not well-formed
     */
    public ElemTree(Sentence container, String representation)
            throws ElemTreeFormatException {
        attachments = new ArrayList();
        containingSentence = container;
        completed = false;
        parent = null;
        span = null;

        loadFromStringRepresentation(representation);
    }
    
    /**
     * Returns true if this <code>ElemTree</code> attaches or adjoins from the 
     * left of its parent.
     * @return a boolean value
     * @see #getSlot()
     */
    public boolean attachesFromLeft() {
        return this.getSlot() == LEFT;
    }
    
    /**
     * Returns true if this <code>ElemTree</code> attaches or adjoins from the 
     * right of its parent.
     * @return a boolean value
     * @see #getSlot()
     */
    public boolean attachesFromRight() {
        return this.getSlot() == RIGHT;
    }

    /**
     * Returns the node representing the lexical anchor of this tree, or null 
     * if there is no anchor (in the case of a coordination structure).
     * @return a <code>SpinalNode</code> element representing the lexical anchor
     */
    public SpinalNode getAnchor() {
        
        Iterator iter = this.getSpine().getAllNodes().iterator();
        SpinalNode current;
        while (iter.hasNext()) {
            current = (SpinalNode) iter.next();
            if (current.isAnchor()) {
                this.anchor = current;
                return current;
            }
        }
        this.anchor = null;
        return null;
    }

    /**
     * Returns the <code>SpinalNode</code> in the parent tree to which this <code>ElemTree</code>
     * is attached, or null if this <code>ElemTree</code> is the root of the derivation tree.
     * @return the parent's attachment site
     */
    public SpinalNode getAttachmentSite() {
        checkCompletion();
        if (this.isRoot()) {
            return null;
        }
        return this.getParent().getSpinalNodeAt(this.getAttachmentToParent().getGornAddress());
    }

    TAGAttachment getAttachmentToParent() {
        
        return attachmentToParent;
    }
    
    /**
     * Returns the type of the attachment of this elementary tree to its parent tree, one of 
     * {@link #ATTACH}, {@link #ADJOIN}, {@link #CONJUNCT} (only used 
     * in the LTAG-spinal treebank and in the output of the incremental parser),
     * and {@link #CONJUNCT_OR_CONNECTIVE} (only used in the output of the 
     * bidirectional parser). If this elementary tree is the root of the sentence,
     * the special value {@link #ROOT} is returned.
     * @return the type of attachment of this <code>ElemTree</code>
     */
    public int getAttachmentType() {
        if (this.isRoot()) {
            return ROOT;
        }
        return attachmentToParent.getType();
    }

    /**
     * Internal method, returns <code>TAGAttachment</code> list for this tree.
     * @return a list of attachments
     */
    private List getAttachments() {
        return attachments;
    }

    /**
     * Returns the <code>ElemTree</code>s that attach to this <code>ElemTree</code>.
     * @return the children of this node in the derivation tree
     */
    public Iterator getChildren() {
        checkCompletion();

        return new TAGAttachmentToElemTreeIterator(getAttachments());
    }

    /**
     * Returns the spans of the <code>ElemTree</code>s that attach to this 
     * <code>ElemTree</code>.
     * @return an iterator over (@link WordSpan} objects
     */
    public Iterator getChildrenSpans() {
        checkCompletion();

        return new TAGAttachmentToWordSpanIterator(getAttachments());
    }

    /**
     * Returns a <code>List</code> of the elementary trees dominated by this 
     * elementary tree, 
     * including this tree itself.
     * @return a list over <code>ElemTree</code> objects
     */
    public List getDominatedElemTrees() {
        // TODO check if this, called on root, gives the same results as 
        // this.getSentence().getElemTrees(this.getSpan());
        Iterator candidates = this.getSentence().getElemTrees().iterator();
        List result = new Vector();
        while (candidates.hasNext()) {
            ElemTree current = (ElemTree) candidates.next();
            if (this.dominates(current)) {
                result.add(current);
            }
        }
        return result;
    }

    /**
     * Returns a <code>List</code> of the terminals attached to the elementary trees 
     * dominated by this elementary tree, 
     * including this tree itself.
     * @return a list of terminals (<code>String</code> objects)
     */
    public List getDominatedTerminals() {
        Iterator iter = this.getDominatedElemTrees().iterator();
        List l = new Vector();
        while (iter.hasNext()) {
            ElemTree current = (ElemTree) iter.next();
            l.add(current.getTerminal());
        }
        return l;
    }

    /**
     * Returns the number of the file that contains the sentence to which 
     * this <code>ElemTree</code> belongs, or -1 if there is no such number.
     * @return an <code>int</code>
     */
    public int getFileNumber() {
        return this.getSentence().getFileNumber();
    }

    /**
     * Returns the node representing the foot node (if any) of this tree, 
     * or null if there is no foot node. (Only auxiliary trees have foot nodes.)
     * @return a <code>SpinalNode</code>
     */
    public SpinalNode getFoot() {
        Iterator iter = this.getSpine().getAllNodes().iterator();
        SpinalNode current;
        while (iter.hasNext()) {
            current = (SpinalNode) iter.next();
            if (current.isFoot()) {
                this.foot = current;
                return current;
            }
        }
        this.foot = null;
        return null;
    }

    /**
     * Method used internally to create the Graphviz representation of this tree.
     */
    String getGraphvizNodeID() {

        return "node_" + this.getNumber();
    }

    /**
     * Returns the number of this tree, corresponding to the position in the sentence.
     * @return the number, or -1 if unknown
     */
    public int getNumber() {
        checkCompletion();
        return number;
    }



    /** 
     * Returns the location of the predicate-argument structure 
     * corresponding to this elementary tree in the Propbank, or null
     * if this tree has no section and file numbers. This will be a string
     * of the following shape: 
     * <code>wsj/&lt;section&gt;/wsj_&lt;section&gt;&lt;file&gt;.mrg</code>.
     * @return a string that indicates the location of this word in the Propbank
     */
    public PASLoc getPASLoc() {


        if (getSectionNumber() == -1 || getFileNumber() == -1) {
            return null;
        }
        // these both return -1 if no number is available.

        String section = Integer.toString(getSectionNumber());
        String file = Integer.toString(getFileNumber());


        if (section.length() == 1) {
            section = "0" + section;
        }
        if (file.length() == 1) {
            file = "0" + file;
        }

        String p = "wsj/" + section + "/wsj_" + section + file + ".mrg";

        return new PASLoc(p, getSentenceNumber(), getNumber());
    }

    /**
     * Returns the part of speech of this elementary tree, or "NA" in the case of a coordination structure
     * (where part of speech is not really applicable since Coord nodes aren't lexicalized).
     * @return the POS tag of the current word
     */
    public String getPOS() {
        if (this.pos != null) return this.pos;
        
        SpinalNode theAnchor = this.getAnchor();
        if (theAnchor == null) {
            assert this.isCoord();
            return "NA";
        } else {
            return theAnchor.getLabel();
        }
    }

    /**
     * Returns the <code>ElemTree</code> to which this <code>ElemTree</code> attaches,
     * or null iff this is the root.
     * @return the parent of this node in the derivation tree
     */
    public ElemTree getParent() {
        checkCompletion();
        return parent;
    }


    /**
     * Returns the number of the section that contains the sentence to which 
     * this <code>ElemTree</code> belongs, or -1 if there is no such number.
     * @return an <code>int</code>
     */
    public int getSectionNumber() {
        return this.getSentence().getSectionNumber();
    }

    /**
     * Return the <code>Sentence</code> to which this <code>ElemTree</code>
     * belongs.
     * @return a <code>Sentence</code> object
     */
    public Sentence getSentence() {
        //checkCompletion();
        return containingSentence;
    }

    /**
     * Returns the number of the the sentence to which 
     * this <code>ElemTree</code> belongs, or -1 if there is no such number.
     * @return an <code>int</code>
     */
    public int getSentenceNumber() {
        return this.getSentence().getSentenceNumber();
    }

    /**
     * Returns whether this elementary tree attaches to the {@link #LEFT} or {@link #RIGHT}
     * side of its parent. Returns {@link #ROOT} if this tree is the root of the
     * sentence and {@link #UNKNOWN} if the value is not known.
     * 
     * @return one of the given values
     * @see #attachesFromLeft()
     * @see #attachesFromRight()
     */
    public int getSlot() {
        if (this.isRoot()) return ROOT;
        
        return this.slot;
    }
    
    /**
     * Returns the part of the sentence that is spanned by the yield of this
     * elementary tree and its descendents. If the span is discontinuous, the
     * left- and rightmost <code>ElemTree</code> locations are given.
     * @return a <code>WordSpan</code> object
     */
    public WordSpan getSpan() {
        checkCompletion();
        if (span == null) {
            computeSpan();
        }
        return span;
    }

    /**
     * Returns the spinal node at a given Gorn address in this tree.
     * @param g a {@link GornAddress}
     * @return a {@link SpinalNode}
     */
    public SpinalNode getSpinalNodeAt(GornAddress g) {
        checkCompletion();
        Iterator parts = g.iterator();
        SpinalNode currentNode = this.getSpine(); // current node is root of spine
        int currentChild = ((Integer) parts.next()).intValue();
        if (currentChild != 0) {
            throw new RuntimeException("Bad GornAddress: "+ g.toString());
        }
        while (parts.hasNext()) {
            // TODO raise exception if we're given a bad address
            currentNode = currentNode.getChild(((Integer) parts.next()).intValue());
        }
        return currentNode;
    }

    /**
     * Returns the spine of this tree.
     * @return a {@link SpinalNode} representing the root of the spine
     */
    public SpinalNode getSpine() {
        checkCompletion();
        return rootOfSpine;
    }

    /**
     * Returns the yield of the subtree rooted in this elementary tree, with
     * terminals separated by a single white space. Empty elements are included.
     * @return a substring corresponding to the current <code>ElemTree</code>
     * and all its descendents.
     */
    public String getSurfaceString() {
        String result = "";
        Iterator iter = this.getDominatedTerminals().iterator();
        while (iter.hasNext()) {
            if (!result.equals("")) {
                result += " ";
            }
            result += (String) iter.next();
        }
        return result;
    }

    /**
     * Returns the actual terminal string (word in most cases) represented by this <code>ElemTree</code>.
     * @return the terminal string at the fringe of this elementary tree
     */
    public String getTerminal() {
        checkCompletion();
        return terminal;
    }

    /**
     * Returns the type of this elementary tree, one of 
     * {@link #UNKNOWN}, {@link #INITIAL}, {@link #AUXILIARY}, and {@link #COORD}.
     * @return the type of this <code>ElemTree</code>
     */
    public int getType() {
        checkCompletion();
        return type;
    }

    /**
     * Returns a string representing the type of this elementary tree, one of
     * <code>initial</code>, <code>auxiliary</code>, <code>coordination</code>, 
     * and <code>unknown</code>.
     * @return the type of this <code>ElemTree</code> in string representation
     */
    public String getTypeAsString() {
        checkCompletion();
        switch (this.type) {
            case INITIAL:
                return "initial";
            case AUXILIARY:
                return "auxiliary";
            case COORD:
                return "coordination";
            case UNKNOWN:
                return "unknown";
            default:
                assert false;
                return "unknown";
        }
    }

    /**
     * Returns true iff this tree is of type "coordination".
     * @return a boolean value
     */
    public boolean isCoord() {
        return this.type == COORD;
    }

    /**
     * Returns true iff this tree is of type "auxiliary".
     * @return a boolean value
     */
    public boolean isAuxiliary() {
        return this.type == AUXILIARY;
    }

    /**
     * Returns true iff this tree is of type "initial".
     * @return a boolean value
     */
    public boolean isInitial() {
        return this.type == INITIAL;
    }

    /**
     * Returns true iff this tree is of unknown type.
     * @return a boolean value
     */
    public boolean isOfUnknownType() {
        return this.type == UNKNOWN;
    }
    

    /**
     * Returns if the anchor (if there is one) is an empty element 
     * by checking if the terminal has an unescaped asterisk. Returns false
     * if this is a coordination node, in which case there is no anchor anyway.
     * @return a boolean value
     */
    public boolean isEmptyElement() {
        if (this.isCoord()) {
            return false;
        }
        String t = this.getTerminal();
        return (t.indexOf("*") != -1 && t.indexOf("\\*") == -1);
    }

    /**
     * Returns true iff this elementary tree has no parent.
     * @return a boolean value
     */
    public boolean isRoot() {
        return this.getParent() == null;
    }

    /**
     * Returns true iff this <code>ElemTree</code> has other 
     * <code>ElemTree</code>s attached to it.
     * @return a boolean value
     */
    public boolean hasChildren() {
        return this.getChildren().hasNext();
    }

    /**
     * Method used internally.
     */
    void computeSpan() {
        int position = this.getNumber();
        WordSpan lexicalSpan = new WordSpan(position, position);

        // Coordination trees need special treatment: Their "lexical span"
        // refers to a dummy node with no lexical content, so we must ignore
        // that span.

        if (this.getType() == COORD) {
            this.span = WordSpan.merge(this.getChildrenSpans());
        } else if (this.hasChildren()) {
            this.span = WordSpan.combine(lexicalSpan, WordSpan.merge(this.getChildrenSpans()));
        } else { // this has no children
            this.span = lexicalSpan;
        }
    }

    /**
     * Method used internally while building up a derivation tree.
     */
    void complete() {

        // tells each child that this is its parent
        for (Iterator it = getAttachments().iterator(); it.hasNext();) {
            TAGAttachment attach = (TAGAttachment) it.next();

            attach.complete();
        }

        completed = true;
    }

    /**
     * Method used internally while building up a derivation tree.
     */
    private void checkCompletion() {
        if (!completed) {
            throw new UncompletedElemTreeException();
        }
    }

    /**
     * Method used internally and called from the constructor to parse
     * a string representation into an ElemTree object.
     */
    void loadFromStringRepresentation(String rep)
            throws ElemTreeFormatException {
        String[] lines = rep.split("\\n");

        // first line has format:
        // #4 will
        // or:
        // &25
        // The latter case is coordination.
        // Actually we have already stripped off the first character (& or #).

        String[] firstLinePieces = lines[0].split("\\s+");

        if (firstLinePieces.length < 1) {
            throw new ElemTreeFormatException("First line empty.");
        }


        if (firstLinePieces.length == 1) { // coordination doesn't come with a word
            this.type=COORD;
        }
        
        this.number = Integer.parseInt(firstLinePieces[0]);

        terminal = "";

        for (int i = 1; i < firstLinePieces.length; i++) {
            terminal += firstLinePieces[i];
        }

        // second line has format:
        // spine: a_( XP NN^ )        // spinePattern
        // or
        // coord                      // coordPattern
        // or (for bidirectional parser output)
        // pos: NN                    // posPattern

        String secondLine = lines[1];
        
        Matcher coordMatcher = coordPattern.matcher(secondLine);
        Matcher posMatcher = posPattern.matcher(secondLine);
        Matcher spineMatcher = spinePattern.matcher(secondLine);
        
        if (coordMatcher.matches()) {
            rootOfSpine = null;
            type = COORD;
        } else if (posMatcher.matches()) { 
            rootOfSpine = null;
            pos = posMatcher.group(1);
            bidirectionalParserOutput = true;
        } else if (spineMatcher.matches())  {
            try {
                secondLine = spineMatcher.group(1);
            } catch (StringIndexOutOfBoundsException e) {
                System.out.println(secondLine);
                throw e;
            }

            char typeChar = secondLine.charAt(0);

            switch (typeChar) {
                case 'a':
                    type = INITIAL;
                    break;
                case 'b':
                    type = AUXILIARY;
                    break;
                case 'c':
                    type = COORD;
                    break;
            }

            // parse the spine
            rootOfSpine = new SpinalNode(secondLine.substring(ATT_TYPE_OFFSET), this);
        } // end treatment of spine
        

        // read attachmentToParent lines
        for (int i = 2; i < lines.length; i++) {
            getAttachments().add(new TAGAttachment(this, lines[i]));
        }
    }

    /**
     * Converts this tree to a canonical string representation of the kind used in Libin 
     * Shen's thesis.
     * @return a string representation
     */
    public String toString() {

        checkCompletion();

        StringBuffer s = new StringBuffer();
        // "#20 remained" (hash, number, terminal: this is the normal case)
        // "&20" (ampersand, number, no terminal: the format for coordination)
        String nodeno = Integer.toString(this.getNumber());

        if (this.getType() == COORD) {
            s.append("&");
            s.append(nodeno);
        } else {
            s.append("#");
            s.append(nodeno);
            s.append(" ");
            s.append(this.getTerminal());
        }
        s.append(newline);
        
        if (this.isBidirectionalParserOutput()) {
            // no line describing the spine, instead just a line like:
            //  pos: DT
            // This is typical of the output of Shen's bidirectional parser.
            s.append(" pos: ");
            s.append(this.getPOS());
            s.append(newline);
        } else if (this.getSpine() != null) {
            // Format of the line describing the spine:
            //  spine: b_( VP VB^ VP* )
            s.append(" spine: ");
            switch (this.getType()) {
                case INITIAL:
                    s.append("a");
                    break;
                case AUXILIARY:
                    s.append("b");
                    break;
                case COORD:
                    s.append("c");
                    break;
            }

            s.append("_");
            s.append(this.getSpine().toString());
            s.append(newline);
        } else if (this.getSpine() == null && this.getType() == COORD) {
            // Mimic the output of Shen's incremental parser
            s.append(" coord");
            s.append(newline);
        }

        Iterator theAttachments = this.getAttachments().iterator();

        while (theAttachments.hasNext()) {
            TAGAttachment current = (TAGAttachment) theAttachments.next();
            s.append(current.toString());
            s.append(newline);
        }
        return s.toString();
    }

     
    // TODO maybe move this code to SpinalNode?
    private void writeSpineToGraphviz(BufferedWriter b, SpinalNode current,
            boolean isRoot, int start, int end, boolean includeSpans, boolean beanPoleStyle)
            throws IOException {

        checkCompletion();
        
        int n = current.getElemTree().getNumber();

        if (start != -1 && end != -1) {
            if (n < start || n > end) throw new IllegalArgumentException();
        }

        // create a graphviz node for the current spinal node
        String currentNode = current.getGraphvizNodeID();
        b.write(currentNode + "[label=\"" + current.getLabel());
        
        if (includeSpans) {
            String thespan = this.getSpan().toString();
            if (!isRoot) {
                thespan = "";
            }
            b.write(" " + thespan);
        }
        
        if (!current.getType().equals(SpinalNode.ANCHOR)) {
            b.write(current.getType());
        }
        b.write("\" ");

        // uncomment the next line to make the root of the spine stand out
        //if (isRoot) b.write("style=bold");

        b.write("];");
        b.newLine();
        
        // create edges from the anchor SpinalNode to the actual
        // lexical anchor, which is represented by the
        // graphviz node ID of the ElemTree itself (i.e. this)

        if (current.getType().equals(SpinalNode.ANCHOR)) {
            b.write(currentNode 
                    + " -> " 
                    + this.getGraphvizNodeID() 
                    + "[style=bold arrowhead=none];");
            b.newLine();
        }

        // create edges from the current node to its children (if any)

        // children are never drawn emphasized (only the root of the
        // present spine is)

        SpinalNode[] children = current.getChildren();

        for (int i = 0; i < children.length; i++) {
            writeSpineToGraphviz(b, children[i], false, start, end, includeSpans, beanPoleStyle);

            // add edges to children
            b.write(currentNode 
                    + " -> " 
                    + children[i].getGraphvizNodeID() 
                    + "[style=bold arrowhead=none];");
            b.newLine();
        }

    }

    /**
     * Method used internally to convert this ElemTree and its subtrees 
     * into Graphviz format.
     * Should only be called by Sentence class --
     * doesn't generate complete graphviz output by itself.
     */
    void writeGraphvizTo(BufferedWriter b, int start, int end, boolean includeSpans, boolean beanPoleStyle, boolean showSpines)
            throws IOException {

        // If we are in bidirectional parser output, we never show the spine because
        // it is not known, but we include the part of speech in the terminal node instead 
        // because otherwise it would not show up.
        if (this.isBidirectionalParserOutput()) {
            showSpines = false;
        }
        
        // We skip any nodes that are not within start and end.
        int n = this.getNumber();
        if ((start != -1 && end != -1) && (n < start || n > end)) {
            return;
        }

        if (showSpines) {
            
            // Make sure that foot nodes appear on the side of the tree on which they should.
            if (this.isAuxiliary()) {
                String anchorID = this.getAnchor().getGraphvizNodeID();
                String footID = this.getFoot().getGraphvizNodeID();
                b.write("subgraph { rank = same; edge [style=invis] ");
                if (this.attachesFromLeft()) {
                    b.write(anchorID + " -> " + footID);
                } else if (this.attachesFromRight()) {
                    b.write(footID + " -> " + anchorID);
                }
                b.write("; }");
                b.newLine();
            }
            
           // Write out the spine of this elementary tree.
           this.writeSpineToGraphviz(b, this.getSpine(), true, start, end, includeSpans, beanPoleStyle);
        }
        
        // Write out the node that represents the current terminal.
        // This node has a different shape (a rounded box).
        b.write(this.getGraphvizNodeID() 
                + "[label=\" #" 
                + Integer.toString(this.getNumber()));
        // If we are in bidirectional parser output, we never show the spine because
        // it is not known, but we include the part of speech in the terminal node instead 
        // because otherwise it would not show up.       
        if (this.isBidirectionalParserOutput()) {
            b.write("\\n" 
                + this.getPOS());
        }
        b.write("\\n" 
                + this.getTerminal() 
                + "\" style=rounded shape=box];");
        b.newLine();

        // Create edges to attached ElemTrees
        // and recursively graphviz them.
        Iterator theAttachments = this.getAttachments().iterator();
        while (theAttachments.hasNext()) {
            TAGAttachment current = (TAGAttachment) theAttachments.next();

            // skip any attached ElemTrees that are not in the span
            int position = current.getElemTree().getNumber();
            if ((start != -1 && end != -1) && (position < start || position > end)) {
                continue;
            }

            // create links between ElemTrees

            String constraint;
            if (beanPoleStyle) {
                constraint = "false";
            } else {
                constraint = "true";
            }
            
            String source = null;
            String target = null;
            
            if (showSpines) {
                source = current.getAttachmentSiteOnParent().getGraphvizNodeID();
                target = current.getChild().getSpine().getGraphvizNodeID(); // root of child's spine
            } else {
                source = current.getParent().getGraphvizNodeID();
                target = current.getChild().getGraphvizNodeID();
            }
            
            String style;
            if (showSpines) {
                style = "dotted";
            } else {
                style = "solid";
            }
            b.write(source
                    + " -> " 
                    + target 
                    + "[style=" + style
                    + " constraint=" +constraint + "];");
            b.newLine();

            // recursive call
            current.getElemTree().writeGraphvizTo(b, start, end, includeSpans, beanPoleStyle, showSpines);
            b.newLine();
        }

    }

    /**
     * Returns true iff this <code>ElemTree</code> dominates the other tree 
     * in the sentence (this includes
     * the case in which <code>this.equals(other)</code>).
     * @param other the other tree
     * @return a boolean value
     */
    public boolean dominates(ElemTree other) {

        if (this.equals(other)) {
            return true;
        }
        if (this.isParentOf(other)) {
            return true;
        }
        return (!other.isRoot()) && this.dominates(other.getParent());
    }

    /**
     * Returns true iff this <code>ElemTree</code> is the direct parent of 
     * the other tree in the derivation tree.
     * @param other the other tree
     * @return a boolean value
     */
    public boolean isParentOf(ElemTree other) {
        Iterator iter = this.getChildren();
        boolean result = false;
        while (iter.hasNext()) {
            result = ((ElemTree) iter.next()).equals(other);
        }
        return result;
    }

    /**
     * Returns true if this <code>ElemTree</code> has been read in from the 
     * format used in the output of Shen's bidirectional parser. If this is the case,
     * no information about the spine is present. 
     * @return a boolean value
     */
    public boolean isBidirectionalParserOutput() {
        return this.bidirectionalParserOutput;
    }

    /**
     * Internal class representing the attachment of another 
     * <code>ElemTree</code> to this one.
     */
    private static class TAGAttachment implements Serializable {

        private int type = -1;
        private int nodeNumber = -1;
        private GornAddress gornAddress;
        private int slot = ElemTree.UNKNOWN;
        private int order = -1;
        private ElemTree child;
        private ElemTree parent;
        private boolean completed;
        
        /**
         * true indicates a format as in the LTAG-spinal treebank or in 
         * the Shen incremental parser output, where slots and numbers are
         * specified. False indicates a format as in the Shen bidirectional parser
         * output, where only the node id of the child is specified.
         */
        private boolean locationKnown = true;

        private void checkCompletion() {
            if (!completed) {
                throw new UncompletedElemTreeException();
            }
        }

        /**
         * Note: The parent is the current tree (this). The child is the tree
         * designated by the attachmentToParent.
         */
        public TAGAttachment(ElemTree parent, String representation)
                throws ElemTreeFormatException {
            this.parent = parent;
            completed = false;

            loadFromStringRepresentation(representation);
        }


        
        public void complete() {
            child = getParent().containingSentence.getElemTree(this.getNodeNumber());
            child.parent=(getParent());
            child.attachmentToParent = this;
            child.slot=this.slot;
            
            // In the LTAG-spinal treebank format, and in the output format of Shen's 
            // incremental parser, the information about attachment
            // and adjunction
            // is recorded redundantly in the spine line (via letter "a" vs. "b") and
            // in the attachment line (via "att" vs. "adj"). 
            
            // By contrast, in the output format of Shen's bidirectional parser, 
            // spine lines are not included and so
            // we propagate att/adj information from the attachment to the 
            // elementary tree beneath it. (We know
            // which nodes are coordination nodes because they are marked with an
            // ampersand (&).)
 
            if (child.isBidirectionalParserOutput()) {
                if (this.getType() == ElemTree.ATTACH) {
                    child.type = ElemTree.INITIAL;
                } else if (this.getType() == ElemTree.ADJOIN) {
                    child.type = ElemTree.AUXILIARY;
                }
            }
            
            completed = true;
            }

        void loadFromStringRepresentation(String rep)
                throws ElemTreeFormatException {
            
            
            // Format is:
            // att #24, on 0, slot 1, order 2
            // or
            // crd #6, on 0.0
            // or (in the output of Libin's bidirectional parser)
            // att #24
            
            rep = rep.trim();
            String[] parts = rep.split("\\s+");

            if ((parts.length != 8) && (parts.length != 4) && (parts.length != 2)) {
                throw new ElemTreeFormatException(
                        "Malformed attachment representation: " + rep);
            }

            // remove trailing commas
            for (int i = 0; i < parts.length; i++) {
                if ((parts[i].length() > 0) &&
                        parts[i].charAt(parts[i].length() - 1) == ',') {
                    parts[i] = parts[i].substring(0, parts[i].length() - 1);
                }
            }

            String typeString = parts[0];

            if (typeString.equals("att")) {
                type = ATTACH;
            } else if (typeString.equals("adj")) {
                type = ADJOIN;
            } else if (typeString.equals("crd")) {
                type = CONJUNCT;
            } else if (typeString.equals("con")) {
                type = CONJUNCT_OR_CONNECTIVE;
                
            } else {
                throw new ElemTreeFormatException("Unknown attachment type " + typeString 
                        + " at " + parent.getSentence().prettyPrintLocation() + ".");
            }

            try {
                this.nodeNumber = Integer.parseInt(parts[1].substring(1));
            } catch (NumberFormatException e) {
                String partsMessage = "";

                for (int i = 0; i < parts.length; i++) {
                    partsMessage += i + "=" + parts[i] + "\n";
                }

                throw new ElemTreeFormatException("Invalid node number " +
                        parts[1].substring(1) +
                        ". Attachment representation was " + rep + ". Parts array was " + partsMessage);
            }

            if (parts.length == 2) { // bidirectional parser output
                locationKnown = false;
                return;
            }
            
            this.gornAddress = new GornAddress(parts[3]);

            if (parts.length == 8) { // LTAG-spinal and incremental parser output
                int theSlot = Integer.parseInt(parts[5]);
                if (theSlot == 0) {
                    this.slot = ElemTree.LEFT;
                } else if (theSlot == 1) {
                    this.slot = ElemTree.RIGHT;
                }
                this.order = Integer.parseInt(parts[7]);
            }
        }

        /**
         * Returns a string representation of this <code>ElemTree</code> in LTAG-spinal format.
         *
         * @return a string representing this elementary tree
         */
        public String toString() {
            checkCompletion();
            StringBuffer s = new StringBuffer(20);
            switch (this.getType()) {
                case ATTACH:
                    s.append(" att");
                    break;
                case ADJOIN:
                    s.append(" adj");
                    break;
                case CONJUNCT:
                    s.append(" crd");
                    break;
                case CONJUNCT_OR_CONNECTIVE:
                    s.append(" con");
                    break;
            }
            s.append(" #");
            s.append(Integer.toString(this.getNodeNumber()));
            if (locationKnown) {
                s.append(", on ");
                s.append(this.getGornAddress().toString());
                if (this.getSlot() != -1 && this.getOrder() != -1) {
                    assert this.getSlot() != -1;
                    assert this.getOrder() != -1;
                    s.append(", slot ");
                    s.append(Integer.toString(this.getSlot()));
                    s.append(", order ");
                    s.append(Integer.toString(this.getOrder()));
                }
            }
            return s.toString();
        }


        /**
         * Returns the ElemTree closer to the root of the sentence
         * (the one to which is attached).
         */
        public ElemTree getParent() {
            return parent;
        }

        /**
         * Returns the ElemTree closer to the fringe of the sentence
         * (the one which attaches to something).
         */
        public ElemTree getChild() {
            return child;
        }

        /**
         * Returns the precise place at which the current attachment
         * is linked to its parent (a node in the spine of the parent designated
         * by the GornAddress of this attachment). Return null if unknown
         * (as in the output of Shen's bidirectional parser).
         */
        public SpinalNode getAttachmentSiteOnParent() {
            if (!locationKnown) return null;
            return this.getParent().getSpinalNodeAt(this.getGornAddress());
        }
        

        /**
         * A synonym of getChild().
         */
        public ElemTree getElemTree() {
            return getChild();
        }

        public int getType() {
            return type;
        }

        public int getNodeNumber() {
            // always known, even if !locationKnown
            return nodeNumber;
        }

        public int getSlot() {
            if (!locationKnown) return -1;
            return slot;
        }

        public int getOrder() {
            if (!locationKnown) return -1;
            return order;
        }

        public GornAddress getGornAddress() {
            if (!locationKnown) return null;
            return gornAddress;
        }
    } // end of class TAGAttachment

    private static class TAGAttachmentToElemTreeIterator implements ListIterator {

        ListIterator wrappedIterator;

        public TAGAttachmentToElemTreeIterator(List attachments) {
            wrappedIterator = attachments.listIterator();
        }

        public void add(Object o) {
            throw new UnsupportedOperationException("add not supported.");
        }

        public void remove() {
            throw new UnsupportedOperationException("remove not supported.");
        }

        public void set(Object o) {
            throw new UnsupportedOperationException("set not supported");
        }

        public boolean hasPrevious() {
            return wrappedIterator.hasPrevious();
        }

        public boolean hasNext() {
            return wrappedIterator.hasNext();
        }

        public int nextIndex() {
            return wrappedIterator.nextIndex();
        }

        public int previousIndex() {
            return wrappedIterator.previousIndex();
        }

        //returns ElemTree
        public Object next() {
            TAGAttachment nextOne = (TAGAttachment) wrappedIterator.next();

            return nextOne.getChild();
        }

        //returns ElemTree
        public Object previous() {
            TAGAttachment previousOne = (TAGAttachment) wrappedIterator.previous();

            return previousOne.getChild();
        }
    }

    static class TAGAttachmentToWordSpanIterator implements ListIterator {

        ListIterator wrappedIterator;

        public TAGAttachmentToWordSpanIterator(List attachments) {
            wrappedIterator = attachments.listIterator();
        }

        public void add(Object o) {
            throw new UnsupportedOperationException("add not supported.");
        }

        public void remove() {
            throw new UnsupportedOperationException("remove not supported.");
        }

        public void set(Object o) {
            throw new UnsupportedOperationException("set not supported");
        }

        public boolean hasPrevious() {
            return wrappedIterator.hasPrevious();
        }

        public boolean hasNext() {
            return wrappedIterator.hasNext();
        }

        public int nextIndex() {
            return wrappedIterator.nextIndex();
        }

        public int previousIndex() {
            return wrappedIterator.previousIndex();
        }

        //returns WordSpan
        public Object next() {
            TAGAttachment nextOne = (TAGAttachment) wrappedIterator.next();

            return nextOne.getChild().getSpan();
        }

        //returns WordSpan
        public Object previous() {
            TAGAttachment previousOne = (TAGAttachment) wrappedIterator.previous();

            return previousOne.getChild().getSpan();
        }
    }
}
