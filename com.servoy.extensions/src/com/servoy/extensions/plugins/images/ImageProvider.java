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
package com.servoy.extensions.plugins.images;

import java.awt.Window;
import java.io.File;

import javax.imageio.ImageIO;

import com.servoy.extensions.plugins.file.JSFile;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISwingRuntimeWindow;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.gui.SnapShot;

/**
 * @author jcompagner
 */
public class ImageProvider implements IScriptObject
{
	private final ImagePlugin plugin;

	public ImageProvider(ImagePlugin plugin)
	{
		this.plugin = plugin;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			ImageIO.scanForPlugins();
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(cl);
		}
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getSample(java.lang.String)
	 */
	public String getSample(String methodName)
	{
		if ("getImage".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var image = plugins.images.getImage(byteArray);\n"); //$NON-NLS-1$
			retval.append("var height = image.getHeight();\n"); //$NON-NLS-1$
			retval.append("var contentType = image.getContentType();\n"); //$NON-NLS-1$
			retval.append("var scaled_image = image.resize(30, 30);\n"); //$NON-NLS-1$
//			return retval.toString();
//		}
//		else if ("createJPGImage".equals(methodName)) //$NON-NLS-1$
//		{
//			StringBuffer retval = new StringBuffer();
//			retval.append("//"); //$NON-NLS-1$
//			retval.append(getToolTip(methodName));
			retval.append("\n\n\n"); //$NON-NLS-1$
			retval.append("var snapshot_image = plugins.images.getImage(forms.companyReports.elements.employeesChartBean);\n"); //$NON-NLS-1$
			retval.append("var tempFile = plugins.file.createTempFile('bean_snapshot','.jpg')\n"); //$NON-NLS-1$
			retval.append("plugins.file.writeFile(tempFile, snapshot_image.getData())\n"); //$NON-NLS-1$
			retval.append("application.setStatusText('Wrote file: '+tempFile)\n"); //$NON-NLS-1$
			return retval.toString();
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(java.lang.String)
	 */
	public String getToolTip(String methodName)
	{
		if ("getImage".equals(methodName)) //$NON-NLS-1$
		{
			return "Get a javascript image/resource object for the given file/bytearray/bean/applet/form_element."; //$NON-NLS-1$
		}
		else if ("createJPGImage".equals(methodName)) //$NON-NLS-1$
		{
			return "Creates a javascript image from the given bean/applet/form_element."; //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getParameterNames(java.lang.String)
	 */
	public String[] getParameterNames(String methodName)
	{
		if ("getImage".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "file/byte_array/bean/applet/form_element" }; //$NON-NLS-1$
		}
		else if ("createJPGImage".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "bean/imageObj/element", "[width", "height]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#isDeprecated(java.lang.String)
	 */
	public boolean isDeprecated(String methodName)
	{
		if ("createJPGImage".equals(methodName)) return true; //$NON-NLS-1$
		return false;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class[] getAllReturnedTypes()
	{
		return new Class[] { JSImage.class };
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public JSImage js_createJPGImage(Object obj)
	{
		IClientPluginAccess access = plugin.getClientPluginAccess();
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISwingRuntimeWindow) currentWindow = ((ISwingRuntimeWindow)runtimeWindow).getWindow();
		return js_getImage(SnapShot.createJPGImage(currentWindow, obj, -1, -1));
	}


	@Deprecated
	public JSImage js_createJPGImage(Object obj, int width, int height)
	{
		IClientPluginAccess access = plugin.getClientPluginAccess();
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISwingRuntimeWindow) currentWindow = ((ISwingRuntimeWindow)runtimeWindow).getWindow();
		return js_getImage(SnapShot.createJPGImage(currentWindow, obj, width, height));
	}

	public JSImage js_getImage(Object object)
	{
		if (object instanceof JSFile)
		{
			object = ((JSFile)object).getFile();
		}
		else if (object instanceof String)
		{
			object = new File((String)object);
		}

		if (object instanceof File)
		{
			File file = (File)object;
			if (file.exists() && file.canRead() && file.length() > 0)
			{
				return new JSImage(file);
			}
		}

		if (object instanceof byte[])
		{
			return new JSImage((byte[])object);
		}

		IClientPluginAccess access = plugin.getClientPluginAccess();
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISwingRuntimeWindow) currentWindow = ((ISwingRuntimeWindow)runtimeWindow).getWindow();
		return new JSImage(SnapShot.createJPGImage(currentWindow, object, -1, -1));
	}
}
