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

/**
 * Inherit from this abstract class to create standalone command-line applications 
 * that operate on a file containing an LTAG-spinal annotated sentence or sentences.
 * See the other <code>XYZWalker</code> classes in this packages for examples.
 * All inherited classes should include the following method:
 * <pre> 
 * public static void main(String argv[]) {
 *      new XYZWalker().process(argv);
 * }
 * </pre>
 * 
 * @author Lucas Champollion
 *
 */
public abstract class AbstractWalker {
    
        /**
         * Contains the arguments that have been passed to {@link #process(String[])}.
         */
        protected String[] args;
        
        /**
         * Indicates whether {@link #terminate()} has been called.
         */
        protected boolean terminate=false;
                
        /**
         * Returns the string array of arguments that has been passed to 
         * {@link #process(String[])}.
         * 
         * @return the arguments
         */
        protected String[] getArgs() {
            return this.args;
        }
        

        /**
         * Call this method from within {@link #forEachSentence(Sentence)} to 
         * instruct {@link #process(String[])} to skip any remaining sentences. 
         * {@link #process(String[])} will 
         * still call {@link #wrapUp()} even after
         * this method is invoked.
         *
         * 
         */
        protected void terminate() {
            this.terminate=true;
        }
       
        /**
         * This method calls {@link #init()}, then opens a file in LTAG-spinal format
         * using the first element
         * of the <code>args</code> array as the filename, and reads in elementary trees from that file.
         * On each of these trees, it calls {@link #forEachSentence(Sentence)}.
         * When {@link #terminate()} is called or the end of the file is reached, 
         * the method calls {@link #wrapUp()}. 
         *
         * @param args an array of arguments. The first of them is used as a filename
         * to indicate the location of the file containing LTAG-spinal annotation.
         *
         */
	protected void process(String[] args) {
            
                this.args = args;
                
                if (args.length == 0) {
                    printUsage();
                    return;
                }
                
                init();
                
                
		String filename=args[0];
		BufferedReader input=null;
		
		try {
			input=new BufferedReader(new FileReader(filename));
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
                
		
		Sentence t=null;
		
		
		try {
			while ((t=Sentence.readTree(input))!=null) {
                            
                            forEachSentence(t);
                            if (terminate) return;
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
		
                wrapUp();
	}
        
        
        /**
         * Implementations of this method should specify what (if anything)
         * the class should do before attempting to read in a file.
         *
         */
        protected abstract void init();
        
        /**
         * Implementations of this method should specify what the class
         * should do on reading in a sentence (a derivation tree) from the file.
         *
         * @param t the current <code>Sentence</code> object
         */
        public abstract void forEachSentence(Sentence t);
        
        /**
         * Implementations of this method should specify what (if anything)
         * the class should do upon reading all sentences from the file. The
         * {@link #process(String[])} method also calls this if {@link #terminate()}
         * has been invoked.
         *
         */
        protected abstract void wrapUp();

        /**
         * Implementations of this method should print out a short string 
         * to <code>stdout</code>
         * describing the function of the class. This method is automatically
         * called by {@link #process(String[])} when no command-line
         * argument is passed.
         *
         */
        protected abstract void printUsage();
        
	
}