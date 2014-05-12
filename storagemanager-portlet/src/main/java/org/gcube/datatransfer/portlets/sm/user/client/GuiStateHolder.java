package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.dnd.core.client.TreeGridDropTarget;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class GuiStateHolder {
	
	public static void set(Side side, TreeGrid<BaseDto> tree) {
		// store the value
		sideToTree.put(side, tree);
		treeToSide.put(tree, side);
	}
	
	public static void set(Side side, StorageType type) {
		// store the value
		TreeGrid<BaseDto> tree = sideToTree.get(side);
		if (tree == null) return;
		treeToType.put(tree, type);
	}
	
	public static StorageType get(TreeGrid<BaseDto> tree) {
		// get the type
		StorageType type = treeToType.get(tree);
		// if its null it is empty
		if (type == null) return StorageType.EMPTY;
		// return otherwise
		return type;
	}
	
	public static StorageType get(Side side) {
		// get the tree
		TreeGrid<BaseDto> tree = sideToTree.get(side);
		// if a tree was not assigned yet it means it's empty 
		if (tree == null) return StorageType.EMPTY;
		// get the type for the tree
		StorageType type = treeToType.get(tree);
		// again if there's no mapping it means it's empty
		if (type == null) return StorageType.EMPTY;
		// return otherwise
		return type;
	}
	
	public static Side side(TreeGrid<BaseDto> tree) {
		// get value
		return treeToSide.get(tree);
	}
	
	public static TreeGrid<BaseDto> tree(Side side) {
		// get value
		return sideToTree.get(side);
	}
	
	public static void setDropper(Side side, TreeGridDropTarget<BaseDto> drop) {
		sideToDrop.put(side, drop);
	}
	
	public static void enableDropping(Side side) {
		// get the dropper
		TreeGridDropTarget<BaseDto> drop = sideToDrop.get(side);
		// if it's null there's nothing to do
		if (drop == null) return;
		// enable dropping
		drop.enable();
	}
	
	public static void disableDropping(Side side) {
		// get the dropper
		TreeGridDropTarget<BaseDto> drop = sideToDrop.get(side);
		// if it's null there's nothing to do
		if (drop == null) return;
		// disable dropping
		drop.disable();
	}
	
	public static void setMenu(Side side, Menu menu) {
		sideToMenu.put(side, menu);
	}
	
	public static void enableMenu(Side side) {
		// get the menu
		Menu menu = sideToMenu.get(side);
		// if it's null there's nothing to do
		if (menu == null) return;
		// enable menu
		menu.enable();
	}
	
	public static void disableMenu(Side side) {
		// get the menu
		Menu menu = sideToMenu.get(side);
		// if it's null there's nothing to do
		if (menu == null) return;
		// enable menu
		menu.disable();
	}
	
	public static void setLoader(Side side, TreeLoader<BaseDto> loader) {
		sideToLoader.put(side, loader);
	}
	
	public static TreeLoader<BaseDto> loader(Side side) {
		return sideToLoader.get(side);
	}

	// tree to storage type mapping
	private static Map<TreeGrid<BaseDto>, StorageType> treeToType = new HashMap<TreeGrid<BaseDto>, StorageType>();
	// side to tree mapping
	private static Map<Side, TreeGrid<BaseDto>> sideToTree = new EnumMap<Side, TreeGrid<BaseDto>>(Side.class);
	// tree to side mapping
	private static Map<TreeGrid<BaseDto>, Side> treeToSide = new HashMap<TreeGrid<BaseDto>, Side>();
	// side to drop mapping
	private static Map<Side, TreeGridDropTarget<BaseDto>> sideToDrop = new EnumMap<Side, TreeGridDropTarget<BaseDto>>(Side.class);
	// side to context menu mapping
	private static Map<Side, Menu> sideToMenu = new EnumMap<Side, Menu>(Side.class);
	// side to loader mapping
	private static Map<Side, TreeLoader<BaseDto>> sideToLoader = new EnumMap<Side, TreeLoader<BaseDto>>(Side.class);
	// side to MongoDB parameters
	private static Map<Side, Map<ServiceParameter, String>> sideToServiceParameter = new EnumMap<Side, Map<ServiceParameter, String>>(Side.class);
}
