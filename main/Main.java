// All Rights Reserved. Copyright 2015 Bishop Accountability
package main;

import java.io.File;

import DirectoryWalker.DirectoryWalker;

public class Main {

	public static void main(String[] args) {
		
		String type = "";
		String first = "";
		String last = "";
		String middle_initial = "";
		
		if (args.length < 3) {
			System.out.println("Usage: priest firstname lastname | place city state");
			System.exit(-1);
		} else {
			if (args[0].equalsIgnoreCase("priest")) {
				type = "priest";
				first = args[1];
				for (int i=2; i<args.length-1; i++) {
					first = first + " "+ args[i]; 
				}				
				last = args[args.length-1];			
			} else if (args[0].equalsIgnoreCase("place")) {
				type = "place";
				first = args[1];
				for (int i=2; i<args.length-1; i++) {
					first = first + " "+ args[i]; 
				}				
				last = args[args.length-1];
			} else if (args[0].equalsIgnoreCase("none")) {
				
			} else {
				System.out.println("Error: USAGE: priest firstname lastname | place city state");
				System.exit(-2);
			}
		}
		System.out.println("first = "+first);
		File file = new File("./");
		file = new File("./");
		System.out.printf("type %s first %s last %s",type,first, last);
		DirectoryWalker directoryWalker = new DirectoryWalker(file, type, first, last);

		directoryWalker.listFilesForFolder();
	}

}
