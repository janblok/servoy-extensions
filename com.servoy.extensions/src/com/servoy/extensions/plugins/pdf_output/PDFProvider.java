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

import java.awt.Color;
import java.awt.Window;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Scriptable;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FdfReader;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
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
		if ("getPDFPrinter".equals(methodName) || "startMetaPrintJob".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[filename]" };
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
		else if ("numberPages".equals(methodName))
		{
			return new String[] { "data", "[fontSize]", "[locationX]", "[locationY]", "[font]", "[fontHexColor]" };
		}
		else if ("addMetaData".equals(methodName))
		{
			return new String[] { "data", "metaData" };
		}
		else if ("encrypt".equals(methodName))
		{
			return new String[] { "data", "ownerPassword", "[userPassword]", "[allowAssembly]", "[allowCopy]", "[allowDegradedPrinting]", "[allowFillIn]", "[allowModifyAnnotations]", "[allowModifyContents]", "[allowPrinting]", "[allowScreenreaders]", "[is128bit]", "[metaData]" };
		}
		else if ("watermark".equals(methodName))
		{
			return new String[] { "data", "image", "[locationX]", "[locationY]", "[isUnderText]", "[pagesToWatermark]" };
		}
		else if ("overlay".equals(methodName))
		{
			return new String[] { "data", "forOverlay", "outputPath", "[isOver]", "[pages]" };
		}
		else if ("overlayText".equals(methodName))
		{
			return new String[] { "data", "text", "[locationX]", "[locationY]", "[isUnderText]", "[fontSize]", "[font]", "[fontHexColor]" };
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
		else if ("numberPages".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//" + getToolTip(methodName) + "\n");
			retval.append("var unNumberedFile = plugins.file.showFileOpenDialog()\n");
			retval.append("if (unNumberedFile) {\n");
			retval.append("\tvar data = plugins.file.readFile(unNumberedFile);\n");
			retval.append("\tpageNumberedPdf = %%elementName%%.numberPages(data, 12, 520, 30, 'Courier', '#ff0033')\n");
			retval.append("}\n");
			return retval.toString();
		}
		else if ("addMetaData".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//" + getToolTip(methodName) + "\n");
			retval.append("var pdf = plugins.file.showFileOpenDialog();\n");
			retval.append("if (pdf) {\n");
			retval.append("\tvar data = plugins.file.readFile(pdf);\n");
			retval.append("\tvar metaData = { Author: 'Servoy' };\n");
			retval.append("\tpdfResult = %%elementName%%.addMetaData(data, metaData)\n");
			retval.append("}\n");
			return retval.toString();
		}
		else if ("encrypt".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//" + getToolTip(methodName) + "\n");
			retval.append("//NOTE: Passwords are case sensitive \n");
			retval.append("var unEncryptedFile = plugins.file.showFileOpenDialog()\n");
			retval.append("if (unEncryptedFile) {\n");
			retval.append("\tvar data = plugins.file.readFile(unEncryptedFile);\n");
			retval.append("\tencryptedResult = %%elementName%%.encrypt(data, 'secretPassword', 'secretUserPassword', false, false, false, false, false, false, false, false, true)\n");
			retval.append("}\n");
			return retval.toString();
		}
		else if ("watermark".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//" + getToolTip(methodName) + "\n");
			retval.append("var pdf = plugins.file.showFileOpenDialog()\n");
			retval.append("if (pdf) {\n");
			retval.append("\tvar data = plugins.file.readFile(pdf);\n");
			retval.append("\tvar image = plugins.file.showFileOpenDialog()\n");
			retval.append("\tmodifiedPdf = %%elementName%%.watermark(data, image)\n");
			retval.append("}\n");
			return retval.toString();
		}
		else if ("overlay".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//" + getToolTip(methodName) + "\n");
			retval.append("\tvar pages = new Array()\n");
			retval.append("\tpages[0] = '1'\n");
			retval.append("\tpages[1] = '3'\n");
			retval.append("\tpages[2] = '5'\n");
			retval.append("\tvar input1 = plugins.file.showFileOpenDialog(1,null,false,'pdf',null,'Select source file')\n");
			retval.append("if (input1) {\n");
			retval.append("\tvar data = plugins.file.readFile(input1);\n");
			retval.append("\tvar input2 = plugins.file.showFileOpenDialog(1,null,false,'pdf',null,'Select file for overlay')\n");
			retval.append("\tif (input2) {\n");
			retval.append("\tvar data2 = plugins.file.readFile(input2);\n");
			retval.append("\t\toverlayedPdf = %%elementName%%.overlay( data, data2, false, pages )\n");
			retval.append("\t\t//overlayedPdf = %%elementName%%.overlay( data, data2 )\n");
			retval.append("\t\t//overlayedPdf = %%elementName%%.overlay( data, data2, false, null )\n");
			retval.append("\t\t//overlayedPdf = %%elementName%%.overlay( data, data2, pages )\n");
			retval.append("\t}\n");
			retval.append("}\n");
			return retval.toString();
		}
		else if ("overlayText".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//" + getToolTip(methodName) + "\n");
			retval.append("var pdf = plugins.file.showFileOpenDialog()\n");
			retval.append("if (pdf) {\n");
			retval.append("\tvar data = plugins.file.readFile(pdf);\n");
			retval.append("\tmodifiedPdf = %%elementName%%.overlayText(data, 'DRAFT', 230, 430, true, 32, 'Helvetica', '#33ff33')\n");
			retval.append("}\n");
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
		else if ("numberPages".equals(methodName))
		{
			return "Add pages numbers to a PDF.";
		}
		else if ("addMetaData".equals(methodName))
		{
			return "Add metadata to the PDF, like Author.";
		}
		else if ("encrypt".equals(methodName))
		{
			return "Add password protection and security options to the PDF.";
		}
		else if ("watermark".equals(methodName))
		{
			return "Add an images as a watermark on every page, or the pages specified as a parameter.";
		}
		else if ("overlay".equals(methodName))
		{
			return "Add some PDF based content over a PDF.";
		}
		else if ("overlayText".equals(methodName))
		{
			return "Add text over every page at a 45 degree angle.";
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

	/**
	 * Scripting method to add metaData to a PDF<br/>
	 * Method adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param metaData a JavaScript object ({@link Scriptable}) that contains the metadata as property/value pairs
	 * 
	 * @return the PDF with metaData added
	 * 
	 * @throws Exception
	 */
	public byte[] js_addMetaData(byte[] data, Scriptable metaData) throws Exception
	{

		if (data == null || metaData == null) throw new IllegalArgumentException("Missing argument"); //$NON-NLS-1$

		Map<String, Object> map = ITextTools.getMapFromScriptable(metaData);
		if (ITextTools.isNullOrEmpty(map)) throw new IllegalArgumentException("No metadata to add"); //$NON-NLS-1$

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return ITextTools.addMetaData(bais, map);

	}


	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param ownerPassword the owner password
	 * 
	 * @return the encrypted PDF
	 * 
	 * @throws Exception
	 */
	public byte[] js_encrypt(byte[] data, String ownerPassword) throws Exception
	{
		return js_encrypt(data, ownerPassword, ownerPassword, true, true, true, true, true, true, true, true, true, null);
	}

	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param ownerPassword the owner password
	 * @param userPassword the user password
	 * 
	 * @return the encrypted PDF
	 * 
	 * @throws Exception
	 */
	public byte[] js_encrypt(byte[] data, String ownerPassword, String userPassword) throws Exception
	{
		return js_encrypt(data, ownerPassword, userPassword, true, true, true, true, true, true, true, true, true, null);
	}

	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param ownerPassword the owner password
	 * @param userPassword the user password
	 * @param allowAssembly whether to set the allow assembly permission
	 * @param allowCopy whether to set the allow copy permission
	 * @param allowDegradedPrinting whether to set the allow degraded printing permission
	 * @param allowFillIn whether to set the allow fill in permission
	 * @param allowModifyAnnotations whether to set the allow modify annotations permission
	 * @param allowModifyContents whether to set the allow modify contents permission
	 * @param allowPrinting whether to set the allow printing permission
	 * @param allowScreenreaders whether to set the allow screen readers permission
	 * 
	 * @return the encrypted PDF
	 * 
	 * @throws Exception
	 */
	public byte[] js_encrypt(byte[] data, String ownerPassword, String userPassword, boolean allowAssembly, boolean allowCopy, boolean allowDegradedPrinting,
		boolean allowFillIn, boolean allowModifyAnnotations, boolean allowModifyContents, boolean allowPrinting, boolean allowScreenreaders) throws Exception
	{
		return js_encrypt(data, ownerPassword, userPassword, allowAssembly, allowCopy, allowDegradedPrinting, allowFillIn, allowModifyAnnotations,
			allowModifyContents, allowPrinting, allowScreenreaders, true, null);
	}

	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param ownerPassword the owner password
	 * @param userPassword the user password
	 * @param allowAssembly whether to set the allow assembly permission
	 * @param allowCopy whether to set the allow copy permission
	 * @param allowDegradedPrinting whether to set the allow degraded printing permission
	 * @param allowFillIn whether to set the allow fill in permission
	 * @param allowModifyAnnotations whether to set the allow modify annotations permission
	 * @param allowModifyContents whether to set the allow modify contents permission
	 * @param allowPrinting whether to set the allow printing permission
	 * @param allowScreenreaders whether to set the allow screen readers permission
	 * @param is128bit whether to use 128-bit encryption
	 * 
	 * @return the encrypted PDF
	 * 
	 * @throws Exception
	 */
	public byte[] js_encrypt(byte[] data, String ownerPassword, String userPassword, boolean allowAssembly, boolean allowCopy, boolean allowDegradedPrinting,
		boolean allowFillIn, boolean allowModifyAnnotations, boolean allowModifyContents, boolean allowPrinting, boolean allowScreenreaders, boolean is128bit)
		throws Exception
	{
		return js_encrypt(data, ownerPassword, userPassword, allowAssembly, allowCopy, allowDegradedPrinting, allowFillIn, allowModifyAnnotations,
			allowModifyContents, allowPrinting, allowScreenreaders, is128bit, null);
	}

	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param ownerPassword the owner password
	 * @param userPassword the user password
	 * @param allowAssembly whether to set the allow assembly permission
	 * @param allowCopy whether to set the allow copy permission
	 * @param allowDegradedPrinting whether to set the allow degraded printing permission
	 * @param allowFillIn whether to set the allow fill in permission
	 * @param allowModifyAnnotations whether to set the allow modify annotations permission
	 * @param allowModifyContents whether to set the allow modify contents permission
	 * @param allowPrinting whether to set the allow printing permission
	 * @param allowScreenreaders whether to set the allow screen readers permission
	 * @param is128bit whether to use 128-bit encryption
	 * @param metaData a JavaScript object ({@link Scriptable}) that contains the metadata as property/value pairs
	 * 
	 * @return the encrypted PDF
	 * 
	 * @throws Exception
	 */
	public byte[] js_encrypt(byte[] data, String ownerPassword, String userPassword, boolean allowAssembly, boolean allowCopy, boolean allowDegradedPrinting,
		boolean allowFillIn, boolean allowModifyAnnotations, boolean allowModifyContents, boolean allowPrinting, boolean allowScreenreaders, boolean is128bit,
		Scriptable metaData) throws Exception
	{
		if (data == null) throw new IllegalArgumentException("Missing argument"); //$NON-NLS-1$
		int sec = 0;
		if (allowAssembly)
		{
			sec = sec | PdfWriter.ALLOW_ASSEMBLY;
		}
		if (allowCopy)
		{
			sec = sec | PdfWriter.ALLOW_COPY;
		}
		if (allowDegradedPrinting)
		{
			sec = sec | PdfWriter.ALLOW_DEGRADED_PRINTING;
		}
		if (allowFillIn)
		{
			sec = sec | PdfWriter.ALLOW_FILL_IN;
		}
		if (allowModifyAnnotations)
		{
			sec = sec | PdfWriter.ALLOW_MODIFY_ANNOTATIONS;
		}
		if (allowModifyContents)
		{
			sec = sec | PdfWriter.ALLOW_MODIFY_CONTENTS;
		}
		if (allowPrinting)
		{
			sec = sec | PdfWriter.ALLOW_PRINTING;
		}
		if (allowScreenreaders)
		{
			sec = sec | PdfWriter.ALLOW_SCREENREADERS;
		}
		Map<String, Object> map = ITextTools.getMapFromScriptable(metaData);

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return ITextTools.encrypt(bais, ownerPassword, userPassword, sec, is128bit, map);
	}


	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * 
	 * @return the PDF with numbered pages
	 * 
	 * @throws Exception
	 */
	public byte[] js_numberPages(byte[] data) throws Exception
	{
		return js_numberPages(data, 10, 520, 30, BaseFont.HELVETICA, "#000000"); //$NON-NLS-1$
	}

	/**
	 * Add page numbering to the PDF provided<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param fontSize the font size to use
	 * @param locationX the x location of the numbers
	 * @param locationY the y location of the numbers
	 * @param font the font to use
	 * @param hexColor the font color to use
	 * 
	 * @return the PDF with numbered pages
	 * 
	 * @throws Exception
	 */
	public byte[] js_numberPages(byte[] data, int fontSize, int locationX, int locationY, String font, String hexColor) throws Exception
	{
		if (data == null) throw new IllegalArgumentException("Missing argument"); //$NON-NLS-1$

		Color myColor = Color.decode(hexColor);

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return ITextTools.numberPDF(bais, fontSize, locationX, locationY, font, myColor);
	}


	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param image the path of an image to use
	 * 
	 * @return the PDF with added watermak
	 * 
	 * @throws Exception
	 */
	public byte[] js_watermark(byte[] data, String image) throws Exception
	{
		return js_watermark(data, image, 200, 400, false, null);
	}

	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param image the path of an image to use
	 * @param locationX the x location of the image
	 * @param locationY the y location of the image
	 * @param isOver whether to put over the content
	 * 
	 * @return the PDF with added watermak
	 * 
	 * @throws Exception
	 */
	public byte[] js_watermark(byte[] data, String image, int locationX, int locationY, boolean isOver) throws Exception
	{
		return js_watermark(data, image, locationX, locationY, isOver, null);
	}

	/**
	 * Adds an image watermark to the PDF provided<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param image the path of an image to use
	 * @param locationX the x location of the image
	 * @param locationY the y location of the image
	 * @param isOver whether to put over the content
	 * @param pages an array of pages where to apply the watermark
	 * 
	 * @return the PDF with added watermak
	 * 
	 * @throws Exception
	 */
	public byte[] js_watermark(byte[] data, String image, int locationX, int locationY, boolean isOver, String[] pages) throws Exception
	{
		if (data == null) throw new IllegalArgumentException("Missing argument"); //$NON-NLS-1$

		Image watermark = Image.getInstance(image);

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return ITextTools.watermarkPDF(bais, watermark, locationX, locationY, isOver, pages);
	}


	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param forOverlay a PDF to use as overlay
	 * 
	 * @return the PDF with added overlay
	 * 
	 * @throws Exception
	 */
	public byte[] js_overlay(byte[] data, byte[] forOverlay) throws Exception
	{
		return js_overlay(data, forOverlay, false, null);
	}

	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param forOverlay a PDF to use as overlay
	 * @param pages an array of page numbers to put the overlay on
	 * 
	 * @return the PDF with added overlay
	 * 
	 * @throws Exception
	 */
	public byte[] js_overlay(byte[] data, byte[] forOverlay, String[] pages) throws Exception
	{
		return js_overlay(data, forOverlay, false, pages);
	}

	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param forOverlay a PDF to use as overlay
	 * @param isOver whether the overlay will be put over the content
	 * 
	 * @return the PDF with added overlay
	 * 
	 * @throws Exception
	 */
	public byte[] js_overlay(byte[] data, byte[] forOverlay, boolean isOver) throws Exception
	{
		return js_overlay(data, forOverlay, isOver, null);
	}


	/**
	 * Overlay some PDF content on top of the provided PDF<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param forOverlay a PDF to use as overlay
	 * @param isOver whether the overlay will be put over the content
	 * @param pages an array of page numbers to put the overlay on
	 * 
	 * @return the PDF with added overlay
	 * 
	 * @throws Exception
	 */
	public byte[] js_overlay(byte[] data, byte[] forOverlay, boolean isOver, String[] pages) throws Exception
	{
		if (data == null || forOverlay == null) throw new IllegalArgumentException("Missing argument"); //$NON-NLS-1$

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayInputStream foais = new ByteArrayInputStream(forOverlay);
		return ITextTools.overlay(bais, foais, isOver, pages);
	}


	/**
	 * Overloaded method<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param text the text to use for the overlay
	 * 
	 * @return the PDF with added overlay
	 * 
	 * @throws Exception
	 */
	public byte[] js_overlayText(byte[] data, String text) throws Exception
	{
		return js_overlayText(data, text, 230, 430, true, 32, BaseFont.HELVETICA, "#000000"); //$NON-NLS-1$
	}

	/**
	 * Overlay a text over the provided PDF<br/>
	 * Adapted from the PDF Pro plugin with full approval from the author
	 * 
	 * @author Scott Buttler
	 * 
	 * @param data the PDF
	 * @param text the text to use for the overlay
	 * @param locationX the x location of the overlay
	 * @param locationY the y location of the overlay
	 * @param isOver whether to put the overlay over the content
	 * @param fontSize the font size to use
	 * @param font the font to use
	 * @param hexColor the font color to use
	 * 
	 * @return the PDF with added overlay
	 * 
	 * @throws Exception
	 */
	public byte[] js_overlayText(byte[] data, String text, int locationX, int locationY, boolean isOver, int fontSize, String font, String hexColor)
		throws Exception
	{
		if (data == null || ITextTools.isNullOrEmpty(text) || ITextTools.isNullOrEmpty(font) || ITextTools.isNullOrEmpty(hexColor))
		{
			throw new IllegalArgumentException("Missing argument"); //$NON-NLS-1$
		}

		Color myColor = Color.decode(hexColor);

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return ITextTools.overlayText(bais, text, locationX, locationY, isOver, fontSize, font, myColor);
	}

}