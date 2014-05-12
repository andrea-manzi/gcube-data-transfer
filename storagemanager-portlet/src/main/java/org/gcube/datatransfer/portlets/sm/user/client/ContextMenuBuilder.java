package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.List;

import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.examples.resources.client.images.ExampleImages;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class ContextMenuBuilder {

	public ContextMenuBuilder(TreeGrid<BaseDto> tree) {
		
		this.tree = tree;
		side = GuiStateHolder.side(tree);
	}
	
	public void buildAddMenuItem() {
		
		// create add menu item
		MenuItem addFolder = new MenuItem();
		addFolder.setText("Create New Folder");
		addFolder.setIcon(ExampleImages.INSTANCE.add());
		
		// add event handler
		addFolder.addSelectionHandler(new SelectionHandler<Item>() {			 
			
			@Override
			public void onSelection(SelectionEvent<Item> event) {
			
				StorageManagerPortlet.instance.popUpCreateNewFolder.popUpNow(side);
			}
		});
		
		// add the item to menu
		menu.add(addFolder);
	}
	
	public void BuildDelMenuItem() {
		
		// create delete menu item
		MenuItem remove = new MenuItem();
		remove.setText("Remove");
		remove.setIcon(ExampleImages.INSTANCE.delete());
		
		// add event handler
		remove.addSelectionHandler(new SelectionHandler<Item>() {			 
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				
				List<BaseDto> selected = tree.getSelectionModel().getSelectedItems();

				// delete selected elements
				NodeDeleter deleter = new NodeDeleter(selected, side);
				deleter.delete();
			}

		});
		
		// add the item to menu
		menu.add(remove);
	}
	
	public Menu get() {
		// get the menu
		return menu;
	}
	
	private Menu menu = new Menu();
	
	private TreeGrid<BaseDto> tree;
	
	private Side side;
	
	private static final String root = "/"; 
}
