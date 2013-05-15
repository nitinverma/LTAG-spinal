package edu.upenn.cis.propbank_shen;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
   This is an interface to a frameset as defined in the propbank 
   lexical guidelines.

   A frameset is associated with a single verb, and contains 
   a list of "predicates", defined to be the verb itself plus
   any phrasal variants which constitute a distinct meaning.

   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.Predicate
   @see edu.upenn.cis.propbank_shen.RoleSet
   @see edu.upenn.cis.propbank_shen.Example
   @see edu.upenn.cis.propbank_shen.Role

 */
public class FrameSet {

    /** the root form of the verb */
    protected String verb;
    /** the xml document associated with the verb */
    protected Document doc;
    /** a list of the associated predicates for this verb 
        @see edu.upenn.cis.propbank_shen.Predicate */
    protected List predicates;

    /**
       construct a FrameSet object from the root form of a verb
       
       This constructor reads an xml file from disk and always returns
       the same object from the same arguments.
    */
    public FrameSet(String v) throws CorruptDataException
    {
        predicates = new LinkedList();
        verb = v;
        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null;
        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            doc = db.parse(FrameSet.getPath(verb));
            Node n = doc.getDocumentElement().getFirstChild();
            while (n != null) {
                if (n.getNodeName().equals("predicate")) {
                    predicates.add(new Predicate(n));
                }
                n = n.getNextSibling();
            }
        } catch (ParserConfigurationException pce) {
            System.err.println("Parser config error: " + pce);
        } catch (FactoryConfigurationError fce) {
            System.err.println("Factory configuration error: " + fce);
        } catch (SAXException se) {
            throw new CorruptDataException("Bad frames file for "+v+".");
        } catch (IOException ie) {
            if (System.getProperty("TREEBANKDIR") == null ||
                System.getProperty("FRAMEDIR") == null) {
                throw new CorruptDataException
                    (ie.getMessage()+ "; Couldn't find "+v+
                     "; System properties TREEBANKDIR and "+
                     "FRAMEDIR must be set properly.");
            }
            // otherwise maybe a misspelled verb in propbank, like 'instal'
            throw new CorruptDataException(ie.getMessage() + 
                                           "; Couldn't find "+v);
        }
    }
    /**
       Find the file for the lexical guidelines for a verb.
       @param verb the verb whose file needs finding.
    */
    public static File getPath(String verb) 
    {
        return new File(PBConfig.FrameDir() + File.separator + verb + ".xml");
    }

    /**
       return a list of Predicate objects see
       @see edu.upenn.cis.propbank_shen.Predicate
     */
    public List getPredicates() 
    {
        return predicates;
    }
    
    /**
       return the verb associated with this FrameSet 
    */
    public String getVerb() 
    {
        return verb;
    }

    /** a little unit test */
    public static void main(String args[]) throws CorruptDataException
    {
        FrameSet fs = null;
        if (args.length > 0) {
            fs = new FrameSet(args[0]);
        } else {
            fs = new FrameSet("go");
        }
        List foo = fs.getPredicates();
        Iterator i = foo.iterator();
        while(i.hasNext()) {
            Predicate p = (Predicate) i.next();
            System.out.println(p.getLemma());
        }
    }
}

