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

import java.util.*;

/**
 * Implements a way of referring unambiguously to a particular
 * node in a tree. A Gorn address a<SUB>1</SUB>, a<SUB>2</SUB>,...a<SUB>n-1</SUB>, a<SUB>n</SUB> denotes the
 * a<SUB>n</SUB>th child of the a<SUB>n-1</SUB>th child of .... the
 * a<SUB>2</SUB>th child of the root. The root itself is always represented as
 * zero. Gorn addresses are used in the LTAG-spinal
 * annotation to specify attachment sites in spinal nodes.
 * @author Lucas Champollion
 */
public class GornAddress extends ArrayList {
    
    /**
     * The symbol used to separate elements of the Gorn address from one another.
     */
    public static final String SEPARATOR = ".";
    
    /**
     * The separator in regex format.
     */
    private static final String SEPARATOR_REGEX = "\\.";
    
    
    /**
     * Creates a Gorn address from a string representation.
     * @param s a string that consists of a series of 1 or more integers separated by dots, 
     * such as <code>0</code> or <code>0.1.1</code>
     * @throws java.lang.NumberFormatException if the string cannot be parsed 
     * into numbers
     */
    public GornAddress(String s) throws NumberFormatException {
        super();
        String[] parts = s.split(SEPARATOR_REGEX);
        for (int i=0; i<parts.length;i++) {
            this.add(new Integer(parts[i]));
        }
    }
    
    /**
     * Returns the canonical representation of this Gorn address -- a series of 
     * integers separated by dots.
     * @return a string like <code>0</code> or <code>0.1.1</code>
     */
    public String toString() {
    
        return this.toString(SEPARATOR);
    }
    
    /**
     * Returns a custom representation of this Gorn address -- a series of 
     * integers separated by a user-supplied argument.
     * 
     * @param separator the string used to separate the integers
     * @return a string like <code>0</code> or <code>0_1_1</code> (if <code>_</code> 
     * is provided as the separator)
     */
    public String toString(String separator) {
        StringBuffer s = new StringBuffer(10);
        
        Iterator iter = this.iterator();
        while (iter.hasNext()) {
            Integer current = (Integer) iter.next();
            s.append(current.toString());
            s.append(separator);
        }
        String result = s.toString();
        if (result.endsWith(separator)) {
            result = result.substring(0, result.length()-1);
        }
        return result;       
    }
    
    /**
     * Returns a hash code based on the canonical representation as indicated 
     * by the {@link #toString()} value.
     * @return a hash code
     */
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    
    /**
     * Returns if this is equal to another <code>GornAddress</code>.
     * Two Gorn addresses are equal iff their
     * canonical representation as indicated by {@link #toString()} is identical.
     * 
     * @param o the other object
     * @return a boolean value
     */
     public boolean equals(Object o) {
         if (o instanceof GornAddress) {
             return this.toString().equals(o.toString());
         } else {
             return super.equals(o);
         }
     }
        
}
