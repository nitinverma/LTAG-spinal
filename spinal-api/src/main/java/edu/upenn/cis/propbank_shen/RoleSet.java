package edu.upenn.cis.propbank_shen;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

/**
   A representation of a RoleSet as defined in the propbank lexical guidelines,
   frameset.dtd.

   <p>

   A roleset defines a sort of coarse grained sense for a verb. When we
   understand a verb as an object which relates things 
   (such as in "John gave Mary a Penny", "gave" is relating John, Mary, and "a
   Penny".), we'll quickly come to see that verbs may relate different kinds 
   of things.  For example, the verb "call" may be used to relate either 
   a caller with an object and a label (in the labelling sense of call) or 
   a caller with a thing being summoned and a place being summoned to. (in the
   summon sense of call (eg "John called Mary over").

   <p>
   
   We then view each of these things 
   <ul>
   <li> (caller, object, label) 
   <li> (caller, thing summoned, destination) 
   </ul>
   as a roleset.  RoleSet objects provide a handle on these coarse grained 
   senses.
   @author Scott Cotton
   
 */

public class RoleSet {

    /** the identifier of the roleset */
    protected String id;    

    /** the name of the roleset, optional, can be null */
    protected String name;
    /** the node from which a roleset is constructed */
    protected Node node;
    /** the set of roles associated with the roleset */
    protected List roles;
    /** the set of examples associated with the roleset */
    protected List examples;
    /** the VerbNet classes to which this roleset belongs */
    protected String vnclasses[];    
    /**
       construct a RoleSet object from a roleset node in a frameset 
       xml document.
    */
    public RoleSet(Node n)  throws CorruptDataException
    {
        name = null;
        vnclasses = new String[0];
        node = n;
        NamedNodeMap attrs = n.getAttributes();
        int len = attrs.getLength();
        String anm;
        for(int i=0; i<len; i++) {
            Attr attr = (Attr) attrs.item(i);
            anm = attr.getNodeName();
            if (anm.equals("id")) {
                id = (String) attr.getNodeValue();
            } else if(anm.equals("vncls")) {
                vnclasses = ((String) attr.getNodeValue()).split(" ");
            } else if (anm.equals("name")) {
                name = (String) attr.getNodeValue();
            }
        }
        // parse the roles
        roles = new LinkedList();
        Node rn = node.getFirstChild();
        while(rn != null) {
            if (rn.getNodeName().equals("roles")) {
                Node nc = rn.getFirstChild();
                while (nc != null) {
                    if (nc.getNodeName().equals("role")) {
                        roles.add(new Role(nc)); 
                    }
                    nc = nc.getNextSibling();
                }
            }
            rn = rn.getNextSibling();
        }
        // parse the examples
        examples = new LinkedList();
        Node ne = node.getFirstChild();
        while(ne != null) {
            if (ne.getNodeName().equals("example")) {
                examples.add(new Example(ne));
            }
            ne = ne.getNextSibling();
        }
    }

    /**
       create a RoleSet object from the id, where the id is in the form
       &lt;verb&gt;.NN, such as "go.01".
       
       @param id the roleset identifier
     */
    public static RoleSet ofId(String id) throws CorruptDataException
    {
        int i = id.indexOf('.');
        if (i == -1) {
            throw new CorruptDataException("invalid roleset id: " + id);
        }
        String verb = id.substring(0, i);
        FrameSet fs = new FrameSet(verb);
        List l = fs.getPredicates();
        Iterator p = l.iterator();
        while (p.hasNext()) {
            Predicate pred = (Predicate) p.next();
            Iterator ri = pred.getRoleSets().iterator();
            while(ri.hasNext()) {
                RoleSet rs = (RoleSet) ri.next();
                if (rs.getId().equals(id)) {
                    return rs;
                }
            }
        }
        System.err.println("no roleset found with id" + id);
        return null;
    }
        
    /** return the identifier associated with the roleset */
    public String getId() 
    {
        return id;
    }

    /** return the name of this roleset, or null if there is none specified. */
    public String getName() 
    {
        return name;
    }
    /** return true iff this roleset has an associated name */
    public boolean hasName()
    {
        return name != null;
    }

    /** return the verbnet class ids associated with this roleset */
    public String[] getVNClasses()
    {
        return vnclasses;
    }

    /** return the list of roles associated with the roleset 
     @see edu.upenn.cis.propbank_shen.Role 
    */
    public List getRoles() 
    {
        return roles;
    }

    /**
       return a list of the Example objects associated with this roleset
       @see edu.upenn.cis.propbank_shen.Example
     */
    public List getExamples()
    {
        return examples;
    }

    /**
       A simple unit test
     */
    public static void main(String args[]) 
        throws CorruptDataException
    {
        if (args.length < 1) {
            System.err.println("sorry, please give me a roleset id (eg go.01)");
            System.exit(1);
        }
        RoleSet rs = RoleSet.ofId(args[0]);
        if (rs.hasName()) {
            System.out.println("roleset " + args[0] + ", name is " + rs.getName());
        }
        String vnclasses[] = rs.getVNClasses();
        for (int j=0; j<vnclasses.length; j++) {
            System.out.println("\tverbnet class " + vnclasses[j]);
        }
        List l = rs.getRoles();
        Iterator ri = l.iterator();
        while(ri.hasNext()) {
            Role r = (Role) ri.next();
            ArgLabel al = r.getArgLabel();
            String d = r.getDescription();
            if (r.hasModLabel()) {
                ModLabel ml = r.getModLabel();
                System.out.println(al.toString() + "-" + ml.toString() + ": " + d);
            } else {
                System.out.println(al.toString() + ": " + d);
            }
        }
        Iterator ei = rs.getExamples().iterator();
        while(ei.hasNext()) {
            Example e = (Example) ei.next();
            System.out.println(e.getText());
        }
    }
}
