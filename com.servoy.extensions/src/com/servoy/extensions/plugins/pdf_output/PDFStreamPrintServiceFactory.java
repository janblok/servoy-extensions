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
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;

public class PDFStreamPrintServiceFactory extends StreamPrintServiceFactory
{
    static final String pdfMimeType = "application/pdf";

    static final DocFlavor[] supportedDocFlavors = {
	 DocFlavor.SERVICE_FORMATTED.PAGEABLE,
    };

    public  String getOutputFormat() 
    {
    	return pdfMimeType;
    }

    public DocFlavor[] getSupportedDocFlavors() 
    {
    	return getFlavors();
    }

    static DocFlavor[] getFlavors() 
    {
		DocFlavor[] flavors = new DocFlavor[supportedDocFlavors.length];
		System.arraycopy(supportedDocFlavors, 0, flavors, 0, flavors.length);
		return flavors;
    }

    public StreamPrintService getPrintService(OutputStream out) 
    {
    	return new PDFStreamPrintService(out);
    } 
}
