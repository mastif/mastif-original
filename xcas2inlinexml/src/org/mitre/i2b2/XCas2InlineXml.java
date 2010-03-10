/*
 * Copyright (c) 2008 The MITRE Corporation
 *
 * See the full RIGHTS STATEMENT at end of this file.
 *
 * Author: David Day
 * Organization: The MITRE Corporation
 * Project: i2b2 challenge effort
 * Date: May, 2008
 */

package org.mitre.i2b2;

import java.io.*;
import java.util.*;
import edu.mayo.bmi.xml.*;

import junit.framework.*;
import com.tecnick.htmlutils.htmlentities.HTMLEntities;

/*
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
*/

public class XCas2InlineXml {

	public static String xCasFilename = null;
	public static String inlineXmlFilename = null;
	public static Annot sentHead = new Annot();
	public static Annot neHead = new Annot();
	public static Annot wordHead = new Annot();
	public static int xCasNECount = 0;
	public static int xCasUnkCount = 0;
	public static int xCasWordCount = 0;
	public static int xCasNewlineCount = 0;
	public static int NEOpenCount = 0;
	public static int NECloseCount = 0;
	public static int lexCount = 0;
	public static int sentCount = 0;
	public static int newlineCount = 0;
	public static int MAX_EMBEDDING_DEPTH = 100;
	public static Annot[] annotStackArray = new Annot[MAX_EMBEDDING_DEPTH];
	public static Annot annotRoot = new Annot();

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		grabCommandLineArgs(args);
		File f = new File(xCasFilename);
		System.out.println("Looking for data in file '" + f.getAbsolutePath() + "'");
		System.out.println("");
		UimaXmlRepresentation uxr;
		try {
			uxr = new UimaXmlRepresentation(f);
			uxr.processXml();
			ArrayList annotList = uxr.annotList;
			System.out.println("Annotations: " + annotList.size());

			createThreeLayerModel(annotList);
			// printThreeLayerModel();
			emitInlineXml();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void emitInlineXml() throws IOException {

		try {
			// Output inline XML
			OutputStream xmlOutStream = new FileOutputStream(inlineXmlFilename);
			OutputStreamWriter xmlWriter = new OutputStreamWriter(xmlOutStream, "UTF-8");
			BufferedWriter xmlBufWriter = new BufferedWriter(xmlWriter);

			xmlBufWriter.write("<DOC>\n<TEXT>\n");
			Annot currSent = sentHead.next;
			Annot currNE = neHead.next;
			Annot currWord = wordHead.next;
			Annot prevWord = null;

			// number of (and pointer to) MNE annotations on annotStackArray stack
			// Note that counting is 1-based, not zero-based. stack is empty when
			// pointer = 0;
			int stackPtr = 0;

			while (currSent != null) {
				sentCount++;
				xmlBufWriter.write("<s>");
				while (currWord != null && currWord.end <= currSent.end) {
					if ((prevWord == null) || (prevWord.end == currWord.start)) {
						// Don't bother emitting any whitespace between words, or at
						// the beginning of a sentence (prevWord == null).
					} else {
						xmlBufWriter.write(" ");
					}

					// Emit MNE element open tag
					if ((currNE != null) && (currNE.start <= currWord.start) && (currNE.openTagEmittedP == false)) {
						NEOpenCount++;
						stackPtr++;
						annotStackArray[stackPtr] = currNE;
						currNE.openTagEmittedP = true;
						xmlBufWriter.write("<MNE type=\"" + currNE.type + "\" status=\"" + currNE.status + "\" neg=\"" + currNE.neg
								+ "\" umls=\"" + currNE.umlsObjsString + "\">");
						currNE = currNE.next;
						while ((currNE != null) && (currNE.start <= currWord.start) && (currNE.openTagEmittedP == false)) {
							NEOpenCount++;
							stackPtr++;
							annotStackArray[stackPtr] = currNE;
							currNE.openTagEmittedP = true;
							xmlBufWriter.write("<MNE type=\"" + currNE.type + "\" status=\"" + currNE.status + "\" neg=\""
									+ currNE.neg + "\" umls=\"" + currNE.umlsObjsString + "\">");
							currNE = currNE.next;
						}
					}

					// Emit Word element
					if (newlineTokenAnnotP(currWord)) {
						// Don't bother writing out newlines; we force newlines for every sentence boundary.
						newlineCount++;
					} else {
						lexCount++;
						// Properly escape as HTML entities any angle brackets that appear in raw text.
						String htmlAngleBracketed = HTMLEntities.htmlAngleBrackets(currWord.text);
						xmlBufWriter.write("<lex pos=\"" + currWord.pennTag + "\">" + htmlAngleBracketed + "</lex>");
					}

					prevWord = currWord;
					currWord = currWord.next;

					// Emit NE element close tag
					if (stackPtr > 0) {
						Annot neOnStack = annotStackArray[stackPtr];

						//                         No more words          MNE currently on top of MNE stack
						//     Something on        after the one just     ends prior to the next word
						//        stack?           emitted                about to be emitted
						while ((stackPtr > 0) && ((currWord == null) || (neOnStack.end <= currWord.start))) {
							NECloseCount++;
							xmlBufWriter.write("</MNE>");
							stackPtr--;
							if (stackPtr > 0) {
								neOnStack = annotStackArray[stackPtr];
							}
						}
					}
				}
				xmlBufWriter.write("</s>\n");
				currSent = currSent.next;
				if (currSent != null) {
				}
			}

			xmlBufWriter.write("</TEXT>\n</DOC>\n");

			xmlBufWriter.flush();
			xmlBufWriter.close();

			System.out.println(xCasWordCount + " xCas word annotations (" + xCasNewlineCount + " newline annots).");
			System.out.println(sentCount + " xCas sentences.");
			System.out.println(xCasNECount + " xCas Mayo NE annotations.");

			System.out.println(lexCount + " inline lex tokens.");
			System.out.println(sentCount + " inline sentences (" + newlineCount + " newlines skipped).");
			if (NEOpenCount != NECloseCount) {
				System.err.println("Error in generating inline xml MNE tags (open=" + NEOpenCount + " close=" + NECloseCount
						+ ")");
			}
			System.out.println(NEOpenCount + " inline Mayo NE (MNE) tags.");
			if (xCasNECount != NEOpenCount) {
				int droppedCount = xCasNECount - NEOpenCount;
				System.err.println(droppedCount + " Mayo NE annotations have been lost in generating inline xml MNE tags.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printThreeLayerModel() {

		Annot currAnnot = sentHead.next;
		while (currAnnot != null) {
			System.out.println("sent(" + currAnnot.id + "): " + currAnnot.text + " start=" + currAnnot.start + " end="
					+ currAnnot.end);
			currAnnot = currAnnot.next;
		}
		currAnnot = wordHead.next;
		while (currAnnot != null) {
			System.out.println("word(" + currAnnot.id + "): " + currAnnot.text + " start=" + currAnnot.start + " end="
					+ currAnnot.end);
			currAnnot = currAnnot.next;
		}
		currAnnot = neHead.next;
		while (currAnnot != null) {
			System.out.println("ne(" + currAnnot.id + "): " + currAnnot.text + " start=" + currAnnot.start + " end="
					+ currAnnot.end);
			currAnnot = currAnnot.next;
		}
	}

	public static void createThreeLayerModel(ArrayList annotList) {
		sentHead.start = -1;
		neHead.start = -1;
		wordHead.start = -1;
		Iterator annotIter = annotList.iterator();
		while (annotIter.hasNext()) {
			Annot annot = (Annot) annotIter.next();
			// System.out.println("localName=" + annot.localName);
			if (sentAnnotP(annot)) {
				// System.out.println("Adding sent...");
				addToList(sentHead, annot);
				annot.nodeType = "sent";
			} else if (tokenAnnotP(annot)) {
				xCasWordCount++;
				if (newlineTokenAnnotP(annot)) {
					xCasNewlineCount++;
				} else {
					annot.nodeType = "word";
				}
				// System.out.println("Adding word...");
				addToList(wordHead, annot);
			} else if (mayoNamedEntityAnnotP(annot)) {
				xCasNECount++;
				// System.out.println("Adding named entity...");
				addToList(neHead, annot);
				countList(neHead);
				annot.nodeType = "mne";
			} else {
				xCasUnkCount++;
				// System.out.println("Unrecognized annotation type" + annot.type);
			}
		}
	}

	public static boolean tokenAnnotP(Annot annot) {
		if (annot.localName.equals("edu.mayo.bmi.uima.common.types.WordTokenAnnotation")
				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.PunctTokenAnnotation")
				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.ContractionTokenAnnotation")
				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.NumTokenAnnotation")
				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.NewlineTokenAnnotation")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean nonNewlineTokenAnnotP(Annot annot) {
		if (annot.localName.equals("edu.mayo.bmi.uima.common.types.WordTokenAnnotation")
				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.PunctTokenAnnotation")
				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.ContractionTokenAnnotation")
				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.NumTokenAnnotation")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean newlineTokenAnnotP(Annot annot) {
		if (annot.localName.equals("edu.mayo.bmi.uima.common.types.NewlineTokenAnnotation")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean mayoNamedEntityAnnotP(Annot annot) {
		if (annot.localName.equals("edu.mayo.bmi.uima.common.types.NamedEntityAnnotation")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean sentAnnotP(Annot annot) {
		if (annot.localName.equals("edu.mayo.bmi.uima.common.types.SentenceAnnotation")) {
			return true;
		} else {
			return false;
		}
	}

	public static void countList(Annot annot) {
		int count = 0;
		while (annot != null) {
			count++;
			// System.out.println("annot offsets: " + annot.start + " " + annot.end);
			annot = annot.next;
		}
		count--;
		// System.out.println(count + " annots in list.");
	}

	public static void addToList(Annot head, Annot newAnnot) {
		Annot hold = null;
		Annot curr = head;
		Annot prev = null;

		while (curr != null) {
			if (newAnnot.start < curr.start || ((newAnnot.start == curr.start) && (newAnnot.end > curr.end))) {
				hold = curr;
				curr = newAnnot;
				prev.next = curr;
				newAnnot.prev = prev;
				newAnnot.next = hold;
				hold.prev = curr;
				return;
			}
			prev = curr;
			curr = curr.next;
		}
		prev.next = newAnnot;
		newAnnot.prev = prev;
	}

	// Command line processing
	public static void grabCommandLineArgs(String[] args) {
		if (args.length == 0) {
			emitHelp();
			System.exit(0);
		} else {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("-i")) {
					if (args.length > i + 1) {
						xCasFilename = args[i + 1];
						File xCasFile = new File(xCasFilename);
						if (!xCasFile.exists()) {
							System.err.println(xCasFilename + " input file does not exist.");
							System.exit(0);
						}
						i++;
					} else {
						System.err.println("No xCas input file provided to the -i command line argument.");
						emitHelp();
						System.exit(0);
					}
				} else if (args[i].equalsIgnoreCase("-o")) {
					if (args.length > i + 1) {
						inlineXmlFilename = args[i + 1];
						i++;
					} else {
						System.err.println("No inlineXml output filename provided to the -o command line argument.");
						emitHelp();
						System.exit(0);
					}
				} else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("-help")) {
					emitHelp();
					System.exit(0);
				} else {
					System.err.println("Unrecognized argument: " + args[i]);
					emitHelp();
					System.exit(0);
				}
			}
		}
	}

	public static void emitHelp() {
		System.out.println("XCas2InlineXml -i <xcasFile input filename>");
		System.out.println("               -o <inline xml output filename>");
		System.out.println("             [ -h (print this message and exit) ]");
	}

}


/*
 * ------------------   RIGHTS STATEMENT   --------------------------
 *
 *                     Copyright (c) 2008
 *                    The MITRE Corporation
 *
 *                     ALL RIGHTS RESERVED
 *
 *
 * The MITRE Corporation (MITRE) provides this software to you without
 * charge to use for your internal purposes only. Any copy you make for
 * such purposes is authorized provided you reproduce MITRE's copyright
 * designation and this License in any such copy. You may not give or
 * sell this software to any other party without the prior written
 * permission of the MITRE Corporation.
 *
 * The government of the United States of America may make unrestricted
 * use of this software.
 *
 * This software is the copyright work of MITRE. No ownership or other
 * proprietary interest in this software is granted you other than what
 * is granted in this license.
 *
 * Any modification or enhancement of this software must inherit this
 * license, including its warranty disclaimers. You hereby agree to
 * provide to MITRE, at no charge, a copy of any such modification or
 * enhancement without limitation.
 *
 * MITRE IS PROVIDING THE PRODUCT "AS IS" AND MAKES NO WARRANTY, EXPRESS
 * OR IMPLIED, AS TO THE ACCURACY, CAPABILITY, EFFICIENCY,
 * MERCHANTABILITY, OR FUNCTIONING OF THIS SOFTWARE AND DOCUMENTATION. IN
 * NO EVENT WILL MITRE BE LIABLE FOR ANY GENERAL, CONSEQUENTIAL,
 * INDIRECT, INCIDENTAL, EXEMPLARY OR SPECIAL DAMAGES, EVEN IF MITRE HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You accept this software on the condition that you indemnify and hold
 * harmless MITRE, its Board of Trustees, officers, agents, and
 * employees, from any and all liability or damages to third parties,
 * including attorneys' fees, court costs, and other related costs and
 * expenses, arising out of your use of this software irrespective of the
 * cause of said liability.
 *
 * The export from the United States or the subsequent reexport of this
 * software is subject to compliance with United States export control
 * and munitions control restrictions. You agree that in the event you
 * seek to export this software you assume full responsibility for
 * obtaining all necessary export licenses and approvals and for assuring
 * compliance with applicable reexport restrictions.
 */