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
import java.io.File;
import java.io.FileOutputStream;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.event.PrintServiceAttributeListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.util.Debug;

/**
 * @author gboros
 *
 */
public class PDFPrintService implements PrintService
{
	private static final String OUTPUT_FILE_NAME = "out"; //$NON-NLS-1$
	private static final String OUTPUT_FILE_EXTENSION = ".pdf"; //$NON-NLS-1$

	private final IClientPluginAccess access;
	private JFileChooser outputFolderChooser;
	private int outputFileNameCounter;

	public PDFPrintService(IClientPluginAccess access)
	{
		this.access = access;
	}

	/*
	 * @see javax.print.PrintService#getName()
	 */
	public String getName()
	{
		return "Servoy PDF output";
	}

	/*
	 * @see javax.print.PrintService#createPrintJob()
	 */
	public DocPrintJob createPrintJob()
	{
		if (outputFolderChooser == null)
		{
			outputFolderChooser = new JFileChooser();
			outputFolderChooser.setDialogTitle("Save PDF output");
			outputFolderChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			outputFolderChooser.setFileFilter(new FileFilter()
			{

				@Override
				public boolean accept(File f)
				{
					return f.getName().toLowerCase().endsWith("pdf"); //$NON-NLS-1$
				}

				@Override
				public String getDescription()
				{
					return "PDF files";
				}

			});
		}
		DocPrintJob docPrintJob = null;
		StringBuffer outputFileName = new StringBuffer(OUTPUT_FILE_NAME);
		if (outputFileNameCounter > 0) outputFileName.append(outputFileNameCounter);
		outputFileName.append(OUTPUT_FILE_EXTENSION);
		outputFolderChooser.setSelectedFile(new File(System.getProperty("user.home"), outputFileName.toString())); //$NON-NLS-1$
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
		if (outputFolderChooser.showSaveDialog(currentWindow) == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				PDFStreamPrintService sps = new PDFStreamPrintService(new FileOutputStream(outputFolderChooser.getSelectedFile()));
				docPrintJob = sps.createPrintJob();
				outputFileNameCounter++;
			}
			catch (Exception ex)
			{
				Debug.error(ex);
			}
		}

		return docPrintJob;
	}

	/*
	 * @see javax.print.PrintService#addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener)
	 */
	public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener)
	{
	}

	/*
	 * @see javax.print.PrintService#removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener)
	 */
	public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener)
	{
	}

	/*
	 * @see javax.print.PrintService#getAttributes()
	 */
	public PrintServiceAttributeSet getAttributes()
	{
		return null;
	}

	/*
	 * @see javax.print.PrintService#getAttribute(java.lang.Class)
	 */
	public <T extends PrintServiceAttribute> T getAttribute(Class<T> category)
	{
		return null;
	}

	/*
	 * @see javax.print.PrintService#getSupportedDocFlavors()
	 */
	public DocFlavor[] getSupportedDocFlavors()
	{
		return PDFStreamPrintServiceFactory.getFlavors();
	}

	/*
	 * @see javax.print.PrintService#isDocFlavorSupported(javax.print.DocFlavor)
	 */
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

	/*
	 * @see javax.print.PrintService#getSupportedAttributeCategories()
	 */
	public Class< ? >[] getSupportedAttributeCategories()
	{
		return null;
	}

	/*
	 * @see javax.print.PrintService#isAttributeCategorySupported(java.lang.Class)
	 */
	public boolean isAttributeCategorySupported(Class< ? extends Attribute> category)
	{
		return false;
	}

	/*
	 * 
	 * @see javax.print.PrintService#getDefaultAttributeValue(java.lang.Class)
	 */
	public Object getDefaultAttributeValue(Class< ? extends Attribute> category)
	{
		return null;
	}

	/*
	 * @see javax.print.PrintService#getSupportedAttributeValues(java.lang.Class, javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	public Object getSupportedAttributeValues(Class< ? extends Attribute> category, DocFlavor flavor, AttributeSet attributes)
	{
		return null;
	}

	/*
	 * @see javax.print.PrintService#isAttributeValueSupported(javax.print.attribute.Attribute, javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	public boolean isAttributeValueSupported(Attribute attrval, DocFlavor flavor, AttributeSet attributes)
	{
		return false;
	}

	/*
	 * @see javax.print.PrintService#getUnsupportedAttributes(javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes)
	{
		return null;
	}

	/*
	 * @see javax.print.PrintService#getServiceUIFactory()
	 */
	public ServiceUIFactory getServiceUIFactory()
	{
		return null;
	}
}