package com.servoy.extensions.template;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.naming.NamingContextEnumeration;
import org.apache.naming.NamingEntry;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;

import com.servoy.j2db.persistence.Form;
import com.servoy.j2db.persistence.IRepository;
import com.servoy.j2db.persistence.IRootObject;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.persistence.RootObjectMetaData;
import com.servoy.j2db.persistence.Solution;
import com.servoy.j2db.persistence.Style;
import com.servoy.j2db.server.headlessclient.SelectSolution;
import com.servoy.j2db.server.headlessclient.ServoyResourceStreamLocator;
import com.servoy.j2db.server.headlessclient.dataui.TemplateGenerator;
import com.servoy.j2db.server.shared.ApplicationServerRegistry;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Pair;

@SuppressWarnings("nls")
public class VirtualFileDirContext extends FileDirContext
{
	private static final String DEFAULTDIR = "/default";

	private final Map<String, NamingEntry> fileMap = new HashMap<String, NamingEntry>();
	private String servletPath = "";

	public VirtualFileDirContext()
	{
		super();
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException
	{
		File file = file(servletPath + name);
		List<NamingEntry> entries = (file != null ? super.list(file) : new ArrayList<NamingEntry>());
		try
		{
			if (name.startsWith(DEFAULTDIR))
			{
				RootObjectMetaData[] defs = ApplicationServerRegistry.get().getLocalRepository().getRootObjectMetaDatasForType(IRepository.SOLUTIONS);
				if (DEFAULTDIR.equals(name) || (DEFAULTDIR + "/").equals(name))
				{
					for (String element : ServoyResourceStreamLocator.editablePages)
					{
						String fname = element + ".html";
						GeneratedResource selectSolutionGr = new GeneratedResource(fname, 0);
						NamingEntry selectSolutionEntry = new NamingEntry(fname, selectSolutionGr, NamingEntry.ENTRY);
						if (!entries.contains(selectSolutionEntry))
						{
							entries.add(selectSolutionEntry);
							fileMap.put(DEFAULTDIR + "/" + fname, selectSolutionEntry);
						}
					}

					// list styles
//					Pair[] styles = TemplateGenerator.getStyles();
//					if (styles != null)
//					{
//						for (Pair s : styles)
//						{
//							String name1 = (String)s.getLeft();
//							name1 = name1 + ".css";
//
//							GeneratedResource gr1 = new GeneratedResource(name1, 0);
//							NamingEntry entry1 = new NamingEntry(name1, gr1, NamingEntry.ENTRY);
//							if (!entries.contains(entry1))
//							{
//								entries.add(entry1);
//								fileMap.put(DEFAULTDIR + "/" + name1, entry1);
//							}
//						}
//					}

					// add default one
					String name1 = "servoy_web_client_default.css";
					GeneratedResource gr1 = new GeneratedResource(name1, 0);
					NamingEntry entry1 = new NamingEntry(name1, gr1, NamingEntry.ENTRY);
					if (!entries.contains(entry1))
					{
						entries.add(entry1);
						fileMap.put(DEFAULTDIR + "/" + name1, entry1);
					}

					if (file != null)
					{
						// list solutions
						for (RootObjectMetaData element : defs)
						{
							FileDirContext fdc = new FileDirContext(env);
							fdc.setDocBase(file.getPath() + "/");
							NamingEntry entry = new NamingEntry(element.getName(), fdc, NamingEntry.ENTRY);
							if (!entries.contains(entry))
							{
								entries.add(entry);
								fileMap.put(DEFAULTDIR + "/" + element.getName() + "/", entry);
								fileMap.put(DEFAULTDIR + "/" + element.getName(), entry);
							}
						}
					}
				}
				else
				{
					for (RootObjectMetaData element2 : defs)
					{
						if (name.equals(DEFAULTDIR + "/" + element2.getName()) || name.equals(DEFAULTDIR + "/" + element2.getName() + "/"))
						{
							// list solution forms
							Solution s = (Solution)ApplicationServerRegistry.get().getLocalRepository().getActiveRootObject(element2.getRootObjectId());
							Iterator<Form> it = s.getForms(null, true);
							while (it.hasNext())
							{
								Form element = it.next();
								String name1 = element.getName() + ".html";
								GeneratedResource gr1 = new GeneratedResource(name1, s.getSolutionID(), element.getID(), true);
								NamingEntry entry1 = new NamingEntry(name1, gr1, NamingEntry.ENTRY);
								if (!entries.contains(entry1))
								{
									entries.add(entry1);
									fileMap.put(DEFAULTDIR + "/" + element2.getName() + "/" + name1, entry1);
								}
								String name2 = element.getName() + ".css";
								GeneratedResource gr2 = new GeneratedResource(name2, s.getSolutionID(), element.getID(), false);
								NamingEntry entry2 = new NamingEntry(name2, gr2, NamingEntry.ENTRY);
								if (!entries.contains(entry2))
								{
									entries.add(entry2);
									fileMap.put(DEFAULTDIR + "/" + element2.getName() + "/" + name2, entry2);
								}
								gr1.setRelatedResource(gr2);
								gr2.setRelatedResource(gr1);
							}
							break;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return new NamingContextEnumeration(entries.iterator());
	}

	@Override
	public void rebind(String name, Object obj, Attributes attrs) throws NamingException
	{
		super.rebind(servletPath + name, obj, attrs);
	}

	@Override
	public void unbind(String name) throws NamingException
	{
		super.unbind(servletPath + name);
	}

	@Override
	protected Object doLookup(String nm)
	{
		String name = filterTime(nm);
		File file = file(servletPath + name);
		if (file == null && name != null)
		{
			NamingEntry entry = fileMap.get(name);
			if (entry == null)
			{
				try
				{
					// make sure fileMap is filled
					list(name.substring(0, name.lastIndexOf('/') + 1));
				}
				catch (NamingException e)
				{
					Debug.error(e);
				}
				entry = fileMap.get(name);
			}
			if (entry != null)
			{
				return entry.value;
			}
			else
			{
				Debug.log("No entry found for: " + name);
			}
		}
		return super.doLookup(servletPath + name);
	}

	private String filterTime(String name)
	{
		int index1 = name.lastIndexOf("t.");
		if (index1 != -1)
		{
			int index2 = name.lastIndexOf("_t", index1);
			if (index2 != -1)
			{
				String tst = name.substring(index2 + 2, index1);
				// test if this can be converted to a String.
				try
				{
					Long.parseLong(tst);
					return name.substring(0, index2) + name.substring(index1 + 1);
				}
				catch (RuntimeException re)
				{
					// ignore runtime exceptions, wasn't a time.
				}
			}
		}
		return name;
	}

	@Override
	protected Attributes doGetAttributes(String nm, String[] attrIds) throws NamingException
	{
		String name = filterTime(nm);
		File file = file(servletPath + name);
		if (file == null)
		{
			NamingEntry entry = fileMap.get(name);
			if (entry != null)
			{
				if (entry.value instanceof GeneratedResource)
				{
					return new GeneratedResourceAttributes((GeneratedResource)entry.value);
				}
				else
				{
					Debug.trace("No attribs found for: " + name);
				}
			}
		}
		else
		{
			return super.doGetAttributes(servletPath + name, attrIds);
		}
		return null;
	}

	protected class GeneratedResource extends Resource
	{
		// -------------------------------------------------------- Constructor
		public GeneratedResource(String name, int solution_id, int form_id, boolean html)
		{
			this(name, solution_id);
			this.form_id = form_id;
			this.html = html;
		}

		public GeneratedResource(String name, int solution_id)
		{
			this.name = name;
			this.solution_id = solution_id;
		}

		// --------------------------------------------------- Member Variables
		protected String name;

		protected int solution_id;

		protected int form_id;

		protected boolean html;

		protected GeneratedResource relatedResource;

		// --------------------------------------------------- Resource Methods
		public String getName()
		{
			return name;
		}

		public int getSolutionID()
		{
			return solution_id;
		}

		/**
		 * Content accessor.
		 * 
		 * @return InputStream
		 */
		@Override
		public InputStream streamContent() throws IOException
		{
			if (binaryContent == null)
			{
				try
				{
					String output = null;
					if (solution_id == 0)
					{
						if (name.endsWith(".html"))
						{
							InputStream is = SelectSolution.class.getResourceAsStream(name);
							byte[] bytes = new byte[is.available()];
							is.read(bytes);
							setContent(bytes);
							ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
							return bais;
						}
						else
						{
							output = TemplateGenerator.getStyleCSS(name);
						}
					}
					else
					{
						Pair<String, String> p = TemplateGenerator.getFormHTMLAndCSS(solution_id, form_id);
						if (html)
						{
							output = p.getLeft();
							if (relatedResource != null) relatedResource.setContent(p.getRight().getBytes());
						}
						else
						{
							output = p.getRight();
							if (relatedResource != null) relatedResource.setContent(p.getLeft().getBytes());
						}
					}
					byte[] outputBytes = output.getBytes();
					setContent(outputBytes);
					ByteArrayInputStream bais = new ByteArrayInputStream(outputBytes);
					return bais;
				}
				catch (RepositoryException e)
				{
					throw new IOException(e.toString());
				}
			}
			return super.streamContent();
		}

		public void setRelatedResource(GeneratedResource relatedResource)
		{
			this.relatedResource = relatedResource;
		}

		@Override
		public void setContent(byte[] arg0)
		{
			lastModified = 0;
			super.setContent(arg0);
		}

		private long lastModified;

		public Date getLastModifiedDate()
		{
			if (solution_id != 0)
			{
				try
				{
					Solution s = (Solution)ApplicationServerRegistry.get().getLocalRepository().getActiveRootObject(solution_id);
					long t = s.getLastModifiedTime();
					if (!html)
					{
						Form form = s.getForm(form_id);
						if (form != null && form.getStyleName() != null)
						{
							Style style = (Style)ApplicationServerRegistry.get().getLocalRepository().getActiveRootObject(form.getStyleName(),
								IRepository.STYLES);
							if (style != null && style.getLastModifiedTime() > t)
							{
								t = style.getLastModifiedTime();
							}
						}
					}
					if (lastModified == 0 || t > lastModified)
					{
						if (lastModified != 0)
						{
							super.setContent((byte[])null); //clear
						}
						lastModified = t;
					}
				}
				catch (Exception e)
				{
					Debug.error(e);
				}
			}
			else
			{
				long t = ApplicationServerRegistry.get().getStartTime();
				try
				{
					if (getName().endsWith(".css"))
					{
						IRootObject activeRootObject = ApplicationServerRegistry.get().getLocalRepository().getActiveRootObject(
							getName().substring(0, getName().length() - 4), IRepository.STYLES);
						if (activeRootObject != null)
						{
							t = activeRootObject.getLastModifiedTime();
						}
					}
				}
				catch (Exception e)
				{
				}
				if (lastModified == 0 || t != lastModified)
				{
					if (lastModified != 0)
					{
						super.setContent((byte[])null); //clear
					}
					lastModified = t;
				}
			}
			if (lastModified == 0)
			{
				lastModified = System.currentTimeMillis();
			}
			Debug.trace("Checking resource " + getName() + " lastChange time " + lastModified);
			return new Date(lastModified);
		}
	}

	/**
	 * This specialized resource attribute implementation does some lazy reading (to speed up simple checks, like checking the last modified date).
	 */
	public static class GeneratedResourceAttributes extends ResourceAttributes
	{
		public GeneratedResourceAttributes(GeneratedResource resource)
		{
			this.resource = resource;
		}

		// --------------------------------------------------- Member Variables

		protected GeneratedResource resource;

		// ----------------------------------------- ResourceAttributes Methods

		/**
		 * Is collection.
		 */
		@Override
		public boolean isCollection()
		{
			return false;
		}

		/**
		 * Get content length.
		 * 
		 * @return content length value
		 */
		@Override
		public long getContentLength()
		{
			if (resource.getContent() == null)
			{
				try
				{
					resource.streamContent();
				}
				catch (IOException e)
				{
					Debug.error(e);
				}
			}
			if (resource.getContent() != null)
			{
				return resource.getContent().length;
			}
			return 0L;
		}

		/**
		 * Get creation time.
		 * 
		 * @return creation time value
		 */
		@Override
		public long getCreation()
		{
			return creationMillis;
		}

		private final long creationMillis = System.currentTimeMillis();

		@Override
		public Date getCreationDate()
		{
			return new Date(creationMillis);
		}

		@Override
		public void setLastModifiedHttp(String arg0)
		{
			lastModifiedHttp = null;//ignore
		}

		@Override
		public void setLastModifiedDate(Date arg0)
		{
			lastModifiedDate = null;//ignore
		}

		@Override
		public void setLastModified(long arg0)
		{
			lastModified = -1l;//ignore
		}

		@Override
		public long getLastModified()
		{
			return resource.getLastModifiedDate().getTime();
		}

		@Override
		public Date getLastModifiedDate()
		{
			return resource.getLastModifiedDate();
		}

		@Override
		public String getLastModifiedHttp()
		{
			lastModifiedHttp = null;
			return super.getLastModifiedHttp();
		}

		@Override
		public String getName()
		{
			return resource.getName();
		}
	}

	/**
	 * @param servletPath
	 */
	public void setServletPath(String servletPath)
	{
		this.servletPath = servletPath;
	}
}
