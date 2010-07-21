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
package com.servoy.extensions.plugins.file;

import java.io.IOException;

/**
 * Defines the general contract of a transfer object that will consume bytes coming from a client<br/>
 * Implementation could write to a file, a database or any other repository
 * 
 * @author Servoy Stuff
 * @since Servoy 5.2
 */
public interface ITransferObject
{

	/**
	 * Write start + length bytes from the given byte array
	 * 
	 * @param bytes the byte array
	 * @param start starting point to use in the array
	 * @param length length of the bytes to use
	 * @throws IOException if something went wrong
	 */
	public void write(final byte[] bytes, final long start, final long length) throws IOException;


	/**
	 * Reads in the next block of bytes from the file until the length is reached or the end of file is reached.
	 * 
	 * @param length the max number of bytes to return
	 * @throws IOException if something went wrong
	 */
	public byte[] read(long length) throws IOException;

	/**
	 * Silently close/dispose any resources and return the identifier of the resource created
	 * 
	 * @returns the identifier of the resource created (can be a String or a UUID or any other unique identifier)
	 */
	public Object close();

}
