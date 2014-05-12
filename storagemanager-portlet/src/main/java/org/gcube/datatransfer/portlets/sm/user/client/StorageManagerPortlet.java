package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.List;

import org.gcube.datatransfer.portlets.sm.user.client.panels.MainToolbar;
import org.gcube.datatransfer.portlets.sm.user.client.panels.SourceAndDestination;
import org.gcube.datatransfer.portlets.sm.user.client.popup.MainDialogBox;
import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpCreateNewFolder;
import org.gcube.datatransfer.portlets.sm.user.client.utils.Utils;
import org.gcube.datatransfer.portlets.sm.user.shared.StorageManagerServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.info.Info;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class StorageManagerPortlet implements EntryPoint, IsWidget{

	// services
	public static final StorageManagerServiceAsync storagemanagerService = StorageManagerServiceHolder.getInstance();

	public static StorageManagerPortlet instance;
	//general
	public RPCalls rpCalls;
	public Utils utils; 
	public boolean callingWorkspaceRootLeft,callingWorkspaceRootRight;
	public String idWorkspaceRootLeft, idWorkspaceRootRight;
	public String workspaceRootLeftPath,workspaceRootRightPath;
	public List<String> inputUrls;
	public String pathForTempUploads; 

	//panels
	public FramedPanel panelGeneral;
	public VerticalPanel center;
	public MainToolbar mainToolbar;
	public SourceAndDestination sourceAndDestination;
	public PopupPanel loadingIconForLeft,loadingIconForRight;

	public PopupPanel getLoadingIconForLeft() {
		if (loadingIconForLeft == null) loadingIconForLeft = createLoadingIcon();
		return loadingIconForLeft;
	}
	
	public PopupPanel getLoadingIconForRight() {
		if (loadingIconForRight == null) loadingIconForRight = createLoadingIcon();
		return loadingIconForRight;
	}
	
	//pop up 
	public MainDialogBox mainDialogBox;

	public PopUpCreateNewFolder popUpCreateNewFolder;

	//json obj
	public String serializedTransferDetails;

	/*
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		instance=this;
		//create the main dialog box (will be used both for pop up messages and pop up panels)
		mainDialogBox=new MainDialogBox();
		mainDialogBox.setDialogBoxForMessages();

		utils = new Utils();
		rpCalls=new RPCalls();

		pathForTempUploads="/tmp/storagemanager-portlet/"+Common.resourceName;

		//initialize other pop up panels ... 
		popUpCreateNewFolder = new PopUpCreateNewFolder();

		// ... TIMERS ...

		Common.refreshLeftAndRightTimer= new Timer() {
			@Override
			public void run() {
				StorageManagerPortlet.instance.sourceAndDestination.onRefresh(Side.LEFT);
				StorageManagerPortlet.instance.sourceAndDestination.onRefresh(Side.RIGHT);
			}
		};
		Common.refreshLeftTimer= new Timer() {
			@Override
			public void run() {
				StorageManagerPortlet.instance.sourceAndDestination.onRefresh(Side.LEFT);
			}
		};
		Common.refreshRightTimer= new Timer() {
			@Override
			public void run() {
				StorageManagerPortlet.instance.sourceAndDestination.onRefresh(Side.RIGHT);
			}
		};
		Common.focusTimer = new Timer() {
			@Override
			public void run() {
				Common.foc.setFocus(true);
			}
		};
		Common.redrawCenterTimer = new Timer() {
			@Override
			public void run() {
				redrawCenter();
			}
		};

		// get user name and scope from the session
		rpCalls.getUserAndScopeAndRole();

		// set the border layout
		RootPanel.get("mainContainer").add(asWidgetLayout());
		Common.totalWidth = RootPanel.get("mainContainer").getOffsetWidth();
		if (Common.totalWidth < Common.minGenWidth)
			Common.totalWidth = Common.minGenWidth;

		// set the central widget for choosing files
		center.clear();
		mainToolbar=new MainToolbar();
		sourceAndDestination=new SourceAndDestination();
		center.add(mainToolbar);
		center.add(sourceAndDestination);

		// getting the workspace variable in order to retrieve later the
		// workspace root folderLeft
		rpCalls.getWorkspace();		

		// resize handler
		Window.addResizeHandler(new ResizeHandler() {
			Timer resizeTimer = new Timer() {
				@Override
				public void run() {
					reDraw();
				}
			};
			@Override
			public void onResize(ResizeEvent event) {
				resizeTimer.schedule(400); // ms
			}
		});		

	}


	public Widget asWidgetLayout() {
		// mainContainer takes the real width after resizing
		// because the width of it is in percents (100%)
		panelGeneral = new FramedPanel();
		panelGeneral.setHeadingText("Storage Manager Portlet");
		panelGeneral.setBorders(true);
		// panelGeneral.setHeight("100%");

		int offsetHeight = RootPanel.get().getOffsetHeight() - 150;
		if ((offsetHeight) < Common.minGenHeight){
			panelGeneral.setHeight(Common.minGenHeight);
			Common.panelGeneralHeight=Common.minGenHeight;
		}
		else {			
			panelGeneral.setHeight(offsetHeight);
			Common.panelGeneralHeight=offsetHeight;
			// panelGeneral.setHeight("99%");
		}

		if (RootPanel.get("mainContainer").getOffsetWidth() < Common.minGenWidth)
			panelGeneral.setWidth(Common.minGenWidth);
		else
			panelGeneral.setWidth("99%");

		panelGeneral.setBodyStyle("background: none; padding: 0px");

		center = new VerticalPanel();

		// Create a Dock Panel
		DockPanel dock = new DockPanel();
		// dock.setSpacing(4);
		dock.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
		dock.add(center, DockPanel.CENTER);

		// Return the content
		dock.ensureDebugId("cwDockPanel");
		panelGeneral.add(dock);

		return panelGeneral;
	}

	/*
	 * reDraw input: Nothing -- returns: Nothing Redraw all the components with
	 * the new width depends on the window resize (MainContainer resize)
	 */
	public void reDraw() {

		RootPanel.get("mainContainer").clear();
		RootPanel.get("mainContainer").add(asWidgetLayout());

		Common.totalWidth = RootPanel.get("mainContainer").getOffsetWidth();
		if (Common.totalWidth < Common.minGenWidth)
			Common.totalWidth = Common.minGenWidth;

		Info.display("Resize", "width="
				+ RootPanel.get("mainContainer").getOffsetWidth()
				+ " - height="
				+ RootPanel.get("mainContainer").getOffsetHeight());
		// center
		mainToolbar.refreshPanel();
		mainToolbar.reloadSize();
		sourceAndDestination.reloadSizeAndRefreshPanel();
		center.add(mainToolbar);
		center.add(sourceAndDestination);
	}

	public void redrawCenter() {
		center.clear();
		mainToolbar.refreshPanel();
		sourceAndDestination.refreshPanel();
		center.add(mainToolbar);
		center.add(sourceAndDestination);
	}

	@Override
	public Widget asWidget() {
		return null;
	}

	public PopupPanel createLoadingIcon() {
		PopupPanel loadingIcon = new PopupPanel();
		loadingIcon.setStyleName("imagePop");
		loadingIcon.getElement().getStyle().setBorderWidth(0, Unit.PX);
		return loadingIcon;
	}
	public void startLoadingIcon(Widget sender, PopupPanel loadingIcon) {
		int width, height;
		if (sender == null)
			return;
		height = sender.getOffsetHeight();
		width = sender.getOffsetWidth();
		loadingIcon.setPopupPosition(sender.getAbsoluteLeft() + width
				/ 2 - 20, sender.getAbsoluteTop() + height / 2 - 20);
		loadingIcon.show();
	}
	public void stopLoadingIcon(PopupPanel loadingIcon) {
		loadingIcon.hide();
		//sometimes it is blocked .. we hide again 
		if(loadingIcon.isShowing())loadingIcon.hide();
		loadingIcon.clear();
	}


	public void printMsg(String message){
		mainDialogBox.printMsgInDialogBox(message);
	}
	
}