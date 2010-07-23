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
package com.servoy.extensions.plugins.rest_ws.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.XML;

import com.servoy.extensions.plugins.rest_ws.RestWSPlugin;
import com.servoy.extensions.plugins.rest_ws.RestWSPlugin.NoClientsException;
import com.servoy.extensions.plugins.rest_ws.RestWSPlugin.NotAuthenticatedException;
import com.servoy.extensions.plugins.rest_ws.RestWSPlugin.NotAuthorizedException;
import com.servoy.j2db.server.headlessclient.IHeadlessClient;
import com.servoy.j2db.util.HTTPUtils;
import com.servoy.j2db.util.Utils;

/**
 * Servlet for mapping RESTfull Web Service request to Servoy methods.
 * <p>
 * Resources are addressed via path
 * 
 * <pre>
 * /servoy-service/rest_ws/mysolution/myform/arg1/arg2
 * </pre>.
 * <p>
 * HTTP methods are
 * <ul>
 * <li>POST<br>
 * call the method mysolution.myform.ws_create(post-data), return the method result in the response
 * <li>GET<br>
 * call the method mysolution.myform.ws_read(args), return the method result in the response or set status NOT_FOUND when null was returned
 * <li>UPDATE<br>
 * call the method mysolution.myform.ws_update(post-data, args), set status NOT_FOUND when FALSE was returned
 * <li>DELETE<br>
 * call the method mysolution.myform.ws_delete(args), set status NOT_FOUND when FALSE was returned
 * </ul>
 * 
 * <p>
 * The solution is opened via a Servoy Headless Client which is shared across multiple requests, requests are assumed to be stateless. Clients are managed via a
 * pool, 1 client per concurrent request is used.
 * 
 * @author rgansevles
 * 
 */
public class RestWSServlet extends HttpServlet
{
	private static final int CONTENT_OTHER = 0;
	private static final int CONTENT_JSON = 1;
	private static final int CONTENT_XML = 2;

	private static final int CONTENT_DEFAULT = CONTENT_JSON;
	private static final String CHARSET_DEFAULT = "UTF-8"; //$NON-NLS-1$

	private final RestWSPlugin plugin;

	private final String webServiceName;

	public RestWSServlet(String webServiceName, RestWSPlugin restWSPlugin)
	{
		this.webServiceName = webServiceName;
		this.plugin = restWSPlugin;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			plugin.log.trace("GET"); //$NON-NLS-1$ 
			Object result = wsService("ws_read", null, request, response); //$NON-NLS-1$
			if (result == null)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			HTTPUtils.setNoCacheHeaders(response);
			sendResult(request, response, result, CONTENT_DEFAULT);
		}
		catch (Exception e)
		{
			handleException(e, request, response);
		}
	}

	private void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		final int error;
		if (e instanceof NotAuthenticatedException)
		{
			plugin.log.debug(request.getRequestURI() + ": Not authenticated"); //$NON-NLS-1$
			response.setHeader("WWW-Authenticate", "Basic realm=\"" + ((NotAuthenticatedException)e).getRealm() + '"'); //$NON-NLS-1$ //$NON-NLS-2$
			error = HttpServletResponse.SC_UNAUTHORIZED;
		}
		else if (e instanceof NotAuthorizedException)
		{
			plugin.log.info(request.getRequestURI() + ": Not authorised: " + e.getMessage()); //$NON-NLS-1$ 
			error = HttpServletResponse.SC_FORBIDDEN;
		}
		else if (e instanceof NoClientsException)
		{
			plugin.log.error(request.getRequestURI(), e);
			error = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
		}
		else if (e instanceof IllegalArgumentException)
		{
			plugin.log.info("Could not parse path '" + e.getMessage() + '\''); //$NON-NLS-1$
			error = HttpServletResponse.SC_BAD_REQUEST;
		}
		else
		{
			plugin.log.error(request.getRequestURI(), e);
			error = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		response.sendError(error);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			plugin.log.trace("DELETE"); //$NON-NLS-1$ 
			if (Boolean.FALSE.equals(wsService("ws_delete", null, request, response))) //$NON-NLS-1$
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			HTTPUtils.setNoCacheHeaders(response);
		}
		catch (Exception e)
		{
			handleException(e, request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			String contents = getBody(request);
			plugin.log.trace("POST contents='" + contents + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			if (contents == null || contents.length() == 0)
			{
				response.sendError(HttpServletResponse.SC_NO_CONTENT);
				return;
			}
			int contentType = getContentType(request, "Content-Type", contents, CONTENT_OTHER); //$NON-NLS-1$
			if (contentType == CONTENT_OTHER)
			{
				response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
				return;
			}
			Object result = wsService("ws_create", new Object[] { decodeRequest(contentType, contents) }, request, response); //$NON-NLS-1$
			HTTPUtils.setNoCacheHeaders(response);
			if (result != null)
			{
				sendResult(request, response, result, contentType);
			}
		}
		catch (Exception e)
		{
			handleException(e, request, response);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			String contents = getBody(request);
			plugin.log.trace("PUT contents='" + contents + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			if (contents == null || contents.length() == 0)
			{
				response.sendError(HttpServletResponse.SC_NO_CONTENT);
				return;
			}
			int contentType = getContentType(request, "Content-Type", contents, CONTENT_OTHER); //$NON-NLS-1$
			if (contentType == CONTENT_OTHER)
			{
				response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
				return;
			}
			if (Boolean.FALSE.equals(wsService("ws_update", new Object[] { decodeRequest(contentType, contents) }, request, response))) //$NON-NLS-1$
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			HTTPUtils.setNoCacheHeaders(response);
		}
		catch (Exception e)
		{
			handleException(e, request, response);
		}
	}

	/**
	 * call the service method.
	 * Throws {@link NoClientsException} when no license is available
	 * @param methodName
	 * @param fixedArgs
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	protected Object wsService(String methodName, Object[] fixedArgs, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String path = request.getPathInfo(); //without servlet name

		plugin.log.debug("Request '" + path + '\''); //$NON-NLS-1$

		// parse the path: /webServiceName/mysolution/myform/arg1/arg2/...
		String[] segments = path == null ? null : path.split("/"); //$NON-NLS-1$
		if (segments == null || segments.length < 4 || !webServiceName.equals(segments[1]))
		{
			throw new IllegalArgumentException(path);
		}

		String solutionName = segments[2];
		checkAuthorization(request, solutionName);

		String formName = segments[3];
		Object[] args = null;
		if (fixedArgs != null || segments.length > 4)
		{
			args = new Object[(fixedArgs == null ? 0 : fixedArgs.length) + segments.length - 4];
			if (fixedArgs != null)
			{
				System.arraycopy(fixedArgs, 0, args, 0, fixedArgs.length);
			}
			System.arraycopy(segments, 4, args, args.length - segments.length + 4, segments.length - 4);
		}

		IHeadlessClient client = plugin.getClient(solutionName);
		try
		{
			plugin.log.debug("executeMethod('" + formName + "', '" + methodName + "', <args>)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Object result = client.getPluginAccess().executeMethod(formName, methodName, args, false);
			plugin.log.debug("result = " + (result == null ? "<NULL>" : ("'" + result + '\''))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return result;
		}
		finally
		{
			plugin.releaseClient(solutionName, client);
		}
	}

	private void checkAuthorization(HttpServletRequest request, String solutionName) throws Exception
	{
		String[] authorizedGroups = plugin.getAuthorizedGroups();
		if (authorizedGroups == null)
		{
			plugin.log.debug("No authorization to check, allow all access"); //$NON-NLS-1$
			return;
		}

		String authorizationHeader = request.getHeader("Authorization"); //$NON-NLS-1$
		String userUid = null;
		String user = null;
		if (authorizationHeader != null && authorizationHeader.toLowerCase().startsWith("basic ")) //$NON-NLS-1$
		{
			String authorization = authorizationHeader.substring(6);
			authorization = new String(Utils.decodeBASE64(authorization));
			int index = authorization.indexOf(':');
			if (index > 0)
			{
				user = authorization.substring(0, index);
				String password = authorization.substring(index + 1);
				userUid = plugin.getServerAccess().checkPasswordForUserName(user, password);
			}
		}
		else
		{
			plugin.log.debug("No or unsupported Authorization header"); //$NON-NLS-1$
		}
		if (userUid == null)
		{
			throw new NotAuthenticatedException(solutionName);
		}

		String[] userGroups = plugin.getServerAccess().getUserGroups(userUid);
		// find a match in groups
		if (userGroups != null)
		{
			for (String ug : userGroups)
			{
				for (String ag : authorizedGroups)
				{
					if (ag.trim().equals(ug))
					{
						if (plugin.log.isDebugEnabled())
						{
							plugin.log.debug("Authorized access for user " + user + ", group " + ug); //$NON-NLS-1$ //$NON-NLS-2$
						}
						return;
					}
				}
			}
		}

		// no match
		throw new NotAuthorizedException(user);
	}

	protected String getBody(HttpServletRequest request) throws IOException
	{
		InputStream is = null;
		try
		{
			is = request.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] buffer = new byte[128];
			int length;
			while ((length = is.read(buffer)) >= 0)
			{
				baos.write(buffer, 0, length);
			}

			return new String(baos.toByteArray(), getCharset(request, "Content-Type", CHARSET_DEFAULT)); //$NON-NLS-1$
		}
		finally
		{
			if (is != null)
			{
				is.close();
			}
		}
	}

	private int getContentType(HttpServletRequest request, String header, String contents, int defaultContentType)
	{
		String contentType = request.getHeader(header);
		if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) //$NON-NLS-1$
		{
			return CONTENT_JSON;
		}
		if (contentType != null && contentType.toLowerCase().indexOf("xml") >= 0) //$NON-NLS-1$
		{
			return CONTENT_XML;
		}

		// start guessing....
		if (contents != null && contents.length() > 0 && contents.charAt(0) == '<')
		{
			return CONTENT_XML;
		}
		if (contents != null && contents.length() > 0 && contents.charAt(0) == '{')
		{
			return CONTENT_JSON;
		}

		return defaultContentType;
	}

	protected String getCharset(HttpServletRequest request, String header, String defaultCharset)
	{
		String contentType = request.getHeader(header);
		if (contentType != null)
		{
			String[] split = contentType.split("; *"); //$NON-NLS-1$
			for (String element : split)
			{
				if (element.toLowerCase().startsWith("charset=")) //$NON-NLS-1$
				{
					String charset = element.substring("charset=".length()); //$NON-NLS-1$
					if (charset.length() > 1 && charset.charAt(0) == '"' && charset.charAt(charset.length() - 1) == '"')
					{
						charset = charset.substring(1, charset.length() - 1);
					}
					return charset;
				}
			}
		}
		return defaultCharset;
	}


	private Object decodeRequest(int contentType, String contents) throws Exception
	{
		switch (contentType)
		{
			case CONTENT_JSON :
				return plugin.getJSONSerializer().fromJSON(contents);

			case CONTENT_XML :
				return plugin.getJSONSerializer().fromJSON(XML.toJSONObject(contents));
		}

		// should not happen, content type was checked before
		throw new IllegalStateException();
	}

	@SuppressWarnings("nls")
	protected void sendResult(HttpServletRequest request, HttpServletResponse response, Object result, int defaultContentType) throws Exception
	{
		int contentType = getContentType(request, "Accept", null, defaultContentType); //$NON-NLS-1$

		Object json = plugin.getJSONSerializer().toJSON(result);
		String content;
		String charset = getCharset(request, "Accept", getCharset(request, "Content-Type", CHARSET_DEFAULT)); //$NON-NLS-1$ //$NON-NLS-2$
		switch (contentType)
		{
			case CONTENT_JSON :
				String callback = request.getParameter("callback");
				if (callback != null && !callback.equals(""))
				{
					content = callback + '(' + json.toString() + ')';
				}
				else
				{
					content = json.toString();
				}
				break;

			case CONTENT_XML :
				content = "<?xml version=\"1.0\" encoding='" + charset + "'?>\n" + XML.toString(json, null); //$NON-NLS-1$ //$NON-NLS-2$
				break;

			default :
				// how can this happen...
				throw new IllegalStateException();
		}


		String resultContentType;
		switch (contentType)
		{
			case CONTENT_JSON :
				resultContentType = "application/json"; //$NON-NLS-1$
				break;

			case CONTENT_XML :
				resultContentType = "application/xml"; //$NON-NLS-1$
				break;

			default :
				// how can this happen...
				throw new IllegalStateException();
		}
		response.setHeader("Content-Type", resultContentType + ";charset=" + charset); //$NON-NLS-1$ //$NON-NLS-2$


		byte[] bytes = content.getBytes(charset);

		ServletOutputStream outputStream = null;
		try
		{
			outputStream = response.getOutputStream();
			outputStream.write(bytes);
		}
		finally
		{
			if (outputStream != null)
			{
				outputStream.flush();
				outputStream.close();
			}
		}
		response.setContentLength(bytes.length);
	}

//	public static void setNoCacheHeaders(HttpServletResponse response)
//	{
//		response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1 //$NON-NLS-1$ //$NON-NLS-2$
//
//		response.setHeader("Pragma", "no-cache"); //HTTP 1.0 //$NON-NLS-1$ //$NON-NLS-2$
//		response.setHeader("Proxy", "no-cache"); //$NON-NLS-1$//$NON-NLS-2$
//	}
}
