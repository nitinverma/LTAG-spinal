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
import java.util.regex.*;
import edu.upenn.cis.propbank_shen.*;

/**
 * Represents a sentence (an LTAG-spinal derivation tree) in Libin Shen's
 * LTAG-spinal treebank.
 *
 * A typical sentence is represented in Libin Shen's thesis, page 73.
 *
 * @author Lucas Champollion
 * @author Ryan Gabbard
 */
public class Sentence implements Serializable {
    
    
    /** 
     * Some sentences in the LTAG-spinal Treebank only consist of the word "skip",
     * i.e. they're not really there -- the word indicates that the corresponding
     * sentence in the Penn Treebank has not been included in the LTAG-spinal
     * treebank.
     */
    private boolean skip=false;
    
    /**
     * The number of the Penn Treebank section from which this 
     * <code>Sentence</code> has been taken, or -1 if not applicable.
     */
    private int sectionNumber = -1;
    
    /**
     * The number of the Penn Treebank file from which this 
     * <code>Sentence</code> has been taken, or -1 if not applicable.
     */
    private int fileNumber=-1;
    
    /**
     * The number of the Penn Treebank section from which this 
     * <code>Sentence</code> has been taken, or -1 if not applicable.
     */
    private int sentenceNumber=-1;
    
    /**
     * The root of the sentence.
     */
    private ElemTree root;
    
    /**
     * Contains all the elementary trees of this sentence in order.
     */
    private ArrayList elemTrees;
    
    /**
     * A lexicon mapping word spans to the corresponding nodes.
     */
    private HashMap spanTable;
    
    /**
     * First call to {@link #computeSpans()} sets this to true.
     */
    private boolean spansComputed=false;
    
    /**
     * First call to {@link #computeSpanTable()} sets this to true.
     */    
    private boolean spanTableComputed=false;
    
    
    /**
     * Pattern used in method (@link loadFromStringRepresentation(String)}
     */
    private Pattern elemTreePattern = 
            Pattern.compile("^[#|&]",Pattern.MULTILINE);
    
    // to be deleted if RelativeClauseFixer is
    static int multirootedParses=0;
    
    /**
     * Creates a new <code>Sentence</code> object from a string representation following 
     * the format defined in Libin Shen's thesis.
     * @param representation a <code>String</code> containing a specification of 
     * a sentence in LTAG-spinal format
     * @throws edu.upenn.cis.spinal.ElemTreeFormatException if an error occurs while parsing the string representation
     */
    public Sentence(String representation)
    throws ElemTreeFormatException {
        elemTrees=new ArrayList();
        root=null;
        sentenceNumber=-1;
        
        loadFromStringRepresentation(representation);
    }
    
    /**
     * Convenience method that calls the constructor, to follow the conventions in the Propbank API.
     * @param representation a <code>String</code> containing a specification of
     * a sentence in LTAG-spinal format
     * @return a new {@link Sentence} constructed from the given <code>String</code>
     * @throws edu.upenn.cis.spinal.ElemTreeFormatException if an error occurs while parsing the string representation
     */
    public Sentence ofString(String representation) throws ElemTreeFormatException {
        return new Sentence(representation);
    }
    
    /**
     * Performs the actual parsing of an LTAG-spinal formatted string 
     * into a <code>Sentence</code>.
     * @param rep a <code>String</code> containing a specification of a sentence in LTAG-spinal format
     * @throws edu.upenn.cis.spinal.ElemTreeFormatException if the input is not well-formed
     */
    void loadFromStringRepresentation(String rep)
    throws ElemTreeFormatException {
        
        String[] lines=rep.split("\\n", 3);
        // first line looks like:
        // 0 1 0
        // (for LTAG spinal treebank)
        // or like:
        // 1
        // (for parser output)
        
        String[] locations = lines[0].split(" ");
        
        try {
            if (locations.length == 1) { // typical of parser output
                sentenceNumber=Integer.parseInt(locations[0]);
            } else if (locations.length == 3) { // typical of LTAG treebank
                sectionNumber=Integer.parseInt(locations[0]);
                fileNumber=Integer.parseInt(locations[1]);
                sentenceNumber=Integer.parseInt(locations[2]);
            } else throw new ElemTreeFormatException
                    ("Invalid sentence number" + locations);
        } catch (NumberFormatException e) {
            throw new ElemTreeFormatException("Invalid sentence number " +
                    locations);
        }
        
        // second line looks like:
        // root 6
        // or like:
        // skip
        
        if (lines[1].equals("skip")) {
            skip=true;
            return;
        }
        
        
        int rootNumber=-1;
        
        try {
            String[] rootParts=lines[1].split("\\s+");
            
            if (rootParts.length>2) {
                
                String location;
                
                if (sectionNumber != -1 && fileNumber != -1) {
                    location = "section " + sectionNumber
                        + ", file " +  fileNumber
                        + ", sentence " + sentenceNumber;

                } else {
                    location = "sentence " + sentenceNumber;
                }
                
                System.err.println("WARNING: "
                        + location
                        + " has a multirooted parse (\""
                        + lines[1] +
                        "\"). This is not supported by the API and not " +
                        "conform to LTAG-spinal standards, although " +
                        "Shen's incremental parser sometimes produces this " +
                        "output. " +
                        "Only the first root has been read in.");
                multirootedParses++;
            }
            
            if (rootParts.length<2) {
                System.err.println("WARNING: Bad root: "
                        + "section " + sectionNumber
                        + " file " +  fileNumber
                        + " sentence " + sentenceNumber);
            }
            
            rootNumber=
                    Integer.parseInt(rootParts[1]);
        } catch (NumberFormatException e) {
            throw new ElemTreeFormatException("Invalid root number: " +
                    lines[1].substring(5));
        }
        
        // now parse the spinal elementary trees in the file
        String[] elemTreeRepresentations=elemTreePattern.split(lines[2],0);
        
        for (int i=1; i<elemTreeRepresentations.length; i++) {
            elemTrees.add(new ElemTree(this, elemTreeRepresentations[i]));
        }
        
        root=(ElemTree)elemTrees.get(rootNumber);
        
        for (Iterator it=elemTrees.iterator(); it.hasNext(); ) {
            ElemTree node=(ElemTree)it.next();
            
            node.complete();
        }
    }
    
    /**
     * Reads a string representation of a derivation tree from the specified
     * <code>BufferedReader</code>.
     * @return a <code>Sentence</code> element representing the derivation tree, or null
     * if the input contained nothing or contained only whitespace
     * @param inp the <code>BufferedReader</code> from which to read
     * @throws edu.upenn.cis.spinal.ElemTreeFormatException if an error occurs while parsing the string representation
     * @throws java.io.IOException if an error occurs while reading
     */
    public static Sentence readTree(BufferedReader inp)
    throws ElemTreeFormatException, IOException {
        StringBuffer s=new StringBuffer("");
        String in=null;
        
        while (true) {
            in=inp.readLine();
            
            if ((in==null) || in.matches("^\\s*$")) { // null or whitespace
                break;
            } else {
                s.append(in);
                s.append("\n"); 
            }
        }
        
        if (s.length()==0) {
            return null;
        } else {
            return new Sentence(s.toString());
        }
    }
    
    /**
     * Prints this sentence to the specified output in LTAG-spinal format.
     * @param w the {@link java.io.Writer} to which this sentence is to be written
     * @throws java.io.IOException if an error occurs during writing
     */
    public void writeTo(Writer w) throws IOException {
        this.writeTo(new BufferedWriter(w));
    }

    /**
     * Prints this sentence to the specified output in LTAG-spinal format.
     * @param b the {@link java.io.BufferedWriter} to which this sentence is to be written
     * @throws java.io.IOException if an error occurs during writing
     */
    public void writeTo(BufferedWriter b) throws IOException {
        b.write(this.getLocation());
        b.newLine();
        if (this.isSkipped()) {
            b.write("skip");
            b.newLine();
            b.newLine();
            
            b.flush();
            return;
        }
        
        b.write("root " + this.getRoot().getNumber());
        b.newLine();
        
        Iterator theElemTrees = this.elemTreesIterator();
        while (theElemTrees.hasNext()) {
            ElemTree current = (ElemTree) theElemTrees.next();
            b.write(current.toString());
            
        }
        b.newLine();
        b.flush();
        
    }

    
    /**
     * Writes a visual representation of a subpart of this sentence in Graphviz format 
     * to the specified {@link java.io.BufferedWriter}.
     * 
     * 
     * @param b the {@link java.io.BufferedWriter} to which the subsentence is to be written
     * @param start the first word of the sentence to be included in the graphical output
     * @param end the last word of the sentence to be included in the graphical output
     * @param includeSpans if true, the word span of the subtree dominated by a node
     * is appended to that node's representation; otherwise, it only
     * consists of the node label
     * @param beanPoleStyle chooses between two very different styles of output -- if true,
     * the output looks like beanpoles, if false, it looks like tadpoles. See the 
     * illustrations on the <a href="http://www.cis.upenn.edu/%7Extag/spinal">LTAG-spinal website</a>.
     * @param showSpines if true, shows the internal structure of the elementary trees; 
     * otherwise, shows each elementary tree as one single node
     * @throws java.io.IOException if an error occurs during writing
     */
    public void writeGraphvizTo(BufferedWriter b, int start, int end, 
            boolean includeSpans, boolean beanPoleStyle,boolean showSpines) throws IOException {
        b.write("digraph {");
        b.newLine();
        
        // write out location
        b.write("node_location[label=\""
                + this.prettyPrintLocation()
                + "\" shape=box];");
        b.newLine();
        
        
        if (this.isSkipped()) {
            b.write("skip;");
        } else {
            
            if (showSpines && !this.isBidirectionalParserOutput()) {
                // Create cluster with all the terminal nodes
                // in order to line them up horizontally.
                // We only do this if we actually have spines to show (i.e.
                // not if we have bidirectional parser output) and if we are 
                // asked to show the spines (i.e. showSpines == true) because otherwise
                // the sentence becomes unreadable.
                b.write("subgraph { rank = same; edge [style=invis] ");
                Iterator trees;
                if (start==-1 || end==-1) {
                    trees = this.getElemTrees().listIterator();
                } else {
                    trees = this.getElemTrees(start,end).listIterator();
                }
                String previousID = null, currentID = null;
                while (trees.hasNext()) {
                    ElemTree current = (ElemTree) trees.next();
                    previousID = currentID;
                    currentID = current.getGraphvizNodeID();
                    if (previousID != null) {
                        b.write(" -> ");
                    }
                    b.write(currentID);
                }
                b.write("; }");
                b.newLine();
            }            
            
            // try to find appropriate subtree
            ElemTree subTree = null;
            if ((start >=0) && (end >= start)) { // if we aren't supposed to output the whole tree
                subTree = this.getSubTree(start,end); 
                // may return null if can't find anything
            }        
            
            if (subTree == null) { // we didn't find anything, or we're working on the whole tree
                subTree = this.getRoot();
            }
            
            
            subTree.writeGraphvizTo(b, start, end, includeSpans, beanPoleStyle, showSpines);
        }
        
        b.newLine();
        b.write("}");
        b.flush();
        return;
        
    }
    
    /**
     * Writes a visual representation of this sentence in Graphviz format 
     * to the specified {@link java.io.Writer}.
     * 
     * @param w the {@link java.io.Writer} to which this sentence is to be written
     * @param includeSpans if true, the word span of the subtree dominated by a node
     * is appended to that node's representation; otherwise, it only
     * consists of the node label
     * @param beanPoleStyle chooses between two very different styles of output -- if true,
     * the output looks like beanpoles, if false, it looks like tadpoles. See the 
     * illustrations on the <a href="http://www.cis.upenn.edu/%7Extag/spinal">LTAG-spinal website</a>.
     * @param showSpines if true, shows the internal structure of the elementary trees; 
     * otherwise, shows each elementary tree as one single node
     * @throws java.io.IOException if an error occurs while writing
     */
    public void writeGraphvizTo(Writer w, boolean includeSpans, boolean beanPoleStyle,boolean showSpines) throws IOException {
        this.writeGraphvizTo(new BufferedWriter(w), -1, -1, includeSpans, beanPoleStyle, showSpines);
    }
    
    /**
     * Writes a visual representation of a subpart of this sentence in Graphviz format 
     * to the specified {@link java.io.Writer}.
     * 
     * 
     * @param w the {@link java.io.Writer} to which the subsentence is to be written
     * @param start the first word of the sentence to be included in the graphical output
     * @param end the last word of the sentence to be included in the graphical output
     * @param includeSpans if true, the word span of the subtree dominated by a node
     * is appended to that node's representation; otherwise, it only
     * consists of the node label
     * @param beanPoleStyle chooses between two very different styles of output -- if true,
     * the output looks like beanpoles, if false, it looks like tadpoles. See the 
     * illustrations on the <a href="http://www.cis.upenn.edu/%7Extag/spinal">LTAG-spinal website</a>.
     * @param showSpines if true, shows the internal structure of the elementary trees; 
     * otherwise, shows each elementary tree as one single node
     * @throws java.io.IOException if an error occurs while writing
     */
    public void writeGraphvizTo(Writer w, int start, int end, boolean includeSpans, boolean beanPoleStyle,boolean showSpines) throws IOException {
        this.writeGraphvizTo(new BufferedWriter(w), start, end, includeSpans, beanPoleStyle, showSpines);
    }

    /**
     * Writes a visual representation of this sentence in Graphviz format 
     * to the specified {@link java.io.BufferedWriter}.
     * 
     * 
     * @param b the {@link java.io.BufferedWriter} to which this sentence is to be written
     * @param includeSpans if true, the word span of the subtree dominated by a node
     * is appended to that node's representation; otherwise, it only
     * consists of the node label
     * @param beanPoleStyle chooses between two very different styles of output -- if true,
     * the output looks like beanpoles, if false, it looks like tadpoles. See the 
     * illustrations on the <a href="http://www.cis.upenn.edu/%7Extag/spinal">LTAG-spinal website</a>.
     * @param showSpines if true, shows the internal structure of the elementary trees; 
     * otherwise, shows each elementary tree as one single node
     * @throws java.io.IOException if an error occurs while writing
     */
    public void writeGraphvizTo(BufferedWriter b, boolean includeSpans, boolean beanPoleStyle,boolean showSpines) throws IOException {
        this.writeGraphvizTo(b, -1, -1, includeSpans, beanPoleStyle, showSpines);
    }
    
    /**
     * Returns a string representation of this sentence in LTAG-spinal format.
     *
     * @return a string representing this sentence
     */
    public String toString() {
        
        StringWriter sw = new StringWriter(100);
        try {
            this.writeTo(new BufferedWriter(sw));
        } catch (IOException ex) {
            // can't occur with a StringWriter
            ex.printStackTrace();
        }
        return sw.toString();
    }
    
    
    /**
     * Returns a visual representation of this sentence in Graphviz format.
     * @param includeSpans if true, the word span of the subtree dominated by a node
     * is appended to that node's representation; otherwise, it only
     * consists of the node label
     * @param beanPoleStyle chooses between two very different styles of output -- if true,
     * the output looks like beanpoles, if false, it looks like tadpoles. See the 
     * illustrations on the <a href="http://www.cis.upenn.edu/%7Extag/spinal">LTAG-spinal website</a>.
     * @param showSpines if true, shows the internal structure of the elementary trees; 
     * otherwise, shows each elementary tree as one single node
     * @return a <code>String</code> containing Graphviz format
     */
    public String toGraphviz(boolean includeSpans, boolean beanPoleStyle,boolean showSpines) {
        return this.toGraphviz(-1,-1, includeSpans, beanPoleStyle, showSpines);
    }
    
    
    
    /**
     * Returns a visual representation of a subpart of this sentence in Graphviz format.
     * 
     * 
     * @param start the first word of the sentence to be included in the graphical output
     * @param end the last word of the sentence to be included in the graphical output
     * @param includeSpans if true, the word span of the subtree dominated by a node
     * is appended to that node's representation; otherwise, it only
     * consists of the node label
     * @param beanPoleStyle chooses between two very different styles of output -- if true,
     * the output looks like beanpoles, if false, it looks like tadpoles. See the 
     * illustrations on the <a href="http://www.cis.upenn.edu/%7Extag/spinal">LTAG-spinal website</a>.
     * @param showSpines if true, shows the internal structure of the elementary trees; 
     * otherwise, shows each elementary tree as one single node
     *
     * @return a <code>String</code> containing Graphviz format
     */
    public String toGraphviz(int start, int end, boolean includeSpans, boolean beanPoleStyle,boolean showSpines) {
        StringWriter sw = new StringWriter(100);
        try {
            this.writeGraphvizTo(new BufferedWriter(sw), start, end, includeSpans, beanPoleStyle, showSpines);
        } catch (IOException ex) {
            // can't occur with a StringWriter
            ex.printStackTrace();
        }
        return sw.toString();
        
    }
    
    /**
     * Returns a <code>String</code> representing the location of the current sentence
     * -- i.e. either a triple of section, file, and sentence number as
     * in the LTAG-spinal treebank (following the Penn Treebank conventions),
     * or simply a sentence number if the sentence is not from the 
     * LTAG-spinal treebank.
     * @return three numbers indicating where this sentence is found in
     * the input
     */
    
    public String getLocation() {
        String result = "";
        if (sectionNumber != -1 && fileNumber != -1) {
            result += sectionNumber + " " + fileNumber + " ";
        }
        return result + sentenceNumber;
    }
    
    /**
     * Returns a human-readable string representing the location of the current sentence.
     * If the sentence is taken from the LTAG-spinal treebank, the string looks 
     * as follows:
     * <pre>
     * Section: X File: Y Sentence: Z
     * </pre>
     * Otherwise,
     * if the sentence only has a sentence number, the string looks like
     * <pre>
     * Sentence: Z
     * </pre>
     * @return a human-readable string indicating where this sentence is found in
     * the input
     */
    public String prettyPrintLocation() {
        String result = "";
        if (sectionNumber != -1 && fileNumber != -1) {
            result += "Section: " + sectionNumber
                    + "  File: " + fileNumber + "  ";
        }
        return result + "Sentence: " + sentenceNumber;
    }
    
    /**
     * Returns the unique <code>ElemTree</code> that is the root of a subtree whose
     * yield is the specified word span, or null if there is no such
     * tree.
     * 
     * @param w the span from the first up to and including the last word
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     * @return an <code>ElemTree</code> or null
     */
    public ElemTree getSubTree(WordSpan w) {
        return this.getSubTree(w.start(), w.end());
    }
    
    
    
    /**
     * Returns the unique <code>ElemTree</code> that is the root of a subtree whose
     * yield is the specified word span, or null if there is no such
     * tree.
     * 
     * @param start the first (leftmost) word included in the span
     * @param end the last (rightmost) word included in the span
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     * @return an <code>ElemTree</code> or null
     */
    public ElemTree getSubTree(int start, int end) {
        
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }
        
        if (this.length()-1 < end) {
            throw new IllegalArgumentException("Attempted to retrieve a subtree from" +
                    "a WordSpan that spans outside of the sentence");
        }
        
        // TODO add warnings if start > end, start < 0, end < 0, etc. here and elsewhere
        
        List candidates = this.getElemTrees(start, end);
        Iterator iter = candidates.iterator();
        ElemTree current;
        WordSpan span;
        while (iter.hasNext()) {
            current = (ElemTree) iter.next();
            span = current.getSpan();
            if (start == span.start() && end == span.end()) {
                return current;
            }
        }
        // if we haven't found anything...
        return null;
    }
    
    /**
     * Returns true if this <code>Sentence</code> has been read in from the 
     * format used in the output of Shen's bidirectional parser. If this is the case,
     * no information about the spine is present. This is implemented as a simple
     * lookup of the corresponding property of the <code>ElemTree</code> at
     * the root of this sentence.
     * @return a boolean value
     * @see ElemTree#isBidirectionalParserOutput()
     */
    public boolean isBidirectionalParserOutput() {
        return this.getRoot().isBidirectionalParserOutput();
    }

    
    /**
     * Returns true iff the annotation for this sentence only consists of the word "skip",
     * indicating that it is contained in the Penn Treebank but
     * not in the LTAG-spinal treebank.
     * @return true iff this sentence is skipped in the LTAG-spinal treebank
     */
    public boolean isSkipped() {
        return this.skip;
    }
    
    /**
     * Returns the number of the current sentence in the Penn Treebank file or parser output.
     * @return the sentence number
     */
    public int getSentenceNumber() {
        return sentenceNumber;
    }
    
    /**
     * Returns the number of the Penn Treebank section in which the current sentence
     * occurred, or -1 if the sentence is not a Penn Treebank sentence.
     * @return the section number, or -1 if there is no such number
     */
    public int getSectionNumber() {
        return sectionNumber;
    }
    
    /**
     * Returns the number of the Penn Treebank file in which the current sentence
     * occurred, or -1 if the sentence is not a Penn Treebank sentence.
     * @return the file number, or -1 if there is no such number
     */
    public int getFileNumber() {
        return fileNumber;
    }
    
    
    /**
     * Force the spans to be computed recursively on every <code>ElemTree</code>.
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     */
    private void computeSpans() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        this.getRoot().computeSpan();
        spansComputed = true;
    }
    
    /**
     * Returns the <code>ElemTree</code> whose yield is the given word span, 
     * or null if there isn't one.
     * @param w the <code>WordSpan</code> for which the dominating tree 
     * is to be returned
     * @return an <code>ElemTree</code> or null
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     */
    public ElemTree subTreeForSpan(WordSpan w) {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        if (!spansComputed) computeSpans();
        if (!spanTableComputed) computeSpanTable();
        if (spanTable.containsKey(w)) {
            return (ElemTree) spanTable.get(w);
        } else {
            return null;
        }
    }
    
    /**
     * Computes the span table, a directory whose keys are word spans and whose
     * values are the corresponding subtrees (if any).
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     */
    public void computeSpanTable() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        if (!spansComputed) computeSpans();
        Iterator iter = this.elemTreesIterator();
        spanTable = new HashMap(this.length());
        ElemTree current;
        String bugs="/Users/lingrad2/srl/ltagtb/bugs/";
        while (iter.hasNext()) {

            current = (ElemTree) iter.next();
            
            
            
// The following code was used to retrieve some buggy sentences.
// "There are at most 28 sentences in the corpus like that. So we can
//ignore them for now, or fix them by hand when we have the chance
//(which would lead us to the question of whether we should put the
//treebank under version control). I'm attaching a zipfile with the
//sentences in question. These are .dot files that you can open in
//graphviz or convert using "dot -Tjpg -o output.jpg filename.dot".
//Actually not all of them have the bug. The common property of the 28
//sentences is that in each of them there are at least two subtrees of
//the derivation tree with identical yield. See the .info files." 
// (Mail by Lucas Champollion to Joshi and Libin Nov 29 2006)


            
            
//            String lws = this.getLocation().replaceAll(" ", "_");
//            // location with underscores
//            if (spanTable.containsKey(current.getSpan())) {
//                try {
//                    
//                    BufferedWriter f = new BufferedWriter
//                            (new FileWriter(bugs+lws+".dot"));
//                    this.writeGraphvizTo(f);
//                    f.close();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//                BufferedWriter f;
//                try {
//                    f = new BufferedWriter(new FileWriter(bugs+lws + ".info"));
//                    f.write("location: " + this.getLocation());
//                    f.newLine();
//                    f.write("span: " + current.getSpan());
//                    f.newLine();
//                    f.write("current: " + current);
//                    f.newLine();
//                    f.write("stored: " + (ElemTree) spanTable.get(current.getSpan()));
//                    f.newLine();
//                    f.close();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//                
//            }
            spanTable.put(current.getSpan(), current);
        }
        spanTableComputed=true;
    }
    
    /**
     * Returns true iff at least one of the elementary trees in this 
     * <code>Sentence</code> is an initial tree.
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     * @return true iff there is at least one attachment operation in the 
     * present tree
     */
    public boolean containsAttachment() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }
        Iterator iter = this.elemTreesIterator();
        
        boolean result = false;
        
        while (iter.hasNext()) {
            ElemTree current = (ElemTree) iter.next();
            if (current.isInitial()) result = true;
        }
        return result;
    }
    
    /**
     * Returns true iff at least one of the elementary trees in this 
     * <code>Sentence</code> is an auxiliary tree.
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     * @return true iff there is at least one adjunction operation in the 
     * present tree
     */
    public boolean containsAdjunction() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }
        Iterator iter = this.elemTreesIterator();
        
        boolean result = false;
        
        while (iter.hasNext()) {
            ElemTree current = (ElemTree) iter.next();
            if (current.isAuxiliary()) result = true;
        }
        return result;
    }

    /**
     * Returns true iff at least one of the elementary trees in this 
     * <code>Sentence</code> is a conjunction tree.
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     * @return true iff there is at least one coordination operation in the 
     * present tree
     */
    public boolean containsCoordination() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }
        Iterator iter = this.elemTreesIterator();
        
        boolean result = false;
        
        while (iter.hasNext()) {
            ElemTree current = (ElemTree) iter.next();
            if (current.isCoord()) result = true;
        }
        return result;
    }
    
//    public void computeExtendedSpanTable() {
//        computeSpanTable();
//        
//        // extended span table is in the same style but trees occur
//        // under multiple entries: a tree may be pruned by removing any
//        // number of children
//        throw new RuntimeException("Not implemented yet");
//    }
    
    
    /**
     * Returns the length of this <code>Sentence</code>, that is, the number of elementary
     * trees in this derivation tree.
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     * @return the number of elementary trees in this <code>Sentence</code>
     */
    public int length() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        return elemTrees.size();
    }
    
    /**
     * Returns the elementary tree at the root of this <code>Sentence</code>.
     * 
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     * @return the <code>ElemTree</code> in which this derivation tree is rooted
     */
    public ElemTree getRoot() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        return root;
    }
    
    /**
     * Iterates over the elementary trees of which this <code>Sentence</code> 
     * consists, in the order in which they are numbered (left to right in the 
     * sentence). 
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     * @return a <code>ListIterator</code> 
     */
    public ListIterator elemTreesIterator() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        return elemTrees.listIterator();
    }
    
    /**
     * Returns the <code>ElemTree</code> associated with the <code>n</code>th word of
     * the sentence.
     * @param n a number between 0 and the length of the sentence
     * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     * @return an <code>ElemTree</code> for the <code>n</code>th word of the sentence
     */
    public ElemTree getElemTree(int n) {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        return (ElemTree)elemTrees.get(n);
    }
    
    /**
     * Returns a <code>List</code> of <code>ElemTree</code>s for 
     * the given word span.
     * @return an ordered list containing some of the elementary trees of which 
     * this sentence consists
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     */
    public List getElemTrees() {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        return this.elemTrees;
    }
    
    /**
     * Returns a <code>List</code> of <code>ElemTree</code>s for 
     * the given word span.
     * @param from the first word to be included in the list
     * @param to the last word to be included in the list
     * @return an ordered list containing some of the elementary trees of which 
     * this sentence consists
     * @throws edu.upenn.cis.spinal.SkippedSentenceException if the current sentence 
     * is a skipped sentence in the LTAG-spinal treebank
     */
    public List getElemTrees(int from, int to) {
        if (this.isSkipped()) {
            throw new SkippedSentenceException(this);
        }

        return this.elemTrees.subList(from, to+1);
        // the "+1" is because our spans are inclusive but subList is exclusive
    }
    


    


    
}