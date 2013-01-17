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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.StringValue;

public class ExtensionFunctionWordMarkerCall extends ExtensionFunctionCall {

	private static final long serialVersionUID = -822735028214925294L;

	private Pattern someValidWordCharacters;
    private Pattern allPossibleDelimiters;
	
	private TreeModel treeModel = null;
	private PipelineConfiguration pipe = null;

	private Properties envProperties = System.getProperties();
	private Properties enforceSaxon9Propertiess = new Properties(envProperties);
	
	private final AttributesImpl atts = new AttributesImpl();
	
	public ExtensionFunctionWordMarkerCall() {
		super();
	    enforceSaxon9Propertiess.put("javax.xml.transform.TransformerFactory",
				"net.sf.saxon.TransformerFactoryImpl");
	}
	
	@Override
	public void copyLocalData(ExtensionFunctionCall destination) {
		ExtensionFunctionWordMarkerCall dest = (ExtensionFunctionWordMarkerCall) destination;
		dest.someValidWordCharacters = this.someValidWordCharacters;
		dest.allPossibleDelimiters = this.allPossibleDelimiters;
		dest.envProperties = this.envProperties;
		dest.enforceSaxon9Propertiess = this.enforceSaxon9Propertiess;
	};
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public SequenceIterator<? extends Item> call(
			SequenceIterator<? extends Item>[] args, XPathContext ctx)
			throws XPathException {
		SequenceIterator<NodeInfo> result = null;
		ByteArrayOutputStream exceptionTraceText = new ByteArrayOutputStream();
		PrintStream exceptionTrace = new PrintStream(exceptionTraceText);
		String tempFileName = "Temp file not set!";
		StringValue in = null;
		StringValue regExp = null;
		QNameValue tag = null;
		ScannerWithDelimiterAccess s = null;
		try {
			in = (StringValue) args[0].next();
			if(null == in) {
				return EmptyIterator.getInstance();
			}

			regExp = (StringValue) args[1].next();
			if(null == regExp) {
				return EmptyIterator.getInstance();
			}			
			
			String regExpSomeValidWordCharacters = regExp.getStringValue();

			regExp = (StringValue) args[2].next();
			if(null == regExp) {
				return EmptyIterator.getInstance();
			}			
			
			tag = (QNameValue) args[3].next();
			if(null == tag) {
				return EmptyIterator.getInstance();
			}
			
			String regExpAllPossibleDelimiters = regExp.getStringValue();
			
			if (null == someValidWordCharacters || !someValidWordCharacters.pattern().equals(regExpSomeValidWordCharacters))
				someValidWordCharacters = Pattern.compile(regExpSomeValidWordCharacters);
			if (null == allPossibleDelimiters || !allPossibleDelimiters.pattern().equals(regExpAllPossibleDelimiters))
				allPossibleDelimiters = Pattern.compile(regExpAllPossibleDelimiters);

			s = new ScannerWithDelimiterAccess(new StringReader(in.getStringValue()));
			s.useDelimiter(allPossibleDelimiters);
			
			net.sf.saxon.Configuration saxonConf = ctx.getConfiguration();
			if (treeModel == null) // that doesn't change afaik.
			{
				treeModel = saxonConf.getParseOptions().getModel();
				pipe = saxonConf.makePipelineConfiguration();
			}
			// The following only works if a TransfromerFactory is used which is aware of the existence of
			// the Saxon 9 implementations, e. g. the Saxon 9 factory net.sf.saxon.TransformerFactoryImpl.
			// One factory that has problems with this is the Saxon 6 factory which is used by <oXygen/>
			
			System.setProperties(enforceSaxon9Propertiess);
			Builder builder = treeModel.makeBuilder(pipe);
			builder.setTiming(false);
			builder.setLineNumbering(false);
			builder.setPipelineConfiguration(pipe);
			
			DocumentInfo doc = null;
			
			ContentHandler ch = new ReceivingContentHandler();
			((ReceivingContentHandler)ch).setPipelineConfiguration(pipe);
			((ReceivingContentHandler)ch).setReceiver(builder);
			try
			{	
				ch.startDocument();
				atts.clear();
				ch.startElement(tag.getNamespaceURI(), "dummy", tag.getPrefix() + ":dummy", atts);
				
				while (s.hasNext())
	            {
	            		String token = s.next();
	            		String delimiter = s.lastDelimiter();
	            		if (someValidWordCharacters.matcher(token).find()) {
	            			ch.startElement(tag.getNamespaceURI(), tag.getLocalName(), tag.getStructuredQName().getDisplayName(), atts);
	            			ch.characters(token.toCharArray(), 0, token.length());
	            			ch.endElement(tag.getNamespaceURI(), tag.getLocalName(), tag.getStructuredQName().getDisplayName());
	            			atts.clear();
	            		} else {
	            			atts.addAttribute(tag.getNamespaceURI(), "type", tag.getPrefix() + ":type", "", "unknown");
	            			ch.startElement(tag.getNamespaceURI(), tag.getLocalName(), tag.getStructuredQName().getDisplayName(), atts);
	            			String cleanedToken = token.replaceAll("&", "&amp;").replaceAll("<","&lt;"); 
	            			ch.characters(cleanedToken.toCharArray(), 0, cleanedToken.length());
	            			ch.endElement(tag.getNamespaceURI(), tag.getLocalName(), tag.getStructuredQName().getDisplayName());
	            			atts.clear();
	            		}
	            		if (delimiter != null && !delimiter.equals(""))
	            			ch.characters(delimiter.toCharArray(), 0, delimiter.length());
	            }
				ch.endElement(tag.getNamespaceURI(), "dummy", tag.getPrefix() + ":dummy");
				ch.endDocument();
			}
			finally
			{
				System.setProperties(envProperties);
			}
						
			doc = (DocumentInfo)builder.getCurrentRoot();
			List<NodeInfo> resultList = new ArrayList<NodeInfo>();
			AxisIterator<NodeInfo> iter = doc.iterateAxis(Axis.CHILD).next().iterateAxis(Axis.CHILD);
			while (iter.moveNext()) {
				resultList.add(iter.current());
			}
			result = new ListIterator<NodeInfo>(resultList);
		} catch (Exception e) {
			e.printStackTrace(exceptionTrace);
			
			// Create temp file.
			File temp;
			try {
				temp = File.createTempFile("wordMarker_", ".txt");

				// Write to temp file
				BufferedWriter tempOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp), Charset.forName("UTF-8")));
				tempOut.write(in.getStringValue());
				tempOut.close();
				tempFileName = temp.getAbsolutePath();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			throw new RuntimeException("See: " + tempFileName + ". Error while parsing. " + exceptionTraceText.toString());
		}
		return result;
	}

}
