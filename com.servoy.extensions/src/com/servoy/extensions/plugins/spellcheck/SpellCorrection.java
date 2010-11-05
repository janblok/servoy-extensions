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
		return offset + "";
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public String getLength()
	{
		return length + "";
	}

	public void setConfidence(int confidence)
	{
		this.confidence = confidence;
	}

	public String getConfidence()
	{
		return confidence + "";
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
