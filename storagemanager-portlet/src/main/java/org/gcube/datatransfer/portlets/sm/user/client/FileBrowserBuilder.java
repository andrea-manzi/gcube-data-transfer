package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.gcube.datatransfer.portlets.sm.user.client.utils.Utils;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;
import org.gcube.datatransfer.portlets.sm.user.shared.prop.BaseDtoProperties;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreAddEvent.StoreAddHandler;
import com.sencha.gxt.dnd.core.client.DND;
import com.sencha.gxt.dnd.core.client.TreeGridDragSource;
import com.sencha.gxt.dnd.core.client.TreeGridDropTarget;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class FileBrowserBuilder {
	
	public FileBrowserBuilder (Side side) {
		
		BaseDtoProperties props = GWT.create(BaseDtoProperties.class);
		
//		int width = (Common.totalWidth - 25 - 50) / 2;
		int width = 90;
		
	    ColumnConfig<BaseDto, String> ccname = new ColumnConfig<BaseDto, String>(props.shortname(), 2 * width, "Name");
	    ColumnConfig<BaseDto, String> ccowner = new ColumnConfig<BaseDto, String>(props.owner(), width, "Owner");
	    ColumnConfig<BaseDto, String> cctype = new ColumnConfig<BaseDto, String>(props.type(), width, "Type");
	    ColumnConfig<BaseDto, String> cclastupdate = new ColumnConfig<BaseDto, String>(props.lastUpdate(), width, "Last update");
	    ColumnConfig<BaseDto, String> ccsize = new ColumnConfig<BaseDto, String>(props.size(), width, "Size");
	     
	    List<ColumnConfig<BaseDto, ?>> l = new ArrayList<ColumnConfig<BaseDto, ?>>();
	    l.add(ccname);
	    l.add(ccowner);
	    l.add(cctype);
	    l.add(cclastupdate);
	    l.add(ccsize);
	    
	    // overload 'hasChildren' so it reports that a folder has always children
	    // this way a proper icon is display
	    final TreeStore<BaseDto> store = new TreeStore<BaseDto>(BaseDtoProperties.key) {
	    	
	    	public boolean hasChildren(BaseDto parent) {
	    		
		    	// if it is the root directory ...
		    	if (parent == null) return true;
		    	// if it is an instance of FolderDto ...
		    	if (parent instanceof FolderDto) {
		    		
		    		FolderDto folder = (FolderDto) parent;
		    		
		    		return folder.getName().endsWith("/");
		    	}
		    	// otherwise it is not a folder
		    	return false;
	    	}
	    };
	    
		// create the tree
		tree = new TreeGrid<BaseDto>(store, new ColumnModel<BaseDto>(l), ccname);
		
		// set sorting method 
		tree.getTreeStore().addSortInfo(
				new StoreSortInfo<BaseDto>(new Comparator<BaseDto>() {

					@Override
					public int compare(BaseDto o1, BaseDto o2) {

						boolean dir1 = o1.getName().endsWith("/"), dir2 = o2.getName().endsWith("/");
						// if the first one is a dir and the second one not ...
						if (dir1 && !dir2) return -1; 
						// if the second one is a dir and the first one not ...
						if (!dir1 && dir2) return 1;
						
						return o1.getShortname().compareToIgnoreCase(o2.getShortname());
					}
					
				}, SortDir.ASC)
			);
		
		// store the information in GuiStateHolder
		GuiStateHolder.set(side, tree);
		
		// enable Drag&Drop
		new TreeGridDragSource<BaseDto>(tree);
		TreeGridDropTarget<BaseDto> drop = new TreeGridDropTarget<BaseDto>(tree);
		drop.setOperation(DND.Operation.COPY);
		drop.disable(); // will be enabled if a button is pressed
		
		GuiStateHolder.setDropper(side, drop);
		
		// create Drag&Drop handler
		final TreeDNDHandler dndHandler = new TreeDNDHandler(tree, side);

		// add Drag&Drop handling
		tree.getTreeStore().addStoreAddHandler(new StoreAddHandler<BaseDto>() {
			@Override
			public void onAdd(StoreAddEvent<BaseDto> event) {
				
				dndHandler.handle(event);
			}
		});
		
		// build the context menu
		ContextMenuBuilder menuBuilder = new ContextMenuBuilder(tree);
		menuBuilder.buildAddMenuItem();
		menuBuilder.BuildDelMenuItem();
		Menu menu = menuBuilder.get();
		// add the context menu to the tree
		tree.setContextMenu(menu);
		GuiStateHolder.setMenu(side, menu);
		
		menu.disable();
		
		tree.setIconProvider(Utils.iconProvider);
	}

	public TreeGrid<BaseDto> get() {
		// get the tree
		return tree;
	}

//	private Tree<BaseDto, String> tree;
	private TreeGrid<BaseDto> tree;
}


