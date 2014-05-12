package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.List;

import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class Reloader {

	public Reloader(TreeGrid<BaseDto> tree, Side side) {
	
		this.side = side;
		this.tree = tree;
	}
	
	public void addCallbackCount(int callbackCount) {
		this.callbackCount += callbackCount;	
	}
	
	protected void reloadDirectory(BaseDto dir, TreeGrid<BaseDto> tree) {
		
		// if it is root (null) reload the whole tree
		if (dir == null) 
			tree.getTreeLoader().load();
		// otherwise reload only the directory children
		else
			tree.getTreeLoader().loadChildren(dir);
	}
	
	protected void reload(BaseDto dir) {
		
		count++;
		if (count < callbackCount) return;
		
		// clear the directory (and its twin if necessary) first
		clear(dir);
		
		// reload the directory
		reloadDirectory(dir, tree);

		// now check if there is a twin on the other side
		Side other = side == Side.LEFT ? Side.RIGHT : Side.LEFT;
		// check if both sides have the same storage type
		if (GuiStateHolder.get(side) == GuiStateHolder.get(other)) {
			// the other tree
			TreeGrid<BaseDto> otherTree = GuiStateHolder.tree(other);
			// the twin directory
			BaseDto twin = null;
			// if directory is not null ...							
			if (dir != null) {
				// lookup the twin
				twin = otherTree.getTreeStore().findModelWithKey("f-" + dir.getName());
			}
			// reload the twin
			reloadDirectory(twin, otherTree);
		}
	}
	
	protected void clearDirectory(BaseDto dir, TreeGrid<BaseDto> tree) {
		// if the directory has not been loaded yet ...
		if (dir == null) return;
		// remove all items
		List<BaseDto> ch = tree.getTreeStore().getAllChildren(dir);
		if(!ch.isEmpty())
			tree.getTreeStore().removeChildren(dir);
		// close the node
		tree.setExpanded(dir, false);
	}
	
	protected void clear(BaseDto dir) {
		
		// in case of root we don't care (we will just reload everything
		if (dir == null) return;
		
		// clear the directory
		clearDirectory(dir, tree);
		
		// now check if there is a twin on the other side
		Side other = side == Side.LEFT ? Side.RIGHT : Side.LEFT;
		// check if both sides have the same storage type
		if (GuiStateHolder.get(side) == GuiStateHolder.get(other)) {
			// the other tree
			TreeGrid<BaseDto> otherTree = GuiStateHolder.tree(other);
			// the twin directory
			BaseDto twin = otherTree.getTreeStore().findModelWithKey("f-" + dir.getName());
			// clear the twin directory
			clearDirectory(twin, otherTree);
		}
	}
	
	private Side side;
	private TreeGrid<BaseDto> tree;
	
	private int callbackCount = 0;
	private int count = 0;
}
