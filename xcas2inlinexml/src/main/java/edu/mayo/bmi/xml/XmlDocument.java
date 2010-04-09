package edu.mayo.bmi.xml;
/*
 * Copyright: (c) 2007-2008   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */

import java.util.List;

/**
 * Basic operations required by a concrete class that reads/parses XML 
 * representation of Annotations.
 * @author M039575
 *
 */
public interface XmlDocument
{
  public List getAnnotations();
  public void processXml();
}
