/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2011 Servoy BV

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

package com.servoy.extensions.plugins.spellcheck;

public class SpellRequest
{
	private int textalreadyclipped = 0;
	private int ignoredups = 0;
	private int ignoredigits = 0;
	private int ignoreallcaps = 0;
	private String text = null;

	public SpellRequest()
	{
	}

	public SpellRequest(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return this.text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getTextalreadyclipped()
	{
		return textalreadyclipped + "";
	}

	public void setTextalreadyclipped(int textalreadyclipped)
	{
		this.textalreadyclipped = textalreadyclipped;
	}

	public String getIgnoredups()
	{
		return ignoredups + "";
	}

	public void setIgnoredups(int ignoredups)
	{
		this.ignoredups = ignoredups;
	}

	public String getIgnoredigits()
	{
		return ignoredigits + "";
	}

	public void setIgnoredigits(int ignoredigits)
	{
		this.ignoredigits = ignoredigits;
	}

	public String getIgnoreallcaps()
	{
		return ignoreallcaps + "";
	}

	public void setIgnoreallcaps(int ignoreallcaps)
	{
		this.ignoreallcaps = ignoreallcaps;
	}

}
