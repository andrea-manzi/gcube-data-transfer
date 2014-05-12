package org.gcube.datatransfer.portlets.sm.user.client;

import org.gcube.datatransfer.portlets.sm.user.client.prop.UriProperties;
import org.gcube.datatransfer.portlets.sm.user.shared.prop.BaseDtoProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FocusPanel;




/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class Common {
	
	public static FocusPanel foc;
	// ... TIMERS ...
	public static Timer focusTimer;
	public static Timer refreshLeftAndRightTimer;
	public static Timer refreshLeftTimer;
	public static Timer refreshRightTimer;
	public static Timer redrawCenterTimer;
	
	public static String defaultFolder = "Empty ...";
	public static String defaultResourceName = "testing"; // this specific value will
	// be used only in case of not retrieved session
	public static String defaultScope = "/gcube/devsec"; // this specific value will
	// be used only in case of not retrieved session
	
	public static BaseDtoProperties baseDtoProp = GWT.create(BaseDtoProperties.class);
	public static UriProperties uriProp = GWT.create(UriProperties.class);

	public static boolean isAdmin = false;
	public static int minGenWidth = 800; // the minimum width of the portlet
	public static int totalWidth = 0;
	public static int minGenHeight = 340;// the minimum height of the portlet
	public static int panelGeneralHeight =340;// resizable! the panel general height of the portlet

	public static boolean gettingUserAndScope=false;
	
	public static String resourceName=defaultResourceName;
	public static String scope=defaultScope;
	
	//serialized objects
	public static String jsonWorkspace;
	


}
