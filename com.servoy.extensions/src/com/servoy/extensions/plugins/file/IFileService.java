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
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * The contract of the File service, as seen from the client.<br/>
 * Defines methods to allow streaming bytes[] by chunk using an {@link ITransferObject}
 * 
 * @author Servoy Stuff
 * @since Servoy 5.2
 */
@SuppressWarnings("nls")
public interface IFileService extends Remote
{
	@Deprecated
	public static final String SERVICE_NAME = IFileService.class.getName();

	/**
	 * The default folder server property key
	 */
	public static final String DEFAULT_FOLDER_PROPERTY = "servoy.FileServerService.defaultFolder";


	/**
	 * Creates the {@link ITransferObject} to use, giving it a fileName to create an {@link OutputStream}<br/>
	 * and returning an identifier
	 * 
	 * @param clientId the id of the client that wants to do the transfer.
	 * @param filePath the path of the file to create in the default folder (or relative to it)
	 * @return the {@link UUID} used later by the client to write bytes[] and to close the transfer
	 * 
	 * @throws IOException  if the OutputStream cannot be created or an IOException occurs
	 */
	public UUID openTransfer(final String clientId, final String filePath) throws RemoteException, IOException, SecurityException;

	/**
	 * Writes start + length bytes, using the {@link ITransferObject} identified by the given uuid
	 * 
	 * @param uuid the identifier of the {@link ITransferObject} to use
	 * @param bytes contains the bytes to write
	 * @param the starting position to read from in the bytes array
	 * @param the length of the bytes to read
	 * 
	 * @throws IOException  if an IOException occurs
	 */
	public void writeBytes(final UUID uuid, final byte[] bytes, final long start, final long length) throws RemoteException, IOException;


	/**
	 * Reads length number of bytes, using the {@link ITransferObject} identified by the given uuid and returns that in the array.
	 * When the end of the file is reached the returned array will have a smaller size then the given length (or null will returned)
	 * 
	 * @param uuid the identifier of the {@link ITransferObject} to use
	 * @param length the max number of bytes to transfer of the line.
	 * 
	 * @throws IOException  if an IOException occurs
	 */
	public byte[] readBytes(final UUID uuid, long length) throws RemoteException, IOException;

	/**
	 * Safely/silently closes the {@link OutputStream} using the {@link ITransferObject} identified by the uuid<br/>
	 * @return the file created, or null if a problem occurred during the process
	 */
	public Object closeTransfer(final UUID uuid) throws RemoteException;

	/**
	 * Retrieves a list of files contained in the folder represented by the path provided,<br/>
	 * or the file itself as first item in the array if the path represents a file.
	 * 
	 * @param clientId the id of the client that wants to get the list.
	 * @param path the path (relative to default folder) of the folder to list
	 * @param filesOption will return files and folders, files only or folders only depending on this value
	 * @param visibleOption will return any files, visibles files or non visible files depending on this value
	 * @param lockedOption will return any files, writable files or non writable files depending on this value
	 * 
	 * @return the list of files contained in the path provided
	 * @throws IOException  if an IOException occurs
	 * @throws SecurityException  if a SecurityException occurs
	 */
	public RemoteFileData[] getRemoteFolderContent(final String clientId, final String path, final String[] filter, final int fileOption,
		final int visibleOption, final int lockedOption) throws RemoteException, IOException, SecurityException;

	/**
	 * Retrieves a {@link RemoteFileData} object for the path provided either file or directory
	 * 
	 * @param clientId the id of the client
	 * @param path the file path on the server
	 * 
	 * @return the remote file data object
	 * @throws IOException  if an IOException occurs
	 * @throws SecurityException  if a SecurityException occurs
	 */
	public RemoteFileData getRemoteFileData(final String clientId, final String path) throws RemoteException, IOException, SecurityException;

	/**
	 * Retrieves {@link RemoteFileData}[] for the paths provided either files or directory
	 * 
	 * @param clientId the id of the client
	 * @param path an array of file path on the server
	 * 
	 * @return an array of remote file data objects
	 * @throws IOException  if an IOException occurs
	 * @throws SecurityException  if a SecurityException occurs
	 */
	public RemoteFileData[] getRemoteFileData(final String clientId, final String[] path) throws RemoteException, IOException, SecurityException;

	/**
	 * Deletes a file on the server side represented by the path provided (relative to the defaultFolder)
	 * 
	 * @param clientId the id of the client that wants to do the delete.
	 * @param path the path of the file to delete - if folder and the folder contains files, will throw an IOException
	 * 
	 * @return true if the file was successfully deleted
	 * @throws IOException  if an IOException occurs
	 */
	public boolean delete(final String clientId, final String filePath) throws RemoteException, IOException;

	/**
	 * Rename of file on the server side, emulating java default behavior, which could lead to IOException
	 * 
	 * @param clientId the id of the client that wants to do the renameTo.
	 * @param srcPath the path of the file to rename
	 * @param destPath the path of the file to rename to
	 * 
	 * @return the {@link RemoteFileData} of the renamed file
	 * @throws IOException  if an IOException occurs
	 */
	public RemoteFileData renameTo(final String clientId, final String srcPath, final String destPath) throws RemoteException, IOException;


	/**
	 * Tries to discover the mime-type of a file, using magic bytes reading and/or name extension map.
	 * @param clientId the id of the client that asks for the contentType.
	 * @param path the path of the file to check
	 * 
	 * @return the mime-type
	 * @throws IOException  if an IOException occurs
	 */
	public String getContentType(final String clientId, final String filePath) throws RemoteException, IOException;


	/**
	 * Returns the defaultFolder location as a String (canonical representation of the folder)
	 * @return the defaultFolder
	 */
	public String getDefaultFolderLocation(final String clientId) throws RemoteException;

}
