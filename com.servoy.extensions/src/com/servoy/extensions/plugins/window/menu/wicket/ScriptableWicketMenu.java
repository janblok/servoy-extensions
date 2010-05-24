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
package com.servoy.extensions.plugins.window.menu.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;

import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;

public class ScriptableWicketMenu extends ScriptableWicketMenuItem implements IMenu
{
	private final List<IMenuItem> menuItems = new ArrayList<IMenuItem>();
	protected final IWebClientPluginAccess app;


	public ScriptableWicketMenu(IMenu parentMenu, IClientPluginAccess access)
	{
		super(parentMenu);
		this.app = (IWebClientPluginAccess)access;
	}

	public void addMenuItem(IMenuItem menuItem, int index)
	{
		if (index < 0 || index >= menuItems.size())
		{
			// at the end
			menuItems.add(menuItem);
		}
		else
		{
			// at specific index
			menuItems.add(index, menuItem);
		}
	}

	public void removeMenuItem(IMenuItem menuItem)
	{
		menuItems.remove(menuItem);
	}

	public void removeMenuItem(int pos)
	{
		menuItems.remove(pos);
	}

	public void removeAllItems()
	{
		menuItems.clear();
	}

	public IMenuItem getMenuItem(int index)
	{
		return menuItems.get(index);
	}

	public int getMenuItemCount()
	{
		return menuItems.size();
	}

	public void addSeparator(int index)
	{
		addMenuItem(null, index);
	}

	private static int menuSequence = 0;

	public String generateMenuJS(ScriptableWicketPopupMenu popupMenu, String menuName)
	{
		StringBuilder js = new StringBuilder();
		int groupCount = 1;
		for (IMenuItem elem : menuItems)
		{
			if (elem == null)
			{
				groupCount++; // separator
			}
			else if (elem instanceof ScriptableWicketMenu)
			{
				menuSequence++;
				int currentMenuNr = menuSequence;
				js.append("var menu" + currentMenuNr + " = new YAHOO.widget.Menu('menu" + currentMenuNr + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				js.append(((ScriptableWicketMenu)elem).generateMenuJS(popupMenu, "menu" + currentMenuNr)); //$NON-NLS-1$ 

				js.append("var m" + currentMenuNr + " = new YAHOO.widget.MenuItem('" + elem.getText() + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				js.append("m" + currentMenuNr + ".cfg.setProperty('submenu',menu" + currentMenuNr + ");"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				js.append(menuName + ".addItem(m" + currentMenuNr + "," + groupCount + ");"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			else
			{
				String imageIcon = null;
				String controlClass = null;

				if (elem instanceof ScriptableWicketCheckBoxMenuItem && ((ScriptableWicketCheckBoxMenuItem)elem).isSelected())
				{
					controlClass = "img_checkbox"; //$NON-NLS-1$ 
				}
				else if (elem instanceof ScriptableWicketRadioButtonMenuItem)
				{
					controlClass = ((ScriptableWicketRadioButtonMenuItem)elem).isSelected() ? "img_radio_on" : "img_radio_off"; //$NON-NLS-1$ //$NON-NLS-2$ 
				}

				if (elem instanceof ScriptableWicketMenuItem)
				{
					String iconURL = ((ScriptableWicketMenuItem)elem).getIconURL();
					if (iconURL != null)
					{
						if (iconURL.startsWith("media:///")) //$NON-NLS-1$ 
						{
							String mediaUrl = RequestCycle.get().urlFor(new ResourceReference("media")).toString(); //$NON-NLS-1$ 
							imageIcon = mediaUrl + "?s=" + app.getSolutionName() + "&id=" + iconURL.substring(9); //$NON-NLS-1$ //$NON-NLS-2$ 
						}
						else imageIcon = iconURL;
					}
				}

				String text = elem.getText();
				if (controlClass != null || imageIcon != null)
				{
					StringBuilder labelText = new StringBuilder("<html><table><tr>"); //$NON-NLS-1$ 
					if (controlClass != null) labelText.append("<td class=\"" + controlClass + "\">&nbsp;&nbsp;&nbsp;&nbsp;</td>"); //$NON-NLS-1$ //$NON-NLS-2$ 
					if (imageIcon != null) labelText.append("<td><img src=\"" + imageIcon + "\" style=\"border:none\"/></td>"); //$NON-NLS-1$ //$NON-NLS-2$ 
					labelText.append("<td>" + text + "</td>"); //$NON-NLS-1$ //$NON-NLS-2$ 
					labelText.append("</tr></table></html>"); //$NON-NLS-1$ 
					text = labelText.toString();
				}
				js.append("mi = new YAHOO.widget.MenuItem('" + text + "');"); //$NON-NLS-1$ //$NON-NLS-2$ 
				js.append("mi.cfg.setProperty('onclick', {fn:svy_popmenu_click,obj:\"" + popupMenu.getCallBackUrl(elem) + "\"});"); //$NON-NLS-1$ //$NON-NLS-2$ 

				js.append(menuName + ".addItem(mi," + groupCount + ");"); //$NON-NLS-1$ //$NON-NLS-2$ 
			}
		}
		return js.toString();
	}

}
