package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.EnumMap;
import java.util.Map;

import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.user.client.ui.PopupPanel;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class LoadingIconMultiton {

	private LoadingIconMultiton (Side side) {
		
		tree = GuiStateHolder.tree(side);
		
		switch (side) {
		
		case LEFT:
			icon = StorageManagerPortlet.instance.getLoadingIconForLeft();
			break;
			
		case RIGHT:
			icon = StorageManagerPortlet.instance.getLoadingIconForRight();
			break;			
		}
	}
	
	static public LoadingIconMultiton getInstance(Side side) {
		
		if (instances == null) instances = new EnumMap<Side, LoadingIconMultiton>(Side.class);
		
		LoadingIconMultiton instance = instances.get(side);
		if (instance == null) {
			instance = new LoadingIconMultiton(side);
			instances.put(side, instance);
		}
		
		return instance;
	}
	
	public void startLodingIcon() {

		count++;
		tree.disable();
		StorageManagerPortlet.instance.startLoadingIcon(tree, icon);
	}
	
	public void stopLodingIcon() {

		count--;
		if (count > 0) return;
		StorageManagerPortlet.instance.stopLoadingIcon(icon);
		tree.enable();
	}
	
	public void reset() {
		count = 0;
		StorageManagerPortlet.instance.stopLoadingIcon(icon);
		tree.enable();
	}
	
	private PopupPanel icon;
	private TreeGrid<BaseDto> tree;
	private int count = 0;
	
	private static Map<Side, LoadingIconMultiton> instances;
}
