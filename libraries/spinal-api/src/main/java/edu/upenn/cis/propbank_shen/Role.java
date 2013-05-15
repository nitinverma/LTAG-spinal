package edu.upenn.cis.propbank_shen;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;


/**
   This class represents a "role" in the propbank lexical guidelines. 

   A role may consist of a number of things.  All roles have an associated
   argument label. 
   @see edu.upenn.cis.propbank_shen.ArgLabel
   @author Scott Cotton
 */
public class Role {

    /** the node of the xml document from which this thing was made */
    protected Node node;
    /** a description of the role */
    protected String descr;
    /** the argument label associated with the role 
        @see edu.upenn.cis.propbank_shen.ArgLabel */
    protected ArgLabel arglabel;
    /** the modifying label associated with the roleset, or null
        if there is none.
        @see edu.upenn.cis.propbank_shen.ModLabel
    */
    protected ModLabel modlabel;
    /**
       A list of the verbnet roles associated with the role.
       @see edu.upenn.cis.propbank_shen.VNRole
    */
    protected List vnroles;
    
    /**
       construct a Role object from a role node in a frameset 
       xml document.
     */
    public Role(Node n) throws CorruptDataException
    {
        node = n;
        NamedNodeMap attrs = n.getAttributes();
        int len = attrs.getLength();
        String anm = null;
        for(int i=0; i<len; i++) {
            Attr attr = (Attr) attrs.item(i);
            anm = attr.getNodeName();
            if (anm.equals("descr")) {
                descr = attr.getNodeValue();
            } else if (anm.equals("n")) {
                arglabel = ArgLabel.ofString("Arg" + attr.getNodeValue());
            } else if (anm.equals("f")) {
                modlabel = ModLabel.ofString(attr.getNodeValue());
            }
        }
        vnroles = new LinkedList();
        Node nc = node.getFirstChild();
        while (nc != null) {
            if (nc.getNodeName().equals("vnrole")) {
                vnroles.add((Object) new VNRole(nc));
            }
            nc = nc.getNextSibling();
        }
    }
    
    /** return the brief description of the role */
    public String getDescription() 
    {
        return descr;
    }

    /** return the associated argument label */
    public ArgLabel getArgLabel() 
    {
        return arglabel;
    }

    /** 
        return the associated modifying label, or null
        if there is no such modifying label.
        @see edu.upenn.cis.propbank_shen.ModLabel
    */
    public ModLabel getModLabel() 
    {
        return modlabel;
    }

    /** return true iff this role has a modifying label 
        @see edu.upenn.cis.propbank_shen.ModLabel */
    public boolean hasModLabel() 
    {
        return modlabel != null;
    }

    /**
       return a list (possibly empty) of the associated VerbNet roles
       see also the <a href="http://www.cis.upenn.edu/verbnet">VerbNet website</a>
    */
    public List getVNRoles() 
    {
        return vnroles;
    }
}
