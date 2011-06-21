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

import java.util.ArrayList;

public class SpellResult
{
	private int errorNumber = 0;
	private int clippedNumber = 0;
	private int charsCheckednumber = 0;

	private ArrayList<SpellCorrection> spellCorrections = new ArrayList<SpellCorrection>();

	public void setErrorNumber(int errorNumber)
	{
		this.errorNumber = errorNumber;
	}

	public String getErrorNumber()
	{
		return errorNumber + "";
	}

	public void setClippedNumber(int clippedNumber)
	{
		this.clippedNumber = clippedNumber;
	}

	public String getClippedNumber()
	{
		return clippedNumber + "";
	}

	public void setCharsCheckednumber(int charsCheckednumber)
	{
		this.charsCheckednumber = charsCheckednumber;
	}

	public String getCharsCheckednumber()
	{
		return charsCheckednumber + "";
	}

	public void setSpellCorrections(ArrayList<SpellCorrection> spellCorrections)
	{
		this.spellCorrections = spellCorrections;
	}

	public ArrayList<SpellCorrection> getSpellCorrections()
	{
		return spellCorrections;
	}

	public void addCorrection(SpellCorrection c)
	{
		spellCorrections.add(c);
	}

	public void removeCorrection(SpellCorrection c)
	{
		spellCorrections.remove(c);
	}

	@Override
	public String toString()
	{
		return spellCorrections.toString();
	}

}
