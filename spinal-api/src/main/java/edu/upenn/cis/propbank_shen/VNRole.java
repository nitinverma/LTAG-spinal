package edu.upenn.cis.propbank_shen;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;


/**
   This class represents a propbank pointer to verbnet roles.

   For a full API for verbnet, please see the
   <a href="http://www.cis.upenn.edu/verbnet"> VerbNet website</a>.

   @author Scott Cotton
 */
public class VNRole {

    /** the theta role, such as Agent, Theme, Patient, etc */
    protected  String vntheta;
    /** the verbnet class */
    protected  String vnclass;
    /**  the node from the frameset xml document */
    protected Node node;

    /** construct a VNRole object from a vnrole node in the
        propbank lexical guidelines */
    public VNRole(Node n) {
        node = n;
        vntheta = null;
        vnclass = null;
        NamedNodeMap attrs = n.getAttributes();
        int len = attrs.getLength();
        String anm = null;
        for(int i=0; i<len; i++) {
            Attr attr = (Attr) attrs.item(i);
            anm = attr.getNodeName();
            if (anm.equals("vncls")) {
                vnclass = attr.getNodeValue();
            } else if (anm.equals("vntheta")) {
                vntheta = attr.getNodeValue();
            }
        }
    }

    /** return the VerbNet theta role associated with this VNRole object */
    public String getVNTheta() 
    {
        return vntheta;
    }

    /** return the VerbNet class identifier associated with this VNRole object */
    public String getVNClass() 
    {
        return vnclass;
    }
}

