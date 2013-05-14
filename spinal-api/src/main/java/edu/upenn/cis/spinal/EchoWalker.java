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
 * Walks through a treebank, reads it in, and prints it to <code>stdout</code>
 * unchanged. This class is provided as a simple example of an implementation
 * of {@link AbstractWalker}. It can be used for debugging purposes when testing 
 * the <code>toString()</code> methods. This class can also print
 * to file if that file is given as a second argument.
 *
 * @author Lucas Champollion
 */
public class EchoWalker extends AbstractWalker {
    
    int numSentences=0, numMultirooted=0;
    
    BufferedWriter out;
    
    /** Creates a new instance of <code>EchoWalker</code>. */
    public EchoWalker() {
        
    }
    
    protected void init() {
         if (args.length == 0) {
             System.err.println("Please supply" +
                 " at least one argument.");
             return;
         }
         if (args.length == 1) {
             this.out = new BufferedWriter
                     (new OutputStreamWriter(System.out));
         }
         if (args.length == 2) {
            try {
                this.out = new BufferedWriter
                        (new FileWriter(args[1]));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
         }
        
    }
    
    public void forEachSentence(Sentence s) {
        try {
            this.out.write(s.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    protected void wrapUp() {
        try {
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    protected void printUsage() {
        System.out.println("Walks through a treebank, reads it in, and prints it " +
                "to stdout unchanged. Alternatively, prints to file if that " +
                "file is given as a second argument.\n" +
                "Syntax: java edu.upenn.cis.spinal.EchoWalker <infile> [<outfile>]");
    }
    
    /**
     * Main method, call from command line.
     * @param argv the command line arguments
     */
    public static void main(String argv[]) {
        new EchoWalker().process(argv);
    }
}

