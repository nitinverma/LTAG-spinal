/*
 * Represents the Propbank itself, and reads in the file prop-all.idx.
 */

package edu.upenn.cis.propbank_shen;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates the actual Propbank annotation file as modified
 * by Libin Shen.
 * 
 * @author Lucas Champollion
 */
public class Propbank extends HashMap {

    
    /** 
     * Creates a new instance of Propbank, using the default location
     * as indicated in (@link PBConfig}.
     */
    public Propbank()  {

        this(PBConfig.PropBankFile());

    }
    
    /**
     * Creates a new instance of Propbank from the file location indicated
     * (e.g. <code>"/usr/local/propbank/prop-all.idx"</code>).
     * @param location a path to Libin Shen's Propbank file
     */
    public Propbank(String location)  {
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(location));
            
            String s;
            Annotation current;
            PASLoc currentLocation;
            System.out.print("Reading in Propbank...");
            int counter=0;
            while (br.ready()) {
                counter++;
                s = br.readLine();
                current = new Annotation(s);
                currentLocation = current.getPASLoc();
                this.put(currentLocation, current);
            }
            System.out.println("done. ("+counter+" entries)");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CorruptDataException ex) {
            ex.printStackTrace();
        }
        
        if (this.isEmpty()) throw new RuntimeException();
        
    }
    
}
