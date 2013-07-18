package com.servoy.extensions.plugins.rest_ws.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;

import javax.activation.DataSource;

public class ServletMultipartDataSource implements DataSource
{
	String contentType;
	InputStream inputStream;

	public ServletMultipartDataSource(InputStream inputStream, String contentType) throws IOException
	{
		this.inputStream = new SequenceInputStream(new ByteArrayInputStream("\n".getBytes()), inputStream);
		this.contentType = contentType;
		{
		}
	}

	public InputStream getInputStream() throws IOException
	{
		return inputStream;
	}

	public OutputStream getOutputStream() throws IOException
	{
		return null;
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getName()
	{
		return "ServletMultipartDataSource";
	}
}