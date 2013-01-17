/*
 * Copyright (c) 2012, Omar Siam. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  I designates this
 * particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.homeunix.siam.wordmarker;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.value.SequenceType;

// declare function shnwm:markWords($inputString as xs:string?,
//                                  $validWordChars as xs:string (:java regexp:),
//                                  $allPossibleDelimiters as xs:string (:java regexp:),
//                                  $tag as xs:QName)
public class ExtensionFunctionWordMarker extends ExtensionFunctionDefinition {

	private static final long serialVersionUID = 5275236263991461306L;
	private static final StructuredQName qName =
            new StructuredQName("shnwm",
                    "http://siam.homeunix.net/wordmarker",
                    "markWords");
	private final static SequenceType[] argumentTypes = new SequenceType[] {
        SequenceType.OPTIONAL_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.makeSequenceType(ItemType.QNAME.getUnderlyingItemType(), StaticProperty.EXACTLY_ONE) };
	
	@Override
	public SequenceType[] getArgumentTypes() {
		return argumentTypes;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

    @Override
    public int getMinimumNumberOfArguments() {
        return 4;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 4;
    }
    
	@Override
	public SequenceType getResultType(SequenceType[] arg0) {
        return SequenceType.NODE_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionWordMarkerCall();
	}
	
	@Override
	public boolean hasSideEffects() {
		return true;
	}

}
