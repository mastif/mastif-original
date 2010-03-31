package org.mitre.i2b2;

import edu.mayo.bmi.xml.Annot;

/**
 * Created by IntelliJ IDEA.
 * User: MCOARR
 * Date: Mar 31, 2010
 * Time: 9:20:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationTypeChecker {
    public static boolean isToken(Annot annot) {
    //		if (annot.localName.equals("edu.mayo.bmi.uima.common.types.WordTokenAnnotation")
    //		|| annot.localName.equals("edu.mayo.bmi.uima.common.types.PunctTokenAnnotation")
    //		|| annot.localName.equals("edu.mayo.bmi.uima.common.types.ContractionTokenAnnotation")
    //		|| annot.localName.equals("edu.mayo.bmi.uima.common.types.NumTokenAnnotation")
    //		|| annot.localName.equals("edu.mayo.bmi.uima.common.types.NewlineTokenAnnotation")) {
        if (annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.WordToken")
                || annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.PunctuationToken")
                || annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.ContractionToken")
                || annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.NumToken")
                || annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.NewlineToken")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNonNewlineToken(Annot annot) {
//		if (annot.localName.equals("edu.mayo.bmi.uima.common.types.WordTokenAnnotation")
//				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.PunctTokenAnnotation")
//				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.ContractionTokenAnnotation")
//				|| annot.localName.equals("edu.mayo.bmi.uima.common.types.NumTokenAnnotation")) {
        if (annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.WordToken")
                || annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.PunctuationToken")
                || annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.ContractionToken")
                || annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.NumToken")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNewlineToken(Annot annot) {
        //if (annot.localName.equals("edu.mayo.bmi.uima.common.types.NewlineTokenAnnotation")) {
        if (annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.NewlineToken")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPunctuationToken(Annot annot) {
        if (annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.PunctuationToken")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMayoNamedEntity(Annot annot) {
        //if (annot.localName.equals("edu.mayo.bmi.uima.common.types.NamedEntityAnnotation")) {
        if (annot.localName.equals("edu.mayo.bmi.uima.core.ae.type.NamedEntity")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCtakesNegation(Annot annot) {
        //if (annot.localName.equals("edu.mayo.bmi.uima.common.types.NamedEntityAnnotation")) {
        if (annot.localName.equals("gov.va.maveric.uima.ctakes.NENegation")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSent(Annot annot) {
        //if (annot.localName.equals("edu.mayo.bmi.uima.common.types.SentenceAnnotation")) {
        if (annot.localName.equals("edu.mayo.bmi.uima.core.sentence.type.Sentence")) {

            return true;
        } else {
            return false;
        }
    }

    public static boolean isChunk(Annot annot) {
        //if (annot.localName.equals("edu.mayo.bmi.uima.common.types.SentenceAnnotation")) {
        if (annot.localName.startsWith("edu.mayo.bmi.uima.chunker")) {

            return true;
        } else {
            return false;
        }
    }
}
