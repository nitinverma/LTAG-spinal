package edu.upenn.cis.propbank_shen;

/**
   This class represents inflectional information as is found in the
   english propbank.  Basically, inflectional information is stuff
   like "third person plural singular", but we only represent a portion
   of this information for a few reasons:  first, some of it is already
   in the part of speech tags in the penn treebank, and second, 
   only some of this information is really relevant to the primary aim
   of this study -- to find out more about the relationship between 
   syntax and semantics (as it appears in a million words of wall street
   journal from the hot 90's...).

   <p>

   Anyway, we keep inflectional information in 5 slots:
   <ol>
   <li> Form
   <li> Tense
   <li> Aspect
   <li> Person
   <li> Voice
   </ol>

   @author Scott Cotton
   @see edu.upenn.cis.propbank_shen.InflForm
   @see edu.upenn.cis.propbank_shen.InflTense      
   @see edu.upenn.cis.propbank_shen.InflAspect
   @see edu.upenn.cis.propbank_shen.InflPerson
   @see edu.upenn.cis.propbank_shen.InflVoice
 */
public class Inflection {

    public InflForm form;
    public InflTense tense;
    public InflAspect aspect;
    public InflPerson person;
    public InflVoice voice;

    public Inflection(InflForm f,
                      InflTense it,
                      InflAspect ia,
                      InflPerson ip,
                      InflVoice iv)
    {
        form = f;
        tense = it;
        aspect = ia;
        person = ip;
        voice = iv;
    }

    /** 
        construct an inflection instance from a string of the form
        FORM . TENSE . ASPECT . PERSON . VOICE
        (one character for each slot, the dots denote concatenation and
        aren't to be taken literally).
    */
    public Inflection(String s) throws CorruptDataException
    {
        if (s.length() != 5) {
            throw new CorruptDataException("invalid inflection string: " + s);
        }
        form = InflForm.ofString(s.substring(0, 1));
        tense = InflTense.ofString(s.substring(1,2));
        aspect = InflAspect.ofString(s.substring(2,3));
        person = InflPerson.ofString(s.substring(3,4));
        voice = InflVoice.ofString(s.substring(4,5));
    }

    /**
       construct a canonical string representing the inflectional information.
     */
    public String toString() 
    {
        return (form.toString() 
                + tense.toString()
                + aspect.toString()
                + person.toString()
                + voice.toString());
    }
}


