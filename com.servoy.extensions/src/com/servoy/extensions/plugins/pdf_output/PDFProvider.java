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

import java.awt.Window;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.FdfReader;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.SimpleBookmark;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.FileChooserUtils;

/**
 * @author jblok
 */
@ServoyDocumented(publicName = "pdf_output")
public class PDFProvider implements IScriptable
{
	private final PDFPlugin plugin;
	private PDFPrinterJob metaPrintJob;
	private File pdfFile = null;

	public PDFProvider(PDFPlugin plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Returns a PDF printer that can be used in print calls. If a file name is provided, then a PDF printer that generates a PDF into the specified file is returned. If no argument is provided, then the PDF printer corresponding to the last started meta print job is returned.
	 *
	 * @sample
	 * //to print current record without printdialog to pdf file in temp dir.
	 * controller.print(true,false,plugins.pdf_output.getPDFPrinter('c:/temp/out.pdf'));
	 *
	 * @param filename optional
	 */
	@SuppressWarnings("nls")
	public PrinterJob js_getPDFPrinter(Object[] varargs)
	{
		try
		{
			if (varargs != null && varargs.length != 0 && varargs[0] != null)
			{
				File f = new File(varargs[0].toString());
				IClientPluginAccess access = plugin.getClientPluginAccess();
				IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
				Window currentWindow = null;
				if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
				pdfFile = FileChooserUtils.getAWriteFile(currentWindow, f, false);
				FileOutputStream os = new FileOutputStream(pdfFile);
				return new PDFPrinterJob(os, false);
			}
			else
			{
				return metaPrintJob;
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
			throw new RuntimeException("Error getting a PDF PrinterJob for " + varargs[0], e);
		}
	}

	/**
	 * Used for printing multiple things into the same PDF document. Starts a meta print job and all print calls made before ending the meta print job will be done into the same PDF document. If a file name is specified, then the PDF document is generated into that file. If no argument is specified, then the PDF document is stored in memory and can be retrieved when ending the meta print job and can be saved into a dataprovider, for example.
	 * 
	 * @sampleas js_endMetaPrintJob()
	 *
	 * @param filename optional
	 */
	@SuppressWarnings("nls")
	public boolean js_startMetaPrintJob(Object[] varargs)
	{
		try
		{
			OutputStream os = null;
			if (varargs != null && varargs.length != 0 && varargs[0] != null)
			{
				File f = new File(varargs[0].toString());
				IClientPluginAccess access = plugin.getClientPluginAccess();
				IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
				Window currentWindow = null;
				if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
				pdfFile = FileChooserUtils.getAWriteFile(currentWindow, f, false);
				os = new FileOutputStream(pdfFile);
			}
			else
			{
				os = new ByteArrayOutputStream();
			}
			metaPrintJob = new PDFPrinterJob(os, true);
			return true;
		}
		catch (Exception e)
		{
			Debug.error(e);
			String message = "Error starting a meta PrinterJob";
			if (varargs != null && varargs.length != 0 && varargs[0] != null)
			{
				message += " for " + varargs[0];
			}
			throw new RuntimeException(message, e); //$NON-NLS-1$
		}
	}

	/**
	 * Ends a previously started meta print job. For meta print jobs that were stored in memory, not in a file on disk, also returns the content of the generated PDF document.
	 *
	 * @sample
	 * //to print multiple forms to one pdf document (on file system).
	 * var success = plugins.pdf_output.startMetaPrintJob('c:/temp/out.pdf')
	 * if (success)
	 * {
	 * forms.form_one.controller.print(false,false,plugins.pdf_output.getPDFPrinter());
	 * application.output('form one printed ' + plugins.pdf_output.getPagesPrinted() + ' pages.');
	 * forms.form_two.controller.print(false,false,plugins.pdf_output.getPDFPrinter());
	 * application.output('form two printed ' + plugins.pdf_output.getPagesPrinted() + ' pages.');
	 * }
	 * application.output('total printed pages: ' + plugins.pdf_output.getTotalPagesPrinted());
	 * plugins.pdf_output.endMetaPrintJob()
	 * 
	 * //to print multiple forms to one pdf document (to store in dataprovider).
	 * var success = plugins.pdf_output.startMetaPrintJob()
	 * if (success)
	 * {
	 * forms.form_one.controller.print(false,false,plugins.pdf_output.getPDFPrinter());
	 * application.output('form one printed ' + plugins.pdf_output.getPagesPrinted() + ' pages.');
	 * forms.form_two.controller.print(false,false,plugins.pdf_output.getPDFPrinter());
	 * application.output('form two printed ' + plugins.pdf_output.getPagesPrinted() + ' pages.');
	 * }
	 * application.output('total printed pages: ' + plugins.pdf_output.getTotalPagesPrinted());
	 * mediaDataProvider = plugins.pdf_output.endMetaPrintJob()
	 */
	public byte[] js_endMetaPrintJob()
	{
		byte[] retval = null;
		if (metaPrintJob != null)
		{
			retval = metaPrintJob.close();
			if (pdfFile != null && metaPrintJob.getTotalPagesPrinted() == 0)
			{
				pdfFile.delete();//zero byte files makes no sense to leave
			}
			metaPrintJob = null;
		}
		pdfFile = null;
		return retval;
	}

	/**
	 * Add a directory that should be searched for fonts. Call this only in the context of an active meta print job.
	 *
	 * @sample
	 * //Insert font directories for font embedding.
	 * //You must create an MetaPrintJob before using it.
	 * plugins.pdf_output.insertFontDirectory('c:/Windows/Fonts');
	 * plugins.pdf_output.insertFontDirectory('c:/WinNT/Fonts');
	 * plugins.pdf_output.insertFontDirectory('/Library/Fonts');
	 * 
	 * @param path
	 */
	public int js_insertFontDirectory(String path)
	{
		if (metaPrintJob != null)
		{
			return metaPrintJob.insertDirectory(path);
		}
		return -1;
	}

	/**
	 * Combine multiple protected PDF docs into one.
	 *
	 * @sample
	 * pdf_blob_column = combineProtectedPDFDocuments(new Array(pdf_blob1,pdf_blob2,pdf_blob3), new Array(pdf_blob1_pass,pdf_blob2_pass,pdf_blob3_pass));
	 *
	 * @param pdf_docs_bytearrays 
	 * @param pdf_docs_passwords 
	 */
	public byte[] js_combineProtectedPDFDocuments(Object[] pdf_docs_bytearrays, Object[] pdf_docs_passwords)
	{
		if (pdf_docs_bytearrays == null || pdf_docs_bytearrays.length == 0) return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			int pageOffset = 0;
			List master = new ArrayList();
			Document document = null;
			PdfCopy writer = null;
			for (int f = 0; f < pdf_docs_bytearrays.length; f++)
			{
				if (!(pdf_docs_bytearrays[f] instanceof byte[])) continue;
				byte[] pdf_file = (byte[])pdf_docs_bytearrays[f];

				// we create a reader for a certain document
				byte[] password = null;
				if (pdf_docs_passwords != null && pdf_docs_passwords.length > f && pdf_docs_passwords[f] != null)
				{
					if (pdf_docs_passwords[f] instanceof String) password = pdf_docs_passwords[f].toString().getBytes();
				}
				PdfReader reader = new PdfReader(pdf_file, password);
				reader.consolidateNamedDestinations();
				// we retrieve the total number of pages
				int n = reader.getNumberOfPages();
				List bookmarks = SimpleBookmark.getBookmark(reader);
				if (bookmarks != null)
				{
					if (pageOffset != 0)
					{
						SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
					}
					master.addAll(bookmarks);
				}
				pageOffset += n;

				if (writer == null)
				{
					// step 1: creation of a document-object
					document = new Document(reader.getPageSizeWithRotation(1));
					// step 2: we create a writer that listens to the document
					writer = new PdfCopy(document, baos);
					// step 3: we open the document
					document.open();
				}

				// step 4: we add content
				PdfImportedPage page;
				for (int i = 0; i < n;)
				{
					++i;
					page = writer.getImportedPage(reader, i);
					writer.addPage(page);
				}
				PRAcroForm form = reader.getAcroForm();
				if (form != null) writer.copyAcroForm(reader);
			}
			if (writer != null && document != null)
			{
				if (master.size() > 0) writer.setOutlines(master);
				// step 5: we close the document
				document.close();
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
			throw new RuntimeException("Error combinding pdf documents", e); //$NON-NLS-1$

		}
		return baos.toByteArray();
	}

	/**
	 * Combine multiple PDF docs into one.
	 *
	 * @sample
	 * pdf_blob_column = combinePDFDocuments(new Array(pdf_blob1,pdf_blob2,pdf_blob3));
	 *
	 * @param pdf_docs_bytearrays 
	 */
	public byte[] js_combinePDFDocuments(Object[] pdf_docs_bytearrays)
	{
		return js_combineProtectedPDFDocuments(pdf_docs_bytearrays, null);
	}

	/**
	 * Convert a protected PDF form to a PDF document.
	 *
	 * @sample
	 * var pdfform = plugins.file.readFile('c:/temp/1040a-form.pdf');
	 * //var field_values = plugins.file.readFile('c:/temp/1040a-data.fdf');//read adobe fdf values or
	 * var field_values = new Array()//construct field values
	 * field_values[0] = 'f1-1=John C.J.'
	 * field_values[1] = 'f1-2=Longlasting'
	 * var result_pdf_doc = plugins.pdf_output.convertProtectedPDFFormToPDFDocument(pdfform, 'pdf_password', field_values)
	 * if (result_pdf_doc != null)
	 * {
	 * 	plugins.file.writeFile('c:/temp/1040a-flatten.pdf', result_pdf_doc)
	 * }
	 *
	 * @param pdf_form 
	 * @param pdf_password 
	 * @param field_values 
	 */
	public byte[] js_convertProtectedPDFFormToPDFDocument(byte[] pdf_form, String pdf_password, Object field_values)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfReader reader = new PdfReader(pdf_form, pdf_password != null ? pdf_password.getBytes() : null);
			PdfStamper stamp = new PdfStamper(reader, baos);
			if (field_values != null)
			{
				AcroFields form = stamp.getAcroFields();
				if (field_values instanceof String)
				{
					FdfReader fdf = new FdfReader((String)field_values);
					form.setFields(fdf);
				}
				else if (field_values instanceof byte[])
				{
					FdfReader fdf = new FdfReader((byte[])field_values);
					form.setFields(fdf);
				}
				else if (field_values instanceof Object[])
				{
					for (int i = 0; i < ((Object[])field_values).length; i++)
					{
						Object obj = ((Object[])field_values)[i];
						if (obj instanceof Object[])
						{
							Object[] row = (Object[])obj;
							if (row.length >= 2)
							{
								Object field = row[0];
								Object value = row[1];
								if (field != null && value != null)
								{
									form.setField(field.toString(), value.toString());
								}
							}
//							else if (row.length >= 3)
//							{
//			                	form.setField(field, value);
//							}
						}
						else if (obj instanceof String)
						{
							String s = (String)obj;
							int idx = s.indexOf('=');
							form.setField(s.substring(0, idx), s.substring(idx + 1));
						}
					}
				}
			}
			stamp.setFormFlattening(true);
			stamp.close();
			return baos.toByteArray();
		}
		catch (Exception e)
		{
			Debug.error(e);
			throw new RuntimeException("Error converting pdf form to pdf document", e); //$NON-NLS-1$
		}
	}

	/**
	 * Convert a PDF form to a PDF document.
	 *
	 * @sample
	 * var pdfform = plugins.file.readFile('c:/temp/1040a-form.pdf');
	 * //var field_values = plugins.file.readFile('c:/temp/1040a-data.fdf');//read adobe fdf values or
	 * var field_values = new Array()//construct field values
	 * field_values[0] = 'f1-1=John C.J.'
	 * field_values[1] = 'f1-2=Longlasting'
	 * var result_pdf_doc = plugins.pdf_output.convertPDFFormToPDFDocument(pdfform, field_values)
	 * if (result_pdf_doc != null)
	 * {
	 * 	plugins.file.writeFile('c:/temp/1040a-flatten.pdf', result_pdf_doc)
	 * }
	 *
	 * @param pdf_form 
	 * @param field_values 
	 */
	public byte[] js_convertPDFFormToPDFDocument(byte[] pdf_form, Object field_values)
	{
		return js_convertProtectedPDFFormToPDFDocument(pdf_form, null, field_values);
	}

	/**
	 * Returns the number of pages printed by the last print call done in the context of a meta print job.
	 *
	 * @sampleas js_endMetaPrintJob()
	 */
	public int js_getPagesPrinted()
	{
		if (metaPrintJob != null)
		{
			return metaPrintJob.getPagesPrinted();
		}
		return 0;
	}

	/**
	 * Returns the total number of pages printed in the context of a meta print job. Call this method before ending the meta print job.
	 *
	 * @sampleas js_endMetaPrintJob()
	 */
	public int js_getTotalPagesPrinted()
	{
		if (metaPrintJob != null)
		{
			return metaPrintJob.getTotalPagesPrinted();
		}
		return 0;
	}

}