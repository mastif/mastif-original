/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.xml;

import java.net.URL;

/**
 *
 * @author MCOARR
 */
public class MedFactsXml {

    public void execute()
    {
        String filename = "rules_sample_09.xml";
        URL resourceUrl = getClass().getResource(filename);
        System.out.format("file url: %s%n", resourceUrl.toString());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        MedFactsXml medFactsXml = new MedFactsXml();
        medFactsXml.execute();
    }

}
