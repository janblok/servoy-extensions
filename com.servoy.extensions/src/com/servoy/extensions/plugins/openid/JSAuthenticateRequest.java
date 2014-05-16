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

import org.apache.wicket.RequestCycle;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;

@ServoyDocumented
public class JSAuthenticateRequest implements IJavaScriptType, IScriptable
{
	private final AuthRequest authReq;
	private FetchRequest fetch;

	public JSAuthenticateRequest()
	{
		//for developer scripting introspection only
		this(null);
	}

	public JSAuthenticateRequest(AuthRequest authReq)
	{
		this.authReq = authReq;
	}


	/**
	 * Add attribute request
	 *
	 * @sample
	 * authenticateRequest.addAttributeRequest('email','http://axschema.org/contact/email',true);
	 *
	 * @param alias 
	 * @param schemaURI 
	 * @param required 
	 */
	public void js_addAttributeRequest(String alias, String schemaURI, boolean required)
	{
		try
		{
			if (fetch == null)
			{
				fetch = FetchRequest.createFetchRequest();
			}
			fetch.addAttribute(alias, // attribute alias 
				schemaURI, // type URI 
				required); // required 

		}
		catch (Exception e)
		{
			Debug.error(e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * 
	 *
	 * @sampleas com.servoy.extensions.plugins.openid.OpenIDProvider#js_createAuthenticateRequest(String, Function)
	 */
	public void js_execute()
	{
		if (fetch != null)
		{
			// attach the extension to the authentication request 
			try
			{
				authReq.addExtension(fetch);
			}
			catch (MessageException e)
			{
				Debug.error(e);
				throw new RuntimeException("Couldnt attach the attributes", e);
			}
		}
		RequestCycle.get().setRequestTarget(new RedirectRequestTarget(authReq.getDestinationUrl(true)));
	}
}
