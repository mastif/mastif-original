package edu.mayo.bmi.xml;

public class Annot {
	
	public static int count = 0;
	public int id = 0;
	public int start = 0;
	public int end   = 0;
	public String localName = null;
	public String umlsObjsString = null;
	public String type = null;
	public String pennTag = null;
	public String status = null;
	public String neg = null;
	public String text = null;
	public Annot prev = null;
	public Annot next = null;
	
	public boolean openTagEmittedP = false;
	public String nodeType = null;
	
	public Annot () {
		id = count++;
	}
	
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		
		b.append("Annot: { id : ");
		b.append(id);
		b.append(", start : ");
		b.append(start);
		b.append(", end: ");
		b.append(end);
		b.append("; localname : \"");
		b.append(localName);
		b.append("\", umlsObjsString : \"");
		b.append(umlsObjsString);
		b.append("\", type : \"");
		b.append(type);
		b.append("\", pennTag : \"");
		b.append(pennTag);
		b.append("\", status : \"");
		b.append(status);
		b.append("\", neg : \"");
		b.append(neg);
		b.append("\", text : \"");
		b.append(text);
		b.append("\", prev.id : ");
		b.append((prev == null) ? "prev is null" : prev.id);
		b.append(", next.id : ");
		b.append((next == null) ? "next is null" : next.id);
		b.append("}");
		
		return b.toString();
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