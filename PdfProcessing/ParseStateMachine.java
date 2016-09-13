package PdfProcessing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

public class ParseStateMachine {
	
	public ParseStateMachine() {
		dioStateHash = new DioceseStateHash();
		dioStateHash.processDioceseStateFile();
		out = null;
	}

	DioceseStateHash dioStateHash;
	private PrintWriter out; // = new PrintWriter(new
								// FileOutputStream("city_query.txt"));
	
	private Set<String> citiesSet;
	private String prevCity = "";
	
	public void CLoseFiles() {
		out.close();
	}
	
	int ParseState = 0; // 0 = looking for "The catholic church in the united states,
	                    // 1 = looking for diocese of, 
	                    // 2 = looking for archidioecesis 
						// 3 = parishInDioceseCity,
	                    // 4 = parsish outside diocese city
	                    // 5 = END
	                    // 6 = alphabetical listing?

	private boolean FuzzyEquals(String str1, String str2, int threshold) {
		
		boolean matches = false;
		
		int distanceNum = StringUtils.getLevenshteinDistance(str1.toUpperCase(),
						str2.toUpperCase());
		if (distanceNum <= threshold) {
			matches = true;
		}
		return matches;
	}

	
	private String parseOutsideChurch(String cLine) {
		String church = "";
		
		String[] pLine = cLine.split("Rev\\.|Very Rev\\.|Revs\\.|Rt Rev\\. Msgr\\.|Very Rev\\.");
		if (pLine.length >= 2) {
			church = pLine[0];
		}
		return church;
	}
	
	private String parseChurch(String cLine) {
		String church = "";

		// For 1950 church is all caps
		for(int i=0; i<cLine.length(); i++) {
			if (((cLine.charAt(i) >= 'A') && (cLine.charAt(i) <= 'Z')) ||
					(cLine.charAt(i) == ' ') || (cLine.charAt(i) == '.') ||
					(cLine.charAt(i) == '\'') || (cLine.charAt(i) == '(') ||
					(cLine.charAt(i) == ',')) {
				church += (cLine.charAt(i));
			} else {
				break;
			}
		}
		if (church.lastIndexOf(',') != -1) {
			church = church.substring(0, church.lastIndexOf(','));
		}
		else if (church.lastIndexOf('(') != -1) {
			church = church.substring(0, church.lastIndexOf('('));
		}
		if (church.length() <= 4) {
			String patternStr = "[(][0-9]{1,4}[)]";
		    Pattern pattern = Pattern.compile(patternStr);
		    Matcher matcher = pattern.matcher(cLine);
		    int parenIndex = -1;
		    if(matcher.find()){
		    	parenIndex = matcher.start();
		    }		
		    if (parenIndex != -1) {
		    	church = cLine.substring(0, parenIndex);
		    }
		}
		if (church.length() <=2 ) {
			String[] pLine = cLine.split("Rev\\.|Very Rev\\.|Revs\\.|Rt Rev\\. Msgr\\.|Very Rev\\.");
			if (pLine.length >= 2) {
				church = pLine[0];
			}
		}
		return church;
	}
	
	
	private String getRtRevMsgr (String line) {
		String revStr = "";
		if (line.indexOf("Rt Rev. Msgr.") != -1) {
			if (line.lastIndexOf("Rev") != -1) {
				revStr = line.substring(line.indexOf("Rt Rev. Msgr."),
					            line.lastIndexOf("Rev"));
			} else {
				revStr = line.substring(line.indexOf("Rt Rev. Msgr."));				
			}
		}		
		return revStr;
	}

	
	private String getVeryRevMsgr(String line) {
		String revStr = "";
		if (line.indexOf("Very Rev. Msgr.") != -1) {
			revStr = line.substring(line.indexOf("Very Rev. Msgr."),
					            line.lastIndexOf("Rev"));			
		}		
		return revStr;
	}
	
	private String getRevs(String line) {
		String revStr = "";
		int revsIndex = line.indexOf("Revs. ");
		int revIndex = line.indexOf("Rev. ");
		int resIndex = line.indexOf("Res.");
		int schoolIndex = line.indexOf("School");
		int addrIndex = line.indexOf("Address:");
		//System.out.println("revIndex "+revsIndex);
		//System.out.println("resIndex "+resIndex);
		//System.out.println("schoolIndex "+schoolIndex);
		if (revsIndex == -1) {
			revsIndex = line.indexOf("Rev.");
		}
		if (revsIndex == -1) {
			revsIndex = line.indexOf("Revs ");
		}
		if ((revIndex != -1) && (revIndex > revsIndex)) {
			revsIndex = revIndex;
		}
		if (resIndex == -1) {
			resIndex = line.indexOf("Res'");
		}
		if (line.indexOf("Res") < resIndex) {
			resIndex = line.indexOf("Res");
		}
		String patternStr = "[(][0-9]{1,3}[)]";
	    Pattern pattern = Pattern.compile(patternStr);
	    Matcher matcher = pattern.matcher(line);
	    int parenIndex = -1;
	    if(matcher.find()){
	    	parenIndex = matcher.start();
	    }		
		int minIndex = 10000;
		if (revsIndex == -1) {
			revStr = "Empty";
		}
		else {
			if ((resIndex > revsIndex) && (resIndex != -1)) {
				minIndex = resIndex;
			}
			if ((schoolIndex < minIndex) && (schoolIndex != -1) &&
				(schoolIndex > revsIndex)) {
				minIndex = schoolIndex;
			}
			if ((addrIndex < minIndex) && (addrIndex != -1) &&
					(addrIndex > revsIndex) && (addrIndex < schoolIndex)) {
					minIndex = addrIndex;
				}
			if (minIndex == 10000) {
				minIndex = line.length()-1;
			}
		    if ((parenIndex > revsIndex) && (parenIndex < minIndex )) {
		    	minIndex = parenIndex;
		    }
			System.out.println("revsIndex "+revsIndex);
			System.out.println("minIndex "+minIndex);
		    
			try {
			revStr = line.substring(revsIndex, minIndex);
			} catch (Exception e) {
				System.out.println("revsIndex "+revsIndex);
				System.out.println("minIndex "+minIndex);
				System.out.println("line "+line);
				e.printStackTrace();
				System.exit(-1);
			}
		}		
		return revStr;
	}
	
	private String getCity(String line) {
		String city = "";
		String twoWords = "";
		String threeWords = "";
		String fourWords = "";
		String fiveWords = "";
 
		String[] pCity = line.split(" "); 
		int lastWord = pCity.length-1;
		if (pCity.length >= 2)
			twoWords = pCity[lastWord-1]+" "+pCity[lastWord]; 
		if (pCity.length >= 3)
		    threeWords = pCity[lastWord-2]+" "+twoWords;
		if (pCity.length >= 4)		
		    fourWords = pCity[lastWord-3]+" "+threeWords;
		if (pCity.length >= 5)
		    fiveWords = pCity[lastWord-4]+" "+fourWords;
		
		if (citiesSet.contains(twoWords)) {
			city = twoWords;
		} else if (citiesSet.contains(threeWords)) {
			city = threeWords;			
		} else if (citiesSet.contains(fourWords)) {
			city = fourWords;			
		} else if (citiesSet.contains(fiveWords)) {
			city = fiveWords;			
		} else {
			city = twoWords;
		}
		return city;
	}
	
	private void parseOutsideParishLines(String parishLine, int year, String diocese) {

		String[] splitParishs = parishLine.split("Co\\.\\,|Co\\.\\.|Co\\,\\.|Co\\,\\,|County|County\\.|\\(\\d\\d\\d\\d\\)");
		for (String pLine : splitParishs) {
			
			String[] parishArray = pLine.split("\\d{1,2}[\u2014]");
			if (parishArray.length > 1) {
				for (String multi : parishArray) {
					if (multi.length() > 20) {
						System.out.println(" multiple churches: "+multi);
						pLine = multi.replaceAll("[^\\x0A\\x0D\\x20-\\x7E]", "");
						pLine = multi.replaceAll("[\\x2d]", "");
						String church = this.parseOutsideChurch(multi);            
						System.out.println("church = "+church);
						String city = this.getCity(multi);
						System.out.println("city = "+city);
						String Revs = this.getRevs(multi);
						String RtRevs = this.getRtRevMsgr(multi);
						String VeryRev = this.getVeryRevMsgr(multi);
						out.write(Integer.toString(year)+"\t"+diocese+"\t"+prevCity+" Co.\t"+church+"\t"+
					             VeryRev+"\t"+RtRevs+"\t"+Revs+"\n");

					}
					
				}
				
			} else if (!pLine.contains("CITY OF")) {
				pLine = pLine.replaceAll("[^\\x0A\\x0D\\x20-\\x7E]", "");
				pLine = pLine.replaceAll("[\\x2d]", "");
				System.out.println("lineoutside = "+pLine);
				String church = this.parseOutsideChurch(pLine);            
				System.out.println("church = "+church);
				String city = this.getCity(pLine);
				System.out.println("city = "+city);
				String Revs = this.getRevs(pLine);
				String RtRevs = this.getRtRevMsgr(pLine);
				String VeryRev = this.getVeryRevMsgr(pLine);
				System.out.println("RtRevs = "+RtRevs);
				System.out.println("VeryRev = "+VeryRev);
				System.out.println("Revs = "+Revs);
				if (church.length() > 2) {
					out.write(Integer.toString(year)+"\t"+diocese+"\t"+prevCity+" Co.\t"+church+"\t"+
			             VeryRev+"\t"+RtRevs+"\t"+Revs+"\n");
					prevCity = city;
				} else {
					prevCity = city;
				}
            }
		}
	}
	
	private void ParseParishLines(String pLines, int year, String diocese) {
			
		String[] parishArray = pLines.split("\\d{1,2}[\u2014]");
		for (String parishL : parishArray) {
			parishL = parishL.replaceAll("[^\\x0A\\x0D\\x20-\\x7E]", "");
			parishL = parishL.replaceAll("[\\x2d]", "");
			System.out.println("parishlines = "+parishL);
			if (!parishL.contains("CITY OF")) {
				String church = this.parseChurch(parishL);
				System.out.println("church2 = "+church);
				String Revs = "";
				Revs = this.getRevs(parishL);
				String RtRevs = "";
				RtRevs = this.getRtRevMsgr(parishL);
				String VeryRev = "";
				VeryRev = this.getVeryRevMsgr(parishL);
				System.out.println("RtRevs = "+RtRevs);
				System.out.println("VeryRev = "+VeryRev);
				System.out.println("Revs = "+Revs);
				System.out.println("diocese = "+diocese);
				if (diocese.length() > 5) {
					out.write(Integer.toString(year)+"\t"+diocese+"\t"+diocese+"\t"+church+"\t"+
							VeryRev+"\t"+RtRevs+"\t"+Revs+"\n");
				}
			}
		}
	}
	
	/**
	 * Parses a PDF to a plain text file.
	 * 
	 * @param pdf
	 *            the original PDF
	 * @param txt
	 *            the resulting text
	 * @throws IOException
	 */
	// parser for ADW.pdf
	public void parsePdf(String pdf, String txt) throws IOException {
		PdfReader reader = new PdfReader(pdf);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		out = new PrintWriter(new FileOutputStream(txt));
		TextExtractionStrategy strategy;
		String dioceseName = "";
		String parishLine = "";
		
		String[] parseYear = pdf.split("_");
		System.out.println(parseYear[1]);
		String[] year = parseYear[1].split("\\.");
		System.out.println("Parsing year "+year[0]);
		int intYear = Integer.parseInt(year[0]);

		out.write("Year\tDiocese\tCity\tChurch\tVery Rt Msgr\tRt Rev\tRevs\n");
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			strategy = parser.processContent(i,
					new SimpleTextExtractionStrategy());
			String tmp = strategy.getResultantText();
			// System.out.println(tmp);
			String[] lines = tmp.split(System.getProperty("line.separator"));
			for (String tmpLine : lines) {

				String[] parsedLine2 = tmpLine.split(" ");
				if ((parsedLine2.length >= 2) && (parsedLine2.length <= 8)) {
					String tmp2 = parsedLine2[0] + " "+ parsedLine2[1];
					if (this.FuzzyEquals(tmp2, "Military Ordinariate", 7)) {
						ParseState = 12;
						System.out.println("***** FOUND MILTARY "+tmpLine);
					}
				}
				
				switch(ParseState) {
				
				case 0:
				{
					if (this.FuzzyEquals(tmpLine, "THE CATHOLIC CHURCH IN THE UNITED STATES", 
							9)) {
						ParseState = 1;
						System.out.println("CATHOLIC CHURCH IN US FOUND");
						System.out.println("tmpLine");
					}
					dioceseName = "";
				}
				break;
				
				case 1: 
				{
					parishLine = "";
					String[] parsedLine = tmpLine.split(" ");
					if ((parsedLine.length >= 2) && (parsedLine.length <= 5)
							&&(tmpLine.indexOf(",") == -1)) {
						String tmp2 = parsedLine[0] + " "+ parsedLine[1];
						if (this.FuzzyEquals(tmp2, "Military Ordinariate", 2) && (i>350)) {
							ParseState = 5;
							System.out.println("***** FOUND MILTARY");
						}
						else if (FuzzyEquals(tmp2, "DIOCESE OF", 4)) {
							String[] pLine = tmpLine.split(" ");
							if (pLine.length == 4) {
								dioceseName = pLine[2] + " " + pLine[3];
								ParseState = 2;
								System.out.println("FOUND DIOCESE: "+tmpLine);
							} else if (pLine.length == 3) {
								dioceseName = pLine[2];
								ParseState = 2;
								System.out.println("FOUND DIOCESE: "+tmpLine);
							}
						} 
						
					}

				}
				break;

				case 2:
				{
					System.out.println("2 STATE "+tmpLine);
					try {
						if (tmpLine.indexOf(" ") != -1) {
							tmpLine = tmpLine.split(" ")[0];
							int distanceNum = StringUtils.getLevenshteinDistance("(ARCHIDIOECESIS",
									tmpLine.toUpperCase());
							if (distanceNum < 6) {
								System.out.println("ARCHDIOCESE name FOUND = "+dioceseName);
								ParseState = 3;
							} else {
								distanceNum = StringUtils.getLevenshteinDistance("(DIOECESIS",
										tmpLine.toUpperCase());
								if (distanceNum < 4) {
									System.out.println("DIOCESE name FOUND = "+dioceseName);
									ParseState = 3;
								} else {
									if ((FuzzyEquals(tmpLine, "Most",2)) || 
										(FuzzyEquals(tmpLine, "Most Reverend",6))) {
										System.out.println("most name FOUND = "+dioceseName);
										ParseState = 3;
									} else { // dioecesis not found look for most in next line
										ParseState = 10;
										System.out.printf("possible error %d %s\n",
												distanceNum, tmpLine);
									}
								}
							}
						}
					} 
					catch (Exception ex) {
						ex.printStackTrace();
						ParseState = 1;
					}
					
				}
				break;
				
				case 3:
				{
					String[] parsedLine = tmpLine.split(" ");
					if (tmpLine.contains("CLERGY, PARISHES, MISSIONS ")) {
						ParseState = 4;
						System.out.println("found "+tmpLine);						
					}
					else if ((parsedLine.length >= 3) && (parsedLine.length <= 6)) {
						String tmp2 = parsedLine[0] + " "+ parsedLine[1]+" "+
								parsedLine[1];
						if (FuzzyEquals(tmp2, "CLERGY, PARISHES, MISSIONS ", 10)) {
							ParseState = 4;
							System.out.println("found "+tmpLine);
						}
					} else if (parsedLine.length > 6) {							
						int pLen = parsedLine.length;
						String tmp2 = parsedLine[pLen-5] + " "+ parsedLine[pLen-4]+" "+
								parsedLine[pLen-3]+ " "+parsedLine[pLen-2] + " "+ parsedLine[pLen-1];
						if (FuzzyEquals(tmp2, "CLERGY, PARISHES, MISSIONS AND PAROCHIAL SCHOOLS", 19)) {
							ParseState = 4;
							System.out.println("foundconcat "+tmpLine);
						}

					}
				}
				break;
				
				case 4: {
					String[] parsedLine = tmpLine.split(" ");
					// TEST CODE REMOVE if equalsIgnoreCase LATER 
//					if (dioceseName.equalsIgnoreCase("Baltimore")) {

	//				}
					if ((parsedLine.length >= 5) && (parsedLine.length <= 8)) {
						String tmp2 = parsedLine[0] + " "+ parsedLine[1]+" "+
								parsedLine[2]+" "+parsedLine[3]+" "+parsedLine[4];
						try {
							if (dioceseName.indexOf(" ") != -1) {
								tmp2 = tmp2+" "+parsedLine[5];
							}
						} catch (Exception e) {}
						if (FuzzyEquals(tmp2, "OUTSIDE THE CITY OF "+dioceseName, 15)) {
							ParseState = 6;
							System.out.println("found2 "+tmpLine);
						} else {
							parishLine += tmpLine;
						}
						
												
					} else if ((parsedLine.length >= 3) && (parsedLine.length <= 8)) {
						String tmp2 = parsedLine[0] + " "+ parsedLine[1]+" "+
								parsedLine[2];
						try {
							if (dioceseName.indexOf(" ") != -1) {
								tmp2 = tmp2+" "+parsedLine[5];
							}
						} catch (Exception e) {}
						if (FuzzyEquals(tmp2, "OUTSIDE METROPOLITAN "+dioceseName, 10)) {
							ParseState = 6;
							System.out.println("found2 "+tmpLine);
						} else {
							parishLine += tmpLine;
						}

					} else if ((parsedLine.length >= 2) && (parsedLine.length <= 8)){
						String tmp2 = parsedLine[0] + " "+ parsedLine[1];					
						if (dioceseName.indexOf(" ") != -1) {
							try {
								tmp2 = tmp2+" "+parsedLine[2];
							} catch (Exception e) {}
						} 						
						if (FuzzyEquals(tmp2, "OUTSIDE "+dioceseName, 6)) {
							ParseState = 6;
							System.out.println("found2 "+tmpLine);
						} else {
							parishLine += tmpLine;
						}
					}
				}
				break;
				
				case 10: {
					try { // try again Boise 1950 case
						tmpLine = tmpLine.split(" ")[0];
						int distanceNum = StringUtils.getLevenshteinDistance("(ARCHIDIOECESIS",
								tmpLine.toUpperCase());
						if (distanceNum < 6) {
							System.out.println("ARCHDIOCESE name FOUND = "+dioceseName);
							ParseState = 3;
						} else {
							distanceNum = StringUtils.getLevenshteinDistance("(DIOECESIS",
									tmpLine.toUpperCase());
							if (distanceNum < 4) {
								System.out.println("DIOCESE name FOUND = "+dioceseName);
								ParseState = 3;
							} else {
								if ((FuzzyEquals(tmpLine, "Most",1)) || 
									(FuzzyEquals(tmpLine, "Most Reverend",6))) {
									System.out.println("most name FOUND = "+dioceseName);
									ParseState = 3;
								} else { // dioecesis not found look for most in next line
									ParseState = 1;
									System.out.printf("ERROR %d %s\n",
											distanceNum, tmpLine);
								}
							}
						}
					} 
					catch (Exception ex) {
						ParseState = 1;
					}
				
				}
				break;
				
				case 12: {
					ParseState = 5;
				}
				break;
				
				case 5: {
					System.out.println("tmpLine "+tmpLine);
					if (FuzzyEquals(tmpLine, "(Vicariatus Castrensia)", 9)) {
						ParseState = 11;
					} else {
						ParseState = 1;
					}
				}

				
				case 6: {
					System.out.println("STATE 6: parishLine = "+parishLine);
					if (parishLine != "") {						
							System.out.println("****** \n");
							System.out.printf("%d diocese: %s\n",intYear,dioceseName);
							ParseParishLines(parishLine,intYear, dioceseName);
							System.out.println("****** \n");
							System.out.printf("%d diocese: %s\n",intYear,dioceseName);
							parishLine = "";						

					}					
					parishLine = tmpLine;
					
					System.out.println("parishLine = "+parishLine);
					citiesSet = new HashSet<String>();
					String[] splitParishs = tmpLine.split("Co\\.\\,|Co\\.\\.|Co\\,\\.|Co\\,\\,|County|County\\.");
					if (splitParishs.length > 1) {
						citiesSet.add(splitParishs[0].trim());
					}
					ParseState = 7;

				}
				break;
				
				case 7: {
					String[] parsedLine = tmpLine.split(" ");
					String[] checkRetired = tmpLine.split(" ");
					String priestsAbsent = "";
					String chineseMission = "";
					String philly = "";
					String sixwords = "";
					if (checkRetired.length >= 4) {
						priestsAbsent = checkRetired[0]+" "+checkRetired[1]+" "+
								checkRetired[2]+" "+checkRetired[3];
						chineseMission = checkRetired[2]+" "+checkRetired[3];
						philly = checkRetired[0]+" "+checkRetired[1]+" "+
								checkRetired[2];

					}
					if (checkRetired.length >= 6) {
						sixwords = checkRetired[0]+" "+checkRetired[1]+" "+
								checkRetired[2]+" "+checkRetired[3] + " " +								
								checkRetired[4]+" "+checkRetired[5];
					}

					if (this.FuzzyEquals(checkRetired[0].toUpperCase(), "Retired: ", 1) ||			
						this.FuzzyEquals(priestsAbsent, "Priests Absent On Leave:", 9) ||
						this.FuzzyEquals(priestsAbsent, "Sick, absent on leave,", 6) ||
						this.FuzzyEquals(priestsAbsent, "absent on sick leave", 6) ||						
						this.FuzzyEquals(priestsAbsent, "UNITED STATES GOVERNMENT SERVICE", 7) ||
						this.FuzzyEquals(priestsAbsent, "PUBLIC INSTITUTIONS WITH RESIDENT", 8) ||
						this.FuzzyEquals(priestsAbsent, "on duty outside diocese", 6) ||						
						this.FuzzyEquals(chineseMission, "Chinese Mission", 4) ||
						this.FuzzyEquals(chineseMission, "SEMINARIES, DIOCESAN", 2) ||
						this.FuzzyEquals(philly, "Philadelphia priests laboring", 9) ||
						this.FuzzyEquals(philly, "OF PUBLIC INSTITUTIONS", 6) ||						
						this.FuzzyEquals(philly, "Institutions with Resident", 6) ||
						this.FuzzyEquals(philly, "On Sick Leave:", 2) ||						
						this.FuzzyEquals(philly, "SEMINARIES, RELIGIOUS, OR", 6) ||
						this.FuzzyEquals(sixwords, "Sick, absent on leave, retired, students.", 5)) {
						if (parishLine != "") {
							parseOutsideParishLines(parishLine,intYear, dioceseName);
							parishLine = "";
						}
					} else {
						String[] splitParishs = tmpLine.split("Co\\.\\,|Co\\.\\.|Co\\,\\.|Co\\,\\,|County|County\\.");
						if (splitParishs.length > 1) {
							citiesSet.add(splitParishs[0].trim());
						}
	
						System.out.println("STATE 7: "+tmpLine);
						if (!this.FuzzyEquals(tmpLine, dioceseName, 5)) {
							parishLine += tmpLine;
						}
						
					}
					if ((parsedLine.length >= 3) && (parsedLine.length <= 5)) {
						String tmp2 = parsedLine[0] + " "+ parsedLine[1]+" "+
								parsedLine[1];
						if (FuzzyEquals(tmp2, "INSTITUTIONS OF THE ", 10)) {
							ParseState = 1;
							if (parishLine != "") {
								parseOutsideParishLines(parishLine,intYear, dioceseName);
								parishLine = "";
							}
							System.out.println("found3 "+tmpLine);							
						}
						else if (FuzzyEquals(tmp2, "ORPHANAGES AND INFANT ", 10)) {
							ParseState = 1;
							System.out.println("found3 "+tmpLine);
						}
						else if (FuzzyEquals(tmp2, "RECAPITULATION OF STATISTICS", 10)) {
							ParseState = 1;
							System.out.println("found3 "+tmpLine);
						}
                        
						
					}
				}

				break;

				} // end switch
			}
			if (ParseState == 11) {
				break;
			}
		}
		out.flush();
		out.close();
		reader.close();
		
	}


}
