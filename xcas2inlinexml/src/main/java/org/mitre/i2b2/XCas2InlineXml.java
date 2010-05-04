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

//import junit.framework.*;
//import com.tecnick.htmlutils.htmlentities.HTMLEntities;

/*
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
*/

public class XCas2InlineXml {

    private TagManager neTagManager;
    private TagManager ctakesNegationTagManager;
    private TagManager negationContextTagManager;
    private TagManager chunkTagManager;
    private TagManager cueTagManager;
    private TagManager xcopeTagManager;

    OutputStream xmlOutStream;
    OutputStreamWriter xmlWriter;
    BufferedWriter xmlBufWriter;

    GenericTagBuilder chunkTagBuilder;
    GenericTagBuilder negationTagBuilder;
    GenericTagBuilder negationContextTagBuilder;
    GenericTagBuilder namedEntityTagBuilder;
    GenericTagBuilder cueTagBuilder;
    GenericTagBuilder xcopeTagBuilder;

	public String xCasFilename = null;
	public String inlineXmlFilename = null;

	public static Annot sentHead = new Annot();
	public static Annot neHead = new Annot();
	public static Annot negationHead = new Annot();
	public static Annot wordHead = new Annot();
	public static Annot chunkHead = new Annot();
	public static int xCasNECount = 0;
	public static int xCasNegationCount = 0;
    public static int xCasNegationContextCount = 0;
	public static int xCasChunkCount = 0;
	public static int xCasUnkCount = 0;
	public static int xCasWordCount = 0;
	public static int xCasNewlineCount = 0;
    public static int xCasPunctuationCount = 0;
	public static int neOpenCount = 0;
	public static int neCloseCount = 0;
    public static int negationOpenCount = 0;
    public static int negationCloseCount = 0;
	public static int negationContextOpenCount = 0;
	public static int negationContextCloseCount = 0;
	public static int chunkOpenCount = 0;
	public static int chunkCloseCount = 0;
	public static int xcopeOpenCount = 0;
	public static int xcopeCloseCount = 0;
	public static int cueOpenCount = 0;
	public static int cueCloseCount = 0;
	public static int lexCount = 0;
	public static int sentCount = 0;
	public static int newlineCount = 0;
    public static int punctuationCount = 0;
	public static int MAX_EMBEDDING_DEPTH = 100;
	public static Annot[] annotStackArray = new Annot[MAX_EMBEDDING_DEPTH];
	public static Annot annotRoot = new Annot();

    public XCas2InlineXml()
    {
        neTagManager = new TagManager();
        neTagManager.setAnnotationTypeString("ne");

        ctakesNegationTagManager = new TagManager();
        ctakesNegationTagManager.setAnnotationTypeString("negation");

        negationContextTagManager = new TagManager();
        negationContextTagManager.setAnnotationTypeString("negationContext");

        chunkTagManager = new TagManager();
        chunkTagManager.setAnnotationTypeString("chunk");
        
        cueTagManager = new TagManager();
        cueTagManager.setAnnotationTypeString("cue");
        
        xcopeTagManager = new TagManager();
        xcopeTagManager.setAnnotationTypeString("xcope");
        
    }

    public void initializeOutputFile()
    {

        try
        {
            xmlOutStream = new FileOutputStream(inlineXmlFilename);
            xmlWriter = new OutputStreamWriter(xmlOutStream, "UTF-8");
            xmlBufWriter = new BufferedWriter(xmlWriter);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Problem initializing output file for inline xml", e);
        }

        chunkTagBuilder = new ChunkTagBuilder(xmlBufWriter, "chunk");
        negationTagBuilder = new NegationTagBuilder(xmlBufWriter, "negation");
        negationContextTagBuilder = new NegationContextTagBuilder(xmlBufWriter, "negationContext");
        namedEntityTagBuilder = new NamedEntityTagBuilder(xmlBufWriter, "MNE");
        cueTagBuilder = new CueTagBuilder(xmlBufWriter,"cue");
        xcopeTagBuilder = new XcopeTagBuilder(xmlBufWriter,"xcope");
        
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

        XCas2InlineXml xCas2InlineXml = new XCas2InlineXml();
        xCas2InlineXml.grabCommandLineArgs(args);
        xCas2InlineXml.initializeOutputFile();
        xCas2InlineXml.execute();
    }

    public void execute()
    {
		File f = new File(xCasFilename);
		System.out.println("Looking for data in file '" + f.getAbsolutePath() + "'");
		System.out.println("");
		UimaXmlRepresentation uxr;
		try {
			uxr = new UimaXmlRepresentation(f);
			System.out.format("===%nuxr.processXml() BEFORE%n===%n");
			uxr.processXml();
			System.out.format("===%nuxr.processXml() AFTER%n===%n");
			ArrayList annotList = uxr.annotList;
			System.out.println("Annotations: " + annotList.size());

			createThreeLayerModel(annotList);
            System.out.format("===%nprintThreeLayerModel(): BEFORE%n===%n");
			printThreeLayerModel();
            System.out.format("===%nprintThreeLayerModel(): AFTER%n===%n");
            System.out.format("===%nemitInlinexml(): BEFORE%n===%n");
			emitInlineXml();
            System.out.format("===%nemitInlinexml(): AFTER%n===%n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public  void emitInlineXml() throws IOException {

		try {
			// Output inline XML
//			OutputStream xmlOutStream = new FileOutputStream(inlineXmlFilename);
//			OutputStreamWriter xmlWriter = new OutputStreamWriter(xmlOutStream, "UTF-8");
//			BufferedWriter xmlBufWriter = new BufferedWriter(xmlWriter);

			xmlBufWriter.write("<DOC>\n<TEXT>\n");
			Annot currSent = sentHead.next;
//			Annot currNegation = negationHead.next;
//			Annot currNE = neHead.next;
//			Annot currChunk = chunkHead.next;
            Annot currNegation = ctakesNegationTagManager.getHead().next;
            Annot currNegationContext = negationContextTagManager.getHead().next;
            Annot currNE = neTagManager.getHead().next;
            Annot currChunk = chunkTagManager.getHead().next;
            Annot currCue = cueTagManager.getHead().next;
            Annot currXcope = xcopeTagManager.getHead().next;

			Annot currWord = wordHead.next;
			Annot prevWord = null;

            GenericTagBuilder chunkTagBuilder = new ChunkTagBuilder(xmlBufWriter, "chunk");
            GenericTagBuilder negationTagBuilder = new NegationTagBuilder(xmlBufWriter, "negation");
            GenericTagBuilder namedEntityTagBuilder = new NamedEntityTagBuilder(xmlBufWriter, "MNE");
            
            GenericTagBuilder cueTagBuilder = new CueTagBuilder(xmlBufWriter,"cue");
            GenericTagBuilder xcopeTagBuilder = new XcopeTagBuilder(xmlBufWriter,"xcope");


			// number of (and pointer to) MNE annotations on annotStackArray stack
			// Note that counting is 1-based, not zero-based. stack is empty when
			// pointer = 0;
			int stackPtr = 0;

            System.out.format("while currSent is not null -- outside before%n");
			while (currSent != null) {
                System.out.format("while currSent is not null -- inside begin%n");
				System.out.println("INSIDE WHILE BEGIN currWord: " + currWord);
				System.out.println("INSIDE WHILE BEGIN currWord.text: " + currWord.text);
				sentCount++;
				xmlBufWriter.write("<s>");
				while (currWord != null && currWord.end <= currSent.end) {
					if ((prevWord == null) || (prevWord.end == currWord.start)) {
						// Don't bother emitting any whitespace between words, or at
						// the beginning of a sentence (prevWord == null).
					} else {
						xmlBufWriter.write(" ");
					}

					System.out.format("    stackPtr before xcope: %d%n", stackPtr);
                    xcopeTagBuilder.setParameters(currXcope, currWord, stackPtr, xcopeOpenCount);
                    xcopeTagBuilder.invoke();
                    stackPtr = xcopeTagBuilder.getStackPtr();
                    xcopeOpenCount = xcopeTagBuilder.getAnnotationTagOpenCount();
                    currXcope = xcopeTagBuilder.getCurrAnnotation();
                    System.out.format("    stackPtr after xcope: %d%n", stackPtr);
                    
					// Emit chunk element open tag
					// BRW - modified 3/11/10
					// Moved chunks before NEs as when they overlap perfectly we'd like chunks appearing on the "outside"
					// e.g. <chunk><mne>pain</mne></chunk>
                    System.out.format("    stackPtr before chunk: %d%n", stackPtr);
                    chunkTagBuilder.setParameters(currChunk, currWord, stackPtr, chunkOpenCount);
                    chunkTagBuilder.invoke();
                    stackPtr = chunkTagBuilder.getStackPtr();
                    chunkOpenCount = chunkTagBuilder.getAnnotationTagOpenCount();
                    currChunk = chunkTagBuilder.getCurrAnnotation();
                    System.out.format("    stackPtr after chunk: %d%n", stackPtr);

					// Emit negation element open tag
                    System.out.format("    stackPtr before negation: %d%n", stackPtr);
                    negationTagBuilder.setParameters(currNegation, currWord, stackPtr, negationOpenCount);
                    negationTagBuilder.invoke();
                    stackPtr = negationTagBuilder.getStackPtr();
                    negationOpenCount = negationTagBuilder.getAnnotationTagOpenCount();
                    currNegation = negationTagBuilder.getCurrAnnotation();
                    System.out.format("    stackPtr after negation: %d%n", stackPtr);

                    // Emit negationContext element open tag
                    System.out.format("    stackPtr before negationContext: %d%n", stackPtr);
                    negationContextTagBuilder.setParameters(currNegationContext, currWord, stackPtr, negationContextOpenCount);
                    negationContextTagBuilder.invoke();
                    stackPtr = negationContextTagBuilder.getStackPtr();
                    negationContextOpenCount = negationContextTagBuilder.getAnnotationTagOpenCount();
                    currNegationContext = negationContextTagBuilder.getCurrAnnotation();
                    System.out.format("    stackPtr after negationContext: %d%n", stackPtr);


                    // Emit MNE element open tag
                    System.out.format("    stackPtr before ne: %d%n", stackPtr);
                    namedEntityTagBuilder.setParameters(currNE, currWord, stackPtr, neOpenCount);
                    namedEntityTagBuilder.invoke();
                    stackPtr = namedEntityTagBuilder.getStackPtr();
                    neOpenCount = namedEntityTagBuilder.getAnnotationTagOpenCount();
                    currNE = namedEntityTagBuilder.getCurrAnnotation();
                    System.out.format("    stackPtr after ne: %d%n", stackPtr);

                    // Emit Cue open tag
                    System.out.format("    stackPtr before cue: %d%n", stackPtr);
                    cueTagBuilder.setParameters(currCue, currWord, stackPtr, cueOpenCount);
                    cueTagBuilder.invoke();
                    stackPtr = cueTagBuilder.getStackPtr();
                    cueOpenCount = cueTagBuilder.getAnnotationTagOpenCount();
                    currCue = cueTagBuilder.getCurrAnnotation();
                    System.out.format("    stackPtr after ne: %d%n", stackPtr);

//                    GenericTagBuilder punctuationTagBuilder = new PunctuationTagBuilder(xmlBufWriter, currChunk, currWord, stackPtr, "punctuation", punctuationOpenCount);
//                    chunkTagBuilder.invoke();
//                    stackPtr = chunkTagBuilder.getStackPtr();
//                    punctuationOpenCount = punctuationTagBuilder.getAnnotationTagOpenCount();


					// Emit Word element
					if (AnnotationTypeChecker.isNewlineToken(currWord)) {
						// Don't bother writing out newlines; we force newlines for every sentence boundary.
						newlineCount++;
					} else {
						lexCount++;
                        String pos = currWord.pennTag;
                        if (AnnotationTypeChecker.isPunctuationToken(currWord))
                        {
                            punctuationCount++;
                            pos = "punctuation";
                        }
						// Properly escape as HTML entities any angle brackets that appear in raw text.
						/*
						System.out.println("HTMLEntities: " + HTMLEntities.class);
						System.out.println("currWord: " + currWord);
						System.out.println("currWord.text: " + currWord.text);
						//String htmlAngleBracketed = HTMLEntities.htmlAngleBrackets(currWord.text);
						String htmlAngleBracketed =
							(currWord == null || currWord.text == null) ? "" : HTMLEntities.htmlAngleBrackets(currWord.text);
						System.out.format("outputting word: \"%s\"%n", currWord.text);
						xmlBufWriter.write("<lex pos=\"" + currWord.pennTag + "\">" + htmlAngleBracketed + "</lex>");
						*/
						xmlBufWriter.write("<lex pos=\"" + pos + "\">" + currWord.text + "</lex>");
					}

					prevWord = currWord;
					currWord = currWord.next;

					System.out.format("(checking to close tag...) stackPtr: %d%n", stackPtr);
					if (stackPtr > 0) { 
						System.out.format("annot type = %s\n", annotStackArray[stackPtr].type);
					}
					// BRW - 3/11/10
					// This appears to be the correct logic now.  Need to be able to pop any series of annotation types
					// off the stack.  Previous code only popped off one type at a time and wouldn't handle multiple annotations
					// of different types that ended at the same position - e.g. <chunk>the <mne>pain</mne></chunk>
					Annot currentOnStack = annotStackArray[stackPtr];
                    System.out.format(">> size of annotStackArray: %d%n", annotStackArray.length);
                    System.out.format(">> stackPtr: %d%n", stackPtr);
                    System.out.format(">> currentOnStack: %s%n", currentOnStack);
                    System.out.format(">> currentOnStack.type: %s%n", (currentOnStack == null) ? "(currentOnStack is null)" : currentOnStack.type);
                    System.out.format("    while stackPtr > 0 and (currWord is null or currentOnStack ends before currWord start OUTSIDE BEFORE%n");
					while ((stackPtr > 0) && ((currWord == null) || (currentOnStack.end <= currWord.start))) {
                        System.out.format("    while stackPtr > 0 and (currWord is null or currentOnStack ends before currWord start INSIDE BEGIN%n");
                        String currentOnStackType = currentOnStack.type;
                        if (currentOnStackType == null) currentOnStackType = "";
                        String currentOnStackNodeType = currentOnStack.nodeType;
                        if (currentOnStackNodeType == null) currentOnStackNodeType = "";
						if (currentOnStackType.equals("chunk")) {
                            //chunkTagBuilder.decrementAnnotationTagOpenCount();
                            //chunkTagBuilder.advanceToNextAnnotation();
							chunkCloseCount++;
							xmlBufWriter.write("</chunk>");
						} else if (currentOnStackNodeType.equals("cue")) {
                            //negationTagBuilder.decrementAnnotationTagOpenCount();
                            //negationTagBuilder.advanceToNextAnnotation();
							negationCloseCount++;
							xmlBufWriter.write("</cue>");
						} else if (currentOnStackNodeType.equals("xcope")) {
                            //negationTagBuilder.decrementAnnotationTagOpenCount();
                            //negationTagBuilder.advanceToNextAnnotation();
							negationCloseCount++;
							xmlBufWriter.write("</xcope>");							
						} else if (currentOnStackNodeType.equals("negation")) {
                            //negationTagBuilder.decrementAnnotationTagOpenCount();
                            //negationTagBuilder.advanceToNextAnnotation();
							negationCloseCount++;
							xmlBufWriter.write("</negation>");
                        } else if (currentOnStackNodeType.equals("negationContext")) {
                            //negationTagBuilder.decrementAnnotationTagOpenCount();
                            //negationTagBuilder.advanceToNextAnnotation();
                            negationContextCloseCount++;
                            xmlBufWriter.write("</negationContext>");
						} else if (currentOnStackNodeType.equals("mne"))
						{
							System.out.format("(debug mne type: %s%n", currentOnStack.type);
                            //namedEntityTagBuilder.decrementAnnotationTagOpenCount();
                            //namedEntityTagBuilder.advanceToNextAnnotation();
							neCloseCount++;
							xmlBufWriter.write("</MNE>");
						} else
						{
						}
						stackPtr--;
						currentOnStack = annotStackArray[stackPtr];
                        System.out.format("    while stackPtr > 0 and (currWord is null or currentOnStack ends before currWord start INSIDE END%n");
					}
                    System.out.format("    while stackPtr > 0 and (currWord is null or currentOnStack ends before currWord start OUTSIDE AFTER%n");
				}
				xmlBufWriter.write("</s>\n");
				currSent = currSent.next;
				if (currSent != null) {
				}
                System.out.format("while currSent is not null -- inside end%n");
			}
            System.out.format("while currSent is not null -- outside after%n");

			xmlBufWriter.write("</TEXT>\n</DOC>\n");

			xmlBufWriter.flush();
			xmlBufWriter.close();

            chunkOpenCount = chunkTagBuilder.getAnnotationTagOpenCount();
            negationOpenCount = negationTagBuilder.getAnnotationTagOpenCount();
            neOpenCount = namedEntityTagBuilder.getAnnotationTagOpenCount();

			System.out.println(xCasWordCount + " xCas word annotations (" + xCasNewlineCount + " newline annots; " + xCasPunctuationCount + " puctuation annots).");
			System.out.println(sentCount + " xCas sentences.");
			System.out.println(xCasNECount + " xCas Mayo NE annotations.");

			System.out.println(lexCount + " inline lex tokens.");
			System.out.println(sentCount + " inline sentences (" + newlineCount + " newlines skipped; " + punctuationCount + " punctuation count).");
			if (neOpenCount != neCloseCount) {
				System.err.println("Error in generating inline xml MNE tags (open=" + neOpenCount + " close=" + neCloseCount
						+ ")");
			}
			System.out.println(neOpenCount + " inline Mayo NE (MNE) tags.");
			if (xCasNECount != neOpenCount) {
				int droppedCount = xCasNECount - neOpenCount;
				System.err.println(droppedCount + " Mayo NE annotations have been lost in generating inline xml MNE tags.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public  void printThreeLayerModel() {
		System.out.format("===%nprintThreeLayerModel() BEGIN%n===%n");

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
        currAnnot = chunkTagManager.getHead();
		while (currAnnot != null) {
			System.out.println("chunk(" + currAnnot.id + "): " + currAnnot.text + " start=" + currAnnot.start + " end="
					+ currAnnot.end);
			currAnnot = currAnnot.next;
		}
		//currAnnot = neHead.next;
        currAnnot = neTagManager.getHead();
		while (currAnnot != null) {
			System.out.println("ne(" + currAnnot.id + "): " + currAnnot.text + " start=" + currAnnot.start + " end="
					+ currAnnot.end);
			currAnnot = currAnnot.next;
		}
		//currAnnot = negationHead.next;
        currAnnot = ctakesNegationTagManager.getHead();
		while (currAnnot != null) {
			System.out.println("negation(" + currAnnot.id + "): " + currAnnot.text + " start=" + currAnnot.start + " end="
					+ currAnnot.end);
			currAnnot = currAnnot.next;
		}
//        currAnnot = punctuationHead.next;
//        while (currAnnot != null) {
//            System.out.println("punctuation(" + currAnnot.id + "): " + currAnnot.text + " start=" + currAnnot.start + " end="
//                    + currAnnot.end);
//            currAnnot = currAnnot.next;
//        }
		System.out.format("===%nprintThreeLayerModel() END%n===%n");
	}

	public void createThreeLayerModel(ArrayList annotList) {
		System.out.format("===%ncreateThreeLayerModel() BEGIN%n===%n");
		sentHead.start = -1;
		neHead.start = -1;
		negationHead.start = -1;
		wordHead.start = -1;
		chunkHead.start = -1;
		Iterator annotIter = annotList.iterator();
		while (annotIter.hasNext()) {
			Annot annot = (Annot) annotIter.next();
			// System.out.println("localName=" + annot.localName);
			if (AnnotationTypeChecker.isSent(annot)) {
				// System.out.println("Adding sent...");
				addToList(sentHead, annot);
				annot.nodeType = "sent";
			} else if (AnnotationTypeChecker.isCue(annot)) {
				cueTagManager.incrementAnnotationCount();
				annot.nodeType = "cue";
				cueTagManager.addToList(annot);
			} else if (AnnotationTypeChecker.isXcope(annot)) {
				xcopeTagManager.incrementAnnotationCount();
				annot.nodeType = "xcope";
				xcopeTagManager.addToList(annot);				
			} else if (AnnotationTypeChecker.isToken(annot)) {
				xCasWordCount++;
				if (AnnotationTypeChecker.isNewlineToken(annot)) {
					xCasNewlineCount++;
				} else if (AnnotationTypeChecker.isPunctuationToken(annot))
                {
                    xCasPunctuationCount++;
                } else
                {
					annot.nodeType = "word";
				}
				// System.out.println("Adding word...");
				addToList(wordHead, annot);
			} else if (AnnotationTypeChecker.isMayoNamedEntity(annot)) {
                neTagManager.incrementAnnotationCount();
                annot.nodeType = "mne";
                neTagManager.addToList(annot);

//				xCasNECount++;
//				// System.out.println("Adding named entity...");
//				addToList(neHead, annot);
//				countList(neHead);
//				annot.nodeType = "mne";
			} else if (AnnotationTypeChecker.isCtakesNegation(annot)) {
                ctakesNegationTagManager.incrementAnnotationCount();
                annot.nodeType = "negation";
                ctakesNegationTagManager.addToList(annot);

//				xCasNegationCount++;
//				// System.out.println("Adding named entity...");
//				addToList(negationHead, annot);
//				countList(negationHead);
//				annot.nodeType = "negation";
            } else if (AnnotationTypeChecker.isNegationContext(annot)) {
                negationContextTagManager.incrementAnnotationCount();
                annot.nodeType = "negationContext";
                negationContextTagManager.addToList(annot);

//				xCasNegationCount++;
//				// System.out.println("Adding named entity...");
//				addToList(negationHead, annot);
//				countList(negationHead);
//				annot.nodeType = "negation";
			} else if (AnnotationTypeChecker.isChunk(annot)) {
                chunkTagManager.incrementAnnotationCount();
                annot.nodeType = "chunk";
                chunkTagManager.addToList(annot);

//				xCasChunkCount++;
//				// System.out.println("Adding named entity...");
//				addToList(chunkHead, annot);
//				countList(chunkHead);
//				annot.nodeType = "chunk";
			} else {
				xCasUnkCount++;
				// System.out.println("Unrecognized annotation type" + annot.type);
			}
		}

        xCasNECount = neTagManager.getAnnotationCount();
        xCasChunkCount = chunkTagManager.getAnnotationCount();
        xCasNegationCount = ctakesNegationTagManager.getAnnotationCount();

		System.out.format("===%ncreateThreeLayerModel() END%n===%n");
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
	public void grabCommandLineArgs(String[] args) {
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

    private class TagManager
    {
        int annotationCount = 0;
        int openTagCount = 0;
        int closeTagCount = 0;
        Annot head;

        String annotationTypeString = "";

        public TagManager()
        {
            head = new Annot();
            head.start = -1;
        }

        public void addToList(Annot newAnnot)
        {
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

        public void print()
        {
            Annot currAnnot = head.next;
            while (currAnnot != null) {
                System.out.println("ne(" + currAnnot.id + "): " + currAnnot.text + " start=" + currAnnot.start + " end="
                        + currAnnot.end);
                currAnnot = currAnnot.next;
            }

        }

        public Annot getHead()
        {
            return head;
        }

        public void incrementAnnotationCount()
        {
            annotationCount++;
        }

        public void incrementOpenTagCount()
        {
            openTagCount++;
        }

        public void incrementCloseTagCount()
        {
            closeTagCount++;
        }

        public int getAnnotationCount()
        {
            return annotationCount;
        }

        public int getOpenTagCount()
        {
            return openTagCount;
        }

        public int getCloseTagCount()
        {
            return closeTagCount;
        }
        public String getAnnotationTypeString()
        {
            return annotationTypeString;
        }

        public void setAnnotationTypeString(String annotationTypeString)
        {
            this.annotationTypeString = annotationTypeString;
        }

    }

    private abstract class GenericTagBuilder {
        protected BufferedWriter xmlBufWriter;
        protected Annot currAnnotation;
        protected Annot currWord;
        protected int stackPtr;
        protected String tagName;
        protected int annotationTagOpenCount;

        //public GenericTagBuilder(BufferedWriter xmlBufWriter, Annot currAnnotation, Annot currWord, int stackPtr, String tagName, int annotationTagOpenCount) {
        public GenericTagBuilder(BufferedWriter xmlBufWriter, String tagName) {
            this.xmlBufWriter = xmlBufWriter;
            //this.currAnnotation = currAnnotation;
            //this.currWord = currWord;
            //this.stackPtr = stackPtr;
            this.tagName = tagName;
            this.annotationTagOpenCount = annotationTagOpenCount;
        }

        public Annot getCurrAnnotation() {
            return currAnnotation;
        }

        public int getStackPtr() {
            return stackPtr;
        }

        public int getAnnotationTagOpenCount() {
            return annotationTagOpenCount;
        }

        public void decrementAnnotationTagOpenCount()
        {
            annotationTagOpenCount--;
        }

        public void advanceToNextAnnotation()
        {
            currAnnotation = currAnnotation.next;
        }

        public void setParameters(Annot currAnnotation, Annot currWord, int stackPtr, int annotationTagOpenCount)
        {
            this.currAnnotation = currAnnotation;
            this.currWord = currWord;
            this.stackPtr = stackPtr;
            this.annotationTagOpenCount = annotationTagOpenCount;
        }

        public GenericTagBuilder invoke() throws IOException {
            if ((currAnnotation != null) && (currAnnotation.start <= currWord.start) && (currAnnotation.openTagEmittedP == false)) {
                annotationTagOpenCount++;
                stackPtr++;
                annotStackArray[stackPtr] = currAnnotation;
                currAnnotation.openTagEmittedP = true;
                constructString();
                currAnnotation = currAnnotation.next;
                while ((currAnnotation != null) && (currAnnotation.start <= currWord.start) && (currAnnotation.openTagEmittedP == false)) {
                    annotationTagOpenCount++;
                    stackPtr++;
                    annotStackArray[stackPtr] = currAnnotation;
                    currAnnotation.openTagEmittedP = true;
                    constructString();
                    //System.out.format("opening MNE tag B... (%s)%n", currAnnotation.type);
                    //xmlBufWriter.write("<negation type=\"" + currAnnotation.type + "\" status=\"" + currAnnotation.status + "\" neg=\""
                    //        + currAnnotation.neg + "\" umls=\"" + currAnnotation.umlsObjsString + "\">");
                    currAnnotation = currAnnotation.next;
                }
            }
            return this;
        }

        public abstract void constructString() throws IOException;
//        private void constructString() throws IOException {
//            System.out.format("opening %s tag A... (%s)%n", tagName, currAnnotation.type);
//            xmlBufWriter.write("<negation type=\"" + currAnnotation.type + "\" status=\"" + currAnnotation.status + "\" neg=\"" + currAnnotation.neg
//                    + "\" umls=\"" + currAnnotation.umlsObjsString + "\">");
//        }
    }

    public class NegationTagBuilder extends GenericTagBuilder
    {
        public NegationTagBuilder(BufferedWriter xmlBufWriter, String tagName) {
            super(xmlBufWriter, tagName);
        }

        public void constructString() throws IOException {
            System.out.format("opening %s tag A... (%s)%n", tagName, currAnnotation.type);
            xmlBufWriter.write("<" + tagName + ">");
//            xmlBufWriter.write("<" + tagName + " type=\"" + currAnnotation.type + "\" status=\"" + currAnnotation.status + "\" neg=\"" + currAnnotation.neg
//                    + "\" umls=\"" + currAnnotation.umlsObjsString + "\">");
        }
    }

    public class NegationContextTagBuilder extends GenericTagBuilder
    {
        public NegationContextTagBuilder(BufferedWriter xmlBufWriter, String tagName) {
            super(xmlBufWriter, tagName);
        }

        public void constructString() throws IOException {
            System.out.format("opening %s tag A... (%s)%n", tagName, currAnnotation.type);
            xmlBufWriter.write("<" + tagName + " focusText=\"" + currAnnotation.focusText + "\">");
        }
    }

    public class NamedEntityTagBuilder extends GenericTagBuilder
    {
        public NamedEntityTagBuilder(BufferedWriter xmlBufWriter, String tagName) {
            super(xmlBufWriter, tagName);
        }

        public void constructString() throws IOException {
            System.out.format("opening %s tag A... (%s)%n", tagName, currAnnotation.type);
            xmlBufWriter.write("<" + tagName + " type=\"" + currAnnotation.type + "\" status=\"" + currAnnotation.status + "\" neg=\"" + currAnnotation.neg
                    + "\" umls=\"" + currAnnotation.umlsObjsString + "\">");
        }
    }

    public class ChunkTagBuilder extends GenericTagBuilder
    {
        public ChunkTagBuilder(BufferedWriter xmlBufWriter, String tagName) {
            super(xmlBufWriter, tagName);
        }

        public void constructString() throws IOException {
            System.out.format("opening chunk tag A... (%s)%n", currAnnotation.pennTag);
            xmlBufWriter.write("<chunk type=\"" + currAnnotation.pennTag + "\" >");
        }
    }
    
    public class CueTagBuilder extends GenericTagBuilder
    {
        public CueTagBuilder(BufferedWriter xmlBufWriter, String tagName) {
            super(xmlBufWriter, tagName);
        }

        public void constructString() throws IOException {
            System.out.format("opening cue tag A... (%s)%n", currAnnotation.pennTag);
            xmlBufWriter.write("<cue ref=\"" + currAnnotation.ref + "\" type=\"" + currAnnotation.subType + "\" >");
        }
    }

    public class XcopeTagBuilder extends GenericTagBuilder
    {
        public XcopeTagBuilder(BufferedWriter xmlBufWriter, String tagName) {
            super(xmlBufWriter, tagName);
        }

        public void constructString() throws IOException {
            System.out.format("opening xcope tag A... (%s)%n", currAnnotation.pennTag);
            xmlBufWriter.write("<xcope id=\"" + currAnnotation.uid + "\" >");
        }
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