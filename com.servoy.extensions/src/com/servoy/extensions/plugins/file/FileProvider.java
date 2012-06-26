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

import java.awt.Window;
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
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IMediaUploadCallback;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.plugins.IUploadData;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.FileChooserUtils;

/**
 * @author jcompagner
 * @author Servoy Stuff
 */
@ServoyDocumented(publicName = FilePlugin.PLUGIN_NAME, scriptingName = "plugins." + FilePlugin.PLUGIN_NAME)
public class FileProvider implements IReturnedTypesProvider, IScriptable
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
	static final int CHUNK_BUFFER_SIZE = 64 * 1024;

	public FileProvider(FilePlugin plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Returns a JSFile instance that corresponds to the Desktop folder of the currently logged in user.
	 *
	 * @sample
	 * var d = plugins.file.getDesktopFolder();
	 * application.output('desktop folder is: ' + d.getAbsolutePath());
	 */
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

	/**
	 * Returns a JSFile instance corresponding to the home folder of the logged in used.
	 *
	 * @sample
	 * var d = plugins.file.getHomeFolder();
	 * application.output('home folder: ' + d.getAbsolutePath());
	 */
	public JSFile js_getHomeFolder()
	{
		return new JSFile(new File(System.getProperty("user.home"))); //$NON-NLS-1$
	}

	/**
	 * @deprecated Replaced by {@link #getHomeFolder()}.
	 */
	@Deprecated
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

	/**
	 * Shows a file open dialog. Filters can be applied on what type of files can be selected. (Web Enabled, you must set the callback method for this to work)
	 *
	 * @sample
	 * // This selects only files ('1'), previous dir must be used ('null'), no multiselect ('false') and
	 * // the filter "JPG and GIF" should be used: ('new Array("JPG and GIF","jpg","gif")').
	 * /** @type {JSFile} *&#47;
	 * var f = plugins.file.showFileOpenDialog(1, null, false, new Array("JPG and GIF", "jpg", "gif"));
	 * application.output('File: ' + f.getName());
	 * application.output('is dir: ' + f.isDirectory());
	 * application.output('is file: ' + f.isFile());
	 * application.output('path: ' + f.getAbsolutePath());
	 * 
	 * // This allows mutliple selection of files, using previous dir and the same filter as above. This also casts the result to the JSFile type using JSDoc.
	 * // if filters are specified, "all file" filter will not show up unless "*" filter is present
	 * /** @type {JSFile[]} *&#47;
	 * var files = plugins.file.showFileOpenDialog(1, null, true, new Array("JPG and GIF", "jpg", "gif", "*"));
	 * for (var i = 0; i < files.length; i++)
	 * {
	 * 	 application.output('File: ' + files[i].getName());
	 * 	 application.output('content type: ' + files[i].getContentType());
	 * 	 application.output('last modified: ' + files[i].lastModified());
	 * 	 application.output('size: ' + files[i].size());
	 * }
	 * //for the web you have to give a callback function that has a JSFile array as its first argument (also works in smart), only multi select and the title are used in the webclient, others are ignored
	 * plugins.file.showFileOpenDialog(null,null,false,myCallbackMethod,'Select some nice files')
	 * 
	 */
	public Object js_showFileOpenDialog()
	{
		return js_showFileOpenDialog(1, null, false, null, null, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param callbackmethod
	 */
	public Object js_showFileOpenDialog(Function callbackmethod)
	{
		return js_showFileOpenDialog(1, null, false, null, callbackmethod, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs 
	 */
	public Object js_showFileOpenDialog(int selectionMode)
	{
		return js_showFileOpenDialog(selectionMode, null, false, null, null, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs
	 * @param callbackmethod
	 */
	public Object js_showFileOpenDialog(int selectionMode, Function callbackmethod)
	{
		return js_showFileOpenDialog(selectionMode, null, false, null, callbackmethod, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs
	 * @param startDirectory null=default/previous 
	 */
	public Object js_showFileOpenDialog(int selectionMode, Object startDirectory)
	{
		return js_showFileOpenDialog(selectionMode, startDirectory, false, null, null, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs
	 * @param startDirectory File or path to default folder,null=default/previous 
	 * @param callbackmethod
	 */
	public Object js_showFileOpenDialog(int selectionMode, Object startDirectory, Function callbackmethod)
	{
		return js_showFileOpenDialog(selectionMode, startDirectory, false, null, callbackmethod, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs
	 * @param startDirectory File or path to default folder,null=default/previous
	 * @param multiselect true/false 
	 */
	public Object js_showFileOpenDialog(int selectionMode, Object startDirectory, boolean multiselect)
	{
		return js_showFileOpenDialog(selectionMode, startDirectory, multiselect, null, null, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs
	 * @param startDirectory File or path to default folder,null=default/previous
	 * @param multiselect true/false 
	 * @param callbackmethod
	 */
	public Object js_showFileOpenDialog(int selectionMode, Object startDirectory, boolean multiselect, Function callbackmethod)
	{
		return js_showFileOpenDialog(selectionMode, startDirectory, multiselect, null, callbackmethod, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs
	 * @param startDirectory File or path to default folder,null=default/previous
	 * @param multiselect true/false 
	 * @param filter
	 */
	public Object js_showFileOpenDialog(int selectionMode, Object startDirectory, boolean multiselect, Object filter)
	{
		return js_showFileOpenDialog(selectionMode, startDirectory, multiselect, filter, null, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs
	 * @param startDirectory File or path to default folder,null=default/previous
	 * @param multiselect true/false
	 * @param filter A filter or array of filters on the folder files.
	 * @param callbackmethod
	 */
	public Object js_showFileOpenDialog(int selectionMode, Object startDirectory, boolean multiselect, Object filter, Function callbackmethod)
	{
		return js_showFileOpenDialog(selectionMode, startDirectory, multiselect, filter, callbackmethod, null);
	}

	/**
	 * @clonedesc js_showFileOpenDialog()
	 * @sampleas js_showFileOpenDialog()
	 * 
	 * @param selectionMode 0=both,1=Files,2=Dirs
	 * @param startDirectory null=default/previous
	 * @param multiselect true/false
	 * @param filter A filter or array of filters on the folder files.
	 * @param callbackmethod
	 * @param title
	 */
	public Object js_showFileOpenDialog(int selectionMode, Object startDirectory, boolean multiselect, Object filter, Function callbackmethod, String title)
	{

		int selection;
		switch (selectionMode)
		{
			case 0 :
				selection = JFileChooser.FILES_AND_DIRECTORIES;
				break;
			case 2 :
				selection = JFileChooser.DIRECTORIES_ONLY;
				break;
			default :
				selection = JFileChooser.FILES_ONLY;
		}

		File file = startDirectory != null ? getFileFromArg(startDirectory, true) : null;
		FunctionDefinition fd = callbackmethod != null ? new FunctionDefinition(callbackmethod) : null;
		String[] filterA = null;

		if (filter instanceof String)
		{
			filterA = new String[] { (String)filter };
		}
		else if (filter instanceof Object[])
		{
			Object[] array = (Object[])filter;
			filterA = new String[array.length];
			for (int i = 0; i < array.length; i++)
			{
				filterA[i] = array[i].toString();
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
					if (fu.length > 0)
					{
						JSFile[] files = new JSFile[fu.length];
						for (int i = 0; i < fu.length; i++)
						{
							files[i] = new JSFile(fu[i]);
							returnList.add(files[i]);
						}
						functionDef.executeSync(plugin.getClientPluginAccess(), new Object[] { files });
					}
				}

				public void onSubmit()
				{
					// submit without uploaded files 
				}
			};
			access.showFileOpenDialog(callback, file != null ? file.getAbsolutePath() : null, multiselect, filterA, selection, title);
			if (returnList.size() > 0) return returnList.toArray(new JSFile[returnList.size()]);
		}
		else
		{
			if (access.getApplicationType() == IClientPluginAccess.WEB_CLIENT)
			{
				throw new RuntimeException("Function callback not set for webclient"); //$NON-NLS-1$
			}

			IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
			Window currentWindow = null;
			if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();

			if (multiselect)
			{
				File[] files = FileChooserUtils.getFiles(currentWindow, file, selection, filterA, title);
				JSFile[] convertedFiles = convertToJSFiles(files);
				return convertedFiles;
			}
			else
			{
				File f = FileChooserUtils.getAReadFile(currentWindow, file, selection, filterA, title);
				if (f != null)
				{
					return new JSFile(f);
				}
			}
		}
		return null;
	}

	/**
	 * Returns an array of JSFile instances corresponding to content of the specified folder. The content can be filtered by optional name filter(s), by type, by visibility and by lock status.
	 *
	 * @sample
	 * var files = plugins.file.getFolderContents('stories', '.txt');
	 * for (var i=0; i<files.length; i++)
	 * 	application.output(files[i].getAbsolutePath());
	 *
	 * @param targetFolder File or path object.
	 * @param fileFilter Filter or array of filters for files in folder.
	 * @param fileOption 1=files, 2=dirs 
	 * @param visibleOption 1=visible, 2=nonvisible 
	 * @param lockedOption 1=locked, 2=nonlocked 
	 */
	public JSFile[] js_getFolderContents(Object targetFolder, Object fileFilter, final int fileOption, final int visibleOption, final int lockedOption)
	{
		if (targetFolder == null) return EMPTY;

		final String[] fileFilterOptions;

		if (fileFilter != null)
		{
			if (fileFilter.getClass().isArray())
			{
				Object[] tmp = (Object[])fileFilter;
				fileFilterOptions = new String[tmp.length];
				for (int i = 0; i < tmp.length; i++)
				{
					fileFilterOptions[i] = ((String)tmp[i]).toLowerCase();
				}
			}
			else
			{
				fileFilterOptions = new String[] { ((String)fileFilter).toLowerCase() };
			}
		}
		else
		{
			fileFilterOptions = null;
		}

		File file = convertToFile(targetFolder);

		FileFilter ff = new FileFilter()
		{
			public boolean accept(File pathname)
			{
				boolean retVal = true;
				if (fileFilterOptions != null)
				{
					String name = pathname.getName().toLowerCase();
					for (String element : fileFilterOptions)
					{
						retVal = name.endsWith(element);
						if (retVal) break;
					}
				}
				if (!retVal) return retVal;

				// file or folder
				if (fileOption == AbstractFile.FILES)
				{
					retVal = pathname.isFile();
				}
				else if (fileOption == AbstractFile.FOLDERS)
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
	 * @clonedesc js_getFolderContents(Object,Object,int,int,int)
	 * @sampleas js_getFolderContents(Object,Object,int,int,int)
	 *  
	 * @param targetFolder File or path object.
	 * @param fileFilter Filter or array of filters for files in folder.
	 * @param fileOption 1=files, 2=dirs 
	 * @param visibleOption 1=visible, 2=nonvisible 
	 */
	public JSFile[] js_getFolderContents(Object targetFolder, Object fileFilter, final int fileOption, final int visibleOption)
	{
		return js_getFolderContents(targetFolder, fileFilter, fileOption, visibleOption, AbstractFile.ALL);
	}

	/**
	 * @clonedesc js_getFolderContents(Object,Object,int,int,int)
	 * @sampleas js_getFolderContents(Object,Object,int,int,int)
	 *  
	 * @param targetFolder File or path object.
	 * @param fileFilter Filter or array of filters for files in folder.
	 * @param fileOption 1=files, 2=dirs 
	 */
	public JSFile[] js_getFolderContents(Object targetFolder, Object fileFilter, final int fileOption)
	{
		return js_getFolderContents(targetFolder, fileFilter, fileOption, AbstractFile.ALL, AbstractFile.ALL);
	}

	/**
	 * @clonedesc js_getFolderContents(Object,Object,int,int,int)
	 * @sampleas js_getFolderContents(Object,Object,int,int,int)
	 *  
	 * @param targetFolder File or path object.
	 * @param fileFilter Filter or array of filters for files in folder.
	 */
	public JSFile[] js_getFolderContents(Object targetFolder, Object fileFilter)
	{
		return js_getFolderContents(targetFolder, fileFilter, AbstractFile.ALL, AbstractFile.ALL, AbstractFile.ALL);
	}

	/**
	 * @clonedesc js_getFolderContents(Object,Object,int,int,int)
	 * @sampleas js_getFolderContents(Object,Object,int,int,int)
	 *  
	 * @param targetFolder File or path object.
	 */
	public JSFile[] js_getFolderContents(Object targetFolder)
	{
		return js_getFolderContents(targetFolder, null, AbstractFile.ALL, AbstractFile.ALL, AbstractFile.ALL);
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

	/**
	 * Moves the file from the source to the destination place. Returns true on success, false otherwise.
	 *
	 * @sample
	 * // Move file based on names.
	 * if (!plugins.file.moveFile('story.txt','story.txt.new'))
	 * 	application.output('File move failed.');
	 * // Move file based on JSFile instances.
	 * var f = plugins.file.convertToJSFile('story.txt.new');
	 * var fmoved = plugins.file.convertToJSFile('story.txt');
	 * if (!plugins.file.moveFile(f, fmoved))
	 * 	application.output('File move back failed.');
	 *
	 * @param source 
	 * @param destination 
	 */
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
	 * @deprecated Replaced by {@link #convertToJSFile(Object)}.
	 * 
	 * @param fileName 
	 */
	@Deprecated
	public JSFile js_convertStringToJSFile(String fileName)
	{
		return new JSFile(new File(fileName));
	}

	/**
	 * Returns a JSFile instance corresponding to an alternative representation of a file (for example a string).
	 *
	 * @sample
	 * var f = plugins.file.convertToJSFile("story.txt");
	 * if (f.canRead())
	 * 	application.output("File can be read.");
	 *
	 * @param file 
	 * 
	 * @return JSFile
	 */
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

	/**
	 * Copies the sourcefolder to the destination folder, recursively. Returns true if the copy succeeds, false if any error occurs.
	 *
	 * @sample
	 * // Copy folder based on names.
	 * if (!plugins.file.copyFolder("stories", "stories_copy"))
	 * 	application.output("Folder copy failed.");
	 * // Copy folder based on JSFile instances.
	 * var d = plugins.file.createFile("stories");
	 * var dcopy = plugins.file.createFile("stories_copy_2");
	 * if (!plugins.file.copyFolder(d, dcopy))
	 * 	application.output("Folder copy failed.");
	 *
	 * @param source 
	 * @param destination
	 * 
	 * @return success boolean
	 */
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

	/**
	 * Copies the source file to the destination file. Returns true if the copy succeeds, false if any error occurs.
	 *
	 * @sample
	 * // Copy based on file names.
	 * if (!plugins.file.copyFile("story.txt", "story.txt.copy"))
	 * 	application.output("Copy failed.");
	 * // Copy based on JSFile instances.
	 * var f = plugins.file.createFile("story.txt");
	 * var fcopy = plugins.file.createFile("story.txt.copy2");
	 * if (!plugins.file.copyFile(f, fcopy))
	 * 	application.output("Copy failed.");
	 *
	 * @param source 
	 * @param destination 
	 */
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

	/**
	 * Creates a folder on disk. Returns true if the folder is successfully created, false if any error occurs.
	 *
	 * @sample
	 * var d = plugins.file.convertToJSFile("newfolder");
	 * if (!plugins.file.createFolder(d))
	 * 	application.output("Folder could not be created.");
	 *
	 * @param destination 
	 */
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

	/**
	 * Removes a file from disk. Returns true on success, false otherwise.
	 *
	 * @sample
	 * if (plugins.file.deleteFile('story.txt'))
	 * 	application.output('File deleted.');
	 *
	 * @param destination 
	 */
	public boolean js_deleteFile(Object destination)
	{
		if (destination instanceof JSFile && ((JSFile)destination).getAbstractFile() instanceof RemoteFile)
		{
			return ((JSFile)destination).js_deleteFile();
		}
		return js_deleteFolder(destination, true);
	}

	/**
	 * Deletes a folder from disk recursively. Returns true on success, false otherwise. If the second parameter is set to true, then a warning will be issued to the user before actually removing the folder.
	 *
	 * @sample
	 * if (plugins.file.deleteFolder('stories', true))
	 * 	application.output('Folder deleted.');
	 *
	 * @param destination 
	 * @param showWarning 
	 */
	public boolean js_deleteFolder(Object destination, boolean showWarning)
	{
		File destFile = convertToFile(destination);
		if (destFile == null) return false;

		if (destFile.isDirectory())
		{
			if (showWarning)
			{
				IClientPluginAccess access = plugin.getClientPluginAccess();
				IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
				Window currentWindow = null;
				if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
				int option = JOptionPane.showConfirmDialog(
					currentWindow,
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

	/**
	 * Returns the size of the specified file.
	 *
	 * @sample
	 * var f = plugins.file.convertToJSFile('story.txt');
	 * application.output('file size: ' + plugins.file.getFileSize(f));
	 *
	 * @param path 
	 */
	public long js_getFileSize(Object path)
	{
		File file = convertToFile(path);
		if (file == null) return -1;
		return file.length();
	}

	/**
	 * Returns the modification date of a file.
	 *
	 * @sample
	 * var f = plugins.file.convertToJSFile('story.txt');
	 * application.output('last changed: ' + plugins.file.getModificationDate(f));
	 *
	 * @param path 
	 */
	public Date js_getModificationDate(Object path)
	{
		File file = convertToFile(path);
		if (file == null) return null;
		return new Date(file.lastModified());
	}

	/**
	 * Returns an Array of JSFile instances correponding to the file system root folders.
	 *
	 * @sample
	 * var roots = plugins.file.getDiskList();
	 * for (var i = 0; i < roots.length; i++)
	 * 	application.output(roots[i].getAbsolutePath());
	 */
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

	/**
	 * Creates a temporary file on disk. A prefix and an extension are specified and they will be part of the file name.
	 *
	 * @sample
	 * var tempFile = plugins.file.createTempFile('myfile','.txt');
	 * application.output('Temporary file created as: ' + tempFile.getAbsolutePath());
	 * plugins.file.writeTXTFile(tempFile, 'abcdefg');
	 *
	 * @param prefix 
	 * @param suffix 
	 */
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

	/**
	 * Creates a JSFile instance. Does not create the file on disk.
	 *
	 * @sample
	 * // Create the JSFile instance based on the file name.
	 * var f = plugins.file.createFile("newfile.txt");
	 * // Create the file on disk.
	 * if (!f.createNewFile())
	 * 	application.output("The file could not be created.");
	 *
	 * @param targetFile 
	 */
	public JSFile js_createFile(Object targetFile)
	{
		return js_convertToJSFile(targetFile);
	}

	/**
	 * Writes data into a text file. (Web Enabled: file parameter can be a string 'mytextfile.txt' to hint the browser what it is, if it is a JSFile instance it will be saved on the server)
	 *
	 * @sample
	 * var fileNameSuggestion = 'myspecialexport.tab'
	 * var textData = 'load of data...'
	 * var success = plugins.file.writeTXTFile(fileNameSuggestion, textData);
	 * if (!success) application.output('Could not write file.');
	 * // For file-encoding parameter options (default OS encoding is used), http://download.oracle.com/javase/1.4.2/docs/guide/intl/encoding.doc.html
	 * // mimeType variable can be left null, and is used for webclient only. Specify one of any valid mime types as referenced here: http://www.w3schools.com/media/media_mimeref.asp'
	 * 
	 *  @param file JSFile or path.
	 *  @param text_data Text to be written.
	 *  
	 *  @return Success boolean.
	 */
	public boolean js_writeTXTFile(Object file, String text_data)
	{
		return js_writeTXTFile(file, text_data, null, null);
	}

	/**
	 * @clonedesc js_writeTXTFile(Object, String)
	 * @sampleas js_writeTXTFile(Object, String)
	 * 
	 * @param file JSFile or path.
	 * @param text_data Text to be written.
	 * @param charsetname Charset name.
	 * 
	 * @return Success boolean.
	 */
	public boolean js_writeTXTFile(Object file, String text_data, String charsetname)
	{
		return js_writeTXTFile(file, text_data, charsetname, null);
	}

	/**
	 * @clonedesc js_writeTXTFile(Object, String)
	 * @sampleas js_writeTXTFile(Object, String)
	 * 
	 * @param file JSFile or path.
	 * @param text_data Text to be written.
	 * @param charsetname Charset name.
	 * @param mimeType Content type (used only on web).
	 * 
	 * @return Success boolean.
	 */
	@SuppressWarnings("nls")
	public boolean js_writeTXTFile(Object file, String text_data, String charsetname, String mimeType)
	{
		if (file == null) return false;
		return writeTXT(file, text_data == null ? "" : text_data, charsetname, mimeType == null ? "text/plain" : mimeType);
	}


	/**
	 * @param f
	 * @param data
	 * @param encoding
	 * @return
	 */
	protected boolean writeTXT(Object f, String data, String encoding, @SuppressWarnings("unused")
	String contentType)
	{
		try
		{
			IClientPluginAccess access = plugin.getClientPluginAccess();
			File file = getFileFromArg(f, true);
			if (file == null)
			{
				IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
				Window currentWindow = null;
				if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
				file = FileChooserUtils.getAWriteFile(currentWindow, file, false);
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

	/**
	 * @clonedesc js_writeXMLFile(Object, String)
	 * @sampleas js_writeXMLFile(Object, String)
	 * 
	 * @param file
	 * @param xml_data
	 * @param encoding
	 */
	@SuppressWarnings("nls")
	public boolean js_writeXMLFile(Object file, String xml_data, String encoding)
	{
		if (xml_data == null) return false;
		return writeTXT(file == null ? "file.xml" : file, xml_data, encoding, "text/xml");
	}

	/**
	 * Writes data into an XML file. The file is saved with the encoding specified by the XML itself. (Web Enabled: file parameter can be a string 'myxmlfile.xml' to hint the browser what it is, if it is a JSFile instance it will be saved on the server)
	 *
	 * @sample
	 * var fileName = 'form.xml'
	 * var xml = controller.printXML()
	 * var success = plugins.file.writeXMLFile(fileName, xml);
	 * if (!success) application.output('Could not write file.');
	 *
	 * @param file 
	 * @param xml_data 
	 */
	@SuppressWarnings("nls")
	public boolean js_writeXMLFile(Object file, String xml_data)
	{
		if (xml_data == null) return false;

		String encoding = "UTF-8";
		int idx1 = xml_data.indexOf("encoding=");
		if (idx1 != -1 && xml_data.length() > idx1 + 10)
		{
			int idx2 = xml_data.indexOf('"', idx1 + 10);
			int idx3 = xml_data.indexOf('\'', idx1 + 10);
			int idx4 = Math.min(idx2, idx3);
			if (idx4 != -1)
			{
				encoding = xml_data.substring(idx1 + 10, idx4);
			}
		}
		return writeTXT(file == null ? "file.xml" : file, xml_data, encoding, "text/xml");
	}

	/**
	 * Writes data into a binary file. (Web Enabled: file parameter can be a string 'mypdffile.pdf' to hint the browser what it is, if it is a JSFile instance it will be saved on the server)
	 *
	 * @sample
	 * /**@type {Array<byte>}*&#47;
	 * var bytes = new Array();
	 * for (var i=0; i<1024; i++)
	 * 	bytes[i] = i % 100;
	 * var f = plugins.file.convertToJSFile('bin.dat');
	 * if (!plugins.file.writeFile(f, bytes))
	 * 	application.output('Failed to write the file.');
	 * // mimeType variable can be left null, and is used for webclient only. Specify one of any valid mime types as referenced here: http://www.w3schools.com/media/media_mimeref.asp'
	 * var mimeType = 'application/vnd.ms-excel'
	 * if (!plugins.file.writeFile(f, bytes, mimeType))
	 * 	application.output('Failed to write the file.');
	 *
	 * @param f 
	 * @param data 
	 */
	public boolean js_writeFile(Object f, byte[] data)
	{
		return js_writeFile(f, data, null);
	}

	/**
	 * @clonedesc js_writeFile(Object, byte[])
	 * @sampleas js_writeFile(Object, byte[])
	 * 
	 * @param f
	 * @param data
	 * @param mimeType
	 */
	public boolean js_writeFile(Object f, byte[] data, @SuppressWarnings("unused")
	String mimeType)
	{
		if (data == null) return false;
		try
		{
			File file = getFileFromArg(f, true);
			if (file == null)
			{
				IClientPluginAccess access = plugin.getClientPluginAccess();
				IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
				Window currentWindow = null;
				if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
				file = FileChooserUtils.getAWriteFile(currentWindow, file, false);
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

	/**
	 * Read all content from a text file. If a file name is not specified, then a file selection dialog pops up for selecting a file. The encoding can be also specified. (Web Enabled only for a JSFile argument)
	 *
	 * @sample
	 * // Read content from a known text file.
	 * var txt = plugins.file.readTXTFile('story.txt');
	 * application.output(txt);
	 * // Read content from a text file selected from the file open dialog.
	 * var txtUnknown = plugins.file.readTXTFile();
	 * application.output(txtUnknown); 
	 */

	public String js_readTXTFile()
	{
		return js_readTXTFile(null, null);
	}

	/**
	 * @clonedesc js_readTXTFile()
	 * @sampleas js_readTXTFile()
	 * 
	 * @param file JSFile or path.
	 */
	public String js_readTXTFile(Object file)
	{
		return js_readTXTFile(file, null);
	}

	/**
	 * @clonedesc js_readTXTFile()
	 * @sampleas js_readTXTFile()
	 * 
	 * @param file JSFile or path.
	 * @param charsetname Charset name.
	 */
	public String js_readTXTFile(Object file, String charsetname)
	{
		try
		{
			File f = null;
			if (file != null)
			{
				f = getFileFromArg(file, true);
			}
			IClientPluginAccess access = plugin.getClientPluginAccess();
			IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
			Window currentWindow = null;
			if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
			File fileObj = FileChooserUtils.getAReadFile(currentWindow, f, JFileChooser.FILES_ONLY, null);

			if (file != null) // !cancelled
			{
				return readTXTFile(charsetname, new FileInputStream(fileObj));
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
	protected String readTXTFile(String encoding, InputStream file) throws FileNotFoundException, IOException
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

	/**
	 * Shows a file save dialog.
	 *
	 * @sample
	 * var file = plugins.file.showFileSaveDialog();
	 * application.output("you've selected file: " + file.getAbsolutePath());
	 */

	public JSFile js_showFileSaveDialog()
	{
		return js_showFileSaveDialog(null, null);
	}

	/**
	 * @clonedesc js_showFileSaveDialog()
	 * @sampleas js_showFileSaveDialog()
	 * 
	 * @param fileNameDir File to save.
	 */
	public JSFile js_showFileSaveDialog(Object fileNameDir)
	{
		return js_showFileSaveDialog(fileNameDir, null);
	}

	/**
	 * @clonedesc js_showFileSaveDialog()
	 * @sampleas js_showFileSaveDialog()
	 * @param fileNameDir
	 * @param title Dialog title.
	 */
	public JSFile js_showFileSaveDialog(Object fileNameDir, String title)
	{
		File file = null;
		if (fileNameDir != null)
		{
			file = getFileFromArg(fileNameDir, true);
		}

		IClientPluginAccess access = plugin.getClientPluginAccess();
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
		File f = FileChooserUtils.getAWriteFile(currentWindow, file, true, title);
		if (f != null)
		{
			return new JSFile(f);
		}
		return null;
	}

	/**
	 * Shows a directory selector dialog.
	 *
	 * @sample
	 * var dir = plugins.file.showDirectorySelectDialog();
	 * application.output("you've selected folder: " + dir.getAbsolutePath());
	 */

	public JSFile js_showDirectorySelectDialog()
	{
		return js_showDirectorySelectDialog(null, null);
	}

	/**
	 * @clonedesc js_showDirectorySelectDialog()
	 * @sampleas js_showDirectorySelectDialog()
	 * 
	 * @param directory Default directory.
	 */
	public JSFile js_showDirectorySelectDialog(Object directory)
	{
		return js_showDirectorySelectDialog(directory, null);
	}

	/**
	 * @clonedesc js_showDirectorySelectDialog()
	 * @sampleas js_showDirectorySelectDialog()
	 * 
	 * @param directory Default directory.
	 * @param title Dialog title.
	 */
	public JSFile js_showDirectorySelectDialog(Object directory, String title)
	{
		File f = null;
		if (directory != null)
		{
			f = getFileFromArg(directory, true);
		}

		IClientPluginAccess access = plugin.getClientPluginAccess();
		if (access.getApplicationType() != IClientPluginAccess.CLIENT && access.getApplicationType() != IClientPluginAccess.RUNTIME) throw new UnsupportedMethodException(
			"Directory selection is only supported in the SmartClient (not in web or headless client)"); //$NON-NLS-1$
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
		File retval = FileChooserUtils.getAReadFile(currentWindow, f, JFileChooser.DIRECTORIES_ONLY, null, title);
		if (retval != null)
		{
			return new JSFile(retval);
		}
		return null;
	}

	/**
	 * Reads all or part of the content from a binary file. If a file name is not specified, then a file selection dialog pops up for selecting a file. (Web Enabled only for a JSFile argument)
	 *
	 * @sample
	 * // Read all content from the file.
	 * var bytes = plugins.file.readFile('big.jpg');
	 * application.output('file size: ' + bytes.length);
	 * // Read only the first 1KB from the file.
	 * var bytesPartial = plugins.file.readFile('big.jpg', 1024);
	 * application.output('partial file size: ' + bytesPartial.length);
	 * // Read all content from a file selected from the file open dialog.
	 * var bytesUnknownFile = plugins.file.readFile();
	 * application.output('unknown file size: ' + bytesUnknownFile.length);
	 * 
	 */
	public byte[] js_readFile()
	{
		return js_readFile(null, -1);
	}

	/**
	 * @clonedesc js_readFile()
	 * @sampleas js_readFile()
	 * 
	 * @param file JSFile or path.
	 */
	public byte[] js_readFile(Object file)
	{
		return js_readFile(file, -1);
	}

	/**
	 * @clonedesc js_readFile()
	 * @sampleas js_readFile()
	 * 
	 * @param file JSFile or path.
	 * @param size Number of bytes to read.
	 */
	public byte[] js_readFile(Object file, long size)
	{
		try
		{
			File f = null;
			if (file != null)
			{
				f = getFileFromArg(file, true);
			}

			IClientPluginAccess access = plugin.getClientPluginAccess();
			IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
			Window currentWindow = null;
			if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
			File fileObj = FileChooserUtils.getAReadFile(currentWindow, f, JFileChooser.FILES_ONLY, null);

			byte[] retval = null;
			if (fileObj != null && fileObj.exists() && !fileObj.isDirectory()) // !cancelled
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					retval = FileChooserUtils.paintingReadFile(access.getExecutor(), access, fileObj, size);
				}
				else
				{
					retval = FileChooserUtils.readFile(fileObj, size);
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

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { JSFile.class, JSProgressMonitor.class };
	}


	/**
	 * Appends a string given in parameter to a file, using default platform encoding.
	 * 
	 * @sample
	 * // append some text to a text file:
	 * 	var ok = plugins.file.appendToTXTFile('myTextFile.txt', '\nMy fantastic new line of text\n');
	 * 
	 * @since Servoy 5.2
	 * 
	 * @param file either a {@link File}, a local {@link JSFile} or a the file path as a String
	 * @param text the text to append to the file
	 * @return true if appending worked
	 */
	public boolean js_appendToTXTFile(Object file, String text)
	{
		return js_appendToTXTFile(file, text, null);
	}

	/**
	 * Appends a string given in parameter to a file, using the specified encoding.
	 * 
	 * @sampleas js_appendToTXTFile(Object, String)
	 * 
	 * @since Servoy 5.2
	 * 
	 * @param file either a {@link File}, a local {@link JSFile} or a the file path as a String
	 * @param text the text to append to the file
	 * @param encoding the encoding to use
	 * 
	 * @return true if appending worked
	 */
	@SuppressWarnings("nls")
	public boolean js_appendToTXTFile(Object file, String text, String encoding)
	{
		if (text != null)
		{
			try
			{
				final IClientPluginAccess access = plugin.getClientPluginAccess();
				File f = getFileFromArg(file, true);
				if (f == null)
				{
					IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
					Window currentWindow = null;
					if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
					f = FileChooserUtils.getAWriteFile(currentWindow, f, false);
				}
				FileOutputStream fos = new FileOutputStream(f, true);
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
	 * Convenience return to get a JSFile representation of a server file based on its path.
	 * 
	 * @sample
	 * var f = plugins.file.convertToRemoteJSFile('/story.txt');
	 * if (f && f.canRead())
	 * 	application.output('File can be read.');
	 *
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
	 * Returns an array of JSFile instances corresponding to content of the specified folder on the server side. The content can be filtered by optional name filter(s), by type, by visibility and by lock status.
	 *
	 * @sample
	 * // retrieves an array of files located on the server side inside the default upload folder:
	 * var files = plugins.file.getRemoteFolderContents('/', '.txt');
	 *
	 * @since Servoy 5.2.1

	 * @param targetFolder  
	 * @return the array of file names
	 */
	public JSFile[] js_getRemoteFolderContents(Object targetFolder)
	{
		return js_getRemoteFolderContents(targetFolder, null, AbstractFile.ALL, AbstractFile.ALL, AbstractFile.ALL);
	}

	/**
	 * @clonedesc js_getRemoteFolderContents(Object)
	 * @sampleas js_getRemoteFolderContents(Object)
	 * @param targetFolder
	 * @param fileFilter
	 * @return the array of file names
	 */
	public JSFile[] js_getRemoteFolderContents(Object targetFolder, Object fileFilter)
	{
		return js_getRemoteFolderContents(targetFolder, fileFilter, AbstractFile.ALL, AbstractFile.ALL, AbstractFile.ALL);
	}

	/**
	 * @clonedesc js_getRemoteFolderContents(Object)
	 * @sampleas js_getRemoteFolderContents(Object)
	 * @param targetFolder
	 * @param fileFilter
	 * @param fileOption
	 * @return the array of file names
	 */
	public JSFile[] js_getRemoteFolderContents(Object targetFolder, Object fileFilter, int fileOption)
	{
		return js_getRemoteFolderContents(targetFolder, fileFilter, fileOption, AbstractFile.ALL, AbstractFile.ALL);
	}

	/**
	 * @clonedesc js_getRemoteFolderContents(Object)
	 * @sampleas js_getRemoteFolderContents(Object)
	 * @param targetFolder
	 * @param fileFilter
	 * @param fileOption
	 * @param visibleOption
	 * @return the array of file names
	 */
	public JSFile[] js_getRemoteFolderContents(Object targetFolder, Object fileFilter, int fileOption, int visibleOption)
	{
		return js_getRemoteFolderContents(targetFolder, fileFilter, fileOption, visibleOption, AbstractFile.ALL);
	}

	/**
	 * @clonedesc js_getRemoteFolderContents(Object)
	 * @sampleas js_getRemoteFolderContents(Object)
	 * @param targetFolder
	 * @param fileFilter
	 * @param fileOption
	 * @param visibleOption
	 * @param lockedOption
	 * @return the array of file names
	 */
	@SuppressWarnings("nls")
	public JSFile[] js_getRemoteFolderContents(Object targetFolder, Object fileFilter, int fileOption, int visibleOption, int lockedOption)
	{
		if (targetFolder == null) return EMPTY;

		String[] fileFilterA = null;
		if (fileFilter != null)
		{
			if (fileFilter instanceof String[])
			{
				fileFilterA = (String[])fileFilter;
				for (int i = 0; i < fileFilterA.length; i++)
				{
					fileFilterA[i] = fileFilterA[i].toLowerCase();
				}
			}
			else
			{
				fileFilterA = new String[] { fileFilter.toString().toLowerCase() };
			}
		}

		String serverFileName = null;
		if (targetFolder instanceof JSFile)
		{
			IAbstractFile abstractFile = ((JSFile)targetFolder).getAbstractFile();
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
			serverFileName = targetFolder.toString();
		}
		try
		{
			final IFileService service = getFileService();
			final String clientId = plugin.getClientPluginAccess().getClientID();
			final RemoteFileData[] remoteList = service.getRemoteFolderContent(clientId, serverFileName, fileFilterA, fileOption, visibleOption, lockedOption);
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
	 * 
	 * @deprecated Replaced by {@link #getRemoteFolderContents(Object[])}.
	 * 
	 * @since Servoy 5.2
	 * 
	 * @param serverPath the path of a remote directory (relative to the defaultFolder)
	 * 
	 * @return the array of file names
	 */
	@Deprecated
	public JSFile[] js_getRemoteList(final Object serverPath)
	{
		return js_getRemoteList(serverPath, false);
	}

	/**
	 * Retrieves an array of files/folders from the server
	 * 
	 * @deprecated Replaced by {@link #getRemoteFolderContents(Object[])}.
	 * 
	 * @since Servoy 5.2
	 * 
	 * @param serverPath a {@link JSFile} or String with the path of a remote directory (relative to the defaultFolder)
	 * @param filesOnly if true only files will be retrieve, if false, files and folders will be retrieved
	 * 
	 * @return the array of file names
	 */
	@Deprecated
	public JSFile[] js_getRemoteList(final Object serverPath, final boolean filesOnly)
	{
		final int fileOption = (filesOnly) ? AbstractFile.FILES : AbstractFile.ALL;
		return js_getRemoteFolderContents(new Object[] { serverPath, null, new Integer(fileOption) });
	}

	/**
	 * Overloaded method, only defines file(s) to be streamed
	 * 
	 * @since Servoy 5.2
	 * 
	 * @sample
	 * // send one file:
	 * var file = plugins.file.showFileOpenDialog( 1, null, false, null, null, 'Choose a file to transfer' );
	 * if (file) {
	 * 	plugins.file.streamFilesToServer( file, callbackFunction );
	 * }
	 * // send an array of files:
	 * var folder = plugins.file.showDirectorySelectDialog();
	 * if (folder) {
	 * 	var files = plugins.file.getFolderContents(folder);
	 * 	if (files) {
	 * 		var monitor = plugins.file.streamFilesToServer( files, callbackFunction );
	 * 	}
	 * }
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
	 * 
	 * @since Servoy 5.2
	 * 
	 * @sampleas js_streamFilesToServer(Object)
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
	 * 
	 * @since Servoy 5.2
	 * 
	 * @sampleas js_streamFilesToServer(Object)
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
	 * Stream 1 or more file from the server to the client.
	 * 
	 * @since Servoy 5.2
	 * 
	 * @sample
	 * // transfer all the files of a chosen server folder to a directory on the client
	 * var dir = plugins.file.showDirectorySelectDialog();
	 * if (dir) {
	 * 	var list = plugins.file.getRemoteFolderContents('/images/user1/', null, 1);
	 * 	if (list) {
	 * 		var monitor = plugins.file.streamFilesFromServer(dir, list, callbackFunction);
	 * 	}
	 * }
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
	 * 
	 * @since Servoy 5.2
	 * 
	 * @sampleas js_streamFilesFromServer(Object, Object)
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
	 * Returns the default upload location path of the server.
	 *
	 * @sample
	 * // get the (server-side) default upload location path:
	 * var serverPath = plugins.file.getDefaultUploadLocation();
	 * 
	 * @return the location as canonical path
	 */
	public String js_getDefaultUploadLocation()
	{
		try
		{
			final IFileService service = getFileService();
			return service.getDefaultFolderLocation(plugin.getClientPluginAccess().getClientID());
		}
		catch (final Exception ex)
		{
			Debug.error(ex);
		}
		return null;
	}

	public void unload()
	{
		timer.cancel();
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
