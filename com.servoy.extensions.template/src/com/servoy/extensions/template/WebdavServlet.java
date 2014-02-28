package com.servoy.extensions.template;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.naming.resources.CacheEntry;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.ProxyDirContext;

import com.servoy.extensions.template.VirtualFileDirContext.GeneratedResource;
import com.servoy.j2db.server.shared.ApplicationServerRegistry;
import com.servoy.j2db.server.shared.IUserManager;
import com.servoy.j2db.server.shared.IUserManagerInternal;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ServoyException;
import com.servoy.j2db.util.Utils;

/**
 * Servlet which adds support for WebDAV level 2 and virtual files.
 *  
 * @author jblok, jcompagner
 */
@SuppressWarnings("nls")
public class WebdavServlet extends org.apache.catalina.servlets.WebdavServlet
{
	private String servletPath;
	private VirtualFileDirContext context;

	/**
	 * Initialize this servlet.
	 */
	@Override
	public void init() throws ServletException
	{
		super.init();
		String oldDocBase = resources.getDocBase();
		context = new VirtualFileDirContext();
		context.setDocBase(oldDocBase);
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put("context", "");
		resources = new ProxyDirContext(env, context);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String path = getRelativePath(req);
		File file = new File(resources.getDocBase(), servletPath + path.substring(0, path.lastIndexOf("/") + 1));
		if (!file.exists())
		{
			file.mkdirs(); //create nonexisting dirs
		}
		super.doPut(req, resp);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			// Authentication
			if (!request.getMethod().equalsIgnoreCase("GET"))
			{
				if (!checkAuthorized(request))
				{
					response.setHeader("WWW-Authenticate", "Basic realm=\"config\"");
					response.sendError(401);
					return;
				}
			}
			if (servletPath == null)
			{
				servletPath = request.getServletPath();
				context.setServletPath(servletPath);
			}
			super.service(request, response);
		}
		catch (Exception ex)
		{
			System.out.println(ex);
			ex.printStackTrace();
		}
	}

	/**
	 * @param request
	 * @return
	 * @throws RemoteException
	 * @throws ServoyException
	 */
	private boolean checkAuthorized(HttpServletRequest request)
	{
		boolean authorized = false;
		String authorizationHeader = request.getHeader("Authorization"); //$NON-NLS-1$
		if (authorizationHeader != null)
		{
			String authorization = authorizationHeader.substring(6);
			// TODO: which encoding to use? see http://tools.ietf.org/id/draft-reschke-basicauth-enc-05.xml
			authorization = new String(Utils.decodeBASE64(authorization));
			int index = authorization.indexOf(':');
			if (index > 0)
			{
				IUserManager userManager = ApplicationServerRegistry.get().getUserManager();
				String user = authorization.substring(0, index);
				String password = authorization.substring(index + 1);
				try
				{
					String userUid = userManager.checkPasswordForUserName(ApplicationServerRegistry.get().getClientId(), user, password);
					if (userUid != null)
					{
						authorized = ((IUserManagerInternal)userManager).checkIfUserIsAdministrator(ApplicationServerRegistry.get().getClientId(), userUid);
					}
				}
				catch (Exception e)
				{
					Debug.error(e);
				}
			}
		}
		return authorized
			? true
			: !((IUserManagerInternal)ApplicationServerRegistry.get().getUserManager()).checkIfAdministratorsAreAvailable(ApplicationServerRegistry.get().getClientId());
	}

	/**
	 * Return the relative path associated with this servlet.
	 * 
	 * @param request The servlet request we are processing
	 */
	@Override
	protected String getRelativePath(HttpServletRequest request)
	{
		// Are we being processed by a RequestDispatcher.include()?
		if (request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null)
		{
			String result = (String)request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
			if (result == null)
			{
				result = (String)request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
			}
			if (result == null || result.equals(""))
			{
				result = "/";
			}
			return result;
		}
		// No, extract the desired path directly from the request
		String result = request.getPathInfo();
		//       if (result == null) {
		//           result = request.getServletPath();
		//       }
		if (result == null || result.equals(""))
		{
			result = "/";
		}
		return result;
	}

	@Override
	protected long getLastModified(HttpServletRequest req)
	{
		String path = getRelativePath(req);
		try
		{
			Object value = context.lookup(path);
			if (value instanceof GeneratedResource)
			{
				return new VirtualFileDirContext.GeneratedResourceAttributes((GeneratedResource)value).getLastModified();
			}
		}
		catch (NamingException e)
		{
			// ignore
		}
		return super.getLastModified(req);
	}

	@Override
	protected InputStream render(String contextPath, CacheEntry cacheEntry) throws IOException, ServletException
	{
		if (!contextPath.endsWith(servletPath)) contextPath += servletPath;
		// fix for bad dir namings when the cache entry is a dir but doesn't end with /
		if (cacheEntry.context instanceof FileDirContext && !cacheEntry.name.endsWith("/"))
		{
			cacheEntry.name += "/";
		}
		return super.render(contextPath, cacheEntry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.catalina.servlets.DefaultServlet#serveResource(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, boolean)
	 */
	@Override
	protected void serveResource(HttpServletRequest request, HttpServletResponse response, boolean arg2) throws IOException, ServletException
	{
		String path = getRelativePath(request);
		CacheEntry cacheEntry = resources.lookupCache(path);
		// check if the resource exists and if it is a directory (context != null)
		if (cacheEntry.exists && cacheEntry.context != null && !checkAuthorized(request))
		{
			// just sent a not found.
			response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
			return;
		}
		super.serveResource(request, response, arg2);
	}
}