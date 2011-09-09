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

public class SpellCorrection
{
	//o = offset from the start if the query to the misspelled word
	private int offset;
	//l = length of the misspelled word
	private int length;
	//s = confidence of Google's suggestion
	private int confidence;

	private String textCorrection;

	public SpellCorrection()
	{

	}

	public SpellCorrection(int offset, int length, int confidence, String textCorrection)
	{
		this.offset = offset;
		this.length = length;
		this.confidence = confidence;
		this.textCorrection = textCorrection;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public String getOffset()
	{
		return String.valueOf(offset);
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public String getLength()
	{
		return String.valueOf(length);
	}

	public void setConfidence(int confidence)
	{
		this.confidence = confidence;
	}

	public String getConfidence()
	{
		return String.valueOf(confidence);
	}

	public void setTextCorrection(String textCorrection)
	{
		this.textCorrection = textCorrection;
	}

	public String getTextCorrection()
	{
		return textCorrection;
	}

	@Override
	public String toString()
	{
		return textCorrection;
	}
}
