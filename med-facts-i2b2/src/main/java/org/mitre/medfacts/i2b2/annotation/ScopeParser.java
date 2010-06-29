/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.annotation;

import java.util.List;
import org.mitre.itc.jcarafe.scopetagger.FullDecoder;
import org.mitre.itc.jcarafe.scopetagger.IndexedAnnot;

/**
 *
 * @author WELLNER
 */
public class ScopeParser {
    FullDecoder decoder = null;
    public ScopeParser(String sModel, String cModel) {
        decoder = FullDecoder.apply(sModel, cModel);

    }

    public List<ScopeOrCueAnnotation> decodeDocument(String [][] toks) {
        List<IndexedAnnot> annots1 = decoder.decodeDocument(toks);
        List<ScopeOrCueAnnotation> annots = null;
        for (IndexedAnnot a : annots1) {
            org.mitre.itc.jcarafe.crf.Annotation an = a.a();
            String typ = an.typ().labelHead();
            if (typ.equals("scope")) {
                ScopeAnnotation s = new ScopeAnnotation();
                s.setScopeId(a.i());
                annots.add(s);
            } else {
                CueAnnotation c = new CueAnnotation();
                String st = an.typ().assoc("type");
                if (st.equals("negation"))
                    c.setCueSubType(CueSubType.NEGATION);
                else
                    c.setCueSubType(CueSubType.SPECULATION);
                c.setScopeIdReference(a.i());
                annots.add(c);
            }
        }
        return annots;
    }
}
