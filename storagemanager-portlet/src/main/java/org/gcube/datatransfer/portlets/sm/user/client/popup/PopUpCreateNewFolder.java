package org.gcube.datatransfer.portlets.sm.user.client.popup;

import org.gcube.datatransfer.portlets.sm.user.client.Common;
import org.gcube.datatransfer.portlets.sm.user.client.FolderMaker;
import org.gcube.datatransfer.portlets.sm.user.client.GuiStateHolder;
import org.gcube.datatransfer.portlets.sm.user.client.Side;
import org.gcube.datatransfer.portlets.sm.user.client.StorageManagerPortlet;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN), Michal Simon(CERN)
 *
 */

public class PopUpCreateNewFolder extends FocusPanel{

	public PopUpCreateNewFolder(){
		create();
	}

	public void create(){
		FramedPanel panel = new FramedPanel();
		panel.setHeadingText("Create a New Folder");
		panel.setWidth(300);
		panel.setBodyStyle("background: none; padding: 5px");
		VerticalLayoutContainer p = new VerticalLayoutContainer();

		p.add(new Label(" "), new VerticalLayoutData(1, -1));

		if (newFolderField == null) {
			newFolderField = new TextField();
			newFolderField.setAllowBlank(false);
		}
		p.add(new FieldLabel(newFolderField, "New Folder"),
				new VerticalLayoutData(1, -1));
		newFolderField.addKeyDownHandler(keyDownHandler);

		// ************** buttons **************
		TextButton cancelButton = new TextButton("Cancel");
		cancelButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				StorageManagerPortlet.instance.mainDialogBox.hide();
				StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
			}
		});

		TextButton createButton = new TextButton("Create");
		createButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				onEnter();
			}
		});

		panel.setWidget(p);
		panel.setButtonAlign(BoxLayoutPack.START);
		panel.addButton(cancelButton);
		panel.addButton(createButton);

		// key handlers -------------
		this.addKeyDownHandler(keyDownHandler);
		this.add(panel);
		// --------------
	}

	KeyDownHandler keyDownHandler=new KeyDownHandler() {
		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
				StorageManagerPortlet.instance.mainDialogBox.hide();
				StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
			} else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				onEnter();
			}
		}
	};


	public void onEnter(){
		if (newFolderField.getCurrentValue() == null) {
			Info.display("","You must type a name! ");
			return;
		}

		StorageManagerPortlet.instance.mainDialogBox.hide();
		StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();

		//add new folder in the left tree
		TreeGrid<BaseDto> tree = GuiStateHolder.tree(side);
		BaseDto parent = tree.getSelectionModel().getSelectedItem();
		
		// make sure it is a directory
		if (parent != null && !parent.getName().endsWith("/")) {
			
			parent = tree.getTreeStore().getParent(parent);
		}
		
		FolderMaker fm = new FolderMaker(newFolderField.getCurrentValue(), parent, side);
		fm.mkdir();
		
		// reset the value in the dialog box
		newFolderField.setValue(new String());
	}

	public void popUpNow(Side side){		

		Common.foc=this;
		this.side = side;
		StorageManagerPortlet.instance.mainDialogBox.popUpSomePanel(this);
	}
	
	public TextField newFolderField;
	private Side side;
}
