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

import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.protocol.http.WebRequest;
import org.mozilla.javascript.Function;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.server.headlessclient.WebClient;
import com.servoy.j2db.server.headlessclient.WebClientSession;
import com.servoy.j2db.util.Debug;

/**
 * @author jblok,jcompagner
 */
@SuppressWarnings("nls")
@ServoyDocumented(publicName = OpenIDPlugin.PLUGIN_NAME, scriptingName = "plugins." + OpenIDPlugin.PLUGIN_NAME)
public class OpenIDProvider implements IScriptable, IReturnedTypesProvider
{
	private static final class CallBackBehavior extends AbstractBehavior implements IBehaviorListener
	{
		private final FunctionDefinition functionDef;
		private final ConsumerManager manager;

		private CallBackBehavior(ConsumerManager manager, FunctionDefinition functionDef)
		{
			this.manager = manager;
			this.functionDef = functionDef;
		}

		public void onRequest()
		{
			WebClientSession wcs = WebClientSession.get();
			WebClient wc = wcs.getWebClient();
			wc.getMainPage().remove(this);
			Object[] args = verifyResponse();
			functionDef.execute(wc.getPluginAccess(), args, false);
		}

		// --- processing the authentication response --- 
		private Object[] verifyResponse()
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

					FetchResponse fetchResp = null;
					if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
					{
						fetchResp = (FetchResponse)authSuccess.getExtension(AxMessage.OPENID_NS_AX);
					}
					return new Object[] { verified.getIdentifier(), new JSAuthenticateResult(fetchResp) }; // success 
				}
			}
			catch (OpenIDException e)
			{
				Debug.error(e);
			}
			return null;
		}

	}

	OpenIDProvider()
	{
	}

	/**
	 * Redirect to openID provider to login, callback method will receive answer.
	 *
	 * @sample
	 * var authenticateRequest = plugins.openid.createAuthenticateRequest('https://www.google.com/accounts/o8/id',openIDLoginCallback);
	 * authenticateRequest.addAttributeRequest('email','http://axschema.org/contact/email',true);
	 * //see http://www.axschema.org/types/ for more attributes, not all are supported by all providers!
	 * authenticateRequest.execute();
	 * 
	 * //sample
	 * //function openIDLoginCallback(identifier,authenticateResult)
	 * //{
	 * //	var ok = false;
	 * //	if (identifier)
	 * //	{
	 * //		var id = identifier.substring(identifier.lastIndexOf('=')+1)
	 * //		application.output('id:'+id)
	 * //		var email = authenticateResult.getAttributeValue('email')
	 * //		application.output('email:'+email)
	 * //		ok = security.login(email, id, ['Administrators'])
	 * //	}
	 * //	if (!ok)
	 * //	{
	 * //		application.output('Login failed')
	 * //	}
	 * //}
	 *
	 * @param identifier 
	 * @param callback 
	 */
	public JSAuthenticateRequest js_createAuthenticateRequest(String identifier, Function callback)
	{
		RequestCycle rc = RequestCycle.get();
		if (rc == null) return null; //is webclient only, during an render cycle
		HttpServletRequest req = ((WebRequest)rc.getRequest()).getHttpServletRequest();

		try
		{
			ConsumerManager manager = new ConsumerManager();

			// perform discovery on the user-supplied identifier 
			List discoveries = manager.discover(identifier);

			// attempt to associate with the OpenID provider 
			// and retrieve one service endpoint for authentication 
			DiscoveryInformation discovered = manager.associate(discoveries);

			// store the discovery information in the user's session 
			req.getSession().setAttribute("openid-disc", discovered);

			IBehavior b = new CallBackBehavior(manager, new FunctionDefinition(callback));
			WebClientSession wcs = WebClientSession.get();
			WebClient wc = wcs.getWebClient();
			wc.getMainPage().add(b);
			CharSequence behaviorUrl = rc.urlFor(wc.getMainPage(), b, IBehaviorListener.INTERFACE);
			URL url = wc.getServerURL();

			String redirectURL = url.toString() + "/servoy-webclient/" + behaviorUrl;
			// obtain a AuthRequest message to be sent to the OpenID provider 
			AuthRequest authReq = manager.authenticate(discovered, redirectURL);

			return new JSAuthenticateRequest(authReq);
		}
		catch (OpenIDException e)
		{
			Debug.error(e);
			throw new RuntimeException(e);
		}

	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { JSAuthenticateRequest.class, JSAuthenticateResult.class };
	}
}