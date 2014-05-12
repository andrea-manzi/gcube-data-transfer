package org.gcube.datatransfer.portlets.sm.user.client.panels;

import org.gcube.datatransfer.portlets.sm.user.client.Common;
import org.gcube.datatransfer.portlets.sm.user.client.GuiStateHolder;
import org.gcube.datatransfer.portlets.sm.user.client.Side;
import org.gcube.datatransfer.portlets.sm.user.client.StorageManagerPortlet;
import org.gcube.datatransfer.portlets.sm.user.client.StorageType;
import org.gcube.datatransfer.portlets.sm.user.client.TreeLoaderBuilder;
import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpMongoDB;
import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpUploadFile;
import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpUris;
import org.gcube.datatransfer.portlets.sm.user.client.utils.Utils;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.WidgetComponent;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN), Michal Simon(CERN)
 *
 */

public class MainToolbar extends FramedPanel{
	private int width;

	public SimpleComboBox<String> comboAgent;//,combo1, destCombo;
	public TextField leftSideF,rightSideF;
	public LabelToolItem leftSideLabel,leftSideLabelF, rightSideLabel, rightSideLabelF;
	public TextButton hideSchedulerButton, showSchedulerButton, sendButton;

	public String lastCombo1Value, lastDestComboValue;

	public ToolTipConfig rightAnchor, leftAnchor;
	public ToolBar toolbarLeftUp,toolbarRightUp,toolbarRightF, toolbarLeftF;

	private TextButton leftMongoDBButton, rightWorkspaceButton, rightUploadButton, rightUriButton, rightMongoDBButton;
	
	public MainToolbar(){
		this.width = Common.totalWidth - 25;
		create();
	}
	
	public void enableAdminInterface() {
		
		leftMongoDBButton.setVisible(true);
		rightWorkspaceButton.setVisible(true);
		rightUploadButton.setVisible(true);
		rightUriButton.setVisible(true);
		
		rightMongoDBButton.setText("Storage Manager");
		
		toolbarLeftUp.forceLayout();
		toolbarRightUp.forceLayout();
	}
	
	public void create(){
		this.setHeadingHtml("");
		this.setPixelSize(width, 130);
		// panel.addStyleName("margin-25");

		VerticalLayoutContainer mainLayout = new VerticalLayoutContainer();
		HorizontalLayoutContainer outer = new HorizontalLayoutContainer();
		VerticalLayoutContainer outer1 = new VerticalLayoutContainer();
		VerticalLayoutContainer outer2 = new VerticalLayoutContainer();

		//LEFT SIDE
		toolbarLeftUp = new ToolBar();
		toolbarLeftF = new ToolBar();	
		leftSideLabel = new LabelToolItem("Select Files From: ");

		leftMongoDBButton = new TextButton("Storage Manager");		       
		leftMongoDBButton.setBorders(true);
		leftMongoDBButton.addSelectHandler(new SelectHandler() {				
			@Override
			public void onSelect(SelectEvent event) {

				onSelectButton(Side.LEFT, StorageType.MONGODB);					
			}
		});
		leftMongoDBButton.setVisible(false);

		TextButton leftWorkspaceButton = new TextButton("Workspace");		
		leftWorkspaceButton.setBorders(true);
		leftWorkspaceButton.addSelectHandler(new SelectHandler() {				
			@Override
			public void onSelect(SelectEvent event) {

				onSelectButton(Side.LEFT, StorageType.WORKSPACE);		            
			}
		});
		
		TextButton leftUploadButton = new TextButton("Upload");		
		leftUploadButton.setBorders(true);
		leftUploadButton.addSelectHandler(new SelectHandler() {				
			@Override
			public void onSelect(SelectEvent event) {

				onSelectButton(Side.LEFT, StorageType.UPLOAD);
			}
		});
		
		TextButton leftUriButton = new TextButton("URI");	
		leftUriButton.setBorders(true);
		leftUriButton.addSelectHandler(new SelectHandler() {				
			@Override
			public void onSelect(SelectEvent event) {

				onSelectButton(Side.LEFT, StorageType.URI);
			}		 
		});


		toolbarLeftUp.setBorders(true);
		toolbarLeftUp.add(leftSideLabel);
		toolbarLeftUp.add(leftMongoDBButton);
		toolbarLeftUp.add(leftWorkspaceButton);
		toolbarLeftUp.add(leftUploadButton);
		toolbarLeftUp.add(leftUriButton);
		outer1.add(toolbarLeftUp);

		if (leftSideF == null) {
			leftSideLabelF = new LabelToolItem("Current Folder: ");
			leftSideF = new TextField();
			leftSideF.setValue(Common.defaultFolder,true);
			// rightSideF.setToolTipConfig(createAnchor(""));
			leftSideF.setAllowBlank(false);
			leftSideF
			.addValueChangeHandler(new ValueChangeHandler<String>() {
				public void onValueChange(ValueChangeEvent<String> event) {
					//	Info.display("Left side's Folder Changed",
					//			"Left side's Folder Changed to "+ 
					//	event.getValue() == null ? "blank": event.getValue());
				}
			});
			leftSideF.disable();
		}

		toolbarLeftF.setBorders(true);
		toolbarLeftF.add(leftSideLabelF);
		toolbarLeftF.add(leftSideF);		

		if(leftAnchor==null)leftAnchor=StorageManagerPortlet.instance.utils.createAnchor(leftSideF.getCurrentValue());
		else leftAnchor.setBodyText(leftSideF.getCurrentValue());
		toolbarLeftF.setToolTipConfig(leftAnchor);
//		outer1.add(toolbarLeftF);

		//RIGHT SIDE
		toolbarRightUp = new ToolBar();
		toolbarRightF = new ToolBar();		

		rightSideLabel = new LabelToolItem("Select Files From: ");

		rightMongoDBButton = new TextButton("Storage Area");		
		rightMongoDBButton.setBorders(true);
		rightMongoDBButton.addSelectHandler(new SelectHandler() {				
			@Override
			public void onSelect(SelectEvent event) {

				onSelectButton(Side.RIGHT, StorageType.MONGODB);
			}
		});
		rightWorkspaceButton = new TextButton("Workspace");	
		rightWorkspaceButton.setBorders(true);
		rightWorkspaceButton.addSelectHandler(new SelectHandler() {				
			@Override
			public void onSelect(SelectEvent event) {

				onSelectButton(Side.RIGHT, StorageType.WORKSPACE);
			}
		});
		rightWorkspaceButton.setVisible(false);
		
		rightUploadButton = new TextButton("Upload");		
		rightUploadButton.setBorders(true);
		rightUploadButton.addSelectHandler(new SelectHandler() {				
			@Override
			public void onSelect(SelectEvent event) {

				onSelectButton(Side.RIGHT, StorageType.UPLOAD);
			}
		});
		rightUploadButton.setVisible(false);
		
		rightUriButton = new TextButton("URI");	
		rightUriButton.setBorders(true);
		rightUriButton.addSelectHandler(new SelectHandler() {				
			@Override
			public void onSelect(SelectEvent event) {

				onSelectButton(Side.RIGHT, StorageType.URI);
			}		 
		});
		rightUriButton.setVisible(false);

		toolbarRightUp.setBorders(true);
		toolbarRightUp.add(rightSideLabel);
		toolbarRightUp.add(rightMongoDBButton);
		toolbarRightUp.add(rightWorkspaceButton);
		toolbarRightUp.add(rightUploadButton);
		toolbarRightUp.add(rightUriButton);
		outer2.add(toolbarRightUp);

		if (rightSideF == null) {
			rightSideLabelF = new LabelToolItem("Current Folder:");
			rightSideF = new TextField();

			rightSideF.setValue(Common.defaultFolder);
			// rightSideF.setToolTipConfig(createAnchor(""));
			rightSideF.setAllowBlank(false);
			rightSideF.addValueChangeHandler(new ValueChangeHandler<String>() {
				public void onValueChange(ValueChangeEvent<String> event) {
					//Info.display(
					//		"Right side's Folder Changed",
					//		"Right side's Folder Changed to "
					//				+ event.getValue() == null ? "blank"
					//						: event.getValue());
				}
			});
			rightSideF.disable();
		}

		toolbarRightF.setBorders(true);
		toolbarRightF.add(rightSideLabelF);
		toolbarRightF.add(rightSideF);		

		if(rightAnchor==null)	rightAnchor=StorageManagerPortlet.instance.utils.createAnchor(rightSideF.getCurrentValue());
		else rightAnchor.setBodyText(rightSideF.getCurrentValue());
		toolbarRightF.setToolTipConfig(rightAnchor);
//		outer2.add(toolbarRightF);
		
		outer.add(outer1, new HorizontalLayoutData(.5, 1));
		outer.add(outer2, new HorizontalLayoutData(.5, 1));
		
		mainLayout.add(
				new WidgetComponent(
						new Image(
								LogoResource.INSTANCE.logo()
							)
					)
			);
		
		mainLayout.add(outer);
		
		this.setWidget(mainLayout);
	}

	public void onSelectButton(Side side, StorageType type){
		
		switch (type) {
		
		case MONGODB:
			PopUpMongoDB.getInstance(side).popUpNow();
			break;
			
		case UPLOAD:
			PopUpUploadFile.getInstance(side).popUpNow();
			break;
			
		case URI:
			PopUpUris.getInstance(side).popUpNow();
			break;
			
		case WORKSPACE:
			StorageManagerPortlet.instance.sourceAndDestination.setStorageType(side, type);
			TreeGrid<BaseDto> tree = GuiStateHolder.tree(side);
			TreeLoaderBuilder builder = new TreeLoaderBuilder(tree);
			builder.buildWorkspaceDBProxy();
			builder.buildServiceReader();
			TreeLoader<BaseDto> loader = builder.get();
			GuiStateHolder.setLoader(side, loader);
			tree.setTreeLoader(loader);
			loader.load();
			GuiStateHolder.enableDropping(side);
			GuiStateHolder.enableMenu(side);
			break;

		default:
			// ...
			break;
		}
	}

	public void setLeftSideFolder(String value, boolean fire){
		leftSideF.setValue(value,fire);
		leftAnchor.setBodyText(value);
	}
	public void setLeftSideFolderToParent(boolean fire){
		String path = leftSideF.getCurrentValue();
		String parentPath=Utils.getParentPath(path);
		if(parentPath!=null){
			leftSideF.setValue(parentPath,fire);
			leftAnchor.setBodyText(parentPath);
		}	
	}
	public void setRightSideFolder(String value,boolean fire){
		rightSideF.setValue(value,fire);
		rightAnchor.setBodyText(value);
	}
	public void setRightSideFolderToParent(boolean fire){
		String path = rightSideF.getCurrentValue();
		String parentPath=Utils.getParentPath(path);
		if(parentPath!=null){
			rightSideF.setValue(parentPath,fire);
			rightAnchor.setBodyText(parentPath);
		}	
	}

	public void refreshPanel(){

		if(leftAnchor==null)leftAnchor=StorageManagerPortlet.instance.utils.createAnchor("");
		else leftAnchor.setBodyText(leftSideF.getCurrentValue());
		toolbarLeftF.setToolTipConfig(leftAnchor);

		if(rightAnchor==null)	rightAnchor=StorageManagerPortlet.instance.utils.createAnchor(rightSideF.getCurrentValue());
		else rightAnchor.setBodyText(rightSideF.getCurrentValue());
		toolbarRightF.setToolTipConfig(rightAnchor);
	}
	public void reloadSize(){
		this.width = Common.totalWidth - 25;
		this.setPixelSize(width, 100);
	}
}
