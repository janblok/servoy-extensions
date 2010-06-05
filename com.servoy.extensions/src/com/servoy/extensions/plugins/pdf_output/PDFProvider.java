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
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.FileChooserUtils;

/**
 * @author jblok
 */
public class PDFProvider implements IScriptObject
{
	private final PDFPlugin plugin;
	private PDFPrinterJob metaPrintJob;
	private OutputStream os = null;
	private File pdfFile = null;

	public PDFProvider(PDFPlugin plugin)
	{
		this.plugin = plugin;
	}

	@SuppressWarnings("nls")
	public PrinterJob js_getPDFPrinter(Object[] varargs)
	{
		try
		{
			if (varargs != null && varargs.length != 0 && varargs[0] != null)
			{
				File f = new File(varargs[0].toString());
				IClientPluginAccess access = plugin.getClientPluginAccess();
				pdfFile = FileChooserUtils.getAWriteFile(access.getCurrentWindow(), f, false);
				os = new FileOutputStream(pdfFile);
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

	@SuppressWarnings("nls")
	public boolean js_startMetaPrintJob(Object[] varargs)
	{
		try
		{
			if (varargs != null && varargs.length != 0 && varargs[0] != null)
			{
				File f = new File(varargs[0].toString());
				IClientPluginAccess access = plugin.getClientPluginAccess();
				pdfFile = FileChooserUtils.getAWriteFile(access.getCurrentWindow(), f, false);
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

	public byte[] js_endMetaPrintJob()
	{
		boolean deletePdfFile = false;
		if (metaPrintJob != null)
		{
			if (pdfFile != null) if (metaPrintJob.getTotalPagesPrinted() == 0) deletePdfFile = true;
			metaPrintJob.close();
			metaPrintJob = null;
		}
		if (os instanceof ByteArrayOutputStream)
		{
			byte[] array = ((ByteArrayOutputStream)os).toByteArray();
			os = null;
			return array;
		}
		if (deletePdfFile)
		{
			pdfFile.delete();
			pdfFile = null;
		}
		return null;
	}

	public int js_insertFontDirectory(String path)
	{
		if (metaPrintJob != null)
		{
			return metaPrintJob.insertDirectory(path);
		}
		return -1;
	}

	/**
	 * Combine muliple protected pdf docs into one.
	 *
	 * @sample
	 * //combine muliple protected pdf docs into one.
	 * pdf_blob_column = combineProtectedPDFDocuments(new Array(pdf_blob1,pdf_blob2,pdf_blob3), new Array(pdf_blob1_pass,pdf_blob2_pass,pdf_blob3_pass));
	 *
	 * @param array 
	 *
	 * @param passwords 
	 */
	public byte[] js_combineProtectedPDFDocuments(Object[] pdf_docs_bytearrays, Object[] pdf_docs_password)
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
				if (pdf_docs_password != null && pdf_docs_password.length > f && pdf_docs_password[f] != null)
				{
					if (pdf_docs_password[f] instanceof String) password = pdf_docs_password[f].toString().getBytes();
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

	public byte[] js_combinePDFDocuments(Object[] pdf_docs_bytearrays)
	{
		return js_combineProtectedPDFDocuments(pdf_docs_bytearrays, null);
	}

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

	public byte[] js_convertPDFFormToPDFDocument(byte[] pdf_form, Object field_values)
	{
		return js_convertProtectedPDFFormToPDFDocument(pdf_form, null, field_values);
	}

	public int js_getPagesPrinted()
	{
		if (metaPrintJob != null)
		{
			return metaPrintJob.getPagesPrinted();
		}
		return 0;
	}

	public int js_getTotalPagesPrinted()
	{
		if (metaPrintJob != null)
		{
			return metaPrintJob.getTotalPagesPrinted();
		}
		return 0;
	}

	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("getPDFPrinter".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "filename" };
		}
		else if ("combinePDFDocuments".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "array" };
		}
		else if ("combineProtectedPDFDocuments".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "array", "passwords" };
		}
		else if ("convertPDFFormToPDFDocument".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "pdf_form", "name_value_array" };
		}
		else if ("convertProtectedPDFFormToPDFDocument".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "pdf_form", "pdf_password", "name_value_array" };
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	@SuppressWarnings("nls")
	public String getSample(String methodName)
	{
		if ("getPDFPrinter".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//to print current record without printdialog to pdf file in temp dir.\n"); //$NON-NLS-1$
			retval.append("controller.print(true,false,%%elementName%%.getPDFPrinter('c:/temp/out.pdf'));\n\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("combinePDFDocuments".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//combine muliple pdf docs into one.\n"); //$NON-NLS-1$
			retval.append("pdf_blob_column = combinePDFDocuments(new Array(pdf_blob1,pdf_blob2,pdf_blob3));\n\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("combineProtectedPDFDocuments".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//combine muliple protected pdf docs into one.\n"); //$NON-NLS-1$
			retval.append("pdf_blob_column = combineProtectedPDFDocuments(new Array(pdf_blob1,pdf_blob2,pdf_blob3), new Array(pdf_blob1_pass,pdf_blob2_pass,pdf_blob3_pass));\n\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("convertPDFFormToPDFDocument".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("var pdfform = plugins.file.readFile('c:/temp/1040a-form.pdf');\n"); //$NON-NLS-1$
			retval.append("//var field_values = plugins.file.readFile('c:/temp/1040a-data.fdf');//read adobe fdf values or\n"); //$NON-NLS-1$
			retval.append("var field_values = new Array()//construct field values\n"); //$NON-NLS-1$
			retval.append("field_values[0] = 'f1-1=John C.J.'\n"); //$NON-NLS-1$
			retval.append("field_values[1] = 'f1-2=Longlasting'\n"); //$NON-NLS-1$
			retval.append("var result_pdf_doc = %%elementName%%.convertPDFFormToPDFDocument(pdfform, field_values)\n"); //$NON-NLS-1$
			retval.append("if (result_pdf_doc != null)\n"); //$NON-NLS-1$
			retval.append("{\n"); //$NON-NLS-1$
			retval.append("\tplugins.file.writeFile('c:/temp/1040a-flatten.pdf', result_pdf_doc)\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("convertProtectedPDFFormToPDFDocument".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("var pdfform = plugins.file.readFile('c:/temp/1040a-form.pdf');\n"); //$NON-NLS-1$
			retval.append("//var field_values = plugins.file.readFile('c:/temp/1040a-data.fdf');//read adobe fdf values or\n"); //$NON-NLS-1$
			retval.append("var field_values = new Array()//construct field values\n"); //$NON-NLS-1$
			retval.append("field_values[0] = 'f1-1=John C.J.'\n"); //$NON-NLS-1$
			retval.append("field_values[1] = 'f1-2=Longlasting'\n"); //$NON-NLS-1$
			retval.append("var result_pdf_doc = %%elementName%%.convertProtectedPDFFormToPDFDocument(pdfform, 'pdf_password', field_values)\n"); //$NON-NLS-1$
			retval.append("if (result_pdf_doc != null)\n"); //$NON-NLS-1$
			retval.append("{\n"); //$NON-NLS-1$
			retval.append("\tplugins.file.writeFile('c:/temp/1040a-flatten.pdf', result_pdf_doc)\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("startMetaPrintJob".equals(methodName) || "endMetaPrintJob".equals(methodName) || "getPagesPrinted".equals(methodName) || "getTotalPagesPrinted".equals(methodName)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//to print multiple forms to one pdf document (on file system).\n"); //$NON-NLS-1$
			retval.append("var success = %%elementName%%.startMetaPrintJob('c:/temp/out.pdf')\n"); //$NON-NLS-1$
			retval.append("if (success)\n{\n"); //$NON-NLS-1$
			retval.append("forms.form_one.controller.print(false,false,%%elementName%%.getPDFPrinter());\n"); //$NON-NLS-1$
			retval.append("application.output('form one printed ' + %%elementName%%.getPagesPrinted() + ' pages.');\n"); //$NON-NLS-1$
			retval.append("forms.form_two.controller.print(false,false,%%elementName%%.getPDFPrinter());\n"); //$NON-NLS-1$
			retval.append("application.output('form two printed ' + %%elementName%%.getPagesPrinted() + ' pages.');\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			retval.append("application.output('total printed pages: ' + %%elementName%%.getTotalPagesPrinted());\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.endMetaPrintJob()\n\n"); //$NON-NLS-1$
			retval.append("//to print multiple forms to one pdf document (to store in dataprovider).\n"); //$NON-NLS-1$
			retval.append("var success = %%elementName%%.startMetaPrintJob()\n"); //$NON-NLS-1$
			retval.append("if (success)\n{\n"); //$NON-NLS-1$
			retval.append("forms.form_one.controller.print(false,false,%%elementName%%.getPDFPrinter());\n"); //$NON-NLS-1$
			retval.append("application.output('form one printed ' + %%elementName%%.getPagesPrinted() + ' pages.');\n"); //$NON-NLS-1$
			retval.append("forms.form_two.controller.print(false,false,%%elementName%%.getPDFPrinter());\n"); //$NON-NLS-1$
			retval.append("application.output('form two printed ' + %%elementName%%.getPagesPrinted() + ' pages.');\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			retval.append("application.output('total printed pages: ' + %%elementName%%.getTotalPagesPrinted());\n"); //$NON-NLS-1$
			retval.append("mediaDataProvider = %%elementName%%.endMetaPrintJob()\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("insertFontDirectory".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//Insert font directories for font embedding.\n"); //$NON-NLS-1$
			retval.append("//You must create an MetaPrintJob before using it.\n");
			retval.append("%%elementName%%.insertFontDirectory('c:/Windows/Fonts');\n");
			retval.append("%%elementName%%.insertFontDirectory('c:/WinNT/Fonts');\n");
			retval.append("%%elementName%%.insertFontDirectory('/Library/Fonts');\n\n");
			return retval.toString();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	@SuppressWarnings("nls")
	public String getToolTip(String methodName)
	{
		if ("getPDFPrinter".equals(methodName))
		{
			return "Returns a PDF printer that can be used in print calls. If a file name is provided, then a PDF printer that generates a PDF into the specified file is returned. If no argument is provided, then the PDF printer corresponding to the last started meta print job is returned.";
		}
		else if ("convertPDFFormToPDFDocument".equals(methodName))
		{
			return "Convert a PDF form to a PDF document.";
		}
		else if ("convertProtectedPDFFormToPDFDocument".equals(methodName))
		{
			return "Convert a protected PDF form to a PDF document.";
		}
		else if ("combinePDFDocuments".equals(methodName))
		{
			return "Combine muliple PDF docs into one.";
		}
		else if ("combineProtectedPDFDocuments".equals(methodName))
		{
			return "Combine muliple protected PDF docs into one.";
		}
		else if ("endMetaPrintJob".equals(methodName))
		{
			return "Ends a previously started meta print job. For meta print jobs that were stored in memory, not in a file on disk, also returns the content of the generated PDF document.";
		}
		else if ("getPagesPrinted".equals(methodName))
		{
			return "Returns the number of pages printed by the last print call done in the context of a meta print job.";
		}
		else if ("getTotalPagesPrinted".equals(methodName))
		{
			return "Returns the total number of pages printed in the context of a meta print job. Call this method before ending the meta print job.";
		}
		else if ("insertFontDirectory".equals(methodName))
		{
			return "Add a directory that should be searched for fonts. Call this only in the context of an active meta print job.";
		}
		else if ("startMetaPrintJob".equals(methodName))
		{
			return "Used for printing multiple things into the same PDF document. Starts a meta print job and all print calls made before ending the meta print job will be done into the same PDF document. If a file name is specified, then the PDF document is generated into that file. If no argument is specified, then the PDF document is stored in memory and can be retrieved when ending the meta print job and can be saved into a dataprovider, for example.";
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class[] getAllReturnedTypes()
	{
		return null;
	}
}