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
 * Represents the spine of a spinal elementary tree.
 * A typical line in the treebank that contains a spine looks like this:
 * 
 * <pre>
 * b_( VP VB^ VP* )
 * </pre>
 *
 * However, the first letter indicates a property of the elementary tree
 * (whether it is initial, auxiliary, or coordination) and is strictly speaking
 * not a part of the spine, nor is the underscore folloing it. 
 * So we only consider the following a spine:
 *
 * <pre>
 * ( VP VB^ VP* )
 * </pre>
 *
 * Other examples of spines:
 *
 * <pre>
 * ( S ( VP VBD^ ) )
 * 
 * ( S ( VP ( XP JJ^ ) ) )
 * 
 * ( XP NONE^ )
 * 
 * JJ^                       
 * 
 * ( S S S )                 # a spine for predicate coordination
 * </pre>
 *
 * Spines may be of three shapes (Libin Shen's thesis, p. 15):
 * <ul>
 *   <li>A spinal initial tree is composed of a lexical spine from the root 
 * to the anchor, and nothing else. 
 *   <li>A spinal auxiliary tree is composed of a lexical spine and a recursive
 * spine from the root to the foot node. (The common part of a lexical spine 
 * and a recursive spine is called the shared spine of an auxiliary tree.)
 *   <li>It is not clear what exactly are the constraints on the shape of 
 * coordination trees. However these trees are not really linguistically
 * meaningful and might be removed or normalized in a future version of the treebank.
 *</ul>
 * Instances of this class are immutable as seen from the "public" view.
 *
 * @author Lucas Champollion
 */
public class SpinalNode {
        
        
    /**
     * Represents the character used to show that a node is an anchor node
     * (the node where the word is attached), i.e. "<code>^</code>".
     */
        public static final String ANCHOR = "^";
        
    /**
     * Some alternative characters that might be used for the anchor because they
     * look similar. These are:
     * 
     * <ul>
     * <li><code>\u02C6 \\u02C6</code> -- unicode modifier letter circumflex accent
     * <li><code>\u005E \\u005E</code> -- unicode circumflex accent
     * <li><code>\u0302 \\u0302</code> -- unicode combining circumflex accent
     * </ul>
     */
        public static final String ANCHORS 
                = "\u02C6" // unicode modifier letter circumflex accent
                + "\u005E" // unicode circumflex accent
                + "\u0302" // unicode combining circumflex accent
                ;
    /**
     * Represents the character used to show that a node is a foot node,
     * in an auxiliary tree, i.e. "<code>*</code>".
     */
        public static final String FOOT = "*";
    /**
     * Represents the character used to show that a node is neither an 
     * anchor nor a foot node (this character is the empty string in Libin Shen's
     * treebank).
     */
        public static final String STANDARD = "";
        
        private ElemTree host;
        
        private SpinalNode parent; // null indicates that we're at the root of the spine
        
        private String label;
               
        private String type; // either anchor or foot or standard
        
        private SpinalNode[] children = new SpinalNode[0];

        
        private SpinalNode(ElemTree host, SpinalNode parent) {
            this.host = host;
            this.parent = parent;
            label=null;
            type="";
        }
        
    
    /**
     * Creates a new instance of <code>SpinalNode</code> from a string representation.
     * @param s the string representation from which to create the <code>SpinalNode</code>
     * @param host the <code>ElemTree</code> to which the spine belongs of which this 
     * <code>SpinalNode</code> is a member, or null if you don't wish to specify it
     * @throws ElemTreeFormatException if the input string can't be parsed
     */
    public SpinalNode(String s, ElemTree host) throws ElemTreeFormatException {
        
        this(Arrays.asList(s.split(" ")), host); // tokenize
    }
    
    /**
     * Creates an instance of <code>SpinalNode</code> from a tokenized list of strings.
     * The list is not modified during the construction of the <code>SpinalNode</code>.
     * 
     * @param list the list of strings
     * @param host the <code>ElemTree</code> to which the spine belongs of which this 
     * <code>SpinalNode</code> is a member, or null if you don't wish to specify it
     * @throws ElemTreeFormatException if list is empty
     * @throws NullPointerException if list is null
     */
    protected SpinalNode(List list, ElemTree host) throws ElemTreeFormatException {
        if (list == null) throw new NullPointerException();
        if (list.size() == 0) throw new ElemTreeFormatException("Can't create a " +
                "SpinalNode from an empty string.");
        
        this.host=host;
        this.parent=null;
        
        if (list.size() == 1) { // list is of shape: JJ^
           parseLabelAndType((String) list.get(0)); // updates this.type and this.label
           this.children=new SpinalNode[0];
        } else { // e.g. list is of shape:  ( XP NNS^ )
            
            list = stripSurroundingBrackets(list); // eg. list is now XP NNS^
            
            parseLabelAndType((String) list.get(0)); // eg. parse XP
            this.children = parseChildren(list.subList(1,list.size())); //  e.g. parse NNS^
            // tell the children who is the boss!
            for (int i = 0; i < this.children.length; i++) {
                this.children[i].parent = this;
            }
        }
      
    }
    
    
    /**
     * Updates the instance variables <code>type</code> and <code>label</code> when passed a label string.
     * @param label the string representing the label
     */
    private void parseLabelAndType(String label) {
        String labelEnd = label.substring(label.length()-1, label.length());
        if (label.endsWith(ANCHOR) || (ANCHORS.indexOf(labelEnd) != -1)) {
            this.type=ANCHOR;
            this.label=label.substring(0,label.length()-1);
        } else if (label.endsWith(FOOT)) {
            this.type=FOOT;
            this.label=label.substring(0,label.length()-1);
        } else {
            this.type=STANDARD;
            this.label=label;
        }
        
    }
    
 
    
    
    /**
     * Internal method, called recursively while parsing a <code>SpinalNode</code>
     * representation.
     * @param list a list containing string representations of the children
     * @return an array of spinal nodes representing the children of the current node
     * @throws ElemTreeFormatException if a parse error occurs
     */
    private SpinalNode[] parseChildren(List list) throws ElemTreeFormatException {
        
        // If the list are surrounded by brackets, then the first element
        // of the list is the mother of all the other elements. This is also
        // true if there is only one part (it need not be surrounded by
        // brackets). Otherwise,
        // all the elements on the list are siblings.
        
        // Returns null if list is empty
        
        if (list.isEmpty()) return null;
        
        if (list.size() == 1) { // list is of shape: JJ^
           SpinalNode onlyChild = new SpinalNode(this.host, this);
           onlyChild.parseLabelAndType((String) list.get(0));
           onlyChild.setChildren(new SpinalNode[0]);
           return new SpinalNode[] { onlyChild };
        } else { // collect children
            ArrayList childrenList = new ArrayList();
            while (!list.isEmpty()) {
                childrenList.add(getNodeFrom(list));
                list = rest(list);
            } 
            
            return (SpinalNode[]) childrenList.toArray(new SpinalNode[0]);
        }
    }
 
    /**
     * Returns the first node from a tokenized spine - that is, the first token,
     * or if the first token is "(", then the content of the first pair of 
     * brackets.
     * @param tokens the list
     * @throws ElemTreeFormatException if a parse error occurs
     * @return the first node from the list
     */
    private SpinalNode getNodeFrom(List tokens) 
    throws ElemTreeFormatException {
        if (tokens.get(0).equals("(")) {
            List subList = tokens.subList(0, whereIsClosingBracket(tokens)+1);
            return new SpinalNode(subList, this.host);
        } else {
            LinkedList l = new LinkedList();
            l.add(tokens.get(0));
            return new SpinalNode(l, this.host);
        }
    }
    
    /**
     * Removes the first child from the list and returns the rest of the list,
     * or returns the empty list if the list is initially empty.
     * (Actually we don't modify the list, we just provide a view on the 
     * appropriate sublist.)
     * @param tokens the list
     * @throws ElemTreeFormatException if a parse error occurs
     * @return the list with the first element removed
     */
    private static List rest(List tokens)
    throws ElemTreeFormatException {
        if (tokens.isEmpty()) return tokens;
        if (tokens.get(0).equals("(")) {
            return tokens.subList
                    (whereIsClosingBracket(tokens)+1, tokens.size());
        } else { // just skip the first element
            return tokens.subList(1, tokens.size());
        }
        
    }
    
    /**
     * Takes a List of strings. If the first and last elements of
     * the List are ( and ) respectively, they're stripped off and this
     * is repeated recursively. Otherwise the List is returned unchanged.
     * (Actually we don't modify the list, we just provide a view on the 
     * appropriate sublist.)
     *
     * @param tokens the list
     * @return the list with one pair of brackets removed
     */
    private static List stripSurroundingBrackets(List tokens) {
        if (tokens.get(0).equals("(")) {
            if (tokens.get(tokens.size()-1).equals(")")) { // the last element
                return stripSurroundingBrackets(tokens.subList(1, tokens.size()-1));
            } else { // unbalanced brackets
                return tokens;
            }   
        } else { // nothing to do
            return tokens;
        }
    }
    
    /**
     * Takes a List of strings and returns the position (counting
     * upwards from zero) of 
     * the bracket that matches the List's initial bracket.
     * If the list doesn't start with a bracket then -1 is returned.
     * @param tokens the list
     * @return the position of the closing bracket matching the initial one
     */
     private static int whereIsClosingBracket(List tokens) {
        
        int pos = -1;
        
        if (!tokens.get(0).equals("(")) {
            return -1;
        } else {
            int stack = 0;
            Iterator iter = tokens.iterator();
            String current;
            while (iter.hasNext()) {
                current = (String) iter.next();
                pos++;
                if (current.equals("(")) {
                    stack++;
                } else if (current.equals(")")) {
                    stack--;
                    if (stack == 0) {
                        return pos;
                    } else if (stack < 0) {
                        throw new RuntimeException("Bad spine");
                    } else { // stack > 0
                        // do nothing
                    }
                } // end if (current == ")")
            } // end iteration over tokens
            if (stack != 0) throw new RuntimeException("Bad spine");
            return tokens.size()-1;
        } // end else
        
    } // end whereIsClosingBracket

    /**
     * Gets the label of this spinal node.
     * @return A string, e.g. <code>XP</code> or <code>NNS</code>. 
     * Characters that denote foot nodes or 
     * anchors are not returned.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the type of this <code>SpinalNode</code>. Returns one of "ANCHOR", "FOOT", or 
     * "STANDARD".
     * @return Either ANCHOR, FOOT, or STANDARD
     */
    public String getType() {
        return type;
    }
    
    /**
     * Returns whether this node is an anchor node.
     * @return a boolean value
     */
    public boolean isAnchor() {
        return type.equals(ANCHOR);
    }

    /**
     * Returns whether this node is a foot node.
     * @return a boolean value
     */
    public boolean isFoot() {
        return type.equals(FOOT);
    }

    /**
     * Returns whether this node is a standard node (neither foot nor anchor).
     * @return a boolean value
     */
    public boolean isStandard() {
        return type.equals(STANDARD);
    }

    /**
     * Returns an array of children, or null if there are no children 
     * to this node. Children are understood as children nodes within the spine of this
     * elementary tree, not as other elementary trees.
     * @return the children of this spinal node
     */
    public SpinalNode[] getChildren() {
        
            return children;
        
    }
    
    /**
     * Returns a list of children, or an empty list if there are no children 
     * to this node. Children are understood as children nodes within the spine of this
     * elementary tree, not as other elementary trees.
     * @return the children of this spinal node
     */    
    public List getChildrenList() {
        return Arrays.asList(this.getChildren());
    }
    
    
    /**
     * Returns a list of all the descendents of this spinal node, including itself.
     * @return the descendents of this node
     */
    public List getAllNodes() {
        List result = new ArrayList();
        result.add(this);
        Iterator theChildren = Arrays.asList(this.getChildren()).iterator();
        while (theChildren.hasNext()) {
            SpinalNode current = (SpinalNode) theChildren.next();
            result.addAll(current.getAllNodes());
        }
        return result;
    }
    
    /**
     * Returns the specified child of this spinal node. 
     * @param n a number specifying the child, starting with zero.
     * @throws ArrayIndexOutOfBoundsException if this node does not have as many
     * children as the specified index
     * @return a <code>SpinalNode</code> for the child
     */
    public SpinalNode getChild(int n) {
        return this.getChildren()[n];
    }
    
    /**
     * Returns the {@link GornAddress} of this spinal node, relative to the
     * root of the spine in which it is contained (rather than the root of the
     * derivation tree in which it is contained).
     * @return a Gorn address, such as <code>0</code> for the root of the spine
     */
    public GornAddress getLocationInSpine() {
        String result = "";
        SpinalNode current = this;
        while (!current.isRootOfSpine()) {
            SpinalNode theParent = current.getParent(false);
            SpinalNode[] siblings = theParent.getChildren();
            for (int i = 0; i < siblings.length; i++) {
                if (current.equals(siblings[i])) {
                    // append to the left as we're walking upward the tree
                    if (!result.equals("")) result = GornAddress.SEPARATOR + result;
                    result = String.valueOf(i) + result;
                    break;
                }
            }
            // move up the tree
            current = theParent;
        }
        // current is now the root
        // finally, prepend 0 for the root
        if (!result.equals("")) result = GornAddress.SEPARATOR + result;
        result = "0" + result;
        
        return new GornAddress(result);
    }
    
    private void setChildren(SpinalNode[] children) {
        if (children==null) throw new IllegalArgumentException("Attempted to set children of spinal node to null." +
                "Please provide empty SpinalNode[0] array instead.");
        
        this.children=children;
    }
    
    /**
     * Returns a canonical representation of the subtree rooted in this
     * spinal node (e.g. <code>(XP NNS^)</code>).
     * @return a string.
     */
    public String toString() {
        String result = this.getLabel()+this.getType();
        SpinalNode[] theChildren = this.getChildren();
        
        if (theChildren.length==0) {
            return result;
        } else {
            for (int i = 0; i < theChildren.length; i++) {
                result = result + " " + theChildren[i];
            }
            return "( " + result + " )";
        }
    }

    /**
     * Returns a unique identifier for this node based on its location in the spine and 
     * the node ID of its elementary tree, for purposes of constructing Graphviz output.
     * @return a string acting as the node ID for Graphviz
     */
    String getGraphvizNodeID() {
                 return 
                 "spine_of_"
                 + this.getElemTree().getGraphvizNodeID().substring(5) // get rid of the word "node" in the node ID
                 + "_at_"
                 + this.getLocationInSpine().toString("_"); // Graphviz doesn't like periods in node names
                 //+ hashCode();
                 
    }
    

    /**
     * Gets the elementary tree to which this spine or spinal node belongs.
     * @return the host tree
     */
    public ElemTree getElemTree() {
        return host;
    }


    /**
     * Returns the <code>SpinalNode</code> that is the parent of this node.
     * 
     * @param acrossElemTrees if true, then if this node is at the root of
     * its <code>ElemTree</code>, then the attachment site in the parent <code>ElemTree</code> will be
     * returned; otherwise if this node is at the root of its <code>ElemTree</code>, null will
     * be returned 
     * 
     * @return the parent of this node in the spine
     */
    public SpinalNode getParent(boolean acrossElemTrees) {
        if (acrossElemTrees && this.isRootOfSpine()) {
            return this.getElemTree().getAttachmentSite();
        } else {
            return parent;
        }
    }
    
    /**
     * Returns whether this node is at the root of its spine.
     * @return a boolean value
     */
    public boolean isRootOfSpine() {
        return parent==null;
    }
    
    
}
