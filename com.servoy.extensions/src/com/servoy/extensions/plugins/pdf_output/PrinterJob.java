/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

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

import java.awt.HeadlessException;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import com.servoy.j2db.documentation.ServoyDocumented;

/**
 * The object that can be given to controller.print() 
 * 
 * @author jcompagner
 *
 */
@ServoyDocumented
public class PrinterJob extends java.awt.print.PrinterJob
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#setPrintable(java.awt.print.Printable)
	 */
	@Override
	public void setPrintable(Printable painter)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#setPrintable(java.awt.print.Printable, java.awt.print.PageFormat)
	 */
	@Override
	public void setPrintable(Printable painter, PageFormat format)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#setPageable(java.awt.print.Pageable)
	 */
	@Override
	public void setPageable(Pageable document) throws NullPointerException
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#printDialog()
	 */
	@Override
	public boolean printDialog() throws HeadlessException
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#pageDialog(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat pageDialog(PageFormat page) throws HeadlessException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#defaultPage(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat defaultPage(PageFormat page)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#validatePage(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat validatePage(PageFormat page)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#print()
	 */
	@Override
	public void print() throws PrinterException
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#setCopies(int)
	 */
	@Override
	public void setCopies(int copies)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#getCopies()
	 */
	@Override
	public int getCopies()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#getUserName()
	 */
	@Override
	public String getUserName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#setJobName(java.lang.String)
	 */
	@Override
	public void setJobName(String jobName)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#getJobName()
	 */
	@Override
	public String getJobName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#cancel()
	 */
	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.PrinterJob#isCancelled()
	 */
	@Override
	public boolean isCancelled()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
