package edu.upenn.cis.propbank_shen;

import java.io.*;
import java.util.List;
import java.util.LinkedList;

import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;


/**
   A class representing a "predicate" in the propbank frames.  A predicate
   is either the root form of a verb, such as "go", or it is a phrasalized 
   root form, such as "go on".

   Phrasalized forms have their parts joined by underscores.
   @author Scott Cotton
*/
public class Predicate {
    /** the lemma (root form) associate with the predicate */
    protected String lemma;
    /** the node from the xml document used to create this Predicate object */
    protected Node node;
    /** a list of the rolesets associated with this predicate 
     @see edu.upenn.cis.propbank_shen.RoleSet */
    protected List rolesets;

    /** the separator for phasal parts, here '_', so "go on" would be "go_on" */
    public static String phrasalSep = "_";

    /** construct a Predicate object from a predicate node in a frameset
        xml document */
    public Predicate(Node n) throws CorruptDataException
    {
        node = n;
        lemma = null;
        NamedNodeMap attrs = n.getAttributes();
        int len = attrs.getLength();
        for(int i=0; i<len; i++) {
            Attr attr = (Attr) attrs.item(i);
            if (attr.getNodeName().equals("lemma")) {
                lemma = attr.getNodeValue();
                break;
            }
        }
        if (lemma == null) {
            System.err.println("error with Predicate object, no lemma found");
        }
        rolesets = new LinkedList();
        Node nc = node.getFirstChild();
        while (nc != null) {
            if (nc.getNodeName().equals("roleset")) {
                rolesets.add(new RoleSet(nc));
            }
            nc = nc.getNextSibling();
        }
    }
    // XXX add constructor from string

    /** return the lemma associated with this Predicate object */
    public String getLemma() 
    {
        return lemma;
    }

    /** return true iff this Predicate object refers to a phrasal predicate */
    public boolean isPhrasal() 
    {
        return lemma.indexOf(Predicate.phrasalSep) != -1;
    }

    /** 
        Return a list of the rolesets associated with this Predicate object.
     */
    public List getRoleSets()
    {
        return rolesets;
    }
}
