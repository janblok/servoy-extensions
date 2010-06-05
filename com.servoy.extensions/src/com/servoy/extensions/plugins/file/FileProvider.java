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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import org.mozilla.javascript.Function;

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
 */
public class FileProvider implements IScriptObject
{
	protected final FilePlugin plugin;
	private final FileSystemView fsf = FileSystemView.getFileSystemView();
	private final Map<String, File> tempFiles = new HashMap<String, File>(); //check this map when saving (txt/binary) files
	private static final JSFile[] EMPTY = new JSFile[0];

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
					functionDef.executeSync(plugin.getClientPluginAccess(), new Object[] { files });
				}

				public void onSubmit()
				{
					// submit without uploaded files 
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
		else filesOption = 0;
		if (options.length > 3) visibleOption = Utils.getAsInteger(options[3]);
		else visibleOption = 0;
		if (options.length > 4) lockedOption = Utils.getAsInteger(options[4]);
		else lockedOption = 0;

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
				if (filesOption == 1)
				{
					retVal = pathname.isFile();
				}
				else if (filesOption == 2)
				{
					retVal = pathname.isDirectory();
				}
				if (!retVal) return false;

				boolean hidden = pathname.isHidden();
				if (visibleOption == 1) retVal = !hidden;
				else if (visibleOption == 2) retVal = hidden;
				if (!retVal) return false;

				boolean canWrite = pathname.canWrite();
				if (lockedOption == 1) retVal = !canWrite;
				else if (lockedOption == 2) retVal = canWrite;
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

	public JSFile js_createTempFile(String prefix, String suffix)
	{
		try
		{
			// If shorter than three, then pad with something, so that we don't get exception.
			if (prefix.length() < 3) prefix += "svy"; //$NON-NLS-1$
			File f = File.createTempFile(prefix, suffix);
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
		if (data == null) data = "";
		return writeTXT(f, data, encoding, "text/plain");
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
				return false;//unknown enc.
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
		if ("convertToJSFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = plugins.file.convertToJSFile(\"story.txt\");\n"); //$NON-NLS-1$
			sb.append("if (f.canRead())\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(\"File can be read.\");\n"); //$NON-NLS-1$
			return sb.toString();

		}
		else if ("copyFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Copy based on file names.\n"); //$NON-NLS-1$
			sb.append("if (!plugins.file.copyFile(\"story.txt\", \"story.txt.copy\"))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(\"Copy failed.\");\n"); //$NON-NLS-1$
			sb.append("// Copy based on JSFile instances.\n"); //$NON-NLS-1$
			sb.append("var f = plugins.file.createFile(\"story.txt\");\n"); //$NON-NLS-1$
			sb.append("var fcopy = plugins.file.createFile(\"story.txt.copy2\");\n"); //$NON-NLS-1$
			sb.append("if (!plugins.file.copyFile(f, fcopy))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(\"Copy failed.\");\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("copyFolder".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Copy folder based on names.\n"); //$NON-NLS-1$
			sb.append("if (!plugins.file.copyFolder(\"stories\", \"stories_copy\"))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(\"Folder copy failed.\");\n"); //$NON-NLS-1$
			sb.append("// Copy folder based on JSFile instances.\n"); //$NON-NLS-1$
			sb.append("var d = plugins.file.createFile(\"stories\");\n"); //$NON-NLS-1$
			sb.append("var dcopy = plugins.file.createFile(\"stories_copy_2\");\n"); //$NON-NLS-1$
			sb.append("if (!plugins.file.copyFolder(d, dcopy))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(\"Folder copy failed.\");\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("createFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Create the JSFile instance based on the file name.\n"); //$NON-NLS-1$
			sb.append("var f = plugins.file.createFile(\"newfile.txt\");\n"); //$NON-NLS-1$
			sb.append("// Create the file on disk.\n"); //$NON-NLS-1$
			sb.append("if (!f.createNewFile())\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(\"The file could not be created.\");\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("createFolder".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var d = plugins.file.convertToJSFile(\"newfolder\");\n"); //$NON-NLS-1$
			sb.append("if (!plugins.file.createFolder(d))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(\"Folder could not be created.\");\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("createTempFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var tempFile = plugins.file.createTempFile('myfile','.txt');\n"); //$NON-NLS-1$
			sb.append("application.output('Temporary file created as: ' + tempFile.getAbsolutePath());\n"); //$NON-NLS-1$
			sb.append("plugins.file.writeTXTFile(tempFile, 'abcdefg');\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("deleteFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("if (plugins.file.deleteFile('story.txt'))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output('File deleted.');\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("deleteFolder".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("if (plugins.file.deleteFolder('stories', true))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output('Folder deleted.');\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("getDesktopFolder".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var d = plugins.file.getDesktopFolder();\n"); //$NON-NLS-1$
			sb.append("application.output('desktop folder is: ' + d.getAbsolutePath());\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("getDiskList".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var roots = plugins.file.getDiskList();\n"); //$NON-NLS-1$
			sb.append("for (var i = 0; i < roots.length; i++)\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(roots[i].getAbsolutePath());\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("getFileSize".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = plugins.file.convertToJSFile('story.txt');\n"); //$NON-NLS-1$
			sb.append("application.output('file size: ' + plugins.file.getFileSize(f));\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("getFolderContents".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var files = plugins.file.getFolderContents('stories', '.txt');\n"); //$NON-NLS-1$
			sb.append("for (var i=0; i<files.length; i++)\n"); //$NON-NLS-1$
			sb.append("\tapplication.output(files[i].getAbsolutePath());\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("getHomeDirectory".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var d = plugins.file.getHomeDirectory();\n"); //$NON-NLS-1$
			sb.append("application.output('home folder: ' + d.getAbsolutePath());\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("getModificationDate".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = plugins.file.convertToJSFile('story.txt');\n"); //$NON-NLS-1$
			sb.append("application.output('last changed: ' + plugins.file.getModificationDate(f));\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("moveFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Move file based on names.\n"); //$NON-NLS-1$
			sb.append("if (!plugins.file.moveFile('story.txt','story.txt.new'))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output('File move failed.');\n"); //$NON-NLS-1$
			sb.append("// Move file based on JSFile instances.\n"); //$NON-NLS-1$
			sb.append("var f = plugins.file.convertToJSFile('story.txt.new');\n"); //$NON-NLS-1$
			sb.append("var fmoved = plugins.file.convertToJSFile('story.txt');\n"); //$NON-NLS-1$
			sb.append("if (!plugins.file.moveFile(f, fmoved))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output('File move back failed.');\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("readFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Read all content from the file.\n"); //$NON-NLS-1$
			sb.append("var bytes = plugins.file.readFile('big.jpg');\n"); //$NON-NLS-1$
			sb.append("application.output('file size: ' + bytes.length);\n"); //$NON-NLS-1$
			sb.append("// Read only the first 1KB from the file.\n"); //$NON-NLS-1$
			sb.append("var bytesPartial = plugins.file.readFile('big.jpg', 1024);\n"); //$NON-NLS-1$
			sb.append("application.output('partial file size: ' + bytesPartial.length);\n"); //$NON-NLS-1$
			sb.append("// Read all content from a file selected from the file open dialog.\n"); //$NON-NLS-1$
			sb.append("var bytesUnknownFile = plugins.file.readFile();\n"); //$NON-NLS-1$
			sb.append("application.output('unknown file size: ' + bytesUnknownFile.length);\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("readTXTFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Read content from a known text file.\n"); //$NON-NLS-1$
			sb.append("var txt = plugins.file.readTXTFile('story.txt');\n"); //$NON-NLS-1$
			sb.append("application.output(txt);\n"); //$NON-NLS-1$
			sb.append("// Read content from a text file selected from the file open dialog.\n"); //$NON-NLS-1$
			sb.append("var txtUnknown = plugins.file.readTXTFile();\n"); //$NON-NLS-1$
			sb.append("application.output(txtUnknown);\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("showDirectorySelectDialog".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var dir = plugins.file.showDirectorySelectDialog();\n"); //$NON-NLS-1$
			sb.append("application.output(\"you've selected folder: \" + dir.getAbsolutePath());\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("showFileOpenDialog".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// This selects only files ('1'), previous dir must be used ('null'), no multiselect ('false') and\n"); //$NON-NLS-1$
			sb.append("// the filter \"JPG and GIF\" should be used: ('new Array(\"JPG and GIF\",\"jpg\",\"gif\")').\n"); //$NON-NLS-1$
			sb.append("var file = plugins.file.showFileOpenDialog(1, null, false, new Array(\"JPG and GIF\",\"jpg\",\"gif\"));\n"); //$NON-NLS-1$
			sb.append("application.output(\"you've selected file: \" + file.getAbsolutePath());\n"); //$NON-NLS-1$
			sb.append("//for the web you have to give a callback function that has a JSFile array as its first argument (also works in smart), other options can be set but are not used in the webclient (yet)\n"); //$NON-NLS-1$
			sb.append("var file = plugins.file.showFileOpenDialog(myCallbackMethod)\n");
			return sb.toString();
		}
		else if ("showFileSaveDialog".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var file = plugins.file.showFileSaveDialog();\n"); //$NON-NLS-1$
			sb.append("application.output(\"you've selected file: \" + file.getAbsolutePath());\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("writeFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var bytes = new Array();\n"); //$NON-NLS-1$
			sb.append("for (var i=0; i<1024; i++)\n"); //$NON-NLS-1$
			sb.append("\tbytes[i] = i % 100;\n"); //$NON-NLS-1$
			sb.append("var f = plugins.file.convertToJSFile('bin.dat');\n"); //$NON-NLS-1$
			sb.append("if (!plugins.file.writeFile(f, bytes))\n"); //$NON-NLS-1$
			sb.append("\tapplication.output('Failed to write the file.');\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("writeTXTFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var fileNameSuggestion = 'myspecialexport.tab'\n"); //$NON-NLS-1$
			sb.append("var textData = 'load of data...'\n"); //$NON-NLS-1$
			sb.append("var success = plugins.file.writeTXTFile(fileNameSuggestion, textData);\n"); //$NON-NLS-1$
			sb.append("if (!success) application.output('Could not write file.');\n"); //$NON-NLS-1$
			sb.append("// For file-encoding parameter options (default OS encoding is used), http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("writeXMLFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("var fileName = 'form.xml'\n"); //$NON-NLS-1$
			retval.append("var xml = controller.printXML()\n"); //$NON-NLS-1$
			retval.append("var success = plugins.file.writeXMLFile(fileName, xml);\n"); //$NON-NLS-1$
			retval.append("if (!success) application.output('Could not write file.');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("convertToJSFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns a JSFile instance corresponding to an alternative representation of a file (for example a string)."; //$NON-NLS-1$
		}
		else if ("copyFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Copies the sourcefile to the destination file. Returns true if the copy succeeds, false if any error occurs."; //$NON-NLS-1$
		}
		else if ("copyFolder".equals(methodName)) //$NON-NLS-1$
		{
			return "Copies the sourcefolder to the destination folder, recursively. Returns true if the copy succeeds, false if any error occurs."; //$NON-NLS-1$
		}
		else if ("createFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Creates a JSFile instance. Does not create the file on disk."; //$NON-NLS-1$
		}
		else if ("createFolder".equals(methodName)) //$NON-NLS-1$
		{
			return "Creates a folder on disk. Returns true if the folder is successfully created, false if any error occurs."; //$NON-NLS-1$
		}
		else if ("createTempFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Creates a temporary file on disk. A prefix and an extension are specified and they will be part of the file name."; //$NON-NLS-1$
		}
		else if ("deleteFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Removes a file from disk. Returns true on success, false otherwise."; //$NON-NLS-1$
		}
		else if ("deleteFolder".equals(methodName)) //$NON-NLS-1$
		{
			return "Deletes a folder from disk recursively. Returns true on success, false otherwise. If the second parameter is set to true, then a warning will be issued to the user before actually removing the folder."; //$NON-NLS-1$
		}
		else if ("getDesktopFolder".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns a JSFile instance that corresponds to the Desktop folder of the currently logged in user."; //$NON-NLS-1$
		}
		else if ("getDiskList".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns an Array of JSFile instances correponding to the file system root folders."; //$NON-NLS-1$
		}
		else if ("getFileSize".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns the size of the specified file."; //$NON-NLS-1$
		}
		else if ("getFolderContents".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns an array of JSFile instances corresponding to content of the specified folder. The content can be filtered by optional name filter(s), by type, by visibility and by lock status."; //$NON-NLS-1$
		}
		else if ("getHomeDirectory".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns a JSFile instance corresponding to the home folder of the logged in used."; //$NON-NLS-1$
		}
		else if ("getModificationDate".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns the modification date of a file."; //$NON-NLS-1$
		}
		else if ("moveFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Moves the file from the source to the destination place. Returns true on success, false otherwise."; //$NON-NLS-1$
		}
		else if ("readFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Reads all or part of the content from a binary file. If a file name is not specified, then a file selection dialog pops up for selecting a file. (Web Enabled)"; //$NON-NLS-1$
		}
		else if ("readTXTFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Read all content from a text file. If a file name is not specified, then a file selection dialog pops up for selecting a file. The encoding can be also specified. (Web Enabled)"; //$NON-NLS-1$
		}
		else if ("showDirectorySelectDialog".equals(methodName)) //$NON-NLS-1$
		{
			return "Shows a directory selector dialog."; //$NON-NLS-1$
		}
		else if ("showFileOpenDialog".equals(methodName)) //$NON-NLS-1$
		{
			return "Shows a file open dialog. Filters can be applied on what type of files can be selected. (Web Enabled)"; //$NON-NLS-1$
		}
		else if ("showFileSaveDialog".equals(methodName)) //$NON-NLS-1$
		{
			return "Shows a file save dialog."; //$NON-NLS-1$
		}
		else if ("writeFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Writes data into a binary file. (Web Enabled)"; //$NON-NLS-1$
		}
		else if ("writeTXTFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Writes data into a text file. (Web Enabled)"; //$NON-NLS-1$
		}
		else if ("writeXMLFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Writes data into an XML file. The file is saved with the encoding specified by the XML itself. (Web Enabled)"; //$NON-NLS-1$
		}
		return null;
	}

	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("convertToJSFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "file" }; //$NON-NLS-1$
		}
		else if ("copyFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "sourceFile", "destinationFile" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if ("copyFolder".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "sourceFolder", "destinationFolder" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if ("createFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "targetFile" }; //$NON-NLS-1$
		}
		else if ("createFolder".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "targetFolder" }; //$NON-NLS-1$
		}
		else if ("createTempFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "filePrefix", "fileSuffix" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if ("deleteFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "targetFile" }; //$NON-NLS-1$
		}
		else if ("deleteFolder".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "targetFolder", "showWarning" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if ("getFileSize".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "targetFile" }; //$NON-NLS-1$
		}
		else if ("getFolderContents".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "targetFolder", "[fileFilter]", "[fileOption(1=files,2=dirs)]", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"[visibleOption(1=visible,2=nonvisible)]", "[lockedOption(1=locked,2=nonlocked)]" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if ("getModificationDate".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "targetFile" }; //$NON-NLS-1$
		}
		else if ("moveFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "sourceFile", "destinationFile" }; //$NON-NLS-1$//$NON-NLS-2$
		}
		else if ("readFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[file]", "[size]" }; //$NON-NLS-1$ //$NON-NLS-2$ 
		}
		else if ("readTXTFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[file]", "[charsetname]" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if ("showDirectorySelectDialog".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[directory suggestion]", "[dialog title text]" }; //$NON-NLS-1$
		}
		else if ("showFileOpenDialog".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[selectionMode(0=both,1=Files,2=Dirs)]", "[startDirectory(null=default/previous)]", "[multiselect(true/false)]", "[filterarray]", "[callbackmethod]", "[dialog title text]" };
		}
		else if ("showFileSaveDialog".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[fileName/dir suggestion]", "[dialog title text]" }; //$NON-NLS-1$
		}
		else if ("writeFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "file", "binary_data" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if ("writeTXTFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "file", "text_data", "[charsetname]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else if ("writeXMLFile".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "file", "xml_data" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		if (methodName.equals("convertStringToJSFile")) //$NON-NLS-1$
		{
			return true;
		}
		return false;
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { JSFile.class };
	}

}
