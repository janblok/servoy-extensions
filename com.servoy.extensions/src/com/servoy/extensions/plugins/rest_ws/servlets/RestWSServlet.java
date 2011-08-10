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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.XML;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.xml.XMLObject;

import com.servoy.extensions.plugins.rest_ws.RestWSPlugin;
import com.servoy.extensions.plugins.rest_ws.RestWSPlugin.NoClientsException;
import com.servoy.extensions.plugins.rest_ws.RestWSPlugin.NotAuthenticatedException;
import com.servoy.extensions.plugins.rest_ws.RestWSPlugin.NotAuthorizedException;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.JSMap;
import com.servoy.j2db.scripting.FunctionDefinition.Exist;
import com.servoy.j2db.server.headlessclient.IHeadlessClient;
import com.servoy.j2db.util.Debug;
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
	/**
	 * 
	 */
	private static final String WS_RESPONSE_HEADERS = "ws_response_headers";
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
		else if (e instanceof WebServiceException)
		{
			plugin.log.info(request.getRequestURI(), e);
			error = ((WebServiceException)e).httpResponseCode;
		}
		else if (e instanceof JavaScriptException)
		{
			plugin.log.info("ws_ method threw an exception '" + e.getMessage() + '\''); //$NON-NLS-1$
			if (((JavaScriptException)e).getValue() instanceof Double)
			{
				error = ((Double)((JavaScriptException)e).getValue()).intValue();
			}
			else
			{
				error = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
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

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		IHeadlessClient client = null;
		WsRequest wsRequest = null;
		try
		{
			plugin.log.trace("OPTIONS"); //$NON-NLS-1$ 
			wsRequest = parsePath(request);

			client = plugin.getClient(wsRequest.solutionName);
			checkAuthorization(request, client.getPluginAccess(), wsRequest.solutionName, wsRequest.formName);

			String retval = "TRACE, OPTIONS"; //$NON-NLS-1$
			if ((new FunctionDefinition(wsRequest.formName, "ws_read")).exists(client.getPluginAccess()) == FunctionDefinition.Exist.METHOD_FOUND) //$NON-NLS-1$
			{
				retval += ", GET"; //$NON-NLS-1$
			}
			//TODO: implement HEAD?
			retval += ", HEAD"; //$NON-NLS-1$
			if ((new FunctionDefinition(wsRequest.formName, "ws_create")).exists(client.getPluginAccess()) == FunctionDefinition.Exist.METHOD_FOUND) //$NON-NLS-1$
			{
				retval += ", POST"; //$NON-NLS-1$
			}
			if ((new FunctionDefinition(wsRequest.formName, "ws_update")).exists(client.getPluginAccess()) == FunctionDefinition.Exist.METHOD_FOUND) //$NON-NLS-1$
			{
				retval += ", PUT"; //$NON-NLS-1$
			}
			if ((new FunctionDefinition(wsRequest.formName, "ws_delete")).exists(client.getPluginAccess()) == FunctionDefinition.Exist.METHOD_FOUND) //$NON-NLS-1$
			{
				retval += ", DELETE"; //$NON-NLS-1$
			}

			response.setHeader("Allow", retval); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			handleException(e, request, response);
		}
		finally
		{
			if (client != null)
			{
				try
				{
					plugin.releaseClient(wsRequest.solutionName, client);
				}
				catch (Exception e)
				{
					Debug.error(e);
				}
			}
		}
	}

	public WsRequest parsePath(HttpServletRequest request)
	{
		String path = request.getPathInfo(); //without servlet name

		plugin.log.debug("Request '" + path + '\''); //$NON-NLS-1$

		// parse the path: /webServiceName/mysolution/myform/arg1/arg2/...
		String[] segments = path == null ? null : path.split("/"); //$NON-NLS-1$
		if (segments == null || segments.length < 4 || !webServiceName.equals(segments[1]))
		{
			throw new IllegalArgumentException(path);
		}

		return new WsRequest(segments[2], segments[3], Utils.arraySub(segments, 4, segments.length));
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

		WsRequest wsRequest = parsePath(request);
		IHeadlessClient client = null;
		try
		{
			client = plugin.getClient(wsRequest.solutionName);
			checkAuthorization(request, client.getPluginAccess(), wsRequest.solutionName, wsRequest.formName);

			FunctionDefinition fd = new FunctionDefinition(wsRequest.formName, methodName);
			Exist functionExists = fd.exists(client.getPluginAccess());
			if (functionExists == FunctionDefinition.Exist.NO_SOLUTION)
			{
				throw new WebServiceException("Solution " + wsRequest.solutionName + "not loaded", HttpServletResponse.SC_SERVICE_UNAVAILABLE); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (functionExists == FunctionDefinition.Exist.FORM_NOT_FOUND)
			{
				throw new WebServiceException("Form " + wsRequest.formName + " not found", HttpServletResponse.SC_NOT_FOUND); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (functionExists != FunctionDefinition.Exist.METHOD_FOUND)
			{
				throw new WebServiceException(
					"Method " + methodName + "not found" + (wsRequest.formName != null ? " on form " + wsRequest.formName : ""), HttpServletResponse.SC_METHOD_NOT_ALLOWED); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			FunctionDefinition fd_headers = new FunctionDefinition(wsRequest.formName, WS_RESPONSE_HEADERS);
			if (fd_headers.exists(client.getPluginAccess()) == FunctionDefinition.Exist.METHOD_FOUND)
			{
				Object result = client.getPluginAccess().executeMethod(wsRequest.formName, WS_RESPONSE_HEADERS, null, false);
				if (result instanceof String)
				{
					String[] l_r = String.valueOf(result).split("=");
					if (l_r.length == 2) response.addHeader(l_r[0], l_r[1]);
				}
				else if (result instanceof Object[])
				{
					Object[] resultArray = (Object[])result;
					for (Object element : resultArray)
					{
						String[] l_r = String.valueOf(element).split("=");
						if (l_r.length == 2) response.addHeader(l_r[0], l_r[1]);
					}
				}
			}

			Object[] args = null;
			if (fixedArgs != null || wsRequest.args.length > 0 || request.getParameterMap().size() > 0)
			{
				args = new Object[((fixedArgs == null) ? 0 : fixedArgs.length) + wsRequest.args.length + (request.getParameterMap().size() > 0 ? 1 : 0)];
				int idx = 0;
				if (fixedArgs != null)
				{
					System.arraycopy(fixedArgs, 0, args, 0, fixedArgs.length);
					idx += fixedArgs.length;
				}
				if (wsRequest.args.length > 0)
				{
					System.arraycopy(wsRequest.args, 0, args, idx, wsRequest.args.length);
					idx += wsRequest.args.length;
				}
				if (request.getParameterMap().size() > 0)
				{
					JSMap jsMap = new JSMap();
					Iterator<Entry<String, Object>> parameters = request.getParameterMap().entrySet().iterator();
					while (parameters.hasNext())
					{
						Entry<String, Object> entry = parameters.next();
						if (entry.getValue() instanceof String)
						{
							jsMap.put(entry.getKey(), entry.getValue());
						}
						else if (entry.getValue() instanceof String[] && ((String[])entry.getValue()).length == 1)
						{
							jsMap.put(entry.getKey(), ((String[])entry.getValue())[0]);
						}
						else if (entry.getValue() instanceof String[] && ((String[])entry.getValue()).length > 1)
						{
							jsMap.put(entry.getKey(), entry.getValue());
						}
					}

					args[idx++] = jsMap;
				}
			}

			plugin.log.debug("executeMethod('" + wsRequest.formName + "', '" + methodName + "', <args>)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Object result = client.getPluginAccess().executeMethod(wsRequest.formName, methodName, args, false);
			plugin.log.debug("result = " + (result == null ? "<NULL>" : ("'" + result + '\''))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return result;
		}
		finally
		{
			if (client != null)
			{
				try
				{
					plugin.releaseClient(wsRequest.solutionName, client);
				}
				catch (Exception e)
				{
					Debug.error(e);
				}
			}
		}
	}

	private void checkAuthorization(HttpServletRequest request, IClientPluginAccess client, String solutionName, String formName) throws Exception
	{
		String[] authorizedGroups = plugin.getAuthorizedGroups();
		FunctionDefinition fd = new FunctionDefinition(formName, "ws_authenticate"); //$NON-NLS-1$
		Exist authMethodExists = fd.exists(client);
		if (authorizedGroups == null && authMethodExists != FunctionDefinition.Exist.METHOD_FOUND)
		{
			plugin.log.debug("No authorization to check, allow all access");
			return;
		}

		//Process authentication Header
		String authorizationHeader = request.getHeader("Authorization"); //$NON-NLS-1$
		String user = null;
		String password = null;
		if (authorizationHeader != null)
		{
			if (authorizationHeader.toLowerCase().startsWith("basic "))
			{
				String authorization = authorizationHeader.substring(6);
				authorization = new String(Utils.decodeBASE64(authorization));
				int index = authorization.indexOf(':');
				if (index > 0)
				{
					user = authorization.substring(0, index);
					password = authorization.substring(index + 1);
				}
			}
			else
			{
				plugin.log.debug("No or unsupported Authorization header");
			}
		}
		else
		{
			plugin.log.debug("No Authorization header");
		}

		if (user == null || password == null || user.trim().length() == 0 || password.trim().length() == 0)
		{
			plugin.log.debug("No credentials to proceed with authentication");
			throw new NotAuthenticatedException(solutionName);
		}

		//Process the Authentication Header values
		if (authMethodExists == FunctionDefinition.Exist.METHOD_FOUND)
		{
			if (Boolean.TRUE.equals(fd.executeSync(client, (new String[] { user, password }))))
			{
				return;
			}
			plugin.log.debug("Authentication method ws_authenticate denied autentication");
			throw new NotAuthenticatedException(solutionName);
		}

		String userUid = plugin.getServerAccess().checkPasswordForUserName(user, password);
		if (userUid == null)
		{
			plugin.log.debug("Supplied credentails not valid");
			throw new NotAuthenticatedException(user);
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
							plugin.log.debug("Authorized access for user " + user + ", group " + ug);
						}
						return;
					}
				}
			}
		}
		// no match
		throw new NotAuthorizedException("User not authorized");
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

			return new String(baos.toByteArray(), getCharset(request, "Content-Type", CHARSET_DEFAULT));
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
		if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0)
		{
			return CONTENT_JSON;
		}
		if (contentType != null && contentType.toLowerCase().indexOf("xml") >= 0)
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
				if (element.toLowerCase().startsWith("charset="))
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

		boolean isXML = (result instanceof XMLObject);
		Object json = (isXML) ? XML.toJSONObject(result.toString()) : plugin.getJSONSerializer().toJSON(result);
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
				content = "<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\n" + ((isXML) ? result.toString() : XML.toString(json, null)); //$NON-NLS-1$ //$NON-NLS-2$
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

	public static class WsRequest
	{
		public final String solutionName;
		public final String formName;
		public final String[] args;

		/**
		 * @param solutionName
		 * @param formName
		 * @param args
		 */
		public WsRequest(String solutionName, String formName, String[] args)
		{
			this.solutionName = solutionName;
			this.formName = formName;
			this.args = args;
		}

		@Override
		public String toString()
		{
			return "WsRequest [solutionName=" + solutionName + ", formName=" + formName + ", args=" + Arrays.toString(args) + "]";
		}
	}

	public static class WebServiceException extends Exception
	{
		public final int httpResponseCode;

		public WebServiceException(String message, int httpResponseCode)
		{
			super(message);
			this.httpResponseCode = httpResponseCode;
		}
	}
}
