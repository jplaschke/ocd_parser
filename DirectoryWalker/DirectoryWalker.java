// All Rights Reserved. Copyright 2015 Bishop Accountability
package DirectoryWalker;

import java.io.File;
import java.io.IOException;

import PdfProcessing.ExtractPageContent;
import PdfProcessing.ParseStateMachine;

public class DirectoryWalker {
	
	private File iDirectory;
	private ExtractPageContent extractPageContent;
	private ParseStateMachine parseStateMachine;
	private String type;
	private String first;
	private String last;
	
	
	public DirectoryWalker(File iDirectory, String type, String first, String last) {
		super();
		this.iDirectory = iDirectory;
		extractPageContent = new ExtractPageContent();
		this.parseStateMachine = new ParseStateMachine();
		this.type = type;
		this.first = first;
		this.last = last;
	}


	public void listFilesForFolder() {
	    this.listFilesForFolder(iDirectory);
	}


	private void listFilesForFolder(final File folder) {
		
	    for (final File fileEntry : folder.listFiles()) {
	        if (!fileEntry.isDirectory()) {
	  //          listFilesForFolder(fileEntry);
	   //     } else {
	            System.out.println(fileEntry.getName());	            
	            String pdfName = fileEntry.getName();
	            if (pdfName.contains("Roll") && pdfName.contains(".pdf")) {
	            	String txtName = pdfName.replace(".pdf", ".txt");
	            	String dioceseName = pdfName.replace(".pdf", ".txt");
	            	dioceseName = "Diocese"+dioceseName;
	            	System.out.println("********************\n*********************************\n");
	            	System.out.println("********************\n*********************************\n");
	            	System.out.println("********************\n*********************************\n");
	            	System.out.println("********************\n*********************************\n");
	            	System.out.println(fileEntry.getAbsoluteFile().toString());
	            	try {
	            		if (type.contains("place")) {
	            			extractPageContent.findPriestsCity(fileEntry.getAbsoluteFile().toString(),txtName,
	            				first, last);
	            		} else if (type.contains("priest")) {
	            			extractPageContent.findPriestAssignments(fileEntry.getAbsoluteFile().toString(),txtName,
		            				first, last);
	            		} else {
	            			this.parseStateMachine.parsePdf(fileEntry.getAbsoluteFile().toString(),txtName);
	            		}
//									"Aurora", "IL");
			//			extractPageContent.createDioceseTable(fileEntry.getAbsoluteFile().toString(),dioceseName);

						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        }
	    }
		//extractPageContent.CLoseFiles();
	}

}
