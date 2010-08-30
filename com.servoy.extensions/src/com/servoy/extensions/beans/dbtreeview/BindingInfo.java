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
package com.servoy.extensions.beans.dbtreeview;

import java.awt.Font;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import com.servoy.j2db.dataprocessing.IFoundSet;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.persistence.Media;
import com.servoy.j2db.plugins.ClientPluginAccessProvider;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.util.DataSourceUtils;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.PersistHelper;
import com.servoy.j2db.util.Utils;

/**
 * Class used to get binding informations need to display a tree node
 * 
 * @author gboros
 */
public class BindingInfo
{
	public static final String MEDIA_URL_DEF = "media:///";

	private static final String ATT_CHECKBOX = "checkBoxValue";
	private static final String ATT_CHILD_SORT = "childSort";
	private static final String ATT_FONT_TYPE = "fontType";
	private static final String ATT_HAS_CHECKBOX = "hasCheckBox";
	private static final String ATT_M_RELATION = "mRelation";
	private static final String ATT_N_RELATION = "nRelation";
	private static final String ATT_TEXT = "text";
	private static final String ATT_TOOLTIP_TEXT = "toolTipText";


	private static final String ATT_RELATION_INFO_LABEL = "label";
	private static final String ATT_RELATION_INFO_N_RELATION = "nRelationName";

	private static transient Map fontsCache;

	private final IClientPluginAccess application;

	private final ArrayList roots = new ArrayList();
	private final ArrayList bindings = new ArrayList();

	public BindingInfo(IClientPluginAccess application)
	{
		this.application = application;
	}

	public String getText(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			String provider = tbi.getTextDataprovider();
			if (provider == null)
			{
				return getConfigurationDataprovider(tn, ATT_TEXT);
			}
			else return getText(tn, provider);
		}
		else
		{
			return null;
		}
	}

	public String getText(FoundSetTreeModel.UserNode tn, String textDataprovider)
	{
		if (textDataprovider != null)
		{
			IRecord record = tn.getRecord();
			if (record != null)
			{
				Object obj = record.getValue(textDataprovider);

				if (obj != null) return obj.toString();
			}
			return null;
		}
		return "<unknown>"; //$NON-NLS-1$		
	}

	public String getText(FoundSetTreeModel.UserNode tn, String textDataprovider, String tableName)
	{
		String textDP = textDataprovider;

		if (textDP != null)
		{
			if (tableName == null)
			{
				String[] server_table = DataSourceUtils.getDBServernameTablename(tn.getFoundSet().getDataSource());
				tableName = (server_table == null ? null : server_table[1]);
			}

			IRecord record = tn.getRecord();
			String[] server_table = DataSourceUtils.getDBServernameTablename(tn.getFoundSet().getDataSource());
			if (record != null && server_table != null && server_table[1] != null && server_table[1].equals(tableName))
			{
				Object obj = record.getValue(textDP);
				if (obj != null && !obj.equals(Scriptable.NOT_FOUND)) return obj.toString();
			}
		}
		else
		{
			return getConfigurationDataprovider(tn, ATT_TEXT);
		}

		return "";
	}

	public String getToolTipText(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		String provider = null;
		if (tbi != null)
		{
			provider = tbi.getToolTipTextDataprovider();
			if (provider == null)
			{
				provider = getConfigurationDataprovider(tn, ATT_TOOLTIP_TEXT);
			}
		}

		if (provider != null)
		{
			IRecord record = tn.getRecord();
			if (record != null)
			{
				Object obj = record.getValue(provider);
				if (obj != null) return obj.toString();
			}
		}
		return null;
	}

	public String getChildSortDataprovider(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			String provider = tbi.getChildSortDataprovider();

			if (provider != null)
			{
				IRecord record = tn.getRecord();
				if (record != null)
				{
					Object obj = record.getValue(provider);
					if (obj != null) return obj.toString();
				}
			}
			else
			{
				return getConfigurationDataprovider(tn, ATT_CHILD_SORT);
			}
		}

		return null;
	}

	public String getNRelationName(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			String provider = tbi.getNRelationDataprovider();

			if (provider != null)
			{
				IRecord record = tn.getRecord();
				if (record != null)
				{
					Object obj = record.getValue(provider);
					if (obj != null) return obj.toString();
				}
			}
			else
			{
				String v = getConfigurationDataprovider(tn, ATT_N_RELATION);
				if (v != null) return v;
			}

			return tbi.getNRelationName();
		}

		return null;
	}

	public RelationInfo[] getNRelationInfos(FoundSetTreeModel.UserNode tn)
	{
		RelationInfo[] nRelationInfo = null;

		Object[] nRelationInfosObj = null;
		Binding tbi = getBinding(tn);
		if (tbi != null)
		{
			nRelationInfosObj = tbi.getNRelationInfos();
			if (nRelationInfosObj == null)
			{
				nRelationInfosObj = getNRelationInfosFromDataprovider(tn);
			}
		}

		if (nRelationInfosObj != null && nRelationInfosObj.length > 0)
		{
			ArrayList nRelationInfosA = new ArrayList();
			for (Object nRelationInfoObj : nRelationInfosObj)
			{
				if (nRelationInfoObj instanceof RelationInfo)
				{
					nRelationInfosA.add(nRelationInfoObj);
				}
			}

			nRelationInfo = new RelationInfo[0];
			nRelationInfo = (RelationInfo[])nRelationInfosA.toArray(nRelationInfo);
		}

		return nRelationInfo;
	}

	public String getMRelationName(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			String provider = tbi.getMRelationDataprovider();

			if (provider != null)
			{
				IRecord record = tn.getRecord();
				if (record != null)
				{
					Object obj = record.getValue(provider);
					if (obj != null) return obj.toString();
				}
			}
			else
			{
				String v = getConfigurationDataprovider(tn, ATT_M_RELATION);
				if (v != null) return v;
			}

			return tbi.getMRelationName();
		}

		return null;
	}

	public Font getFont(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		String provider = null;
		if (tbi != null)
		{
			provider = tbi.getFontTypeDataprovider();

			if (provider != null)
			{
				IRecord record = tn.getRecord();
				if (record != null)
				{
					Object obj = record.getValue(provider);
					if (obj != null)
					{
						if (fontsCache == null) fontsCache = new HashMap();
						String sfont = obj.toString();
						Font f = (Font)fontsCache.get(sfont);
						if (f == null)
						{
							f = PersistHelper.createFont(sfont);
							fontsCache.put(sfont, f);
						}
						return f;
					}
				}
			}
			else
			{
				String v = getConfigurationDataprovider(tn, ATT_FONT_TYPE);
				if (v != null)
				{
					if (fontsCache == null) fontsCache = new HashMap();
					Font f = (Font)fontsCache.get(v);
					if (f == null)
					{
						f = PersistHelper.createFont(v);
						fontsCache.put(v, f);
					}
					return f;
				}
			}
		}

		return null;
	}

	public Icon getIcon(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		String imgMediaProvider = null;
		String imgURLProvider = null;
		if (tbi != null)
		{
			imgMediaProvider = tbi.getImageMediaDataprovider();
			imgURLProvider = tbi.getImageURLDataprovider();
		}

		Icon icon = null;

		IRecord record = tn.getRecord();
		if (record != null)
		{
			if (imgMediaProvider != null)
			{
				Object obj = record.getValue(imgMediaProvider);
				if (obj instanceof byte[])
				{
					String base64HashCode = null;
					try
					{
						MessageDigest md = MessageDigest.getInstance("MD5");
						byte[] hash = md.digest((byte[])obj);
						base64HashCode = Utils.encodeBASE64(hash).trim();
					}
					catch (Exception ex)
					{
						Debug.error(ex);
					}
					String oldHash = (String)tn.getUserObject();
					if (oldHash != null && oldHash.equals(base64HashCode)) icon = (Icon)tn.getExUserObject();
					else
					{
						icon = new ImageIcon((byte[])obj);
						tn.setUserObject(base64HashCode);
						tn.setExUserObject(icon);
					}
				}
			}
			else if (imgURLProvider != null)
			{
				Object obj = record.getValue(imgURLProvider);
				if (obj != null)
				{
					String newIconURL = obj.toString();
					String oldIconURL = null;

					if (tn.getUserObject() != null)
					{
						oldIconURL = tn.getUserObject().toString();
					}

					if (!newIconURL.equals(oldIconURL))
					{
						int index = newIconURL.indexOf(MEDIA_URL_DEF);
						if (index == -1)
						{
							try
							{
								URL url = new URL(newIconURL);
								icon = new ImageIcon(url);
								tn.setUserObject(newIconURL);
								tn.setExUserObject(icon);
							}
							catch (MalformedURLException e)
							{
								Debug.error("Error loading media for url: " + newIconURL, e);
							}
						}
						else
						{
							if (application instanceof ClientPluginAccessProvider)
							{
								String name = newIconURL.substring(index + MEDIA_URL_DEF.length());
								try
								{
									Media media = ((ClientPluginAccessProvider)application).getApplication().getFlattenedSolution().getMedia(name);
									if (media != null)
									{
										icon = new ImageIcon(media.getMediaData());
										tn.setUserObject(newIconURL);
										tn.setExUserObject(icon);
									}
								}
								catch (Exception ex)
								{
									Debug.error("Error loading media for url: " + newIconURL, ex);
								}
							}
						}
					}
					else
					{
						icon = (Icon)tn.getExUserObject();
					}
				}
			}
		}


		return icon;
	}

	public boolean hasCheckBox(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			String provider = tbi.getHasCheckBoxDataprovider();

			if (provider != null)
			{
				IRecord record = tn.getRecord();
				if (record != null)
				{
					Object obj = record.getValue(provider);

					if (obj != null)
					{
						if (obj.toString().equals("disabled")) return true;
						return Utils.getAsBoolean(obj);
					}
				}
			}
			else
			{
				String v = getConfigurationDataprovider(tn, ATT_HAS_CHECKBOX);

				if (v != null)
				{
					if (v.equals("disabled")) return true;
					return Utils.getAsBoolean(v);
				}
			}
		}


		return false;
	}

	public boolean isCheckBoxEnabled(FoundSetTreeModel.UserNode tn)
	{
		Object oHasCheckBox = null;
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			String provider = tbi.getHasCheckBoxDataprovider();

			if (provider != null)
			{
				IRecord record = tn.getRecord();
				if (record != null)
				{
					oHasCheckBox = record.getValue(provider);
				}
			}
			else
			{
				oHasCheckBox = getConfigurationDataprovider(tn, ATT_HAS_CHECKBOX);
			}
		}

		if (oHasCheckBox != null)
		{
			String sHasCheckBox = oHasCheckBox.toString().trim();
			if (sHasCheckBox.equals("2") || sHasCheckBox.equals("disabled")) return false;
		}

		return true;
	}

	public boolean isCheckBoxChecked(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			String provider = tbi.getCheckBoxValueDataprovider();

			if (provider != null)
			{
				IRecord record = tn.getRecord();
				if (record != null)
				{
					Object obj = record.getValue(provider);

					if (obj != null)
					{
						return Utils.getAsBoolean(obj);
					}
				}
			}
			else
			{
				String v = getConfigurationDataprovider(tn, ATT_CHECKBOX);
				if (v != null)
				{
					return Utils.getAsBoolean(v);
				}
			}
		}

		return false;
	}


	public void setCheckBox(FoundSetTreeModel.UserNode tn, boolean checked)
	{
		Binding tbi = getBinding(tn);

		String provider = null;
		if (tbi != null)
		{
			provider = tbi.getCheckBoxValueDataprovider();
		}

		if (provider != null)
		{
			IRecord record = tn.getRecord();
			if (record != null)
			{
				Object oldValue = record.getValue(provider);
				Object newValue = null;
				if (oldValue instanceof String)
				{
					newValue = checked ? "true" : "false";
				}
				else if (oldValue instanceof Boolean)
				{
					newValue = new Boolean(checked);
				}
				else if (oldValue instanceof Integer)
				{
					newValue = checked ? new Integer(1) : new Integer(0);
				}

				if (newValue != null)
				{
					if (record.startEditing()) record.setValue(provider, newValue);
				}
			}
		}
	}

	public FunctionDefinition getCallBack(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			return tbi.getCallBack();
		}
		else
		{
			return null;
		}
	}

	public String getReturnDataprovider(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			return tbi.getReturnDataprovider();
		}
		else
		{
			return null;
		}
	}

	public FunctionDefinition getMethodToCallOnCheckBoxChange(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			return tbi.getMethodToCallOnCheckBoxChange();
		}
		else
		{
			return null;
		}
	}

	public String getReturnDataproviderOnCheckBoxChange(FoundSetTreeModel.UserNode tn)
	{
		Binding tbi = getBinding(tn);

		if (tbi != null)
		{
			return tbi.getReturnDataproviderOnCheckBoxChange();
		}
		else
		{
			return null;
		}
	}


	public void addBinding(Binding b)
	{
		bindings.add(b);
	}

	public void addRoots(IFoundSet foundSet)
	{
		try
		{
			roots.add(foundSet.copy(false));
		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}
	}

	public void removeRoots()
	{
		roots.clear();
	}

	public ArrayList getRootFoundSets()
	{
		return roots;
	}

	private String getConfigurationDataprovider(FoundSetTreeModel.UserNode tn, String configurationName)
	{
		Binding tbi = getBinding(tn);

		String provider = null;
		if (tbi != null)
		{
			provider = tbi.getConfigurationDataprovider();
		}

		if (provider != null)
		{
			IRecord record = tn.getRecord();
			if (record != null)
			{
				Object obj = record.getValue(provider);
				if (obj instanceof NativeObject)
				{
					NativeObject nObject = (NativeObject)obj;
					Object value = nObject.get(configurationName, nObject);
					if (value != Scriptable.NOT_FOUND)
					{
						return value.toString();
					}
				}
			}
		}
		return null;
	}

	private RelationInfo[] getNRelationInfosFromDataprovider(FoundSetTreeModel.UserNode tn)
	{
		RelationInfo[] nRelationInfosFromDataprovider = null;
		Binding tbi = getBinding(tn);

		String provider = null;
		if (tbi != null)
		{
			provider = tbi.getConfigurationDataprovider();
		}

		if (provider != null)
		{
			IRecord record = tn.getRecord();
			if (record != null)
			{
				Object obj = record.getValue(provider);
				if (obj instanceof NativeObject)
				{
					NativeObject nObject = (NativeObject)obj;
					Object value = nObject.get(ATT_N_RELATION, nObject);
					if (value instanceof NativeArray)
					{
						NativeArray nArray = (NativeArray)value;
						NativeObject nArrayObject;
						int nArrayLen = (int)nArray.getLength();

						nRelationInfosFromDataprovider = new RelationInfo[nArrayLen];

						for (int i = 0; i < nArrayLen; i++)
						{
							Object arrayObject = nArray.get(i, nArray);
							if (arrayObject instanceof NativeObject)
							{
								nArrayObject = (NativeObject)arrayObject;

								RelationInfo relationInfo = new RelationInfo();

								value = nArrayObject.get(ATT_RELATION_INFO_LABEL, nArrayObject);
								if (value != Scriptable.NOT_FOUND) relationInfo.setLabel(value.toString());
								value = nArrayObject.get(ATT_RELATION_INFO_N_RELATION, nArrayObject);
								if (value != Scriptable.NOT_FOUND) relationInfo.setNRelationName(value.toString());

								nRelationInfosFromDataprovider[i] = relationInfo;
							}
						}
					}
				}
			}
		}
		return nRelationInfosFromDataprovider;
	}

	private Binding getBinding(FoundSetTreeModel.UserNode tn)
	{
		String datasource = tn.getFoundSet().getDataSource();


		Binding b;
		for (int i = 0; i < bindings.size(); i++)
		{
			b = (Binding)bindings.get(i);

			if (datasource.equals(b.getDataSource()))
			{
				return b;
			}
		}

		return null;
	}
}
