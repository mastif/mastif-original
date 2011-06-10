/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.annotation;

import java.util.List;
import java.util.ArrayList;
import org.mitre.medfacts.i2b2.util.Location;
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
        List<ScopeOrCueAnnotation> annots = new ArrayList<ScopeOrCueAnnotation>();
        for (IndexedAnnot a : annots1) {
            org.mitre.itc.jcarafe.util.Annotation an = a.a();
            String typ = an.typ().labelHead();
            int lineNum = Integer.parseInt(an.vl().get());
            if (typ.equals("xcope")) {
                ScopeAnnotation s = new ScopeAnnotation();
                s.setScopeId(a.i());
                s.setBegin(new Location(lineNum,an.st()));
                s.setEnd(new Location(lineNum,an.en()));
                annots.add(s);
            } else if (typ.equals("cue")) {
                CueAnnotation c = new CueAnnotation();
                String st = an.typ().assoc("type");
                if (st.equals("negation"))
                    c.setCueSubType(CueSubType.NEGATION);
                else
                    c.setCueSubType(CueSubType.SPECULATION);
                c.setBegin(new Location(lineNum,an.st()));
                c.setEnd(new Location(lineNum,an.en()));
                c.setScopeIdReference(a.i());
                annots.add(c);
            }
        }
        return annots;
    }
}
