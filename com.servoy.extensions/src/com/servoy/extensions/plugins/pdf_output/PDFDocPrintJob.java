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

import java.awt.print.Pageable;
import javax.print.Doc;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.StreamPrintService;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;

public class PDFDocPrintJob implements DocPrintJob
{
	private StreamPrintService orgin;
	
	PDFDocPrintJob(StreamPrintService o)
	{
		orgin = o;
	}

	public void addPrintJobAttributeListener(PrintJobAttributeListener listener, PrintJobAttributeSet attributes)
	{
		// TODO Auto-generated method stub
	}

	public void addPrintJobListener(PrintJobListener listener)
	{
		// TODO Auto-generated method stub
	}

	public PrintJobAttributeSet getAttributes()
	{
		return new HashPrintJobAttributeSet();
	}

	public PrintService getPrintService()
	{
		return orgin;
	}

	public void removePrintJobAttributeListener(PrintJobAttributeListener listener)
	{
		// TODO Auto-generated method stub
	}

	public void removePrintJobListener(PrintJobListener listener)
	{
		// TODO Auto-generated method stub
	}

	public void print(Doc doc, PrintRequestAttributeSet attributes) throws PrintException
	{
		try
		{
			if (doc != null && doc.getPrintData() instanceof Pageable)
			{
				PDFPrinterJob job = new PDFPrinterJob(orgin.getOutputStream(), false);
				job.setPageable((Pageable)doc.getPrintData());
				job.print();
				job.close();
			}
		}
		catch (Exception e)
		{
			throw new PrintException(e);
		}
	}
}
