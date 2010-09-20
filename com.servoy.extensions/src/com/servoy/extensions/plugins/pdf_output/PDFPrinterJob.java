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

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author jblok
 */
public class PDFPrinterJob extends PrinterJob
{
	private String jobName = "pdfJob"; //$NON-NLS-1$
	private Pageable printableDocument;
	private final boolean isMetaPrintJob;
	private Document document;
	private PdfWriter writer;
	private PdfContentByte cb;
	private OutputStream os;
	private final DefaultFontMapper mapper;
	private int totalPagesPrinted;
	private int pagesPrinted;

	PDFPrinterJob(OutputStream os, boolean isMetaPrintJob)
	{
		this.os = os;
		this.isMetaPrintJob = isMetaPrintJob;
		this.mapper = new DefaultFontMapper();
		this.totalPagesPrinted = 0;
		this.pagesPrinted = 0;
	}

	@Override
	public PageFormat defaultPage(PageFormat page)
	{
		return page;
	}

	@Override
	public int getCopies()
	{
		return 1;
	}

	@Override
	public String getJobName()
	{
		return jobName;
	}

	@Override
	public String getUserName()
	{
		return null;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public boolean isCancelled()
	{
		return false;
	}

	public int getTotalPagesPrinted()
	{
		return totalPagesPrinted;
	}

	public int getPagesPrinted()
	{
		return pagesPrinted;
	}

	public int insertDirectory(String path)
	{
		return mapper.insertDirectory(path);
	}

	@Override
	public PageFormat pageDialog(PageFormat page)
	{
		if (page == null) page = new PageFormat();
		return page;
	}

	private float d2f(double value)
	{
		float result = (new Double(value)).floatValue();
		if (result < 0.00099) result = 0.0f;
		return result;
	}

	private Rectangle pageSize(PageFormat pf)
	{
		return new Rectangle(0, 0, d2f(pf.getWidth()), d2f(pf.getHeight()));
	}

	private float pageMargin(String side, PageFormat pf)
	{
		float margin = 0.0f;
		if (side.equals("L")) //$NON-NLS-1$
		{
			margin = d2f(pf.getImageableX());
			return margin;
		}
		if (side.equals("T")) //$NON-NLS-1$
		{
			margin = d2f(pf.getImageableY());
			return margin;
		}
		if (side.equals("R")) //$NON-NLS-1$
		{
			margin = d2f(pf.getWidth() - pf.getImageableX() - pf.getImageableWidth());
			return margin;
		}
		if (side.equals("B")) //$NON-NLS-1$
		margin = d2f(pf.getHeight() - pf.getImageableY() - pf.getImageableHeight());
		return margin;
	}

	@Override
	public synchronized void print() throws PrinterException
	{
		//do work
		if (printableDocument != null)
		{
			try
			{
				if (document == null)
				{
					Printable printable = printableDocument.getPrintable(0);
					PageFormat pf = printableDocument.getPageFormat(0);
					document = new Document(pageSize(pf), pageMargin("L", pf), //$NON-NLS-1$
						pageMargin("R", pf), pageMargin("T", pf), //$NON-NLS-1$//$NON-NLS-2$
						pageMargin("B", pf)); //$NON-NLS-1$
					// we create a writer that listens to the document and
					// directs a PDF-stream to a file
					writer = PdfWriter.getInstance(document, os);
					document.open();
					// we create a template and a Graphics2D object that
					// corresponds with it
					cb = writer.getDirectContent();
				}
				int numPages = printableDocument.getNumberOfPages();
				if (numPages == 0) document.add(new Paragraph(" "));
				pagesPrinted = 0;
				for (int i = 0; i < numPages; i++)
				{
					pagesPrinted++;
					Printable printable = printableDocument.getPrintable(i);
					PageFormat pf = printableDocument.getPageFormat(i);
					document.setPageSize(pageSize(pf));
					document.setMargins(pageMargin("L", pf), //$NON-NLS-1$
						pageMargin("R", pf), pageMargin("T", pf), //$NON-NLS-1$//$NON-NLS-2$
						pageMargin("B", pf)); //$NON-NLS-1$
					cb.saveState();
					Graphics2D g2d = cb.createGraphics((int)pf.getWidth(), (int)pf.getHeight(), mapper);
					printable.print(g2d, pf, i);
					g2d.dispose();
					cb.restoreState();
					document.newPage();
				}
				totalPagesPrinted += pagesPrinted;
				if (!isMetaPrintJob)
				{
					close();
				}
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
		}
	}

	byte[] close()
	{
		byte[] retval = null;
		try
		{
			if (document != null)
			{
				document.close();
				document = null;
				if (writer != null) writer.close();
				writer = null;
				cb = null;
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		try
		{
			if (os instanceof ByteArrayOutputStream)
			{
				retval = ((ByteArrayOutputStream)os).toByteArray();
			}
			os = Utils.closeOutputStream(os);
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return retval;
	}

	@Override
	public boolean printDialog()
	{
		return true;
	}

	@Override
	public void setCopies(int copies)
	{
	}

	@Override
	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	@Override
	public void setPageable(Pageable document) throws NullPointerException
	{
		this.printableDocument = document;
	}

	@Override
	public void setPrintable(Printable painter, PageFormat format)
	{
		//only support pagable
	}

	@Override
	public void setPrintable(Printable painter)
	{
		//only support pagable
	}

	@Override
	public PageFormat validatePage(PageFormat page)
	{
		return page;
	}
}