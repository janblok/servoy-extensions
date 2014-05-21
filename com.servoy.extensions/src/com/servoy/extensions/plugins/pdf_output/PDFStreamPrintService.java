/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */
package com.servoy.extensions.plugins.pdf_output;

import java.io.OutputStream;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.ServiceUIFactory;
import javax.print.StreamPrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.event.PrintServiceAttributeListener;

/**
 * @author jblok
 *
 * We have to do some work on this class to make it more compatible with a real StreamPrintService class.
 * @see StreamPrintService
 */
public class PDFStreamPrintService extends StreamPrintService
{
	PDFStreamPrintService(OutputStream os)
	{
		super(os);
	}

	@Override
	public String getOutputFormat()
	{
		return PDFStreamPrintServiceFactory.pdfMimeType;
	}

	public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener)
	{
		// TODO Auto-generated method stub

	}

	public DocPrintJob createPrintJob()
	{
		return new PDFDocPrintJob(this);
	}

	public PrintServiceAttribute getAttribute(Class category)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public PrintServiceAttributeSet getAttributes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object getDefaultAttributeValue(Class category)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getName()
	{
		return "PDF output";
	}

	public ServiceUIFactory getServiceUIFactory()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Class[] getSupportedAttributeCategories()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object getSupportedAttributeValues(Class category, DocFlavor flavor, AttributeSet attributes)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public DocFlavor[] getSupportedDocFlavors()
	{
		return PDFStreamPrintServiceFactory.getFlavors();
	}

	public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAttributeCategorySupported(Class category)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAttributeValueSupported(Attribute attrval, DocFlavor flavor, AttributeSet attributes)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDocFlavorSupported(DocFlavor flavor)
	{
		DocFlavor[] flavors = getSupportedDocFlavors();
		for (DocFlavor flavor2 : flavors)
		{
			if (flavor.equals(flavor2))
			{
				return true;
			}
		}
		return false;
	}

	public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener)
	{
		// TODO Auto-generated method stub

	}
}
