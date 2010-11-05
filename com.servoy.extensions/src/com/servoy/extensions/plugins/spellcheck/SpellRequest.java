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
