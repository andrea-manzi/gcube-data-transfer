package org.gcube.datatransfer.portlets.sm.user.client.panels;

import org.gcube.datatransfer.portlets.sm.user.client.Common;
import org.gcube.datatransfer.portlets.sm.user.client.FileBrowserBuilder;
import org.gcube.datatransfer.portlets.sm.user.client.GuiStateHolder;
import org.gcube.datatransfer.portlets.sm.user.client.LoadingIconMultiton;
import org.gcube.datatransfer.portlets.sm.user.client.Side;
import org.gcube.datatransfer.portlets.sm.user.client.StorageManagerPortlet;
import org.gcube.datatransfer.portlets.sm.user.client.StorageType;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;
/**
 *	
 * @author Nikolaos Drakopoulos (CERN), Michał Simon (CERN)
 *
 */

public class SourceAndDestination extends FramedPanel{
	public int width;
	public int heightForThis;
	public int heightForCp1;

	public HorizontalLayoutContainer outerToolbar;
	public TreeGrid<BaseDto> leftTree, rightTree;
	public HorizontalLayoutContainer vpListFiles;
	public ContentPanel cp1;

	public SourceAndDestination(){
		this.width= Common.totalWidth - 15;
		this.heightForThis=Common.panelGeneralHeight-143;
		this.heightForCp1=Common.panelGeneralHeight-143-57;
		create();
	}

	public void create(){
		cp1 = new ContentPanel();
		//cp1.setPixelSize(width - 15, 140);
		cp1.setPixelSize(width-15, heightForCp1);
		cp1.addStyleName("margin-8");

		VerticalLayoutContainer outer = new VerticalLayoutContainer();
		vpListFiles = new HorizontalLayoutContainer();

		FileBrowserBuilder fbb = new FileBrowserBuilder(Side.LEFT);
		leftTree = fbb.get();
		
		fbb = new FileBrowserBuilder(Side.RIGHT);
		rightTree = fbb.get();
		
		vpListFiles.add(leftTree, new HorizontalLayoutData(.5, 1));
		vpListFiles.add(rightTree, new HorizontalLayoutData(.5, 1));
		vpListFiles.setBorders(true);
		leftTree.setBorders(true);
		rightTree.setBorders(true);

		// *********** buttons ************

		TextButton refreshLeftButton = new TextButton("Refresh");
		refreshLeftButton.setToolTipConfig(StorageManagerPortlet.instance.utils.createAnchor("Refresh the left tree"));
		refreshLeftButton.setBorders(true);
		TextButton resetRightButton = new TextButton("Refresh");
		resetRightButton.setToolTipConfig(StorageManagerPortlet.instance.utils.createAnchor("Refresh the right tree"));
		resetRightButton.setBorders(true);
		refreshLeftButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				onRefresh(Side.LEFT);
			}
		});
		resetRightButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				onRefresh(Side.RIGHT);
			}
		});

		outer.add(vpListFiles, new VerticalLayoutData(1, 1));
		cp1.setWidget(outer);

		this.setHeaderVisible(false);
		//this.setPixelSize(width, 197);
		this.setPixelSize(width, heightForThis);

		VerticalLayoutContainer p = new VerticalLayoutContainer();
		p.add(cp1);


		if(outerToolbar==null){
			outerToolbar = new HorizontalLayoutContainer();
			outerToolbar.setWidth(width-15);
			ToolBar leftButtonBar = new ToolBar();
			ToolBar rigthButtonBar = new ToolBar();
			leftButtonBar.add(refreshLeftButton);
			rigthButtonBar.add(resetRightButton);

			VerticalLayoutContainer outer1 = new VerticalLayoutContainer();
			outer1.add(leftButtonBar);
			VerticalLayoutContainer outer2 = new VerticalLayoutContainer();
			outer2.add(rigthButtonBar);
			outerToolbar.add(outer1, new HorizontalLayoutData(.5, 1));
			outerToolbar.add(outer2, new HorizontalLayoutData(.5, 1));
		}
		p.add(outerToolbar);

		this.add(p);

		cp1.setHeadingHtml(createHeader(width));
	}


	public SafeHtml createHeader(int width) {
		String left = null;
		String right = null;

		switch(GuiStateHolder.get(Side.LEFT)) {
		
		case EMPTY:
			left =  "Empty ...";
			break;
			
		case MONGODB:
			left = "Storage Manager";
			break;
			
		case UPLOAD:
			left = "Uploaded Files";
			break;
			
		case URI:
			left = "URIs";
			break;
			
		case WORKSPACE:
			left = "Workspace";
			break;
			
		default:
			left = "";
			break;
		}
		
		switch(GuiStateHolder.get(Side.RIGHT)) {
		
		case EMPTY:
			right =  "Empty ...";
			break;
			
		case MONGODB:
			right = "Storage Manager";
			break;
			
		case UPLOAD:
			right = "Uploaded Files";
			break;
			
		case URI:
			right = "URIs";
			break;	

		case WORKSPACE:
			right = "Workspace";
			break;
			
		default:
			right = "";
			break;
		
		}

		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<table align=\"center\" height=\"2px\" width=\""
				+ width
				+ "\" "
				+ "style=\"font-size:14px;font-family:times;color:black;\"><tr>"
				+ "<td align=\"left\" height=\"2px\" width=\""
				+ width
				/ 2
				+ "\">"
				+ left
				+ "</td>"
				+ "<td align=\"left\" height=\"2px\" width=\""
				+ width
				/ 2
				+ "\">" + right + "</td>" + "</tr></table>");

		return builder.toSafeHtml();
	}

	public void onRefresh(Side side){
		
		LoadingIconMultiton.getInstance(side).reset();
		GuiStateHolder.loader(side).load();
	}

	public void onDeleteUploadedFiles(boolean refreshSource){
		StorageManagerPortlet.instance.rpCalls.deleteUploadedFiles(refreshSource);										
	}

	//it will be occured when the source type is changed .. 
	public void cleanLocalFiles(){
//		if(!StorageManagerPortlet.instance.popUpUploadFile.localFilesDeleted){
//			onDeleteUploadedFiles(false);
//		}		
	}

	public void refreshPanel(){
		this.clear();
		this.create();
	}

	public void reloadSizeAndRefreshPanel(){
		this.clear();
		this.outerToolbar=null;
		this.width= Common.totalWidth - 25;
		this.heightForThis=Common.panelGeneralHeight-143;
		this.heightForCp1=Common.panelGeneralHeight-143-57;
		this.create();
	}
	
	public void setStorageType(Side side, StorageType type) {
		
		GuiStateHolder.set(side, type);

		cp1.setHeadingHtml(
				StorageManagerPortlet.instance.sourceAndDestination.createHeader(
						StorageManagerPortlet.instance.sourceAndDestination.width
					)
			);
	}	
}
