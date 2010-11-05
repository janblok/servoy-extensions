package com.servoy.extensions.plugins.spellcheck;

import java.util.ArrayList;

import com.servoy.extensions.plugins.spellcheck2.SpellCorrection;

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
