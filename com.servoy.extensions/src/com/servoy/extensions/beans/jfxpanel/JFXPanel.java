/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

package com.servoy.extensions.beans.jfxpanel;

import java.net.URL;

import com.servoy.j2db.IServoyBeanFactory;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ExtendableURLClassLoader;
import com.servoy.j2db.util.Utils;

/**
 * @author lvostinar
 *
 */
public class JFXPanel implements IServoyBeanFactory
{
	@Override
	public String getName()
	{
		return "JFXPanel";
	}

	@Override
	public IComponent getBeanInstance(int servoyApplicationType, IClientPluginAccess access, Object[] cargs)
	{
		if (servoyApplicationType == IClientPluginAccess.WEB_CLIENT || servoyApplicationType == IClientPluginAccess.HEADLESS_CLIENT)
		{
			return new EmptyWicketFxPanel((String)cargs[0]);
		}
		else
		{
			if (Utils.isJavaFXAvaible(getClass().getClassLoader()))
			{
				try
				{
					return new ServoyJFXPanel();
				}
				catch (Exception ex)
				{
					Debug.error(ex);
				}
				catch (NoClassDefFoundError e)
				{
					Debug.error(e);
				}
			}
			else
			{
				URL fxUrl = Utils.getJavaFXURL();
				if (fxUrl != null && getClass().getClassLoader() instanceof ExtendableURLClassLoader)
				{
					((ExtendableURLClassLoader)getClass().getClassLoader()).addURL(fxUrl);
					try
					{
						return new ServoyJFXPanel();
					}
					catch (Exception ex)
					{
						Debug.error(ex);
					}
					catch (NoClassDefFoundError e)
					{
						Debug.error(e);
					}
				}
			}
			return new EmptyJFXPanel();
		}

	}
}
