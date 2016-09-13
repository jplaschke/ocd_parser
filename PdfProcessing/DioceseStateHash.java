// All rights reserved (c) 2015 Bishop Accountability
package PdfProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class DioceseStateHash {
	
	public DioceseStateHash() {
		hashState = new Hashtable<String, HashSet<String>>();
	}

	// hash key is state
	// hash value is a hash of diocese in the state
    Hashtable<String, HashSet<String>> hashState;
    private String currentDiocese;

    // open a file with the state diocese data
    // data is tab delimited 
    // example of data is: 
    public void processDioceseStateFile() {
    	
    	String dioStateFileName = "./Diocese_states.txt";
    	File fin = new File(dioStateFileName);
    	FileInputStream fis;
		try {
			fis = new FileInputStream(fin);
		
    	 
	    	//Construct BufferedReader from InputStreamReader
	    	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	     
	    	String line = null;
	    	while ((line = br.readLine()) != null) {

	    		String[] parsedLine = line.split("\t");
	    		if (parsedLine.length >= 2) {
	    			this.addDioceseToState(parsedLine[0], parsedLine[1]);
	    		}
	    	}
	     
	    	br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public boolean isDioceseCity(String city, String state) {
    	boolean retValue = false;
    	HashSet<String> diocese = hashState.get(state);
    	Iterator<String> itr = diocese.iterator();
        while(itr.hasNext()){
        	String dioStr = itr.next().toString();
        	
            if (dioStr.contains(city) || dioStr.contains(city.toUpperCase())) {
            	retValue = true;
            	break;
            }
        }
    	
    	return retValue;
    }
    
	public boolean isDioceseInState(String line, String state) {
		boolean dioInState = false;
        HashSet<String> diocese = hashState.get(state);
		
        Iterator<String> itr = diocese.iterator();
        while(itr.hasNext()){
        	String dioStr = itr.next().toString();
        	
            if (line.contains(dioStr) || line.contains(dioStr.toUpperCase())) {
            	setCurrentDiocese(dioStr);
            	dioInState = true;
            	break;
            }
        }

		return dioInState;
	}

    
    public void printHash() {
    	for (String key: hashState.keySet()){

            HashSet<String> value = hashState.get(key);
            Iterator<String> itr = value.iterator();
            System.out.println("*** State: "+key);
            while(itr.hasNext()){
                System.out.println("" + itr.next().toString());
            }

    	} 


    }
    
	// add a diocese, state to the hashset
	private void addDioceseToState(String diocese, String state) {		
		if (stateExists(state)) {
			HashSet<String> dioceseSet = hashState.get(state);
			dioceseSet.add(diocese);		
		    hashState.put(state, dioceseSet);
		} else {
			HashSet<String> dioceseSet = new HashSet<String>();
			dioceseSet.add( diocese );
		    hashState.put(state, dioceseSet);
		}
	}
	
	
	
	private boolean stateExists(String state) {		
		return hashState.containsKey(state);
	}

	public String getCurrentDiocese() {
		return currentDiocese;
	}

	public void setCurrentDiocese(String currentDiocese) {
		this.currentDiocese = currentDiocese;
	}
}
