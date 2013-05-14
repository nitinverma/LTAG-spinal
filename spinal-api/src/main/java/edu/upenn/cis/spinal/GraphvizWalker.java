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

/**
 * Walks through a treebank, reads it in, and outputs representations 
 * of the trees in Graphviz format. Alternatively, selects a given sentence (identified by its location)
 * in the treebank and outputs a Graphviz representation for that sentence.
 * <pre>
 * Usage: java edu.upenn.cis.spinal.GraphvizWalker &lt;infile&gt; [&lt;sentence_location&gt;] 
 * Output will be placed in files named &lt;sentence_location&gt;.dot.
 * If &lt;sentence_location&gt; is not given, then all the sentences in the file will be processed and placed into a separate file each.
 * </pre>
 *
 * @author Lucas Champollion
 */
public class GraphvizWalker extends AbstractWalker {

// Some commented-out code indicates how this class can be modified to only 
// search for trees that fulfill certain criteria.

    // modify these to get different behavior.
    boolean includeSpans = true;
    boolean beanPoleStyle = false;
    boolean showSpines = true;
    
    int numSentences=0, numMultirooted=0;
    
    
    String location = null; // the location of the sentence we want to display
    
    BufferedWriter out;
    
    /** Creates a new instance of <code>GraphvizWalker</code>. */
    public GraphvizWalker() {
        
    }
    
    protected void init() {
         if (args.length == 0) {
             printUsage();
             System.exit(1);
         }
         
         if (args.length > 1) {
             
             // the rest of the args specify a sentence location
             this.location = "";
             for (int i = 1; i < args.length; i++) {
                
                 location += args[i];
                 location += " ";
             }
             location = location.trim();
         }
        
    }
    
    public void forEachSentence(Sentence s) {
        
        // The following filter can be used to find short sentences that exemplify both
        // adjunction and coordination.
        
//        if (s.isSkipped() || !s.containsAdjunction() || !s.containsCoordination() || s.length() > 18) {
//            return;
//        }
        

        if (this.location == null || this.location.equals(s.getLocation())) {
            try {
                this.out = new BufferedWriter
                        (new FileWriter(s.getLocation().replaceAll(" ", "_")+".dot"));
                this.out.write(s.toGraphviz(includeSpans, beanPoleStyle, showSpines));
                this.out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (this.location != null && this.location.equals(s.getLocation())) {
                    terminate();
                }
            }
        }
    }
    
    protected void wrapUp() {
        try {
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Prints out the following message:
     * 
     * <pre>
     * Usage: java edu.upenn.cis.spinal.GraphvizWalker &lt;infile&gt; [&lt;sentence_location&gt;] 
     * Output will be placed in files named &lt;sentence_location&gt;.dot.
     * If &lt;sentence_location&gt; is not given, then all the sentences in the file will be processed and placed into a separate file each.
     * </pre>
     */
    protected void printUsage() {
        System.out.println("Usage: java edu.upenn.cis.spinal.GraphvizWalker " +
                "<infile> [<sentence_location>] ");
        System.out.println("Output will be placed in files named <sentence_location>.dot.");
        System.out.println("If <sentence_location> is not given, then all the sentences " +
                "in the file will be processed and placed into a separate file each.");
    }
    
    /**
     * Main method, call from command line.
     * @param argv the command line arguments
     */
    public static void main(String argv[]) {
        new GraphvizWalker().process(argv);
    }
}

