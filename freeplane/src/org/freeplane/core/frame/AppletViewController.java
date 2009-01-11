/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneVersionInformation;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.ui.components.FreeplaneMenuBar;


/**
 * @author Dimitry Polivaev
 */
public class AppletViewController extends ViewController {
	final private JApplet applet;
	private JComponent mComponentInSplitPane;
	private JPanel southPanel;

	public AppletViewController(final JApplet applet) {
		this.applet = applet;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.views.ViewController#getContentPane()
	 */
	@Override
	public Container getContentPane() {
		return applet.getContentPane();
	}

	@Override
	public FreeplaneMenuBar getFreeplaneMenuBar() {
		return (FreeplaneMenuBar) applet.getJMenuBar();
	}

	public FreeplaneVersionInformation getFreeplaneVersion() {
		return Controller.VERSION;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getJFrame()
	 */
	@Override
	public JFrame getJFrame() {
		throw new IllegalArgumentException("The applet has no frames");
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getSouthPanel()
	 */
	public JPanel getSouthPanel() {
		return southPanel;
	}

	@Override
	public void init() {
		final Controller controller = Controller.getController();
		controller.getViewController().changeAntialias(
		    Controller.getResourceController().getProperty(ViewController.RESOURCE_ANTIALIAS));
		controller.getViewController().setToolbarVisible(false);
		controller.getViewController().setMenubarVisible(false);
		getContentPane().add(getScrollPane(), BorderLayout.CENTER);
		southPanel = new JPanel(new BorderLayout());
		southPanel.add(getStatusLabel(), BorderLayout.SOUTH);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		super.init();
		SwingUtilities.updateComponentTreeUI(applet);
		if (!EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
					};
				});
			}
			catch (final InterruptedException e) {
				org.freeplane.core.util.Tools.logException(e);
			}
			catch (final InvocationTargetException e) {
				org.freeplane.core.util.Tools.logException(e);
			}
		}
		controller.selectMode(Controller.getResourceController().getProperty("initial_mode"));
		String initialMapName = Controller.getResourceController().getProperty(
		    "browsemode_initial_map");
		if (initialMapName != null && initialMapName.startsWith(".")) {
			/* new handling for relative urls. fc, 29.10.2003. */
			try {
				final URL documentBaseUrl = new URL(applet.getDocumentBase(), initialMapName);
				initialMapName = documentBaseUrl.toString();
			}
			catch (final java.net.MalformedURLException e) {
				Controller.getController().errorMessage(
				    "Could not open relative URL " + initialMapName + ". It is malformed.");
				System.err.println(e);
				return;
			}
			/* end: new handling for relative urls. fc, 29.10.2003. */
		}
		if (initialMapName != "") {
			try {
				final URL mapUrl = new URL(initialMapName);
				Controller.getModeController().getMapController().newMap(mapUrl);
			}
			catch (final Exception e) {
				org.freeplane.core.util.Tools.logException(e);
			}
		}
	}

	@Override
	public JSplitPane insertComponentIntoSplitPane(final JComponent pMindMapComponent) {
		if (mComponentInSplitPane == pMindMapComponent) {
			return null;
		}
		removeSplitPane();
		mComponentInSplitPane = pMindMapComponent;
		southPanel.add(pMindMapComponent, BorderLayout.CENTER);
		southPanel.revalidate();
		return null;
	}

	@Override
	public boolean isApplet() {
		return true;
	}

	@Override
	public void openDocument(final URL doc) throws Exception {
		applet.getAppletContext().showDocument(doc, "_blank");
	}

	@Override
	public void removeSplitPane() {
		if (mComponentInSplitPane != null) {
			southPanel.remove(mComponentInSplitPane);
			southPanel.revalidate();
			mComponentInSplitPane = null;
		}
	}

	@Override
	void setFreeplaneMenuBar(final FreeplaneMenuBar menuBar) {
		applet.setJMenuBar(menuBar);
	}

	@Override
	public void setTitle(final String title) {
	}

	@Override
	public void setWaitingCursor(final boolean waiting) {
		if (waiting) {
			applet.getRootPane().getGlassPane().setCursor(
			    Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			applet.getRootPane().getGlassPane().setVisible(true);
		}
		else {
			applet.getRootPane().getGlassPane().setCursor(
			    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			applet.getRootPane().getGlassPane().setVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#exit()
	 */
	@Override
	public void stop() {
	}

	public void start() {
		try {
			IMapSelection selection = Controller.getController().getSelection();
			
			if (selection != null) {
				selection.selectRoot();
			}
			else {
				System.err.println("View is null.");
			}
		}
		catch (final Exception e) {
			org.freeplane.core.util.Tools.logException(e);
		}
	}
}
