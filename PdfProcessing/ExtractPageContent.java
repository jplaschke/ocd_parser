// All Rights Reserved. Copyright 2015 Bishop Accountability  
package PdfProcessing;

/*
 * This class is part of the book "iText in Action - 2nd Edition"
 * written by Bruno Lowagie (ISBN: 9781935182610)
 * For more info, go to: http://itextpdf.com/examples/
 * This example only works with the AGPL version of iText.
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

public class ExtractPageContent {

	public ExtractPageContent() {
		dioStateHash = new DioceseStateHash();
		dioStateHash.processDioceseStateFile();
		out = null;
	}

	DioceseStateHash dioStateHash;
	private PrintWriter out; // = new PrintWriter(new
								// FileOutputStream("city_query.txt"));

	public void CLoseFiles() {
		out.close();
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
	public void parsePdf_ADW(String pdf, String txt) throws IOException {
		PdfReader reader = new PdfReader(pdf);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		PrintWriter out = new PrintWriter(new FileOutputStream(txt));
		TextExtractionStrategy strategy;
		String dateStr;
		String firstName;
		String lastName;
		String ordination;
		String title;
		String parish;
		String city;
		boolean readline = false;
		int writeLine = 0;
		boolean write = true;

		// out.write("Last Name\tFirst Name\tOrdination\tPosition\tParish\tCity\n");
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			strategy = parser.processContent(i,
					new SimpleTextExtractionStrategy());
			String tmp = strategy.getResultantText();
			// System.out.println(tmp);
			String[] lines = tmp.split(System.getProperty("line.separator"));
			for (String tmpLine : lines) {

				if (tmpLine
						.contains("Priests of the Archdiocese in the Order of Ordination")
						&& !tmpLine.contains("G-28")) {
					// System.out.println(tmpLine);
					write = false;
				}

				if (write == false) {
					break;
				}
				if (tmpLine.contains("Updated:")) {
					dateStr = tmpLine;
				}
				// Ailer, Rev. Gellert J. (2006)
				if (tmpLine.contains("(COL)")) {
					String[] parsedLine = tmpLine.split("\\(COL\\)");
					firstName = parsedLine[1];
					parsedLine = firstName.split(" ");
					firstName = parsedLine[0];
					lastName = parsedLine[1];
					ordination = "";
				} else if (tmpLine.contains("(Mel)")) {
					String[] parsedLine = tmpLine.split("\\(Mel\\)");
					firstName = parsedLine[1];
					parsedLine = firstName.split(" ");
					firstName = parsedLine[0];
					lastName = parsedLine[1];
					ordination = "";
				} else if (tmpLine.contains("(Cdr.)")
						|| tmpLine.contains("(Col.)")
						|| tmpLine.contains("(Charles)")) {

				}
				// Check Cdr. Col.
				else if ((!tmpLine.contains("Cdr.")) && (tmpLine.contains("("))
						&& (tmpLine.contains(")"))
						&& (!tmpLine.contains("Phone"))) {
					// System.out.println("tmpLine = " + tmpLine);
					String[] parsedLine = tmpLine.split(",");
					lastName = parsedLine[0];
					int startIndex = 1;
					// System.out.println("parsedLine len = " +
					// parsedLine.length);
					if (parsedLine.length == 3) {
						startIndex = 2;
					} else if (parsedLine.length == 4) {
						startIndex = 3;
					}
					System.out
							.println("first name = " + parsedLine[startIndex]);
					parsedLine = parsedLine[startIndex].split("\\(");
					if (parsedLine.length == 4)
						firstName = parsedLine[1];
					else
						firstName = parsedLine[0];
					firstName = firstName.replace("Rev.", "");
					firstName = firstName.replace("Msgr.", "");
					firstName = firstName.replace("Most", "");
					firstName = firstName.replace("Cdr.", "");
					ordination = parsedLine[1].replace(")", "");
					readline = true;
					String outLine = lastName + "\t" + firstName + "\t"
							+ ordination + "\t";
					out.write(outLine);
				} else if (readline) {
					// Pastor, St. John the Baptist, Silver Spring
					String[] titleParsed = tmpLine.split(",");
					System.out.println("title " + tmpLine);
					System.out.println("len " + titleParsed.length);
					if (titleParsed.length == 1) {
						title = tmpLine;
						parish = "";
						city = "";
					} else if (titleParsed.length == 2) {
						title = titleParsed[0];
						parish = titleParsed[1];
						city = "";
					} else {
						title = titleParsed[0];
						parish = titleParsed[1];
						city = titleParsed[2];
					}
					String outLine = title + "\t" + parish + "\t" + city + "\n";
					out.write(outLine);
					readline = false;
				}
			}
		}
		out.flush();
		out.close();
		reader.close();
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
	// parser for OCD 1969 Falls River
	public void parsePdf(String pdf, String txt) throws IOException {
		PdfReader reader = new PdfReader(pdf);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		PrintWriter out = new PrintWriter(new FileOutputStream(txt));
		TextExtractionStrategy strategy;
		String dateStr;
		String firstName;
		String lastName;
		String ordination;
		String title;
		String parish = "";
		String city;
		String dioceseName = "";
		String[] parsedLine;
		boolean concat = false;
		boolean parseChurches = false;
		boolean parseInstitutions = false;
		boolean done = false;
		boolean concatTitle = false;
		String titlePrefix = "";
		boolean formerbishops = false;
		int state = 0;
		out.write("Diocese\tName\tTitle\tParish\n");
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			// state = 0; ?
			strategy = parser.processContent(i,
					new SimpleTextExtractionStrategy());
			String tmp = strategy.getResultantText();
			// System.out.println(tmp);
			String[] lines = tmp.split(System.getProperty("line.separator"));
			String name = "";
			String bishopName = "";
			title = "";
			for (String tmpLine : lines) {

				if (tmpLine
						.contains("The Catholic Church in the United States")) {
					// 1—CATHEDRAL OF ST. MARY OF THE ASSUMPTION,
					// Rt RPV. Robert L. Stanton. Rector; Revs.
					// Paul F. McCarrick: William G. Campbell;
					// Edward J. Mitchell. S.T.L., J.C.D.
					// Res..
					System.out.println("found line in " + pdf);
					concat = false;
					// parseChurches = true;
				}
				if (tmpLine.contains("INSTITUTIONS OF THE DIOCESE")) {
					// System.exit(-1);
					parseChurches = false;
				}

				if (done == false) {
					if (parseInstitutions == true) {

					} else if (parseChurches == true) {
						System.out.println("parish line = " + tmpLine);
						// ("parse church tmpline = "+tmpLine);

						if ((tmpLine.contains("—") || tmpLine
								.contains("OUTSIDE THE CITY OF"))
								&& (concat == true)) {

							if (tmpLine.contains("OUTSIDE THE CITY OF")) {
								// System.exit(-1);
								parseChurches = false;
								parseInstitutions = true;
								try {
									String[] tmp2 = name.split("Res.");
									out.write(dioceseName + "\t" + tmp2[0]
											+ "\t\t" + parish + "\t" + tmp2[1]
											+ "\n");
								} catch (Exception e) {
									out.write("ERROR \t" + name);
								}
							}
							System.out.println("name = " + name);
							String[] tmpparse = tmpLine.split("—");
							try {
								int number = Integer.parseInt(tmpparse[0]);
								System.out.println("*** parish " + parish);
								System.out.println("****  name = " + name);
								// concat = false;
								name = name.replace(", OF M.Conv.", "");
								String[] tmp2 = name.split("Res\\.");
								String[] priestNames2 = tmp2[0].split(";");
								for (String priestName2 : priestNames2) {
									String[] priestNames = priestName2
											.split(":");
									for (String priestName : priestNames) {
										priestName = priestName
												.replace("-", "");
										priestName = priestName.replace(
												", O.P.", "");
										priestName = priestName.replace(
												", J.C.D.", "");
										String[] priestNames3 = priestName
												.split(",");
										for (String priestName3 : priestNames3) {
											out.write(dioceseName + "\t"
													+ priestName3 + "\t\t"
													+ parish + "\t" + tmp2[1]
													+ "\n");
										}
									}
								}
								name = "";
								parish = tmpparse[1];
								System.out.println("next parish " + parish);
								System.out.println("next parish line "
										+ tmpLine);
								tmpparse[1] = "";
								tmpparse[0] = "";
								if (parish.contains("Rt RPV")) {
									tmpparse = parish.split("Rt RPV");
									parish = tmpparse[0];
								}
								if (parish.contains("Rt. Rev.")) {
									tmpparse = parish.split("Rt\\. Rev\\.");
									parish = tmpparse[0];
								}
								if (parish.contains("Revs.")) {
									tmpparse = parish.split("Revs\\.");
									parish = tmpparse[0];
								}
								if (parish.contains("Rev.")) {
									tmpparse = parish.split("Rev\\.");
									parish = tmpparse[0];
								}

								try {
									name = tmpparse[1];
									System.out.println("parish2 = " + parish);
									System.out.println("name = " + name);

									concat = true;
								} catch (Exception ex) {
									System.out.println("EXCEPTION tmpLine");
									// name = "";
								}
							} catch (Exception e) {

							}

						} else if (tmpLine.contains("—")) {

							String[] tmpparse = tmpLine.split("—");
							parish = tmpparse[1];
							tmpparse[1] = "";
							tmpparse[0] = "";
							if (parish.contains("Rt RPV")) {
								tmpparse = parish.split("Rt RPV");
								parish = tmpparse[0];
							}
							if (parish.contains("Rt. Rev.")) {
								tmpparse = parish.split("Rt. Rev.");
								parish = tmpparse[0];
							}
							if (parish.contains("Revs.")) {
								tmpparse = parish.split("Revs.");
								parish = tmpparse[0];
							}
							try {
								name = tmpparse[1];
								System.out.println("parish2 = " + parish);
								System.out.println("name = " + name);

								concat = true;
							} catch (Exception ex) {
								System.out.println("EXCEPTION tmpLine");
								// name = "";
							}
						} else if (concat == true) {
							name = name + tmpLine;
							System.out.println("name1 = " + name);
						}
					} else { // parse bishop section
						System.out.println("tmpline = " + tmpLine);
						if (tmpLine.contains("Diocese of")
								&& !tmpLine.contains("G-28")) {
							System.out.println(tmpLine);
							dioceseName = tmpLine;
						}

						if (tmpLine.contains("Examiners of the Clergy ")
								|| tmpLine.contains("Friends of the Catholic ")
								|| tmpLine
										.contains("The National Catholic Office for")) {
							concatTitle = true;
							titlePrefix = tmpLine;
							System.out.println("concat Examiners!");
						}
						if (tmpLine.contains("Former Bishops")) {
							state = -1;
							formerbishops = true;
						}
						if (tmpLine.contains("Most Rev") && state != -1) {
							state = 1; // read bishop name next
						} else if (state == 1) {
							bishopName = tmpLine;
							state = 2;
						} else if (state == 2) {
							out.write(dioceseName + "\t" + bishopName + "\t"
									+ tmpLine + "\n");
							state = 0;
						} else if ((tmpLine.contains("—"))
								&& (tmpLine.contains("Rev"))
								&& (concat == true)) {
							System.out.println("tmpLine2 = " + tmpLine);
							System.out.println("formerbishops = "
									+ formerbishops);
							if (formerbishops == true) {
								// out.write(dioceseName + "\t" + bishopName
								// + "\t" + tmpLine + "\n");
								formerbishops = false;
							} else if ((!bishopName.contains(";"))
									&& (formerbishops == false)) {
								out.write(dioceseName + "\t" + bishopName
										+ "\t" + title + "\n");
							} else if ((!bishopName.contains("ordained"))
									&& (formerbishops == false)
									&& (!bishopName.contains("cons."))) {
								System.out.println("MULTIPLE2 bishiops!!");
								String[] bishopNames = bishopName.split(";");
								for (String tmpBishopName : bishopNames)
									out.write(dioceseName + "\t"
											+ tmpBishopName + "\t" + title
											+ "\n");
							} else {
								formerbishops = false;
								out.write(dioceseName + "\t" + bishopName
										+ "\t" + title + "\n");
							}
							concat = false;
						}
						if ((tmpLine.contains("—"))
								&& (tmpLine.contains("Rev"))) {
							System.out.println("split tmpLine?  = "
									+ bishopName);
							parsedLine = tmpLine.split("—");
							title = parsedLine[0];
							if (concatTitle == true) {
								title = titlePrefix + title;
								concatTitle = false;
							}

							bishopName = parsedLine[1];
							System.out.println("title = " + title);
							System.out.println("bishopName  = " + bishopName);
							concat = true;
						} else if (concat == true) {
							bishopName = bishopName + tmpLine;
							System.out.println("bishopName concat = "
									+ bishopName);
						} else {
							title = "";
							concat = false;
						}

						// if (write == false) {
						// break;

					}
				}
			}
		}
		out.flush();
		out.close();
		System.out.println("OUTPUT file name = " + txt);
		reader.close();

	}

	private boolean isDioceseInState(String line, String state) {
		return this.dioStateHash.isDioceseInState(line, state);
	}
	
	private boolean isDioceseCity(String city, String state) {
		return this.dioStateHash.isDioceseCity(city, state);
	}
	
	private boolean fuzzyFind(String line, String searchStr) {
		boolean ret = false;
		int maxScore = 0;
		
		String tokens[] = line.toUpperCase().split(" ");
		for (String token : tokens) {
			int tmpScore = 0;
			int minLength = searchStr.length()-2;
			if (token.length()>= minLength) {
				for (int k=0;k<minLength; k++) {
					if (searchStr.charAt(k) == token.charAt(k)) {
						++tmpScore;
					}
				}
				if (tmpScore > maxScore) {
					maxScore = tmpScore;
				}
			}
		}
		ret = (maxScore >= searchStr.length()-2);
		return ret;
	}

	public void findPriestAssignments(String pdf, String txt, String first,
			String last) throws IOException {
		PdfReader reader = new PdfReader(pdf);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		if (out == null) {
			String firstName = first.replace(" ", "_");
			out = new PrintWriter(new FileOutputStream(firstName + "_" + last
					+ "_query.txt"));
			out.write("Year\tDiocese\tCity\tPosition\tPriest\tParish\tResidence\tpage\n");
		}

		TextExtractionStrategy strategy;
		String[] parseYear = pdf.split("_");
		System.out.println(parseYear[1]);
		String[] year = parseYear[1].split("\\.");
		System.out.println(year[0]);
		int intYear = Integer.parseInt(year[0]);

		String ordination;
		String title;
		String parish = "";
		String city;
		String dioceseName = "";
		boolean readline = false;
		int writeLine = 0;
		boolean write = true;
		boolean writeBishop = false;
		boolean writeTitle = false;
		String[] parsedLine;
		boolean concat = false;
		boolean parseAbbrev = false;
		boolean parseInstitutions = false;
		boolean done = false;
		boolean concatTitle = false;
		String prevLine = "";
		boolean formerbishops = false;
		boolean searchArch = false;
		int state = 0;
		boolean getLastName = false;
		boolean findCities = false;
		boolean dioceseFound = false;
		boolean upperFound = false;
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			// state = 0; ?
			boolean findDiocese = false;
			boolean firstNameFound = false;
			String savedLine = "";
			strategy = parser.processContent(i,
					new SimpleTextExtractionStrategy());
			String tmp = strategy.getResultantText();
			// System.out.println(tmp);
			String[] lines = tmp.split(System.getProperty("line.separator"));
			String name = "";

			String priestLines = "";
			title = "";
			String cityStr = "";
			int count = 0;
			int found = -1;
			int foundChurch = -1;
			String dirStr = "";			
			String lastName = "";
			for (String tmpLine : lines) {

				if (tmpLine.contains(last.toUpperCase())) {
					System.out.printf("%d %s\n",i,"last name line = "+tmpLine);
					upperFound = true;
				} else {
					upperFound = false;
				}
				if (tmpLine.toUpperCase().contains(
						"SECULAR AND REGULAR PRIESTS")) {
					getLastName = true;
				}
				if (getLastName) {
					if (tmpLine.contains(",")) {
						lastName = tmpLine.substring(0, tmpLine.indexOf(","));
					}
					if (tmpLine.contains("sick leave")
							&& tmpLine.contains(last)) {
						System.out.println("\n*** " + lastName
								+ " *** SICK leave **** " + tmpLine);
					}
				}
				// if ((tmpLine.contains("Ray")) ||
				// (tmpLine.contains("Robert Jack-"))) {
				if (tmpLine.toUpperCase().contains("CITY OF ")) {
					cityStr = tmpLine.substring(9);
				}
				if (tmpLine.toUpperCase().contains("METROPOLITAN")) {
					cityStr = tmpLine.substring(14);
				}
				if (tmpLine.toUpperCase().contains("OUTSIDE THE")) {
					findCities = true;
				}
				if ((findCities)
						&& ((tmpLine.contains("Co.")) || (tmpLine
								.contains("Co,")))) {
					cityStr = tmpLine.substring(0, tmpLine.indexOf("Co"));
				}
				if ( ((tmpLine.toUpperCase().contains("DIOECESIS")) ||
						(tmpLine.toUpperCase().contains("DIOECESIA")))
						&& (findDiocese)) {
					try {
						dioceseFound = true;
					} catch (Exception e) {
						e.printStackTrace();
						dioceseName = "Unknown";
					}
				} else if (!dioceseFound) {
					try {
						dioceseName = tmpLine.split(" ")[1];
					} catch (Exception ex) {}
				}
				if (tmpLine.toUpperCase().contains("DIOCESE OF")
						&& (!findDiocese)) {
					findDiocese = true;
					dioceseName = tmpLine.substring(tmpLine
							.toUpperCase().indexOf("DIOCESE OF") + 11);
					if (dioceseName.contains(",")) {
						dioceseName = dioceseName.substring(0, dioceseName.indexOf(","));
					}
					
				} else {
					prevLine = tmpLine;
					findDiocese = false;
				}
				if (tmpLine.toUpperCase().contains("STATISTICS")
						|| tmpLine.toUpperCase().contains("RECAP")
						|| tmpLine.toUpperCase().contains("(CONTINUED")) {
					findDiocese = false;
					dioceseFound = false;
				}

				// TODO add edit distance for fuzzy search
				// Create search String
				// find: first last
				// last, first MI
				// last, fist
				// look for hyphenated names
				// look for names split across rows.
				// Add edit distance
				String searchFirstLast = first + " " + last;
				String searchLastFirst = last + ", " + first;
				boolean searchFound;
				if (first.split(" ").length == 2) {
					// search first/last and not middle.
					String[] firstOnly = first.split(" ");
					String searchFirstOnlyLast = firstOnly[0] + " " + last;
					searchFound = (tmpLine.contains(searchFirstLast)
							|| tmpLine.contains(searchLastFirst) || tmpLine
							.contains(searchFirstOnlyLast));
				} else {
					searchFound = (tmpLine.contains(searchFirstLast) || tmpLine
							.contains(searchLastFirst));
				}
				if (searchFound) {
					System.out.printf("book %s page %d line %s\n", pdf, i,
							tmpLine);
					found = count;
					break;
				} else {
					
		//			if (upperFound) {
			//			System.out.println("search upper *** "+tmpLine);
				//	}
					// Look for first only or last only and then set search
					// to look on next line.
					// look for upper case
					String searchFirstLast2 = first + " " + last;
					String searchLastFirst2 = last + ", " + first;
					boolean searchFound2;
					searchFirstLast2 = searchFirstLast2.toUpperCase();
					searchLastFirst2 = searchLastFirst2.toUpperCase();
					String[] firstOnly = first.split(" ");
					if (first.split(" ").length == 2) {
						// search first/last and not middle.
						String searchFirstOnlyLast2 = firstOnly[0] + " " + last;
						searchFirstOnlyLast2 = searchFirstOnlyLast2.toUpperCase();
						searchFound2 = (tmpLine.contains(searchFirstLast2)
								|| tmpLine.contains(searchLastFirst2) || tmpLine
								.contains(searchFirstOnlyLast2));
					} else {
						searchFound2 = (tmpLine.contains(searchFirstLast2) || tmpLine
								.contains(searchLastFirst2));
					}

					if ((tmpLine.toUpperCase().contains("DIRECTOR OF CONFRAT"))) {
						tmpLine = tmpLine.replace("\u2014", "");
						tmpLine = tmpLine.substring(0, tmpLine.length()-1);
						dirStr = tmpLine;
					}
					if ((tmpLine.indexOf("\u2014") != -1)) {
						if (savedLine != "") {
							// parse savedLine
							if (savedLine.toUpperCase().contains(searchFirstLast2.toUpperCase()) ||
								savedLine.toUpperCase().contains(searchLastFirst2.toUpperCase())) {
								savedLine = dirStr + savedLine;
								
					//			System.out.println("SAVED LINE = "+savedLine);
								String pos[] = savedLine.split("\u2014");
								System.out.println("pos0 = "+pos[0]);
								if (Pattern.matches("[0-9]+", pos[0]) == false) {
									out.write(Integer.toString(intYear) + "\t" + dioceseName + "\t"
											+ cityStr + "\t"+pos[0]+"\t" + first + " " + last + "\t"
											+ "" + "\t" + "" + "\t" + i + "\n");
								}
							} 						
						}
						savedLine = tmpLine;
					} else {
						savedLine += tmpLine;
					}
				}
				++count;
				// if (i == 439) System.out.println("tempLine = "+tmpLine);
			} // for lines
			if (found != -1) {
				count = 0;
				System.out.println("\n***found:");
				for (String tmpLine : lines) {
					if ((count > found - 8) && (count <= found + 8)) {
						if (tmpLine.indexOf(last) == 0) {
							System.out.printf("book %s page %d line %s\n", pdf, i,
									tmpLine);							
						}
						priestLines += tmpLine;
					}

					++count;
				}
				//System.out.println("priestline = " + priestLines);				
				String priests[] = priestLines.split("\u2014");
				String resStr = "";
				String parishGuess = "none";
				for (String guessPriest : priests) {
					System.out.println("guessPriest = "+guessPriest);
					if (guessPriest.toUpperCase().contains("ACTIVE OUTSIDE")) {
						if (guessPriest.indexOf(last+", ") != -1) {
							parishGuess = guessPriest.split(last+", ")[1];
							resStr = parishGuess.substring(parishGuess.indexOf(","));
							parishGuess = parishGuess.split(",")[0];			
							System.out.println("active outside guess = "+resStr);
							System.out.println("index = "+resStr.indexOf(":"));
							if (resStr.indexOf(";") != -1) {
								resStr = resStr.substring(0, resStr.indexOf(";"));
							}
							if (resStr.indexOf(":") != -1) {
								resStr = resStr.substring(0, resStr.indexOf(":"));
							}							
							System.out.println("active outside guess = "+resStr);
						}
					}
					else if (guessPriest.indexOf(last) != -1) {
						System.out.println("PARSE THIS?? : " + guessPriest);
						if (guessPriest.indexOf(first) != -1) {
							String[] beforeLines = guessPriest.split(first);
							beforeLines = beforeLines[0].split("\u2014");
						//	for (String tmp2 : beforeLines) {
							//	System.out.println("parish quess "+tmp);
						//	}
						}
						if ((guessPriest.indexOf("Co,,") != -1) ||
								(guessPriest.indexOf("Co.,") != -1)) {
							String[] pline = guessPriest.split("Co,,"); 
							if (guessPriest.indexOf("Co.,") != -1) { 
								// TODO add Rev Msgr etc.
								 pline = guessPriest.split("Co.,"); 
								 int splitIndex = pline[1].indexOf("Very") != -1 ?
										 pline[1].indexOf("Very") : pline[1].indexOf("Rev");
								parishGuess = pline[1].substring(0, splitIndex);
							} else {
								// TODO add Rev Msgr etc.
								 int splitIndex = pline[1].indexOf("Very") != -1 ?
									 pline[1].indexOf("Very") : pline[1].indexOf("Rev");								
								parishGuess = pline[1].substring(0, splitIndex);
							}

						} else { 
							if (guessPriest.indexOf(" (") != -1) {
								parishGuess = guessPriest.substring(0, guessPriest.indexOf(" ("));
							} 
							if (guessPriest.indexOf("Sister") != -1) {
							
								// parse school
								if (guessPriest.indexOf(first) != -1) {
									String[] pline = guessPriest.split(" \\("); // TODO add Co.,
									pline = pline[0].split(" ");
									parishGuess = pline[pline.length-3] + " " +
											pline[pline.length-2] + " " +
											pline[pline.length-1];  // TODO fix this
								}
							}
						}
						if (guessPriest.contains("Res")) {							
							resStr = guessPriest.substring(guessPriest
									.indexOf("Res") + 5);
							System.out.println ("resStr = "+resStr);
							if ((resStr.contains("Co.")) ||
									(resStr.contains("Co,"))) {
								resStr = resStr.substring(0, resStr.indexOf("Co"));
							}
							if ((resStr.contains("Schoo"))) {
								resStr = resStr.substring(0, resStr.indexOf("Schoo"));
							}
							if ((resStr.contains("St."))) {
								resStr = resStr.substring(0, resStr.indexOf("St."));
							}

						}
						System.out.println("parishGuess = "+parishGuess);
					}
				}

				String tmp2 = Integer.toString(intYear) + "\t" + dioceseName
						+ "\t" + first + " " + last + "\n";
				System.out.println(tmp2);

				if (priestLines.contains("Absent on sick leave")) {
					parishGuess = "Absent on sick leave";
				} else if (priestLines.contains(" on sick leave")) {
					int indexLastName = priestLines.indexOf(last);
					int indexsick = priestLines.indexOf("on sick leave");
					System.out
							.printf(" ilast %d is %d\n",
									indexLastName + last.length()
											+ first.length() + 12, indexsick);
					if (indexsick < indexLastName + last.length()
							+ first.length() + 12) {
						parishGuess = "on sick leave";
					}
				} else if (priestLines.contains("Absent with leave")) {
					parishGuess = "Absent with leave";
				} 

				out.write(Integer.toString(intYear) + "\t" + dioceseName + "\t"
						+ cityStr + "\t\t" + first + " " + last + "\t"
						+ parishGuess + "\t" + resStr + "\t" + i + "\n");

			}

			if (foundChurch != -1) {
				count = 0;
				System.out.println("\n***found CHURCH:");
				for (String tmpLine : lines) {
					if ((count > foundChurch - 6) && (count <= foundChurch + 3)) {
						System.out.printf("book %s page %d line %s\n", pdf, i,
								tmpLine);
					}

					++count;
				}
			}

		}

		reader.close();

	}

	public void findPriestsCity(String pdf, String txt, String city,
			String state) throws IOException {
		
		if (this.isDioceseCity(city, state)) {
			System.out.println("Find churches in diocese city "+city);
		} else {
			System.out.println("Find churches outside of diocese city "+city);
			findPriestsOutsideCity(pdf, txt, city, state);
		}
	}
	
	private void findPriestsDioceseCity(String pdf, String txt, String city,
			String state) throws IOException {
		PdfReader reader = new PdfReader(pdf);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);

		TextExtractionStrategy strategy;

		boolean firstDash = false;
		boolean parseChurches = false;
		boolean parsePriests = false;
		boolean parsePriests2 = false;
		boolean parishesFound = false;
		boolean dioceseFound = false;
		boolean done = false;
		boolean findCity = false;
		boolean findCityStart = false;
		boolean findOutsideInsideCity = false;
		boolean nextbook = false;
		boolean skipLine = false;
		String currentDiocese = "";
		String parishLine = "";
		String cityLine = "";
		// out.write("Diocese\tName\tTitle\tParish\n");
		if (out == null) {
			String cityName = city.replace(" ", "_");
			out = new PrintWriter(new FileOutputStream(cityName + "_" + state
					+ "_query.txt"));
			out.write("Year\tDiocese\tParish\tPriests\tResidence\n");
		}

		System.out.println("pdf = " + pdf);
		String[] parseYear = pdf.split("_");
		System.out.println(parseYear[1]);
		String[] year = parseYear[1].split("\\.");
		System.out.println(year[0]);
		int intYear = Integer.parseInt(year[0]);

		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			// state = 0; ?
			strategy = parser.processContent(i,
					new SimpleTextExtractionStrategy());
			String tmp = strategy.getResultantText();

			String[] lines = tmp.split(System.getProperty("line.separator"));

			for (String tmpLine : lines) {

			
				if ((tmpLine.toUpperCase().contains("RECAPITULATION"))
						|| (tmpLine.toUpperCase().contains("NECROLOGY"))
						|| (tmpLine.toUpperCase()
								.contains("INSTITUTIONS OF THE DIO"))
						|| (tmpLine.toUpperCase().contains("ORPHANAGES"))) {
					dioceseFound = false;
					parseChurches = true;
					parsePriests = false;
					parsePriests2 = false;
					
					findCity = false;
					findCityStart = false;
					findOutsideInsideCity = false;
					nextbook = false;
					skipLine = false;
				}
				if (tmpLine.toUpperCase().contains(
						"THE CATHOLIC CHURCH IN THE UNITED STATES")) {
					// 1—CATHEDRAL OF ST. MARY OF THE ASSUMPTION,
					// Rt RPV. Robert L. Stanton. Rector; Revs.
					// Paul F. McCarrick: William G. Campbell;
					// Edward J. itchell. S.T.L., J.C.D.
					// Res..
					parseChurches = true;

				}
				if ((parseChurches)
						&& (tmpLine.toUpperCase()
								.contains("PLACES IN THE UNITED STATES"))
						&& (!tmpLine.contains("of Men and Women;"))
						&& (!tmpLine.contains(" Having a "))) {
					System.out.println("found END line in " + pdf);
					System.out.println("found END line " + tmpLine);
					nextbook = true;
					parseChurches = false;
				}
				if (parseChurches) {
					if (((tmpLine.toUpperCase().contains("DIOCESE OF")) || tmpLine
							.toUpperCase().contains("(DIOECESIS"))
							&& (!tmpLine.toUpperCase().contains("REPRESENTED"))) {

						if (this.isDioceseInState(tmpLine, state)) {
							System.out.println("FOUND dioceses " + tmpLine);
							parseChurches = false;
							findCityStart = true;
							findOutsideInsideCity = false;
							currentDiocese = this.dioStateHash
									.getCurrentDiocese();
							System.out.println("FOUND CURRENT dioceses " + currentDiocese);
						}
						if (tmpLine.toUpperCase().contains(city.toUpperCase())) {
							System.out.println("FOUND dioceses2 " + tmpLine);
							parseChurches = false;
							findCityStart = true;
							findOutsideInsideCity = false;
							currentDiocese = city;
						}
					}		
				} else if (findCityStart) {
					//System.out.println("findCityStart "+tmpLine);
					//CLERGY, PARISHES, MISSIONS AND PAROCHIAL SCHOOLS 
					if (tmpLine.toUpperCase().contains("CLERGY, PARISHES")) {
						findOutsideInsideCity = true;
						findCityStart = false;
						System.out.println("************** HERE");
					}
				
				} else if (findOutsideInsideCity) {

					String token = "";
					String token2 = "";
					//System.out.println("tmpLion = "+tmpLine);
					if (city.toUpperCase().equals(currentDiocese.toUpperCase())) {
						token = "CITY OF";
						token2 = "METROPOLITAN";
						dioceseFound = true;
						findCity = false;
						if (tmpLine.toUpperCase().contains(city.toUpperCase())) {
							findOutsideInsideCity = false;
							parsePriests2 = true;
							System.out.println("************** HERE2");
						} 
					} else {
						token = "OUTSIDE THE CIT";
						token2 = "OUTSIDE METROPOLITAN";
						findCity = true;
						//System.out.println("here? "+tmpLine);
						if ((tmpLine.toUpperCase().contains(token))
								|| (tmpLine.toUpperCase().contains(token2))
								|| (tmpLine.toUpperCase()
										.contains("OUTSDDE THE CITY OF "))
								|| (tmpLine.toUpperCase()
										.contains("OUTSIDE THE CZTY OF "))) {
							dioceseFound = true;
							findCity = true;
							findOutsideInsideCity = false;
							System.out.println("FOUND CITY " + tmpLine);
						}
					}
				} else if (findCity) {
					// System.out.println("findCity STATE: "+tmpLine);
					if (((tmpLine.indexOf(city + ",") == 0)
							|| (tmpLine.indexOf(city + ",") == 0) || (tmpLine
							.indexOf(city + ".") == 0))
							&& ((tmpLine.contains("Co.") || (tmpLine
									.contains("Co,"))))) {
						System.out.println("FOUND CITY **** " + city);
						System.out.println("FOUND CITY line **** " + tmpLine);
						findCity = false;
						parsePriests = true;
						parseChurches = false;
						cityLine = tmpLine.substring(tmpLine.indexOf("Co")+ 4);
						cityLine = cityLine.substring(0, cityLine.length()-1);
					}
				} else if (parsePriests2) {
					//if (tmpLine.toUpperCase().contains("CATHED")) {
					//	System.out.println("tmpLine = "+tmpLine);
					//	for (int k=0; k<tmpLine.length(); k++) {
					//		System.out.printf("%c %d\n",tmpLine.charAt(k),(int)tmpLine.charAt(k));
					//	}
					//	System.exit(-1);
					//}					
					if (!tmpLine.toUpperCase().contains("OUTSIDE ")) {
							//parishesFound = true;
							parishLine = parishLine + tmpLine;
								// System.out.println ("p="+tmpLine);
							//System.out.println("parishLine = "+parishLine);
							if (intYear < 1960) { // change check for
															// year < 1960
								skipLine = true;							
							} else {
								skipLine = false;
							}

					} else {
						System.out.println("!parse priests " + tmpLine);
						parsePriests = false;
						parseChurches = true;
						dioceseFound = false;
						if (parishLine != "") {
							System.out.println("parish line = " + parishLine);
							// parseParishLine(parishLine);
							parishLine = cityLine + parishLine;
							String parishes[] = parishLine.split("\u2014");
							for (String parishStr : parishes) {
								if ((!parishStr.toUpperCase().startsWith(
										"SCHOOL"))
										&& (parishStr.indexOf("Rev") != -1)) {

									// parish
									String parish = parishStr.substring(0, parishStr.indexOf(","));
									// revs
									String revStr = parishStr.substring(parishStr.indexOf("Rev")+5,
														parishStr.indexOf("Res"));
									// res
									String resStr = parishStr.substring(parishStr.indexOf("Res"+4));
								
									System.out.println("parish = " + parishStr);
									out.write(Integer.toString(intYear) + "\t"
											+ currentDiocese + "\t" + parish + "\t" +
											revStr + "\t" + resStr
											+ "\n");

								}
							}
						}

					}
					
				
				} else if (parsePriests) {

					parseChurches = false;
					parsePriests = true;

					if (!tmpLine.contains(" Co., ")
							&& !tmpLine.contains(" Co.. ")) {
						if (!skipLine) {
							parishesFound = true;
							if (tmpLine.toUpperCase().indexOf(
									currentDiocese.toUpperCase()) != 0) {
								parishLine = parishLine + tmpLine;
								// System.out.println ("p="+tmpLine);
							} else if (intYear < 1960) { // change check for
															// year < 1960
								skipLine = true;
							}
						} else {
							skipLine = false;
						}

					} else {
						System.out.println("!parse priests " + tmpLine);
						parsePriests = false;
						parseChurches = true;
						dioceseFound = false;
						if (parishLine != "") {
							System.out.println("parish line = " + parishLine);
							// parseParishLine(parishLine);
							parishLine = cityLine + parishLine;
							String parishes[] = parishLine.split("\u2014");
							for (String parishStr : parishes) {
								if ((!parishStr.toUpperCase().startsWith(
										"SCHOOL"))
										&& (parishStr.indexOf("Rev") != -1)) {

									parishStr = parishStr.replace("-", "");
									if (parishStr.contains("Very")) {
										parishStr = parishStr.replaceFirst(
												"Very", "\t");
										parishStr = parishStr.replaceFirst(
												"\t", "\tVery");
									} else if (parishStr.contains("Rt.")) {
										parishStr = parishStr.replaceFirst(
												"Rt.", "\t");
										parishStr = parishStr.replaceFirst(
												"\t", "\tRt.");
									} else {
										parishStr = parishStr.replaceFirst(
												"Rev", "\t");
										parishStr = parishStr.replaceFirst(
												"\t", "\tRev");
									}
									parishStr = parishStr.replaceFirst("Res.,",
											"\t");
									parishStr = parishStr.replaceFirst("Res..",
											"\t");
									if (parishStr.indexOf("[CEM]") != -1) {
										parishStr = parishStr.substring(0,
												parishStr.indexOf("[CEM]") - 1);
									}
									if (parishStr.indexOf("School") != -1) {
										parishStr = parishStr
												.substring(0, parishStr
														.indexOf("School") - 1);
									}
									System.out.println("parish = " + parishStr);
									out.write(Integer.toString(intYear) + "\t"
											+ currentDiocese + "\t" + parishStr
											+ "\n");

								}
							}
						}

					}
				}

			}
			if ((!parsePriests) && (!findCity) && (!parsePriests2)) {
				parseChurches = true;
			} else {
				parseChurches = false;
			}
			// if ((dioceseFound) && (!parishesFound)) {
			// out.write(Integer.toString(intYear)+"\tNot Found\n");
			// }
			if (nextbook) {
				break;
			}
		}

		reader.close();

	}
	
	private void findPriestsOutsideCity(String pdf, String txt, String city,
			String state) throws IOException {
		PdfReader reader = new PdfReader(pdf);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);

		TextExtractionStrategy strategy;

		boolean firstDash = false;
		boolean parseChurches = false;
		boolean parsePriests = false;
		boolean parsePriests2 = false;
		boolean parishesFound = false;
		boolean dioceseFound = false;
		boolean done = false;
		boolean findCity = false;
		boolean findCityStart = false;
		boolean findOutsideInsideCity = false;
		boolean nextbook = false;
		boolean skipLine = false;
		String currentDiocese = "";
		String parishLine = "";
		String cityLine = "";
		// out.write("Diocese\tName\tTitle\tParish\n");
		if (out == null) {
			String cityName = city.replace(" ", "_");
			out = new PrintWriter(new FileOutputStream(cityName + "_" + state
					+ "_query.txt"));
			out.write("Year\tDiocese\tParish\tPriests\tResidence\n");
		}

		System.out.println("pdf = " + pdf);
		String[] parseYear = pdf.split("_");
		System.out.println(parseYear[1]);
		String[] year = parseYear[1].split("\\.");
		System.out.println(year[0]);
		int intYear = Integer.parseInt(year[0]);
		boolean alphList = false; // alphabetical list reached.  parse differently

		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			// state = 0; ?
			strategy = parser.processContent(i,
					new SimpleTextExtractionStrategy());
			String tmp = strategy.getResultantText();

			String[] lines = tmp.split(System.getProperty("line.separator"));
			if (alphList) {
				break;
			}

			for (String tmpLine : lines) {

			
				if ((tmpLine.toUpperCase()
						.contains("Secular and Regular Priests of the United States"))) {
					alphList = true;
				}
				
				if ((tmpLine.toUpperCase().contains("RECAPITULATION"))
						|| (tmpLine.toUpperCase().contains("NECROLOGY"))
						|| (tmpLine.toUpperCase()
								.contains("INSTITUTIONS OF THE DIO"))
						|| (tmpLine.toUpperCase().contains("ORPHANAGES"))) {
					dioceseFound = false;
					parseChurches = true;
					parsePriests = false;
					parsePriests2 = false;
					
					findCity = false;
					findCityStart = false;
					findOutsideInsideCity = false;
					nextbook = false;
					skipLine = false;
				}
				if (tmpLine.toUpperCase().contains(
						"THE CATHOLIC CHURCH IN THE UNITED STATES")) {
					// 1—CATHEDRAL OF ST. MARY OF THE ASSUMPTION,
					// Rt RPV. Robert L. Stanton. Rector; Revs.
					// Paul F. McCarrick: William G. Campbell;
					// Edward J. itchell. S.T.L., J.C.D.
					// Res..
					parseChurches = true;

				}
				if ((parseChurches)
						&& (tmpLine.toUpperCase()
								.contains("PLACES IN THE UNITED STATES"))
						&& (!tmpLine.contains("of Men and Women;"))
						&& (!tmpLine.contains(" Having a "))) {
					System.out.println("found END line in " + pdf);
					System.out.println("found END line " + tmpLine);
					nextbook = true;
					parseChurches = false;
				}
				if (parseChurches) {
					if (((tmpLine.toUpperCase().contains("DIOCESE OF")) || tmpLine
							.toUpperCase().contains("(DIOECESIS"))
							&& (!tmpLine.toUpperCase().contains("REPRESENTED"))) {

						if (this.isDioceseInState(tmpLine, state)) {
							System.out.println("FOUND dioceses " + tmpLine);
							parseChurches = false;
							findCityStart = true;
							findOutsideInsideCity = false;
							currentDiocese = this.dioStateHash
									.getCurrentDiocese();
							System.out.println("FOUND CURRENT dioceses " + currentDiocese);
						}
						if (tmpLine.toUpperCase().contains(city.toUpperCase())) {
							System.out.println("FOUND dioceses2 " + tmpLine);
							parseChurches = false;
							findCityStart = true;
							findOutsideInsideCity = false;
							currentDiocese = city;
						}
					}		
				} else if (findCityStart) {
					//System.out.println("findCityStart "+tmpLine);
					//CLERGY, PARISHES, MISSIONS AND PAROCHIAL SCHOOLS 
					if (tmpLine.toUpperCase().contains("CLERGY, PARISHES")) {
						findOutsideInsideCity = true;
						findCityStart = false;
						System.out.println("************** HERE");
					}
				
				} else if (findOutsideInsideCity) {

					String token = "";
					String token2 = "";
					//System.out.println("tmpLion = "+tmpLine);
					token = "OUTSIDE THE CIT";
					token2 = "OUTSIDE METROPOLITAN";
					findCity = true;
					//System.out.println("here? "+tmpLine);
					if ((tmpLine.toUpperCase().contains(token))
							|| (tmpLine.toUpperCase().contains(token2))
							|| (tmpLine.toUpperCase()
									.contains("OUTSDDE THE CITY OF "))
							|| (tmpLine.toUpperCase()
									.contains("OUTSIDE THE CZTY OF "))) {
						dioceseFound = true;
						findCity = true;
						findOutsideInsideCity = false;
						System.out.println("FOUND CITY " + tmpLine);
					}
					
				} else if (findCity) {
					// System.out.println("findCity STATE: "+tmpLine);
					if (((tmpLine.indexOf(city + ",") == 0)
							|| (tmpLine.indexOf(city + ",") == 0) || (tmpLine
							.indexOf(city + ".") == 0))
							&& ((tmpLine.contains("Co.") || (tmpLine
									.contains("Co,"))))) {
						System.out.println("FOUND CITY **** " + city);
						System.out.println("FOUND CITY line **** " + tmpLine);
						findCity = false;
						parsePriests = true;
						parseChurches = false;
						cityLine = tmpLine.substring(tmpLine.indexOf("Co")+ 4);
						cityLine = cityLine.substring(0, cityLine.length()-1);
					}
				} else if (parsePriests2) {
					//if (tmpLine.toUpperCase().contains("CATHED")) {
					//	System.out.println("tmpLine = "+tmpLine);
					//	for (int k=0; k<tmpLine.length(); k++) {
					//		System.out.printf("%c %d\n",tmpLine.charAt(k),(int)tmpLine.charAt(k));
					//	}
					//	System.exit(-1);
					//}					
					if (!tmpLine.toUpperCase().contains("OUTSIDE ")) {
							//parishesFound = true;
							parishLine = parishLine + tmpLine;
								// System.out.println ("p="+tmpLine);
							//System.out.println("parishLine = "+parishLine);
							if (intYear < 1960) { // change check for
															// year < 1960
								skipLine = true;							
							} else {
								skipLine = false;
							}

					} else {
						System.out.println("!parse priests " + tmpLine);
						parsePriests = false;
						parseChurches = true;
						dioceseFound = false;
						if (parishLine != "") {
							System.out.println("parish line = " + parishLine);
							// parseParishLine(parishLine);
							parishLine = cityLine + parishLine;
							String parishes[] = parishLine.split("\u2014");
							for (String parishStr : parishes) {
								if ((!parishStr.toUpperCase().startsWith(
										"SCHOOL"))
										&& (parishStr.indexOf("Rev") != -1)) {

									// parish
									String parish = parishStr.substring(0, parishStr.indexOf(","));
									// revs
									String revStr = parishStr.substring(parishStr.indexOf("Rev")+5,
														parishStr.indexOf("Res"));
									// res
									String resStr = parishStr.substring(parishStr.indexOf("Res"+4));
								
									System.out.println("parish = " + parishStr);
									out.write(Integer.toString(intYear) + "\t"
											+ currentDiocese + "\t" + parish + "\t" +
											revStr + "\t" + resStr
											+ "\n");

								}
							}
						}

					}
					
				
				} else if (parsePriests) {

					parseChurches = false;
					parsePriests = true;

					if (!tmpLine.contains(" Co., ")
							&& !tmpLine.contains(" Co.. ")) {
						if (!skipLine) {
							parishesFound = true;
							if (tmpLine.toUpperCase().indexOf(
									currentDiocese.toUpperCase()) != 0) {
								parishLine = parishLine + tmpLine;
								// System.out.println ("p="+tmpLine);
							} else if (intYear < 1960) { // change check for
															// year < 1960
								skipLine = true;
							}
						} else {
							skipLine = false;
						}

					} else {
						System.out.println("!parse priests " + tmpLine);
						parsePriests = false;
						parseChurches = true;
						dioceseFound = false;
						if (parishLine != "") {
							System.out.println("parish line = " + parishLine);
							// parseParishLine(parishLine);
							parishLine = cityLine + parishLine;
							String parishes[] = parishLine.split("\u2014");
							for (String parishStr : parishes) {
								if ((!parishStr.toUpperCase().startsWith(
										"SCHOOL"))
										&& (parishStr.indexOf("Rev") != -1)) {

									parishStr = parishStr.replace("-", "");
									if (parishStr.contains("Very")) {
										parishStr = parishStr.replaceFirst(
												"Very", "\t");
										parishStr = parishStr.replaceFirst(
												"\t", "\tVery");
									} else if (parishStr.contains("Rt.")) {
										parishStr = parishStr.replaceFirst(
												"Rt.", "\t");
										parishStr = parishStr.replaceFirst(
												"\t", "\tRt.");
									} else {
										parishStr = parishStr.replaceFirst(
												"Rev", "\t");
										parishStr = parishStr.replaceFirst(
												"\t", "\tRev");
									}
									parishStr = parishStr.replaceFirst("Res.,",
											"\t");
									parishStr = parishStr.replaceFirst("Res..",
											"\t");
									if (parishStr.indexOf("[CEM]") != -1) {
										parishStr = parishStr.substring(0,
												parishStr.indexOf("[CEM]") - 1);
									}
									if (parishStr.indexOf("School") != -1) {
										parishStr = parishStr
												.substring(0, parishStr
														.indexOf("School") - 1);
									}
									System.out.println("parish = " + parishStr);
									out.write(Integer.toString(intYear) + "\t"
											+ currentDiocese + "\t" + parishStr
											+ "\n");

								}
							}
						}

					}
				}

			}
			if ((!parsePriests) && (!findCity) && (!parsePriests2)) {
				parseChurches = true;
			} else {
				parseChurches = false;
			}
			// if ((dioceseFound) && (!parishesFound)) {
			// out.write(Integer.toString(intYear)+"\tNot Found\n");
			// }
			if (nextbook) {
				break;
			}
		}

		reader.close();

	}

	private void parseParishLine(String line) {
		// 1st group church name, 2nd group priests, 3rd group Residence,
		// 1st and second group separated by ", Rev" |
		String regEx = "\\\\d?\\d?\\-?([\\w\\(\\)]*)[,\\s*Revs\\.\\s*|Rt\\.Rev\\.Msgr\\.|Rev\\.]\\([\\w\\(\\)]*)([Res\\.,|School]\\([\\w\\(\\)]*))?(School?([\\w\\(\\)]*)\\";
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
	// parser for OCD 1969 Falls River
	public void createDioceseTable(String pdf, String txt) throws IOException {
		PdfReader reader = new PdfReader(pdf);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		PrintWriter out = new PrintWriter(new FileOutputStream(txt));
		TextExtractionStrategy strategy;
		String dateStr;
		String firstName;
		String lastName;
		String ordination;
		String title;
		String parish = "";
		String city;
		String dioceseName = "";
		boolean readline = false;
		int writeLine = 0;
		boolean write = true;
		boolean writeBishop = false;
		boolean writeTitle = false;
		String[] parsedLine;
		boolean concat = false;
		boolean parseAbbrev = false;
		boolean parseInstitutions = false;
		boolean done = false;
		boolean concatTitle = false;
		String titlePrefix = "";
		boolean formerbishops = false;
		boolean searchArch = false;
		int state = 0;
		out.write("Diocese\tState\tAbbreviation\n");
		boolean findDiocese = true;
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			// state = 0; ?
			strategy = parser.processContent(i,
					new SimpleTextExtractionStrategy());
			String tmp = strategy.getResultantText();
			// System.out.println(tmp);
			String[] lines = tmp.split(System.getProperty("line.separator"));
			String name = "";

			String bishopName = "";
			title = "";
			int count = 0;
			int found = -1;
			int foundChurch = -1;
			for (String tmpLine : lines) {

				// if (tmpLine.contains("Rita of Cascia")) {
				// foundChurch = count;
				// }
				// if ((tmpLine.contains("Ray")) ||
				// (tmpLine.contains("Robert Jack-"))) {
				if ((tmpLine.contains("Raymond Wahl"))
						|| (tmpLine.contains("Raymond J. Wahl"))
						|| (tmpLine.contains("Wahl,"))) {
					System.out.printf("book %s page %d line %s\n", pdf, i,
							tmpLine);
					found = count;
					break;
				}
				if (tmpLine.contains("Holdren")) {
					// System.out.printf("book %s page %d line %s\n",pdf,i,tmpLine);
					found = count;
					break;
				}
				++count;
				// if (i == 439) System.out.println("tempLine = "+tmpLine);
				if (searchArch) {
					if (tmpLine.contains("ARCHDIOCESE")) {
						// 1—CATHEDRAL OF ST. MARY OF THE ASSUMPTION,
						// Rt RPV. Robert L. Stanton. Rector; Revs.
						// Paul F. McCarrick: William G. Campbell;
						// Edward J. Mitchell. S.T.L., J.C.D.
						// Res..
						concat = false;

						parseAbbrev = true;
					}

				}
				searchArch = false;
				if (tmpLine.contains("AN ALPHABETICAL LIST OF")) {
					// 1—CATHEDRAL OF ST. MARY OF THE ASSUMPTION,
					// Rt RPV. Robert L. Stanton. Rector; Revs.
					// Paul F. McCarrick: William G. Campbell;
					// Edward J. Mitchell. S.T.L., J.C.D.
					// Res..
					concat = false;
					parseAbbrev = false;
					findDiocese = false;
				}
				if (parseAbbrev & findDiocese) {
					// System.out.println("abbrev line = "+tmpLine);
					String parsedDioLine = tmpLine.replace(" (", "\t(");
					out.write(parsedDioLine + "\n");
				}

				if (tmpLine.contains("Abbreviations")) {
					// 1—CATHEDRAL OF ST. MARY OF THE ASSUMPTION,
					// Rt RPV. Robert L. Stanton. Rector; Revs.
					// Paul F. McCarrick: William G. Campbell;
					// Edward J. Mitchell. S.T.L., J.C.D.
					// Res..
					concat = false;

					searchArch = true;
				}
			} // for lines
			if (found != -1) {
				count = 0;
				System.out.println("\n***found:");
				for (String tmpLine : lines) {
					if ((count > found - 5) && (count <= found + 5)) {
						System.out.printf("book %s page %d line %s\n", pdf, i,
								tmpLine);
					}

					++count;
				}
			}
			if (foundChurch != -1) {
				count = 0;
				System.out.println("\n***found CHURCH:");
				for (String tmpLine : lines) {
					if ((count > foundChurch - 6) && (count <= foundChurch + 3)) {
						System.out.printf("book %s page %d line %s\n", pdf, i,
								tmpLine);
					}

					++count;
				}
			}

		}
		out.flush();
		out.close();
		System.out.println("OUTPUT file name = " + txt);
		reader.close();

	}
}