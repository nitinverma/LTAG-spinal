package edu.upenn.cis.propbank_shen;

import java.util.LinkedList;
import java.util.List;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

/**
   This class represents an example in the lexical guidelines.

   An example consists of some text together with an optional
   name and a list of arguments for the verb described in the
   frameset file of which this example is a part.
   @author Scott Cotton


 */
public class Example {

    protected Node node;
    protected String name;

    protected String text;
    protected List arguments;
    
    /** construct an Example object from a dom example Node in the frameset.dtd */
    public Example(Node n)
    {
        node = n;
        arguments = null;
        name = null;
        NamedNodeMap attrs = n.getAttributes();
        int len = attrs.getLength();
        String anm = null;
        for(int i=0; i<len; i++) {
            Attr attr = (Attr) attrs.item(i);
            anm = attr.getNodeName();
            if (anm.equals("name")) {
                name = (String) attr.getNodeValue();
            }
        }
        // find the text node
        text = null;
        Node nc = node.getFirstChild();
        while (nc != null) {
            if (nc.getNodeName().equals("text")) {
                    text = (String) nc.getFirstChild().getNodeValue();
                break;
            }
            nc = nc.getNextSibling();
        }
    }

    /** return truee iff this example has a name */
    public boolean hasName() 
    {
        return name != null;
    }


    /** get the name of this example, or return null if there is no name */
    public String getName()
    {
        return name;
    }



    /** return the example text as a string. */
    public String getText() 
    {
        return text;
    }
}
