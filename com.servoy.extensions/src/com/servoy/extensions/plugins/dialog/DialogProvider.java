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
package com.servoy.extensions.plugins.dialog;

import java.awt.Dialog;
import java.awt.Window;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.scripting.IScriptObject;

/**
 * Scritptable object for dialog plugin
 * @author jblok
 */
public class DialogProvider implements IScriptObject
{
	private final DialogPlugin plugin;

	public DialogProvider(DialogPlugin plugin)
	{
		this.plugin = plugin;
	}

	@Deprecated
	public String js_showDialog(Object[] array)//old one
	{
		return js_showWarningDialog(array);
	}

	public String js_showWarningDialog(Object[] array)
	{
		if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.WEB_CLIENT && (array.length == 2 || array.length == 3))
		{
			BrowserDialog.alert(plugin.getClientPluginAccess(), String.valueOf(array[1]));
			return (array.length == 3 ? String.valueOf(array[2]) : "OK");
		}
		return js_showDialogEx(array, JOptionPane.WARNING_MESSAGE);
	}

	public String js_showInfoDialog(Object[] array)
	{
		return js_showDialogEx(array, JOptionPane.INFORMATION_MESSAGE);
	}

	public String js_showErrorDialog(Object[] array)
	{
		return js_showDialogEx(array, JOptionPane.ERROR_MESSAGE);
	}

	public String js_showQuestionDialog(Object[] array)
	{
		return js_showDialogEx(array, JOptionPane.QUESTION_MESSAGE);
	}

	private String js_showDialogEx(Object[] array, int type)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new RuntimeException("Can't use the dialog plugin in a none Swing thread/environment");
		}
		String title = Messages.getString("servoy.general.warning"); //$NON-NLS-1$
		if (array != null && array.length > 0 && array[0] != null)
		{
			title = Messages.getStringIfPrefix(array[0].toString());
		}
		String msg = Messages.getString("servoy.general.clickOk"); //$NON-NLS-1$
		if (array != null && array.length > 1 && array[1] != null) msg = Messages.getStringIfPrefix(array[1].toString());
		Vector buttons = new Vector();
		if (array != null)
		{
			for (int i = 2; i < array.length; i++)
			{
				if (array[i] != null && !("".equals(array[i]))) //$NON-NLS-1$
				{
					buttons.addElement(Messages.getStringIfPrefix(array[i].toString()));
				}
			}
		}
		String[] options = new String[buttons.size()];
		buttons.copyInto(options);
		if (options.length == 0) options = new String[] { Messages.getString("servoy.button.ok") }; //$NON-NLS-1$
		JOptionPane pane = new JOptionPane(msg, type, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
		pane.setInitialValue(options[0]);
		createAndShowDialog(title, pane);
		return (String)pane.getValue();
	}

	/**
	 * @param title
	 * @param pane
	 * @return
	 */
	private void createAndShowDialog(String title, JOptionPane pane)
	{
		IClientPluginAccess access = plugin.getClientPluginAccess();
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
		pane.setComponentOrientation(((currentWindow == null) ? JOptionPane.getRootFrame() : currentWindow).getComponentOrientation());
		JDialog dialog = pane.createDialog(currentWindow, title);
		modalizeDialog(dialog);
		pane.selectInitialValue();
		dialog.setVisible(true);
		dialog.dispose();
	}

	/**
	 * @param dialog
	 */
	private void modalizeDialog(JDialog dialog)
	{
		Object setModalityTypeMethod = null;
		Object modalityDocumentModalValue = null;
		try
		{
			Class< ? > clz = Class.forName("java.awt.Dialog$ModalityType");
			modalityDocumentModalValue = clz.getField("DOCUMENT_MODAL").get(clz);
			setModalityTypeMethod = Dialog.class.getMethod("setModalityType", clz);
		}
		catch (Exception e)
		{
			setModalityTypeMethod = Boolean.FALSE;
		}

		if (setModalityTypeMethod instanceof Method)
		{
			try
			{
				((Method)setModalityTypeMethod).invoke(dialog, new Object[] { modalityDocumentModalValue });
			}
			catch (Exception e)
			{
				setModalityTypeMethod = Boolean.FALSE;
			}
		}
	}

	public String js_showSelectDialog(Object[] array)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new RuntimeException("Can't use the dialog plugin in a none Swing thread/environment");
		}
		String title = Messages.getString("servoy.general.warning"); //$NON-NLS-1$
		if (array != null && array.length > 0 && array[0] != null) title = Messages.getStringIfPrefix(array[0].toString());
		String msg = Messages.getString("servoy.general.clickOk"); //$NON-NLS-1$
		if (array != null && array.length > 1 && array[1] != null) msg = Messages.getStringIfPrefix(array[1].toString());
		Vector buttons = new Vector();
		if (array != null)
		{
			if (array.length == 3 && array[2] instanceof Object[])
			{
				Object[] args = ((Object[])array[2]);
				for (Object element : args)
				{
					buttons.addElement(element == null ? "" : Messages.getStringIfPrefix(element.toString())); //$NON-NLS-1$
				}
			}
			else
			{
				for (int i = 2; i < array.length; i++)
				{
					buttons.addElement(array[i] == null ? "" : Messages.getStringIfPrefix(array[i].toString())); //$NON-NLS-1$
				}
			}
		}
		Object[] options = new String[buttons.size()];
		buttons.copyInto(options);
		if (options.length == 0) options = new String[] { Messages.getString("servoy.button.ok") }; //$NON-NLS-1$
		JOptionPane pane = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, options[0]);
		pane.setWantsInput(true);
		pane.setSelectionValues(options);
		pane.setInitialSelectionValue(options[0]);
		createAndShowDialog(title, pane);
		Object value = pane.getInputValue();
		return (value != JOptionPane.UNINITIALIZED_VALUE ? value.toString() : null);
	}

	public String js_showInputDialog(Object[] array)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new RuntimeException("Can't use the dialog plugin in a none Swing thread/environment");
		}
		String title = Messages.getString("servoy.general.warning"); //$NON-NLS-1$
		if (array != null && array.length > 0 && array[0] != null) title = Messages.getStringIfPrefix(array[0].toString());
		String msg = Messages.getString("servoy.general.clickOk"); //$NON-NLS-1$
		if (array != null && array.length > 1 && array[1] != null) msg = Messages.getStringIfPrefix(array[1].toString());
		String val = null;
		if (array != null && array.length > 2 && array[2] != null) val = array[2].toString();
		JOptionPane pane = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, val);
		pane.setWantsInput(true);
		pane.setInitialSelectionValue(val);
		createAndShowDialog(title, pane);
		Object value = pane.getInputValue();
		return (value != JOptionPane.UNINITIALIZED_VALUE ? value.toString() : null);
	}

	public boolean isDeprecated(String methodName)
	{
		if ("showDialog".equals(methodName)) //$NON-NLS-1$
		{
			return true;
		}
		return false;
	}

	public String[] getParameterNames(String methodName)
	{
		if ("showErrorDialog".equals(methodName) || "showInfoDialog".equals(methodName) || "showWarningDialog".equals(methodName) || "showQuestionDialog".equals(methodName)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{
			return new String[] { "dialog_title", "msg", "[...button]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		}
		else if ("showInputDialog".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "dialog_title", "msg", "[initialValue]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else if ("showSelectDialog".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "dialog_title", "msg", "optionArray/option1", "[...option]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
		}
		else if ("showDialog".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[dialog_title]", "[msg]", "[initialValue]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("showErrorDialog".equals(methodName) || "showInfoDialog".equals(methodName) || "showWarningDialog".equals(methodName) || "showQuestionDialog".equals(methodName)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//show dialog\n"); //$NON-NLS-1$
			retval.append("var thePressedButton = plugins.dialogs." + methodName + "('Title', 'Value not allowed','OK');\n"); //$NON-NLS-1$ //$NON-NLS-2$
			return retval.toString();
		}
		else if ("showInputDialog".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//show input dialog ,returns nothing when canceled \n"); //$NON-NLS-1$
			retval.append("var typedInput = plugins.dialogs.showInputDialog('Specify','Your name');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("showSelectDialog".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//show select,returns nothing when canceled \n"); //$NON-NLS-1$
			retval.append("var selectedValue = plugins.dialogs.showSelectDialog('Select','please select a name','jan','johan','sebastiaan');\n"); //$NON-NLS-1$
			retval.append("//also possible to pass array with options\n"); //$NON-NLS-1$
			retval.append("//var selectedValue = plugins.dialogs.showSelectDialog('Select','please select a name', new Array('jan','johan','sebastiaan'));\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String methodName)
	{
		if ("showErrorDialog".equals(methodName) || "showInfoDialog".equals(methodName) || "showWarningDialog".equals(methodName) || "showQuestionDialog".equals(methodName)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{
			return "Shows a message dialog with the specified title, message and a customizable set of buttons."; //$NON-NLS-1$
		}
		else if ("showInputDialog".equals(methodName)) //$NON-NLS-1$
		{
			return "Shows an input dialog where the user can enter data. Returns the entered data, or nothing when canceled."; //$NON-NLS-1$
		}
		else if ("showSelectDialog".equals(methodName)) //$NON-NLS-1$
		{
			return "Shows a selection dialog, where the user can select an entry from a list of options. Returns the selected entry, or nothing when canceled."; //$NON-NLS-1$
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
		return null;
	}
}
