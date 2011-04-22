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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;

import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IMediaUploadCallback;
import com.servoy.j2db.plugins.IUploadData;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.FileChooserUtils;
import com.servoy.j2db.util.Utils;

/**
 * @author jcompagner
 * @author Servoy Stuff
 */
public class FileProvider implements IScriptObject
{

	protected final FilePlugin plugin;
	private final FileSystemView fsf = FileSystemView.getFileSystemView();
	private final Map<String, File> tempFiles = new HashMap<String, File>(); //check this map when saving (txt/binary) files
	private static final JSFile[] EMPTY = new JSFile[0];
	private final Timer timer = new Timer();

	/**
	 * Line Separator constant, used to append to Text file
	 * @since Servoy 5.2
	 */
	private static final String LF = System.getProperty("line.separator"); //$NON-NLS-1$

	/**
	 * Size of the buffer used to stream files to the server
	 * @since Servoy 5.2
	 */
	public static final int CHUNK_BUFFER_SIZE = 64 * 1024;

	public FileProvider(FilePlugin plugin)
	{
		this.plugin = plugin;
	}

	public JSFile js_getDesktopFolder()
	{
		// in Unix-based systems, the root directory is "/", not ~<user>/Desktop => old implementation not correct
		// in Windows the root directory is indeed the desktop directory, but it's also <user.home>\Desktop 
		File homeDir = fsf.getHomeDirectory();
		File desktopDir = new File(homeDir.getAbsolutePath() + File.separator + "Desktop"); //$NON-NLS-1$
		if (desktopDir != null && desktopDir.isDirectory())
		{
			return new JSFile(desktopDir);
		}
		else
		{
			//the old implementation - shouldn't normally reach this piece of code
			File[] roots = fsf.getRoots();
			for (File element : roots)
			{
				if (fsf.isRoot(element))
				{
					return new JSFile(element);
				}
			}
			return null;
		}
	}

	public JSFile js_getHomeDirectory()
	{
		return new JSFile(new File(System.getProperty("user.home"))); //$NON-NLS-1$
	}

	protected File getFileFromArg(Object f, boolean createFileInstance)
	{
		File file = null;
		if (f instanceof String)
		{
			file = tempFiles.get(f);
			if (file == null && createFileInstance) file = new File((String)f);
		}
		else if (f instanceof JSFile)
		{
			file = ((JSFile)f).getFile();
		}
		else if (f instanceof File)
		{
			file = (File)f;
		}
		return file;
	}

	public Object js_showFileOpenDialog(Object[] args)
	{
		int selection = JFileChooser.FILES_ONLY;
		boolean multiSelect = false;
		String[] filter = null;
		String title = null;

		File file = null;
		FunctionDefinition fd = null;
		if (args != null && args.length > 0)
		{
			if (args[0] instanceof Function)
			{
				fd = new FunctionDefinition((Function)args[0]);
			}
			else
			{
				int arg = Utils.getAsInteger(args[0]);
				if (arg == 1)
				{
					selection = JFileChooser.FILES_ONLY;
				}
				else if (arg == 2)
				{
					selection = JFileChooser.DIRECTORIES_ONLY;
				}
			}

			// start dir
			if (args.length > 1 && args[1] != null)
			{
				if (args[1] instanceof Function)
				{
					fd = new FunctionDefinition((Function)args[1]);
				}
				else
				{
					file = getFileFromArg(args[1], true);
				}
			}
			// multi select
			if (args.length > 2 && args[2] != null)
			{
				if (args[2] instanceof Function)
				{
					fd = new FunctionDefinition((Function)args[2]);
				}
				else
				{
					multiSelect = Utils.getAsBoolean(args[2]);
				}
			}
			// filter
			if (args.length > 3)
			{
				if (args[3] instanceof Function)
				{
					fd = new FunctionDefinition((Function)args[3]);
				}
				else
				{
					if (args[3] instanceof String)
					{
						filter = new String[] { (String)args[3] };
					}
					else if (args[3] instanceof Object[])
					{
						Object[] array = (Object[])args[3];
						filter = new String[array.length];
						for (int i = 0; i < array.length; i++)
						{
							filter[i] = array[i].toString();
						}
					}
				}
			}

			if (args.length > 4 && args[4] instanceof Function)
			{
				fd = new FunctionDefinition((Function)args[4]);
			}

			// title
			if (args.length > 5 && args[5] instanceof String)
			{
				title = args[5].toString();
			}
		}

		IClientPluginAccess access = plugin.getClientPluginAccess();
		if (fd != null)
		{
			final FunctionDefinition functionDef = fd;
			final List<JSFile> returnList = new ArrayList<JSFile>();
			IMediaUploadCallback callback = new IMediaUploadCallback()
			{
				public void uploadComplete(IUploadData[] fu)
				{
					JSFile[] files = new JSFile[fu.length];
					for (int i = 0; i < fu.length; i++)
					{
						files[i] = new JSFile(fu[i]);
						returnList.add(files[i]);
					}
					functionDef.execute(plugin.getClientPluginAccess(), new Object[] { files }, false);
				}
			};
			access.showFileOpenDialog(callback, file != null ? file.getAbsolutePath() : null, multiSelect, filter, selection, title);
			if (returnList.size() > 0) return returnList.toArray(new JSFile[returnList.size()]);
		}
		else
		{
			if (access.getApplicationType() == IClientPluginAccess.WEB_CLIENT)
			{
				throw new RuntimeException("Function callback not set for webclient"); //$NON-NLS-1$
			}

			if (multiSelect)
			{
				File[] files = FileChooserUtils.getFiles(access.getCurrentWindow(), file, selection, filter, title);
				JSFile[] convertedFiles = convertToJSFiles(files);
				return convertedFiles;
			}
			else
			{
				File f = FileChooserUtils.getAReadFile(access.getCurrentWindow(), file, selection, filter, title);
				if (f != null)
				{
					return new JSFile(f);
				}
			}
		}
		return null;
	}

	public JSFile[] js_getFolderContents(Object[] options)
	{
		Object path = options[0];
		if (path == null) return EMPTY;

		final String[] fileFilter;
		final int filesOption; // null/0 = files and dirs, 1 = files, 2 = dirs.
		final int visibleOption;// null/0 = visible and non, 1 = visible, 2 = non.
		final int lockedOption; // null/0 = locked and non, 1 = locked, 2 = non.

		if (options.length > 1 && options[1] != null)
		{
			if (options[1].getClass().isArray())
			{
				Object[] tmp = (Object[])options[1];
				fileFilter = new String[tmp.length];
				for (int i = 0; i < tmp.length; i++)
				{
					fileFilter[i] = ((String)tmp[i]).toLowerCase();
				}
			}
			else
			{
				fileFilter = new String[] { ((String)options[1]).toLowerCase() };
			}
		}
		else fileFilter = null;
		if (options.length > 2) filesOption = Utils.getAsInteger(options[2]);
		else filesOption = AbstractFile.ALL;
		if (options.length > 3) visibleOption = Utils.getAsInteger(options[3]);
		else visibleOption = AbstractFile.ALL;
		if (options.length > 4) lockedOption = Utils.getAsInteger(options[4]);
		else lockedOption = AbstractFile.ALL;

		File file = convertToFile(path);

		FileFilter ff = new FileFilter()
		{
			public boolean accept(File pathname)
			{
				boolean retVal = true;
				if (fileFilter != null)
				{
					String name = pathname.getName().toLowerCase();
					for (String element : fileFilter)
					{
						retVal = name.endsWith(element);
						if (retVal) break;
					}
				}
				if (!retVal) return retVal;

				// file or folder
				if (filesOption == AbstractFile.FILES)
				{
					retVal = pathname.isFile();
				}
				else if (filesOption == AbstractFile.FOLDERS)
				{
					retVal = pathname.isDirectory();
				}
				if (!retVal) return false;

				boolean hidden = pathname.isHidden();
				if (visibleOption == AbstractFile.VISIBLE) retVal = !hidden;
				else if (visibleOption == AbstractFile.NON_VISIBLE) retVal = hidden;
				if (!retVal) return false;

				boolean canWrite = pathname.canWrite();
				if (lockedOption == AbstractFile.LOCKED) retVal = !canWrite;
				else if (lockedOption == AbstractFile.NON_LOCKED) retVal = canWrite;
				return retVal;
			}
		};
		return convertToJSFiles(file.listFiles(ff));
	}

	/**
	 * @param files
	 * @return
	 */
	private JSFile[] convertToJSFiles(File[] files)
	{
		if (files == null) return EMPTY;
		JSFile[] jsFiles = new JSFile[files.length];
		for (int i = 0; i < files.length; i++)
		{
			jsFiles[i] = new JSFile(files[i]);
		}
		return jsFiles;
	}

	public boolean js_moveFile(Object source, Object destination)
	{
		File sourceFile = convertToFile(source);
		File destFile = convertToFile(destination);
		if (sourceFile == null || destFile == null) return false;

		if (destFile.exists())
		{
			destFile.delete();
		}
		else
		{
			destFile.getAbsoluteFile().getParentFile().mkdirs();
		}

		if (sourceFile.equals(destFile)) return false;

		if (!sourceFile.renameTo(destFile))
		{
			// rename wouldn't work copy it
			if (!js_copyFile(sourceFile, destFile))
			{
				return false;
			}
			sourceFile.delete();
		}
		return true;
	}

	/**
	 * returns a JSFile for the given string
	 *
	 * @sample 
	 *
	 * @param fileName 
	 */
	@Deprecated
	public JSFile js_convertStringToJSFile(String fileName)
	{
		return new JSFile(new File(fileName));
	}

	public JSFile js_convertToJSFile(Object file)
	{
		if (file instanceof JSFile) return (JSFile)file;
		if (file instanceof File) return new JSFile((File)file);
		if (file != null) return new JSFile(new File(file.toString()));
		return null;
	}

	private File convertToFile(Object source)
	{
		if (source instanceof JSFile) return ((JSFile)source).getFile();
		else if (source instanceof File) return (File)source;
		else if (source != null) return new File(source.toString());
		return null;
	}

	public boolean js_copyFolder(Object source, Object destination)
	{
		File sourceDir = convertToFile(source);
		File destDir = convertToFile(destination);
		if (sourceDir == null || destDir == null) return false;

		if (sourceDir.equals(destDir)) return false;

		if (!sourceDir.exists()) return false;
		if (!sourceDir.isDirectory())
		{
			return js_copyFile(sourceDir, destDir);
		}

		if (destDir.exists())
		{
			if (!destDir.isDirectory())
			{
				return false;
			}
		}
		else if (!destDir.mkdirs())
		{
			return false;
		}

		boolean succes = true;
		File[] files = sourceDir.listFiles();
		if (files != null && files.length > 0)
		{
			for (int i = 0; i < files.length; i++)
			{
				File dest = new File(destDir, files[i].getName());
				if (files[i].isDirectory())
				{
					if (!files[i].equals(destDir))
					{
						succes = (js_copyFolder(files[i], dest) && succes);
						if (!succes) return false;
					}
				}
				else
				{
					succes = (js_copyFile(files[i], dest) && succes);
					if (!succes) return false;
				}
			}
		}
		return succes;
	}

	public boolean js_copyFile(Object source, Object destination)
	{
		File sourceFile = convertToFile(source);
		File destFile = convertToFile(destination);
		if (sourceFile == null || destFile == null) return false;

		if (sourceFile.equals(destFile)) return false;

		try
		{
			if (destFile.exists())
			{
				destFile.delete();
			}
			else
			{
				destFile.getAbsoluteFile().getParentFile().mkdirs();
			}
			if (destFile.createNewFile())
			{
				FileInputStream fis = null;
				FileOutputStream fos = null;
				FileChannel sourceChannel = null;
				FileChannel destinationChannel = null;


				try
				{
					fis = new FileInputStream(sourceFile);
					fos = new FileOutputStream(destFile);
					sourceChannel = fis.getChannel();
					destinationChannel = fos.getChannel();
					// Copy source file to destination file
					destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
				}
				catch (Exception e1)
				{
					throw e1;
				}
				finally
				{
					try
					{
						if (sourceChannel != null) sourceChannel.close();
					}
					catch (Exception e)
					{
					}
					try
					{
						if (destinationChannel != null) destinationChannel.close();
					}
					catch (Exception e)
					{
					}
					try
					{
						if (fis != null) fis.close();
					}
					catch (Exception e)
					{
					}
					try
					{
						if (fos != null) fos.close();
					}
					catch (Exception e)
					{
					}
				}
				return true;
			}
		}
		catch (Exception e)
		{
			// handle any IOException
			Debug.error(e);
		}
		return false;

	}

	public boolean js_createFolder(Object destination)
	{
		File destFile = convertToFile(destination);
		if (destFile == null) return false;

		boolean b = (destFile.exists() && destFile.isDirectory());
		if (!b)
		{
			b = destFile.mkdirs();
		}
		return b;
	}

	public boolean js_deleteFile(Object destination)
	{
		if (destination instanceof JSFile && ((JSFile)destination).getAbstractFile() instanceof RemoteFile)
		{
			return ((JSFile)destination).js_deleteFile();
		}
		return js_deleteFolder(destination, true);
	}

	public boolean js_deleteFolder(Object destination, boolean warning)
	{
		File destFile = convertToFile(destination);
		if (destFile == null) return false;

		if (destFile.isDirectory())
		{
			if (warning)
			{
				IClientPluginAccess access = plugin.getClientPluginAccess();
				int option = JOptionPane.showConfirmDialog(
					access.getCurrentWindow(),
					Messages.getString("servoy.plugin.file.folderDelete.warning") + destFile.getAbsolutePath(), Messages.getString("servoy.plugin.file.folderDelete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				if (option != JOptionPane.YES_OPTION) return false;
			}
			File[] files = destFile.listFiles();
			for (File element : files)
			{
				js_deleteFolder(element, false);
			}
		}
		return destFile.delete();
	}

	public long js_getFileSize(Object path)
	{
		File file = convertToFile(path);
		if (file == null) return -1;
		return file.length();
	}

	public Date js_getModificationDate(Object path)
	{
		File file = convertToFile(path);
		if (file == null) return null;
		return new Date(file.lastModified());
	}

	public JSFile[] js_getDiskList()
	{
		File[] roots = File.listRoots();
		JSFile[] jsRoots = new JSFile[roots.length];
		for (int i = 0; i < roots.length; i++)
		{
			jsRoots[i] = new JSFile(roots[i]);
		}
		return jsRoots;
	}

	@SuppressWarnings("nls")
	public JSFile js_createTempFile(String prefix, String suffix)
	{
		try
		{
			// If shorter than three, then pad with something, so that we don't get exception.
			File f = File.createTempFile((prefix.length() < 3) ? (prefix + "svy") : prefix, suffix);
			f.deleteOnExit();
			String name = f.getAbsolutePath();
			tempFiles.put(name, f);
			return new JSFile(f);
		}
		catch (Exception e)
		{
			Debug.error(e);
			return null;
		}
	}

	public JSFile js_createFile(Object name)
	{
		return js_convertToJSFile(name);
	}

	@SuppressWarnings("nls")
	public boolean js_writeTXTFile(Object[] args)
	{
		if (args == null) return false;
		Object f = (args.length > 0 ? args[0] : null);
		String data = (args.length > 1 && args[1] != null ? args[1].toString() : null);
		String encoding = (args.length > 2 && args[2] != null ? args[2].toString() : null);
		String mimeType = (args.length > 3 && args[3] != "text/plain" ? args[3].toString() : null);
		if (data == null) data = "";
		return writeTXT(f, data, encoding, mimeType);
	}


	/**
	 * @param f
	 * @param data
	 * @param encoding
	 * @return
	 */
	protected boolean writeTXT(Object f, String data, String encoding, @SuppressWarnings("unused") String contentType)
	{
		try
		{
			IClientPluginAccess access = plugin.getClientPluginAccess();
			File file = getFileFromArg(f, true);
			if (file == null)
			{
				file = FileChooserUtils.getAWriteFile(access.getCurrentWindow(), file, false);
			}

			if (file != null)
			{
				FileOutputStream fos = new FileOutputStream(file);
				try
				{
					return writeToOutputStream(fos, data, encoding);
				}
				finally
				{
					fos.close();
				}
			}
			return false;
		}
		catch (Exception e)
		{
			Debug.error(e);
			return false;
		}
	}

	/**
	 * @param data
	 * @param encoding
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected boolean writeToOutputStream(OutputStream os, String data, String encoding) throws FileNotFoundException, IOException
	{
		Charset cs = null;
		if (encoding != null)
		{
			if (Charset.isSupported(encoding))
			{
				cs = Charset.forName(encoding);
			}
			else
			{
				return false;//unknown encoding
			}
		}

		OutputStreamWriter writer = null;
		if (cs != null)
		{
			writer = new OutputStreamWriter(os, cs);
		}
		else
		{
			writer = new OutputStreamWriter(os);//default char encoding
		}
		BufferedWriter bw = new BufferedWriter(writer);
		bw.write(data);
		bw.close();
		return true;
	}

	@SuppressWarnings("nls")
	public boolean js_writeXMLFile(Object f, String xml, String encoding)
	{
		if (xml == null) return false;
		return writeTXT(f == null ? "file.xml" : f, xml, encoding, "text/xml");
	}

	@SuppressWarnings("nls")
	public boolean js_writeXMLFile(Object f, String xml)
	{
		if (xml == null) return false;

		String encoding = "UTF-8";
		int idx1 = xml.indexOf("encoding=");
		if (idx1 != -1 && xml.length() > idx1 + 10)
		{
			int idx2 = xml.indexOf('"', idx1 + 10);
			int idx3 = xml.indexOf('\'', idx1 + 10);
			int idx4 = Math.min(idx2, idx3);
			if (idx4 != -1)
			{
				encoding = xml.substring(idx1 + 10, idx4);
			}
		}
		return writeTXT(f == null ? "file.xml" : f, xml, encoding, "text/xml");
	}

	public boolean js_writeFile(Object f, byte[] data)
	{
		return js_writeFile(f, data, null);
	}

	public boolean js_writeFile(Object f, byte[] data, @SuppressWarnings("unused") String mimeType)
	{
		if (data == null) return false;
		try
		{
			File file = getFileFromArg(f, true);
			if (file == null)
			{
				IClientPluginAccess access = plugin.getClientPluginAccess();
				file = FileChooserUtils.getAWriteFile(access.getCurrentWindow(), file, false);
			}
			if (file != null && !file.isDirectory())
			{
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bos.write(data);
				bos.close();
				fos.close();
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
			Debug.error(e);
			return false;
		}
	}

	public String js_readTXTFile(Object[] args)
	{
		try
		{
			File f = null;
			if (args != null && args.length != 0 && args[0] != null)
			{
				f = getFileFromArg(args[0], true);
			}
			IClientPluginAccess access = plugin.getClientPluginAccess();
			File file = FileChooserUtils.getAReadFile(access.getCurrentWindow(), f, JFileChooser.FILES_ONLY, null);

			if (file != null) // !cancelled
			{
				return readTXTFile(args, new FileInputStream(file));
			}
			return null;
		}
		catch (Exception e)
		{
			Debug.error(e);
			return null;
		}
	}

	/**
	 * @param args
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected String readTXTFile(Object[] args, InputStream file) throws FileNotFoundException, IOException
	{
		String encoding = (args != null && args.length > 1 ? args[1].toString() : null);
		Charset cs = null;
		if (encoding != null)
		{
			if (Charset.isSupported(encoding))
			{
				cs = Charset.forName(encoding);
			}
			else
			{
				return null;//unknown enc.
			}
		}

		InputStreamReader reader = null;
		if (cs != null)
		{
			reader = new InputStreamReader(file, cs);
		}
		else
		{
			reader = new InputStreamReader(file);//default char encoding
		}

		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append('\n');
		}
		sb.setLength(sb.length() - 1); // remove newline
		br.close();
		return sb.toString();
	}

	public JSFile js_showFileSaveDialog(Object[] args)
	{
		File file = null;
		if (args != null && args.length != 0 && args[0] != null)
		{
			file = getFileFromArg(args[0], true);
		}
		String title = null;
		if (args != null && args.length > 1 && args[1] != null)
		{
			title = args[1].toString();
		}
		IClientPluginAccess access = plugin.getClientPluginAccess();
		File f = FileChooserUtils.getAWriteFile(access.getCurrentWindow(), file, true, title);
		if (f != null)
		{
			return new JSFile(f);
		}
		return null;
	}

	public JSFile js_showDirectorySelectDialog(Object[] args)
	{
		File f = null;
		if (args != null && args.length != 0 && args[0] != null)
		{
			f = getFileFromArg(args[0], true);
		}
		String title = null;
		if (args != null && args.length > 1 && args[1] != null)
		{
			title = args[1].toString();
		}
		IClientPluginAccess access = plugin.getClientPluginAccess();
		File retval = FileChooserUtils.getAReadFile(access.getCurrentWindow(), f, JFileChooser.DIRECTORIES_ONLY, null, title);
		if (retval != null)
		{
			return new JSFile(retval);
		}
		return null;
	}

	public byte[] js_readFile(Object[] args)
	{
		try
		{

			File f = null;
			if (args != null && args.length != 0 && args[0] != null)
			{
				f = getFileFromArg(args[0], true);
			}

			IClientPluginAccess access = plugin.getClientPluginAccess();
			File file = FileChooserUtils.getAReadFile(access.getCurrentWindow(), f, JFileChooser.FILES_ONLY, null);

			byte[] retval = null;
			if (file != null && file.exists() && !file.isDirectory()) // !cancelled
			{
				long size = -1;
				if (args != null && args.length >= 2) size = Utils.getAsLong(args[1]);
				else if (args != null && args.length == 1 && f == null) size = Utils.getAsLong(args[0]);
				if (SwingUtilities.isEventDispatchThread())
				{
					retval = FileChooserUtils.paintingReadFile(access.getExecutor(), access, file, size);
				}
				else
				{
					retval = FileChooserUtils.readFile(file, size);
				}
			}
			return retval;
		}
		catch (Exception e)
		{
			Debug.error(e);
			return null;
		}
	}

	@SuppressWarnings("nls")
	public String getSample(String methodName)
	{
		if ("convertToJSFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile(\"story.txt\");\n");
			sb.append("if (f.canRead())\n");
			sb.append("\tapplication.output(\"File can be read.\");\n");
			return sb.toString();

		}
		else if ("copyFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Copy based on file names.\n");
			sb.append("if (!%%elementName%%.copyFile(\"story.txt\", \"story.txt.copy\"))\n");
			sb.append("\tapplication.output(\"Copy failed.\");\n");
			sb.append("// Copy based on JSFile instances.\n");
			sb.append("var f = %%elementName%%.createFile(\"story.txt\");\n");
			sb.append("var fcopy = %%elementName%%.createFile(\"story.txt.copy2\");\n");
			sb.append("if (!%%elementName%%.copyFile(f, fcopy))\n");
			sb.append("\tapplication.output(\"Copy failed.\");\n");
			return sb.toString();
		}
		else if ("copyFolder".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Copy folder based on names.\n");
			sb.append("if (!%%elementName%%.copyFolder(\"stories\", \"stories_copy\"))\n");
			sb.append("\tapplication.output(\"Folder copy failed.\");\n");
			sb.append("// Copy folder based on JSFile instances.\n");
			sb.append("var d = %%elementName%%.createFile(\"stories\");\n");
			sb.append("var dcopy = %%elementName%%.createFile(\"stories_copy_2\");\n");
			sb.append("if (!%%elementName%%.copyFolder(d, dcopy))\n");
			sb.append("\tapplication.output(\"Folder copy failed.\");\n");
			return sb.toString();
		}
		else if ("createFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Create the JSFile instance based on the file name.\n");
			sb.append("var f = %%elementName%%.createFile(\"newfile.txt\");\n");
			sb.append("// Create the file on disk.\n");
			sb.append("if (!f.createNewFile())\n");
			sb.append("\tapplication.output(\"The file could not be created.\");\n");
			return sb.toString();
		}
		else if ("createFolder".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var d = %%elementName%%.convertToJSFile(\"newfolder\");\n");
			sb.append("if (!%%elementName%%.createFolder(d))\n");
			sb.append("\tapplication.output(\"Folder could not be created.\");\n");
			return sb.toString();
		}
		else if ("createTempFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var tempFile = %%elementName%%.createTempFile('myfile','.txt');\n");
			sb.append("application.output('Temporary file created as: ' + tempFile.getAbsolutePath());\n");
			sb.append("%%elementName%%.writeTXTFile(tempFile, 'abcdefg');\n");
			return sb.toString();
		}
		else if ("deleteFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("if (%%elementName%%.deleteFile('story.txt'))\n");
			sb.append("\tapplication.output('File deleted.');\n");
			return sb.toString();
		}
		else if ("deleteFolder".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("if (%%elementName%%.deleteFolder('stories', true))\n");
			sb.append("\tapplication.output('Folder deleted.');\n");
			return sb.toString();
		}
		else if ("getDesktopFolder".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var d = %%elementName%%.getDesktopFolder();\n");
			sb.append("application.output('desktop folder is: ' + d.getAbsolutePath());\n");
			return sb.toString();
		}
		else if ("getDiskList".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var roots = %%elementName%%.getDiskList();\n");
			sb.append("for (var i = 0; i < roots.length; i++)\n");
			sb.append("\tapplication.output(roots[i].getAbsolutePath());\n");
			return sb.toString();
		}
		else if ("getFileSize".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('story.txt');\n");
			sb.append("application.output('file size: ' + %%elementName%%.getFileSize(f));\n");
			return sb.toString();
		}
		else if ("getFolderContents".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var files = %%elementName%%.getFolderContents('stories', '.txt');\n");
			sb.append("for (var i=0; i<files.length; i++)\n");
			sb.append("\tapplication.output(files[i].getAbsolutePath());\n");
			return sb.toString();
		}
		else if ("getHomeDirectory".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var d = %%elementName%%.getHomeDirectory();\n");
			sb.append("application.output('home folder: ' + d.getAbsolutePath());\n");
			return sb.toString();
		}
		else if ("getModificationDate".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('story.txt');\n");
			sb.append("application.output('last changed: ' + %%elementName%%.getModificationDate(f));\n");
			return sb.toString();
		}
		else if ("moveFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Move file based on names.\n");
			sb.append("if (!%%elementName%%.moveFile('story.txt','story.txt.new'))\n");
			sb.append("\tapplication.output('File move failed.');\n");
			sb.append("// Move file based on JSFile instances.\n");
			sb.append("var f = %%elementName%%.convertToJSFile('story.txt.new');\n");
			sb.append("var fmoved = %%elementName%%.convertToJSFile('story.txt');\n");
			sb.append("if (!%%elementName%%.moveFile(f, fmoved))\n");
			sb.append("\tapplication.output('File move back failed.');\n");
			return sb.toString();
		}
		else if ("readFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Read all content from the file.\n");
			sb.append("var bytes = %%elementName%%.readFile('big.jpg');\n");
			sb.append("application.output('file size: ' + bytes.length);\n");
			sb.append("// Read only the first 1KB from the file.\n");
			sb.append("var bytesPartial = %%elementName%%.readFile('big.jpg', 1024);\n");
			sb.append("application.output('partial file size: ' + bytesPartial.length);\n");
			sb.append("// Read all content from a file selected from the file open dialog.\n");
			sb.append("var bytesUnknownFile = %%elementName%%.readFile();\n");
			sb.append("application.output('unknown file size: ' + bytesUnknownFile.length);\n");
			return sb.toString();
		}
		else if ("readTXTFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Read content from a known text file.\n");
			sb.append("var txt = %%elementName%%.readTXTFile('story.txt');\n");
			sb.append("application.output(txt);\n");
			sb.append("// Read content from a text file selected from the file open dialog.\n");
			sb.append("var txtUnknown = %%elementName%%.readTXTFile();\n");
			sb.append("application.output(txtUnknown);\n");
			return sb.toString();
		}
		else if ("showDirectorySelectDialog".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var dir = %%elementName%%.showDirectorySelectDialog();\n");
			sb.append("application.output(\"you've selected folder: \" + dir.getAbsolutePath());\n");
			return sb.toString();
		}
		else if ("showFileOpenDialog".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// This selects only files ('1'), previous dir must be used ('null'), no multiselect ('false') and\n");
			sb.append("// the filter \"JPG and GIF\" should be used: ('new Array(\"JPG and GIF\",\"jpg\",\"gif\")').\n");
			sb.append("var file = %%elementName%%.showFileOpenDialog(1, null, false, new Array(\"JPG and GIF\",\"jpg\",\"gif\"));\n");
			sb.append("application.output(\"you've selected file: \" + file.getAbsolutePath());\n");
			sb.append("//for the web you have to give a callback function that has a JSFile array as its first argument (also works in smart), other options can be set but are not used in the webclient (yet)\n");
			sb.append("var file = %%elementName%%.showFileOpenDialog(myCallbackMethod)\n");
			return sb.toString();
		}
		else if ("showFileSaveDialog".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var file = %%elementName%%.showFileSaveDialog();\n");
			sb.append("application.output(\"you've selected file: \" + file.getAbsolutePath());\n");
			return sb.toString();
		}
		else if ("writeFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var bytes = new Array();\n");
			sb.append("\tfor (var i=0; i<1024; i++)\n");
			sb.append("\t\tbytes[i] = i % 100;\n");
			sb.append("\tvar f = %%elementName%%.convertToJSFile('bin.dat');\n");
			sb.append("\tif (!%%elementName%%.writeFile(f, bytes))\n");
			sb.append("\t\tapplication.output('Failed to write the file.');\n");
			sb.append("\t// mimeType variable can be left null, and is used for webclient only. Specify one of any valid mime types as referenced here: http://www.w3schools.com/media/media_mimeref.asp'\n"); //$NON-NLS-1$
			sb.append("\tvar mimeType = 'application/vnd.ms-excel'\n"); //$NON-NLS-1$
			sb.append("\tif (!%%elementName%%.writeFile(f, bytes, mimeType))\n"); //$NON-NLS-1$
			sb.append("\t\tapplication.output('Failed to write the file.');\n");
			return sb.toString();
		}
		else if ("writeTXTFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var fileNameSuggestion = 'myspecialexport.tab'\n");
			sb.append("\tvar textData = 'load of data...'\n");
			sb.append("\tvar success = %%elementName%%.writeTXTFile(fileNameSuggestion, textData);\n");
			sb.append("\tif (!success) application.output('Could not write file.');\n");
			sb.append("\t// For file-encoding parameter options (default OS encoding is used), http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html\n");
			sb.append("\t// mimeType variable can be left null, and is used for webclient only. Specify one of any valid mime types as referenced here: http://www.w3schools.com/media/media_mimeref.asp'\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("writeXMLFile".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("var fileName = 'form.xml'\n");
			retval.append("var xml = controller.printXML()\n");
			retval.append("var success = %%elementName%%.writeXMLFile(fileName, xml);\n");
			retval.append("if (!success) application.output('Could not write file.');\n");
			return retval.toString();
		}
		else if ("streamFilesToServer".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// send one file:\n");
			sb.append("\tvar file = %%elementName%%.showFileOpenDialog( 1, null, false, null, null, 'Choose a file to transfer' );\n");
			sb.append("\tif (file) {\n");
			sb.append("\t\t%%elementName%%.streamFilesToServer( file, callbackFunction );\n");
			sb.append("\t}\n");
			sb.append("\t// send an array of files:\n");
			sb.append("\tvar folder = %%elementName%%.showDirectorySelectDialog();\n");
			sb.append("\tif (folder) {\n");
			sb.append("\t\tvar files = %%elementName%%.getFolderContents(folder);\n");
			sb.append("\t\tif (files) {\n");
			sb.append("\t\t\tvar monitor = %%elementName%%.streamFilesToServer( files, callbackFunction );\n");
			sb.append("\t\t}\n");
			sb.append("\t}\n");
			return sb.toString();
		}
		else if ("streamFilesFromServer".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// transfer all the files of a chosen server folder to a directory on the client\n");
			sb.append("\tvar dir = %%elementName%%.showDirectorySelectDialog();\n");
			sb.append("\tif (dir) {\n");
			sb.append("\t\tvar list = %%elementName%%.getRemoteFolderContents('/images/user1/', null, 1);\n");
			sb.append("\t\tif (list) {\n");
			sb.append("\t\t\tvar monitor = %%elementName%%.streamFilesFromServer(dir, list, callbackFunction);\n");
			sb.append("\t\t}\n");
			sb.append("\t}\n");
			return sb.toString();
		}
		else if ("getRemoteFolderContents".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// retrieves an array of files located on the server side inside the default upload folder:\n");
			sb.append("\tvar files = %%elementName%%.getRemoteFolderContents('/', '.txt');\n");
			return sb.toString();
		}
		else if ("convertToRemoteJSFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToRemoteJSFile('/story.txt');\n");
			sb.append("if (f && f.canRead())\n");
			sb.append("\tapplication.output('File can be read.');\n");
			return sb.toString();
		}
		else if ("appendToTXTFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// append some text to a text file:\n");
			sb.append("\tvar ok = %%elementName%%.appendToTXTFile('myTextFile.txt', '\\nMy fantastic new line of text\\n');\n");
			return sb.toString();
		}
		else if ("getDefaultUploadLocation".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// get the (server-side) default upload location path:\n");
			sb.append("\tvar serverPath = %%elementName%%.getDefaultUploadLocation();\n");
			return sb.toString();
		}
		return null;
	}

	@SuppressWarnings("nls")
	public String getToolTip(String methodName)
	{
		if ("convertToJSFile".equals(methodName))
		{
			return "Returns a JSFile instance corresponding to an alternative representation of a file (for example a string).";
		}
		else if ("copyFile".equals(methodName))
		{
			return "Copies the source file to the destination file. Returns true if the copy succeeds, false if any error occurs.";
		}
		else if ("copyFolder".equals(methodName))
		{
			return "Copies the sourcefolder to the destination folder, recursively. Returns true if the copy succeeds, false if any error occurs.";
		}
		else if ("createFile".equals(methodName))
		{
			return "Creates a JSFile instance. Does not create the file on disk.";
		}
		else if ("createFolder".equals(methodName))
		{
			return "Creates a folder on disk. Returns true if the folder is successfully created, false if any error occurs.";
		}
		else if ("createTempFile".equals(methodName))
		{
			return "Creates a temporary file on disk. A prefix and an extension are specified and they will be part of the file name.";
		}
		else if ("deleteFile".equals(methodName))
		{
			return "Removes a file from disk. Returns true on success, false otherwise.";
		}
		else if ("deleteFolder".equals(methodName))
		{
			return "Deletes a folder from disk recursively. Returns true on success, false otherwise. If the second parameter is set to true, then a warning will be issued to the user before actually removing the folder.";
		}
		else if ("getDesktopFolder".equals(methodName))
		{
			return "Returns a JSFile instance that corresponds to the Desktop folder of the currently logged in user.";
		}
		else if ("getDiskList".equals(methodName))
		{
			return "Returns an Array of JSFile instances correponding to the file system root folders.";
		}
		else if ("getFileSize".equals(methodName))
		{
			return "Returns the size of the specified file.";
		}
		else if ("getFolderContents".equals(methodName))
		{
			return "Returns an array of JSFile instances corresponding to content of the specified folder. The content can be filtered by optional name filter(s), by type, by visibility and by lock status.";
		}
		else if ("getHomeDirectory".equals(methodName))
		{
			return "Returns a JSFile instance corresponding to the home folder of the logged in used.";
		}
		else if ("getModificationDate".equals(methodName))
		{
			return "Returns the modification date of a file.";
		}
		else if ("moveFile".equals(methodName))
		{
			return "Moves the file from the source to the destination place. Returns true on success, false otherwise.";
		}
		else if ("readFile".equals(methodName))
		{
			return "Reads all or part of the content from a binary file. If a file name is not specified, then a file selection dialog pops up for selecting a file. (Web Enabled only for a JSFile argument)";
		}
		else if ("readTXTFile".equals(methodName))
		{
			return "Read all content from a text file. If a file name is not specified, then a file selection dialog pops up for selecting a file. The encoding can be also specified. (Web Enabled only for a JSFile argument)";
		}
		else if ("showDirectorySelectDialog".equals(methodName))
		{
			return "Shows a directory selector dialog.";
		}
		else if ("showFileOpenDialog".equals(methodName))
		{
			return "Shows a file open dialog. Filters can be applied on what type of files can be selected. (Web Enabled)";
		}
		else if ("showFileSaveDialog".equals(methodName))
		{
			return "Shows a file save dialog.";
		}
		else if ("writeFile".equals(methodName))
		{
			return "Writes data into a binary file. (Web Enabled: file parameter can be a string 'mypdffile.pdf' to hint the browser what it is, if it is a JSFile instance it will be saved on the server)";
		}
		else if ("writeTXTFile".equals(methodName))
		{
			return "Writes data into a text file. (Web Enabled: file parameter can be a string 'mytextfile.txt' to hint the browser what it is, if it is a JSFile instance it will be saved on the server)";
		}
		else if ("writeXMLFile".equals(methodName))
		{
			return "Writes data into an XML file. The file is saved with the encoding specified by the XML itself. (Web Enabled: file parameter can be a string 'myxmlfile.xml' to hint the browser what it is, if it is a JSFile instance it will be saved on the server)";
		}
		else if ("streamFilesToServer".equals(methodName))
		{
			return "Streams a file or an array of files to the server in a background task - with optional relative path(s)/(new) name(s). If provided, calls back a Servoy function when done for each file received with a JSFile and an exception if anything went wrong, returns a JSProgressMonitor object. Note: This only streams files for the smart client, in the webclient the streaming from the browser to the server is done by the browser";
		}
		else if ("streamFilesFromServer".equals(methodName))
		{
			return "Streams a file or an array of files from the server in a background task to a file (or files) on the client. If provided, calls back a Servoy function when done for each file received with a JSFile and an exception if anything went wrong, returns a JSProgressMonitor object. Note: This only streams files for the smart client, in the webclient the streaming from the server to the browser is done by the browser";
		}
		else if ("getRemoteFolderContents".equals(methodName))
		{
			return "Returns an array of JSFile instances corresponding to content of the specified folder on the server side. The content can be filtered by optional name filter(s), by type, by visibility and by lock status.";
		}
		else if ("convertToRemoteJSFile".equals(methodName))
		{
			return "Returns the JSFile object of a server file, given its path (relative the default server location).";
		}
		else if ("appendToTXTFile".equals(methodName))
		{
			return "Appends data into a text file.";
		}
		else if ("getDefaultUploadLocation".equals(methodName))
		{
			return "Returns the default upload location path of the server";
		}
		return null;
	}

	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("convertToJSFile".equals(methodName))
		{
			return new String[] { "file" };
		}
		else if ("copyFile".equals(methodName))
		{
			return new String[] { "sourceFile", "destinationFile" };
		}
		else if ("copyFolder".equals(methodName))
		{
			return new String[] { "sourceFolder", "destinationFolder" };
		}
		else if ("createFile".equals(methodName))
		{
			return new String[] { "targetFile" };
		}
		else if ("createFolder".equals(methodName))
		{
			return new String[] { "targetFolder" };
		}
		else if ("createTempFile".equals(methodName))
		{
			return new String[] { "filePrefix", "fileSuffix" };
		}
		else if ("deleteFile".equals(methodName))
		{
			return new String[] { "targetFile" };
		}
		else if ("deleteFolder".equals(methodName))
		{
			return new String[] { "targetFolder", "showWarning" };
		}
		else if ("getFileSize".equals(methodName))
		{
			return new String[] { "targetFile" };
		}
		else if ("getFolderContents".equals(methodName))
		{
			return new String[] { "targetFolder", "[fileFilter]", "[fileOption(1=files,2=dirs)]", "[visibleOption(1=visible,2=nonvisible)]", "[lockedOption(1=locked,2=nonlocked)]" };
		}
		else if ("getModificationDate".equals(methodName))
		{
			return new String[] { "targetFile" };
		}
		else if ("moveFile".equals(methodName))
		{
			return new String[] { "sourceFile", "destinationFile" };
		}
		else if ("readFile".equals(methodName))
		{
			return new String[] { "[file]", "[size]" };
		}
		else if ("readTXTFile".equals(methodName))
		{
			return new String[] { "[file]", "[charsetname]" };
		}
		else if ("showDirectorySelectDialog".equals(methodName))
		{
			return new String[] { "[directory suggestion]", "[dialog title text]" };
		}
		else if ("showFileOpenDialog".equals(methodName))
		{
			return new String[] { "[selectionMode(0=both,1=Files,2=Dirs)]", "[startDirectory(null=default/previous)]", "[multiselect(true/false)]", "[filterarray]", "[callbackmethod]", "[dialog title text]" };
		}
		else if ("showFileSaveDialog".equals(methodName))
		{
			return new String[] { "[fileName/dir suggestion]", "[dialog title text]" };
		}
		else if ("writeFile".equals(methodName))
		{
			return new String[] { "file", "binary_data", "[mimeType]" };
		}
		else if ("writeTXTFile".equals(methodName))
		{
			return new String[] { "file", "text_data", "[charsetname]", "[mimeType]" };
		}
		else if ("writeXMLFile".equals(methodName))
		{
			return new String[] { "file", "xml_data" };
		}
		else if ("streamFilesToServer".equals(methodName))
		{
			return new String[] { "file/fileName|fileArray/fileNameArray", "[serverFile/serverFileName|serverFileArray/serverFileNameArray]", "[callbackFunction]" };
		}
		else if ("streamFilesFromServer".equals(methodName))
		{
			return new String[] { "file/fileName|fileArray/fileNameArray", "serverFile/serverFileName|serverFileArray/serverFileNameArray", "[callbackFunction]" };
		}
		else if ("getRemoteFolderContents".equals(methodName))
		{
			return new String[] { "targetFolder", "[fileFilter]", "[fileOption(1=files,2=dirs)]", "[visibleOption(1=visible,2=nonvisible)]", "[lockedOption(1=locked,2=nonlocked)]" };
		}
		else if ("convertToRemoteJSFile".equals(methodName))
		{
			return new String[] { "serverPath" };
		}
		else if ("appendToTXTFile".equals(methodName))
		{
			return new String[] { "file/fileName", "text", "[encoding]" };
		}
		return null;
	}

	@SuppressWarnings("nls")
	public boolean isDeprecated(String methodName)
	{
		if (methodName.equals("convertStringToJSFile") || methodName.equals("getRemoteList"))
		{
			return true;
		}
		return false;
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { JSFile.class, JSProgressMonitor.class };
	}


	/**
	 * Appends a string given in parameter to a file, using default platform encoding
	 * @since Servoy 5.2
	 * 
	 * @param f either a {@link File}, a local {@link JSFile} or a the file path as a String
	 * @param text the text to append to the file
	 * @return true if appending worked
	 */
	public boolean js_appendToTXTFile(Object f, String text)
	{
		return js_appendToTXTFile(f, text, null);
	}

	/**
	 * Appends a string given in parameter to a file, using default platform encoding
	 * @since Servoy 5.2
	 * 
	 * @param f either a {@link File}, a local {@link JSFile} or a the file path as a String
	 * @param text the text to append to the file
	 * @param encoding the encoding to use
	 * @return true if appending worked
	 */
	@SuppressWarnings("nls")
	public boolean js_appendToTXTFile(Object f, String text, String encoding)
	{
		if (text != null)
		{
			try
			{
				final IClientPluginAccess access = plugin.getClientPluginAccess();
				File file = getFileFromArg(f, true);
				if (file == null)
				{
					file = FileChooserUtils.getAWriteFile(access.getCurrentWindow(), file, false);
				}
				FileOutputStream fos = new FileOutputStream(file, true);
				try
				{
					return writeToOutputStream(fos, text.replaceAll("\\n", LF), encoding);
				}
				finally
				{
					fos.close();
				}
			}
			catch (final Exception ex)
			{
				Debug.error(ex);
			}
		}
		return false;
	}

	/**
	 * Convenience return to get a JSFile representation of a server file based on its path<br/>
	 * @since Servoy 5.2
	 * 
	 * @param path the path representing a file on the server (should start with "/")
	 * @return the {@link JSFile}
	 */
	@SuppressWarnings("nls")
	public JSFile js_convertToRemoteJSFile(final String path)
	{
		if (path == null)
		{
			throw new IllegalArgumentException("Server path cannot be null");
		}
		if (path.charAt(0) != '/')
		{
			throw new IllegalArgumentException("Remote path should start with '/'");
		}
		try
		{
			final IFileService service = getFileService();
			final String clientId = plugin.getClientPluginAccess().getClientID();
			final RemoteFileData data = service.getRemoteFileData(clientId, path);
			return new JSFile(new RemoteFile(data, service, clientId));
		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}
		return null;
	}

	/**
	 * Retrieves an array of files/folders from the server
	 * @since Servoy 5.2.1
	 * 
	 * @param options the path (mandatory), an array of file extensions, fileOptions, visibleOption and lockedOption
	 * 
	 * @return the array of file names
	 */
	@SuppressWarnings("nls")
	public JSFile[] js_getRemoteFolderContents(final Object[] options)
	{
		Object path = options[0];
		if (path == null) return EMPTY;

		final String[] fileFilter;
		final int filesOption; // null/0 = files and dirs, 1 = files, 2 = dirs.
		final int visibleOption;// null/0 = visible and non, 1 = visible, 2 = non.
		final int lockedOption; // null/0 = locked and non, 1 = locked, 2 = non.

		if (options.length > 1 && options[1] != null)
		{
			if (options[1].getClass().isArray())
			{
				Object[] tmp = (Object[])options[1];
				fileFilter = new String[tmp.length];
				for (int i = 0; i < tmp.length; i++)
				{
					fileFilter[i] = ((String)tmp[i]).toLowerCase();
				}
			}
			else
			{
				fileFilter = new String[] { ((String)options[1]).toLowerCase() };
			}
		}
		else fileFilter = null;
		if (options.length > 2) filesOption = Utils.getAsInteger(options[2]);
		else filesOption = AbstractFile.ALL;
		if (options.length > 3) visibleOption = Utils.getAsInteger(options[3]);
		else visibleOption = AbstractFile.ALL;
		if (options.length > 4) lockedOption = Utils.getAsInteger(options[4]);
		else lockedOption = AbstractFile.ALL;

		String serverFileName = null;
		if (path instanceof JSFile)
		{
			IAbstractFile abstractFile = ((JSFile)path).getAbstractFile();
			if (abstractFile instanceof RemoteFile)
			{
				serverFileName = ((RemoteFile)abstractFile).getAbsolutePath();
			}
			else
			{
				throw new IllegalArgumentException("Local file path doesn't make sense for the getRemoteDirList method");
			}
		}
		else
		{
			serverFileName = path.toString();
		}
		try
		{
			final IFileService service = getFileService();
			final String clientId = plugin.getClientPluginAccess().getClientID();
			final RemoteFileData[] remoteList = service.getRemoteFolderContent(clientId, serverFileName, fileFilter, filesOption, visibleOption, lockedOption);
			if (remoteList != null)
			{
				final JSFile[] files = new JSFile[remoteList.length];
				for (int i = 0; i < files.length; i++)
				{
					files[i] = new JSFile(new RemoteFile(remoteList[i], service, clientId));
				}
				return files;
			}
			else
			{
				return EMPTY;
			}

		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}

		return null;
	}

	/**
	 * Retrieves an array of files/folders from the server
	 * @since Servoy 5.2
	 * 
	 * @param serverPath the path of a remote directory (relative to the defaultFolder)
	 * 
	 * @return the array of file names
	 * @deprecated
	 */
	@Deprecated
	public JSFile[] js_getRemoteList(final Object serverPath)
	{
		return js_getRemoteList(serverPath, false);
	}

	/**
	 * Retrieves an array of files/folders from the server
	 * @since Servoy 5.2
	 * 
	 * @param serverPath a {@link JSFile} or String with the path of a remote directory (relative to the defaultFolder)
	 * @param filesOnly if true only files will be retrieve, if false, files and folders will be retrieved
	 * 
	 * @return the array of file names
	 * @deprecated
	 */
	@Deprecated
	public JSFile[] js_getRemoteList(final Object serverPath, final boolean filesOnly)
	{
		final int fileOption = (filesOnly) ? AbstractFile.FILES : AbstractFile.ALL;
		return js_getRemoteFolderContents(new Object[] { serverPath, null, new Integer(fileOption) });
	}

	/**
	 * Overloaded method, only defines file(s) to be streamed
	 * @since Servoy 5.2
	 * 
	 * @param f file(s) to be streamed (can be a String path, a {@link File} or a {@link JSFile}) or an Array of these
	 * @return a {@link JSProgressMonitor} object to allow client to subscribe to progress notifications
	 */
	public JSProgressMonitor js_streamFilesToServer(final Object f)
	{
		return js_streamFilesToServer(f, null, null);
	}

	/**
	 * Overloaded method, defines file(s) to be streamed and a callback function
	 * @since Servoy 5.2
	 * 
	 * @param f file(s) to be streamed (can be a String path, a {@link File} or a {@link JSFile}) or an Array of these
	 * @param o can be a JSFile or JSFile[], a String or String[] or the {@link Function} to be called back at the end of the process
	 * @return a {@link JSProgressMonitor} object to allow client to subscribe to progress notifications
	 */
	public JSProgressMonitor js_streamFilesToServer(final Object f, final Object o)
	{
		if (o instanceof Function)
		{
			return js_streamFilesToServer(f, null, (Function)o);
		}
		else
		{
			return js_streamFilesToServer(f, o, null);
		}
	}

	/**
	 * Overloaded method, defines file(s) to be streamed, a callback function and file name(s) to use on the server
	 * @since Servoy 5.2
	 * 
	 * @param f file(s) to be streamed (can be a String path, a {@link File} or a {@link JSFile}) or an Array of these
	 * @param s can be a JSFile or JSFile[], a String or String[]
	 * @param callback the {@link Function} to be called back at the end of the process
	 * @return a {@link JSProgressMonitor} object to allow client to subscribe to progress notifications
	 */
	public JSProgressMonitor js_streamFilesToServer(final Object f, final Object s, final Function callback)
	{
		if (f != null)
		{
			final Object[] fileObjects = unwrap(f);
			final Object[] serverFiles = unwrap(s);
			if (fileObjects != null)
			{
				// the FunctionDefinition is only created once for all files:
				final FunctionDefinition function = (callback == null) ? null : new FunctionDefinition(callback);
				final File[] files = new File[fileObjects.length];
				long totalBytes = 0;
				for (int i = 0; i < fileObjects.length; i++)
				{
					final File file = getFileFromArg(fileObjects[i], true);
					if (file != null && file.canRead())
					{
						totalBytes += file.length();
						files[i] = file;
					}
					else if (fileObjects[i] instanceof JSFile)
					{
						IAbstractFile af = ((JSFile)fileObjects[i]).getAbstractFile();
						if (af instanceof UploadData)
						{
							throw new RuntimeException(
								"Using streamFilesToServer with an uploadData in the web client makes no sense since the process is already on the server-side, consider using writeFile(), writeTXTFile() or writeXMLFile() instead!"); //$NON-NLS-1$
						}
						files[i] = null;
					}
				}
				final JSProgressMonitor progressMonitor = new JSProgressMonitor(this, totalBytes, files.length);
				try
				{
					final IFileService service = getFileService();
					plugin.getClientPluginAccess().getExecutor().execute(new ToServerWorker(files, serverFiles, function, progressMonitor, service));

					return progressMonitor;
				}
				catch (Exception ex)
				{
					Debug.error(ex);
				}

			}
		}
		return null;
	}

	/**
	 * Stream 1 or more file from the server to the client
	 * @since Servoy 5.2
	 * 
	 * @param f file(s) to be streamed into (can be a String path, a {@link File} or a {@link JSFile}) or an Array of these
	 * @param s of the files on the server that will be transfered to the client, can be a String or a String[]
	 * @return a {@link JSProgressMonitor} object to allow client to subscribe to progress notifications
	 */
	public JSProgressMonitor js_streamFilesFromServer(final Object f, final Object s)
	{
		return js_streamFilesFromServer(f, s, null);
	}

	/**
	 * Stream 1 or more files from the server to the client, the callback method is invoked after every file, with as argument
	 * the filename that was transfered. An extra second exception parameter can be given if an exception did occur.
	 * @since Servoy 5.2
	 * 
	 * @param f file(s) to be streamed into (can be a String path, a {@link File} or a {@link JSFile}) or an Array of these
	 * @param s the files on the server that will be transfered to the client, can be a JSFile or JSFile[], a String or String[]
	 * @param callback the {@link Function} to be called back at the end of the process (after every file)
	 * @return a {@link JSProgressMonitor} object to allow client to subscribe to progress notifications
	 */
	@SuppressWarnings("nls")
	public JSProgressMonitor js_streamFilesFromServer(final Object f, final Object s, final Function callback)
	{
		if (f != null && s != null)
		{
			final Object[] fileObjects = unwrap(f);
			final Object[] serverObjects = unwrap(s);
			if (fileObjects != null && serverObjects != null)
			{
				final File firstFile = getFileFromArg(fileObjects[0], true);
				if (fileObjects.length != serverObjects.length)
				{
					// we may have a folder, but then it must be a single argument:
					if (fileObjects.length == 1)
					{
						if (!firstFile.isDirectory())
						{
							throw new IllegalArgumentException(
								"The first argument must represent an existing folder or an array of files to receive the server files.");
						}
					}
					else
					{
						throw new IllegalArgumentException("The number of files on the client side and on the server side don't match.");
					}
				}
				// the FunctionDefinition is only created once for all files:
				final FunctionDefinition function = (callback == null) ? null : new FunctionDefinition(callback);
				final File[] files = new File[serverObjects.length];
				long totalBytes = 0;

				try
				{
					final IFileService service = getFileService();
					final String clientId = plugin.getClientPluginAccess().getClientID();
					final RemoteFile[] remoteFiles = new RemoteFile[serverObjects.length];
					if (serverObjects instanceof JSFile[])
					{
						for (int i = 0; i < serverObjects.length; i++)
						{
							Object serverFile = serverObjects[i];
							if (serverFile != null)
							{
								IAbstractFile abstractFile = ((JSFile)serverFile).getAbstractFile();
								if (abstractFile instanceof RemoteFile)
								{
									remoteFiles[i] = (RemoteFile)abstractFile;
								}
								else
								{
									throw new IllegalArgumentException("Wrong file type provided: the JSFile to transfer must be a remote file!");
								}
							}
							if (remoteFiles[i] != null)
							{
								totalBytes += remoteFiles[i].size();
								// we can have a related local file, else a folder has been provided, thus we create a related local file to receive the transfer:
								files[i] = (i < fileObjects.length && !firstFile.isDirectory()) ? getFileFromArg(fileObjects[i], true) : new File(firstFile,
									remoteFiles[i].getName());
							}
						}
					}
					else
					{
						String[] serverFileNames = new String[serverObjects.length];
						for (int i = 0; i < serverObjects.length; i++)
						{
							serverFileNames[i] = serverObjects[i].toString();
						}
						RemoteFileData[] datas = service.getRemoteFileData(clientId, serverFileNames);
						for (int i = 0; i < datas.length; i++)
						{
							remoteFiles[i] = new RemoteFile(datas[i], service, clientId);
							totalBytes += datas[i].size();
							files[i] = (i < fileObjects.length && !firstFile.isDirectory()) ? getFileFromArg(fileObjects[i], true) : new File(firstFile,
								remoteFiles[i].getName());
						}
					}
					JSProgressMonitor progressMonitor = new JSProgressMonitor(this, totalBytes, remoteFiles.length);
					plugin.getClientPluginAccess().getExecutor().execute(new FromServerWorker(files, remoteFiles, function, progressMonitor, service));
					return progressMonitor;
				}
				catch (Exception ex)
				{
					Debug.error(ex);
				}
			}
		}
		return null;
	}

	/**
	 * Utility method to give access to the {@link IFileService} remote service
	 * @since Servoy 5.2
	 * 
	 * @return the service
	 */
	protected IFileService getFileService() throws Exception
	{
		return (IFileService)plugin.getClientPluginAccess().getServerService(IFileService.SERVICE_NAME);
	}

	/**
	 * Utility method to unwrap a given object to Object[] array
	 * @since Servoy 5.2
	 * 
	 * @param f The object to unwrap
	 * @return The Object[] array
	 */
	protected Object[] unwrap(Object f)
	{
		Object[] files = null;
		if (f != null)
		{
			if (f instanceof NativeArray)
			{
				files = (Object[])((NativeArray)f).unwrap();
			}
			else if (f instanceof Object[])
			{
				return (Object[])f;
			}
			else
			{
				files = new Object[] { f };
			}
		}
		return files;

	}

	/**
	 * Schedule a JSProgressMonitor to be run at fixed interval
	 * 
	 * @param monitor the {@link JSProgressMonitor} to schedule
	 * @param interval the interval (in seconds) to run the callback
	 */
	public void scheduleMonitor(final JSProgressMonitor monitor, final float interval)
	{
		long delay = Math.round(interval * 1000);
		timer.scheduleAtFixedRate(monitor, 0L, delay);
	}


	/**
	 * Callback a Servoy {@link Function} passing a JSProgressMonitor
	 * 
	 * @param monitor the {@link JSProgressMonitor} to return to the client
	 * @param function the client {@link FunctionDefinition} of a Servoy {@link Function} to callback
	 */
	public void callbackProgress(final JSProgressMonitor monitor, final FunctionDefinition function)
	{
		if (function != null)
		{
			function.execute(plugin.getClientPluginAccess(), new Object[] { monitor }, true);
		}
	}

	/**
	 * Retrieves the server default upload location on the server
	 * @return the location as canonical path
	 */
	public String js_getDefaultUploadLocation()
	{
		try
		{
			final IFileService service = getFileService();
			return service.getDefaultFolderLocation();
		}
		catch (final Exception ex)
		{
			Debug.error(ex);
		}
		return null;
	}


	private final class FromServerWorker implements Runnable
	{

		private final File[] files;
		private final RemoteFile[] remoteFiles;
		private final FunctionDefinition function;
		private final JSProgressMonitor progressMonitor;
		private final IFileService service;

		/**
		 * @param files
		 * @param remoteFiles
		 * @param function
		 * @param progressMonitor
		 * @param service
		 */
		public FromServerWorker(final File[] files, RemoteFile[] remoteFiles, final FunctionDefinition function, final JSProgressMonitor progressMonitor,
			final IFileService service)
		{
			this.files = files;
			this.remoteFiles = remoteFiles;
			this.function = function;
			this.progressMonitor = progressMonitor;
			this.service = service;
		}

		public void run()
		{
			try
			{
				long totalTransfered = 0L;
				final String clientId = plugin.getClientPluginAccess().getClientID();
				for (int i = 0; i < files.length; i++)
				{
					UUID uuid = null;
					OutputStream os = null;
					Exception ex = null;
					File file = files[i];
					if (file != null)
					{
						RemoteFile remote = remoteFiles[i];
						try
						{
							if (file.exists() || file.createNewFile())
							{
								long currentTransferred = 0L;
								progressMonitor.setCurrentFileName(remote.getAbsolutePath());
								progressMonitor.setCurrentBytes(remote.size());
								progressMonitor.setCurrentFileIndex(i + 1);
								progressMonitor.setCurrentTransferred(0L);

								os = new FileOutputStream(file);
								uuid = service.openTransfer(clientId, remote.getAbsolutePath());
								if (uuid != null)
								{
									byte[] bytes = service.readBytes(uuid, CHUNK_BUFFER_SIZE);
									while (bytes != null && !progressMonitor.js_isCanceled())
									{
										os.write(bytes);
										totalTransfered += bytes.length;
										currentTransferred += bytes.length;
										progressMonitor.setTotalTransferred(totalTransfered);
										progressMonitor.setCurrentTransferred(currentTransferred);
										if (progressMonitor.getDelay() > 0)
										{
											Thread.sleep(progressMonitor.getDelay()); // to test the process
										}
										// check for the length (this results in 1 less call to the server)
										if (bytes.length == CHUNK_BUFFER_SIZE)
										{
											bytes = service.readBytes(uuid, CHUNK_BUFFER_SIZE);
										}
										else break;
									}
								}
							}
						}
						catch (final Exception e)
						{
							Debug.error(e);
							ex = e;
						}
						finally
						{
							try
							{
								if (uuid != null) service.closeTransfer(uuid);
							}
							catch (final RemoteException ignore)
							{
							}
							try
							{
								if (os != null) os.close();
							}
							catch (final IOException ignore)
							{
							}
							if (function != null && !progressMonitor.js_isCanceled())
							{
								function.execute(plugin.getClientPluginAccess(), new Object[] { new JSFile(file), ex }, true);
							}
							if (progressMonitor.js_isCanceled())
							{
								file.delete();
								progressMonitor.run();
								break;
							}
						}
					}
				}
			}
			finally
			{
				if (!progressMonitor.js_isCanceled())
				{
					progressMonitor.setFinished(true);
					progressMonitor.run();
				}
				progressMonitor.cancel(); // stops the TimerTask
			}
		}
	}

	private final class ToServerWorker implements Runnable
	{

		private final File[] files;
		private final Object[] serverFiles;
		private final FunctionDefinition function;
		private final JSProgressMonitor progressMonitor;
		private final IFileService service;

		/**
		 * @param files
		 * @param serverFiles
		 * @param function
		 * @param progressMonitor
		 * @param service
		 */
		public ToServerWorker(final File[] files, final Object[] serverFiles, final FunctionDefinition function, final JSProgressMonitor progressMonitor,
			final IFileService service)
		{
			this.files = files;
			this.serverFiles = serverFiles;
			this.function = function;
			this.progressMonitor = progressMonitor;
			this.service = service;
		}

		public void run()
		{
			try
			{
				long totalTransfered = 0L;
				for (int i = 0; i < files.length; i++)
				{
					final File file = files[i];
					if (file != null)
					{
						// the serverName can be derived from an Array of String, at the same index as the file
						String serverFileName = null;
						if (serverFiles != null && i < serverFiles.length)
						{
							if (serverFiles[i] instanceof JSFile)
							{
								JSFile jsFile = (JSFile)serverFiles[i];
								IAbstractFile abstractFile = jsFile.getAbstractFile();
								if (abstractFile instanceof RemoteFile)
								{
									serverFileName = ((RemoteFile)abstractFile).getAbsolutePath();
								}
								else
								{
									serverFileName = abstractFile.getName();
								}
							}
							else
							{
								serverFileName = serverFiles[i].toString();
							}
						}
						else
						{
							serverFileName = "/" + file.getName(); //$NON-NLS-1$
						}

						long currentTransferred = 0L;
						progressMonitor.setCurrentFileName(file.getAbsolutePath());
						progressMonitor.setCurrentBytes(file.length());
						progressMonitor.setCurrentFileIndex(i + 1);
						progressMonitor.setCurrentTransferred(0L);

						final String clientId = plugin.getClientPluginAccess().getClientID();
						UUID uuid = null;
						RemoteFileData remoteFile = null;
						InputStream is = null;
						Exception ex = null;
						try
						{
							is = new FileInputStream(file);
							uuid = service.openTransfer(clientId, serverFileName);
							if (uuid != null)
							{
								byte[] buffer = new byte[CHUNK_BUFFER_SIZE];
								int read = is.read(buffer);
								while (read > -1 && !progressMonitor.js_isCanceled())
								{
									service.writeBytes(uuid, buffer, 0, read);
									totalTransfered += read;
									currentTransferred += read;
									progressMonitor.setTotalTransferred(totalTransfered);
									progressMonitor.setCurrentTransferred(currentTransferred);

									if (progressMonitor.getDelay() > 0)
									{
										Thread.sleep(progressMonitor.getDelay()); // to test the process
									}

									read = is.read(buffer);
								}
							}
						}
						catch (final Exception e)
						{
							Debug.error(e);
							ex = e;
						}
						finally
						{
							try
							{
								if (uuid != null)
								{
									remoteFile = (RemoteFileData)service.closeTransfer(uuid);
									if (remoteFile != null) remoteFile.refreshSize();
								}
							}
							catch (final RemoteException ignore)
							{
							}
							try
							{
								if (is != null) is.close();
							}
							catch (final IOException ignore)
							{
							}
							if (function != null && !progressMonitor.js_isCanceled())
							{
								final JSFile returnedFile = (remoteFile == null) ? null : new JSFile(new RemoteFile(remoteFile, service, clientId));
								function.execute(plugin.getClientPluginAccess(), new Object[] { returnedFile, ex }, true);
							}
							if (progressMonitor.js_isCanceled())
							{
								try
								{
									service.delete(clientId, remoteFile.getAbsolutePath());
									progressMonitor.run();
									break;
								}
								catch (final IOException ignore)
								{
								}
							}
						}
					}
				}
			}
			finally
			{
				if (!progressMonitor.js_isCanceled())
				{
					progressMonitor.setFinished(true);
					progressMonitor.run();
				}
				progressMonitor.cancel(); // stops the TimerTask
			}
		}
	}

}
