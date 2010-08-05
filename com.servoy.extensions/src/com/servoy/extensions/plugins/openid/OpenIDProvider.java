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

package com.servoy.extensions.plugins.openid;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.mozilla.javascript.Function;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.server.headlessclient.WebClient;
import com.servoy.j2db.server.headlessclient.WebClientSession;
import com.servoy.j2db.util.Debug;

/**
 * @author jblok
 */
public class OpenIDProvider implements IScriptObject
{
	private final ConsumerManager manager;

	OpenIDProvider(OpenIDPlugin plugin) throws ConsumerException
	{
		// instantiate a ConsumerManager object 
		manager = new ConsumerManager();
	}

	// --- placing the authentication request --- 
	public void js_authenticateRequest(String identifier, String redirectURL, final Function callback) throws IOException
	{
		RequestCycle rc = RequestCycle.get();
		if (rc == null) return; //is webclient only, during an render cycle
		HttpServletRequest req = ((WebRequest)rc.getRequest()).getHttpServletRequest();

		try
		{
			// perform discovery on the user-supplied identifier 
			List discoveries = manager.discover(identifier);

			// attempt to associate with the OpenID provider 
			// and retrieve one service endpoint for authentication 
			DiscoveryInformation discovered = manager.associate(discoveries);

			// store the discovery information in the user's session 
			req.getSession().setAttribute("openid-disc", discovered);

			// obtain a AuthRequest message to be sent to the OpenID provider 
			AuthRequest authReq = manager.authenticate(discovered, redirectURL);

			// Attribute Exchange example: fetching the 'email' attribute 
			FetchRequest fetch = FetchRequest.createFetchRequest();
			fetch.addAttribute("email", // attribute alias 
				"http://schema.openid.net/contact/email", // type URI 
				true); // required 

			// attach the extension to the authentication request 
			authReq.addExtension(fetch);

			WebClientSession wcs = WebClientSession.get();
			final WebClient wc = wcs.getWebClient();
			IBehavior b = new AbstractBehavior()
			{
				boolean remove = false;

				@Override
				public void onRendered(org.apache.wicket.Component component)
				{
					try
					{
						FunctionDefinition functionDef = new FunctionDefinition(callback);
						Object[] args = verifyResponse();
						wc.getPluginAccess().executeMethod(functionDef.getFormName(), functionDef.getMethodName(), args, true);
					}
					catch (Exception e)
					{
						Debug.error(e);
					}
					finally
					{
						remove = true;
					}
				}

				@Override
				public void detach(Component component)
				{
					if (remove) wc.getMainPage().remove(this);
				}
			};
			wc.getMainPage().add(b);

//assume everyone is on version2 by now			
//			if (!discovered.isVersion2())
			{
				// Option 1: GET HTTP-redirect to the OpenID Provider endpoint 
				// The only method supported in OpenID 1.x 
				// redirect-URL usually limited ~2048 bytes 
				rc.setRequestTarget(new RedirectRequestTarget(authReq.getDestinationUrl(true)));
//				res.sendRedirect(authReq.getDestinationUrl(true));
			}
//			else
//			{
//				// Option 2: HTML FORM Redirection (Allows payloads >2048 bytes) 
//				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("formredirection.jsp");
//				httpReq.setAttribute("parameterMap", authReq.getParameterMap());
//				httpReq.setAttribute("destinationUrl", authReq.getDestinationUrl(false));
//				dispatcher.forward(httpReq, httpResp);
//			}
		}
		catch (OpenIDException e)
		{
			Debug.error(e);
		}

	}

	// --- processing the authentication response --- 
	private String[] verifyResponse()
	{
		RequestCycle rc = RequestCycle.get();
		HttpServletRequest httpReq = ((WebRequest)rc.getRequest()).getHttpServletRequest();

		try
		{
			// extract the parameters from the authentication response 
			// (which comes in as a HTTP request from the OpenID provider) 
			ParameterList response = new ParameterList(httpReq.getParameterMap());

			// retrieve the previously stored discovery information 
			DiscoveryInformation discovered = (DiscoveryInformation)httpReq.getSession().getAttribute("openid-disc");
			httpReq.getSession().removeAttribute("openid-disc"); //remove to prevent mem leaks

			// extract the receiving URL from the HTTP request 
			StringBuffer receivingURL = httpReq.getRequestURL();
			String queryString = httpReq.getQueryString();
			if (queryString != null && queryString.length() > 0) receivingURL.append("?").append(httpReq.getQueryString());

			// verify the response; ConsumerManager needs to be the same 
			// (static) instance used to place the authentication request 
			VerificationResult verification = manager.verify(receivingURL.toString(), response, discovered);

			// examine the verification result and extract the verified identifier 
			Identifier verified = verification.getVerifiedId();
			if (verified != null)
			{
				AuthSuccess authSuccess = (AuthSuccess)verification.getAuthResponse();

				String email = null;
				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
				{
					FetchResponse fetchResp = (FetchResponse)authSuccess.getExtension(AxMessage.OPENID_NS_AX);

					List emails = fetchResp.getAttributeValues("email");
					email = (String)emails.get(0);
				}
				return new String[] { verified.getIdentifier(), email }; // success 
			}
		}
		catch (OpenIDException e)
		{
			Debug.error(e);
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public String[] getParameterNames(String methodName)
	{
		if ("authenticateRequest".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "identifier", "redirectURL", "callbackFunction" };
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("authenticateRequest".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.openid.authenticateRequest('https://www.google.com/accounts/o8/id',application.getServerURL()+'/ss/s/'+application.getSolutionName(),afterLoginCallback);"); //$NON-NLS-1$
			return retval.toString();
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String methodName)
	{
		if ("authenticateRequest".equals(methodName)) //$NON-NLS-1$
		{
			return "Redirect to openID provider to login."; //$NON-NLS-1$
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class[] getAllReturnedTypes()
	{
		return new Class[] { };
	}
}