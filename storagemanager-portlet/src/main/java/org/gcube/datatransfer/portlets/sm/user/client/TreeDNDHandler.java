package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpMongoDB;
import org.gcube.datatransfer.portlets.sm.user.shared.StorageManagerServiceAsync;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.TransferDetails;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class TreeDNDHandler extends Reloader {

	public TreeDNDHandler(TreeGrid<BaseDto> tree, Side side) {

		super(tree, side);
		
		this.tree = tree;
		this.side = side;
	}
	
	public void handle(final StoreAddEvent<BaseDto> event) {
		// get items that have been dragged and dropped
		List<BaseDto> items = getItems(event);
		// if the list is empty there is nothing to do
		if (items.isEmpty()) return;
		// determine the destination folder (it should be the same for all items)
		BaseDto	parent = tree.getTreeStore().getParent(items.get(0));
		// and do the copy
		copy(items, parent, true);
	}
	
	void copy(List<? extends BaseDto> items, BaseDto parent, boolean reload) {
		// get files to be copied
		List<BaseDto> files = getFiles(items);
		// get directories to be copied
		List<BaseDto> dirs = getDirectories(items);
		// set the number of callbacks (for all the files there is just one callback)
		if (reload) addCallbackCount(dirs.size() + (files.isEmpty() ? 0 : 1));
		// copy files
		copyFiles(files, parent, reload);
		// copy directories
		copyDirectories(dirs, parent, reload);		
	}	
	
	void copyFiles(final List<? extends BaseDto> items, final BaseDto parent, final boolean reload) {
		
		// Prepare transfer details
		String transferDetails = prepare(items, parent);
		
		// if there are no details ...
		if (transferDetails == null) return;
		
		// start loading icon (it will be stopped when files are copied)
		LoadingIconMultiton.getInstance(side).startLodingIcon();
		
		// get the service
		StorageManagerServiceAsync service = StorageManagerServiceHolder.getInstance();
		// submit transfer job
		service.transfer(
				transferDetails, 
				new AsyncCallback<Map<String, String>>() {

					@Override
					public void onFailure(Throwable caught) {
						// reload the directory
						if (reload) reload(parent);
						// stop load icon
						LoadingIconMultiton.getInstance(side).stopLodingIcon();
						// display message
						Info.display("","error during the transfer");
					}

					@Override
					public void onSuccess(Map<String, String> handles) {
						// reload directory if necessary
						if (reload) reload(parent);
						// stop loading icon
						LoadingIconMultiton.getInstance(side).stopLodingIcon();
						// display message
						if (handles == null || handles.isEmpty()) {
							Info.display("","error during the transfer");
						} else {
							Info.display("", "been transferred ...");
						}
					}
				
				}
			);
	}
	
	void copyDirectories(final List<BaseDto> items, final BaseDto parent, final boolean reload) {
		
		if (items.isEmpty()) return;
		
		// for each directory  ...
		for (BaseDto item : items) {
			// start loading icon (it will be stopped when directories are copied) 
			LoadingIconMultiton.getInstance(side).startLodingIcon();
			// create a folder maker object
			FolderMaker mkdir = new FolderMaker(item.getShortname(), parent, side);
			// item is the new parent and the new folder is the destination folder
			final BaseDto subParent = item;
			final BaseDto dst = mkdir.getNewDir();
			// disable reloading (we will do it manually)
			mkdir.setReload(false);
			// set a post action that gets folder's children and copy them
			mkdir.setPostMkDir(new FolderMaker.PostMkDir() {
				
				@Override
				public void postSuccess() {
					// reload the directory
					if (reload) reload(parent);
					// copy all children from the current item (directory) to the newly created folder
					copyChildren(subParent, dst);
				}
				
				@Override
				public void postFailure() {
					// reload the directory
					if (reload) reload(parent);
					// stop loading icon
					LoadingIconMultiton.getInstance(side).stopLodingIcon();
				}
			});
			// create the folder
			mkdir.mkdir();

		}
	}
	
	List<BaseDto> getFiles(List<? extends BaseDto> items) {
		
		List<BaseDto> list = new ArrayList<BaseDto>();
		for (BaseDto item : items) if (!item.getName().endsWith("/")) list.add(item);
		
		return list;
	}
	
	List<BaseDto> getDirectories(List<? extends BaseDto> items) {
		
		List<BaseDto> list = new ArrayList<BaseDto>();
		for (BaseDto item : items) if (item.getName().endsWith("/")) list.add(item);
		
		return list;
	}
	
	AsyncCallback<String> recurrentCopyCallback(final BaseDto destination) {
		
		AsyncCallback<String> ret = new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				// stop loading icon
				LoadingIconMultiton.getInstance(side).stopLodingIcon();
			}

			@Override
			public void onSuccess(String result) {
				// we care about the results only if it's not empty
				if (result != null && !result.isEmpty()) {
					// de-serialize the string 
					FolderDto fd = (FolderDto) FolderDto.createSerializer().deSerialize(result, FolderDto.class.getName());
					// if there are some children copy them
					if (!fd.getChildren().isEmpty())
						copy(fd.getChildren(), destination, false);
				}
				// stop loading icon
				LoadingIconMultiton.getInstance(side).stopLodingIcon();
			}
		};
		
		return ret;
	}
	
	
	void copyChildren (BaseDto parent, BaseDto destination) {
		// get service
		StorageManagerServiceAsync service = StorageManagerServiceHolder.getInstance();
		
		StorageType source = GuiStateHolder.get(
				side == Side.LEFT ? Side.RIGHT : Side.LEFT
			);
		
		
		switch(source) {
		
		case MONGODB:
			
			// get parameters		
		  	PopUpMongoDB params = PopUpMongoDB.getInstance(side == Side.LEFT ? Side.RIGHT : Side.LEFT);
		  	
		  	// get children from MongoDB
			service.getFileListOfMongoDB(
					params.get(ServiceParameter.CLASS),
					params.get(ServiceParameter.NAME),
					params.get(ServiceParameter.OWNER),
					params.get(ServiceParameter.ACCESS_TYPE),
					params.get(ServiceParameter.AREA_TYPE),
					parent.getName(), 
					Common.scope,
					recurrentCopyCallback(destination)
				);			
			
			break;
			
		case WORKSPACE:
			
			// parent id
			String folderId = parent.getIdInWorkspace();
			// need parent flag
			boolean needTheParent = false;

			// get the children from workspace
			service.getWorkspaceFolder(
					Common.jsonWorkspace, 
					folderId, 
					needTheParent, 
					recurrentCopyCallback(destination)
				);
			
			break;
		
		default:
			// do nothing (we deal with directories only in case of MongoDB and Workspace)
			break;
		}
		

	}
	
	String prepare (List<? extends BaseDto> items, BaseDto parent) {
		
		// check if there is staff to transfer
		if (items.isEmpty()) return null;
		
		// determine transfer details
		TransferDetailsBuilder builder = new TransferDetailsBuilder(parent, items);
		
		// destination
		switch (GuiStateHolder.get(tree)) {
		
		case MONGODB:
			// set destination type
			builder.buildMongoDbDestinationType();
			// set destination folder
			builder.buildMongoDBDestinationFolder();
			// set destination parameters (depending which tree is the destination
			builder.buildMongoDbDestinationParameters(side);
			break;
			
		case WORKSPACE:
			// set destination type
			builder.buildWorkspaceDestinationType();
			// set destination folder
			builder.buildWorkspaceDestinationFolder();
			break;
			
		default:
			break;
		}
		
		// check which one is the other side
		Side other = GuiStateHolder.side(tree) == Side.LEFT ? Side.RIGHT : Side.LEFT;
		
		// source
		switch (GuiStateHolder.get(other)) {
		
		case MONGODB:
			// set source files to be transfered
			builder.buildMongoDBSourceFiles();
			// set source type
			builder.buildMongoDbSourceType();
			// set source parameters (in this case it is the other way round)
			builder.buildMongoDBSourceParameters(other);
			break;
			
		case WORKSPACE:
			// set source files to be transfered
			builder.buildWorkspaceSourceFiles();
			// set source type
			builder.buildWorkspaceSourceType();
			break;
			
		case UPLOAD:
			// set source files to be transfered
			builder.buildExternalSourceFiles();
			// set source type
			builder.buildUploadSourceType();
			break;
			
		case URI:
			// set source files to be transfered
			builder.buildExternalSourceFiles();
			// set source type
			builder.buildUriSourceType();
			break;
			
		default:
			break;
		}
		
		// serialize transfer details
		return TransferDetails.createSerializer().serialize(builder.get());
	}
	
	private List<BaseDto> getItems(StoreAddEvent<BaseDto> event) {
		
		List<BaseDto> ret = new ArrayList<BaseDto>();
		
		for (BaseDto item : event.getItems()) {

			// if the ID equals -1 it means that it has been marked as added manually
			// change the ID so in the future when the item is dragged and dropped it
			// will be handled correctly
			if (item.getId() == -1) {
				item.setId(1);
				continue;
			}
			
			// if the ID is not -1 add it
			ret.add(item);
		}
		
		return ret;
	}
	
	private TreeGrid<BaseDto> tree;
	private Side side;
//	private boolean filesReadyToReload = false;
//	private boolean dirsReadyToReload = false;
//	private int dirReadyToReload = 0;
//	private int copyCounter = 0;
//	private int nItemsToCopy = 0;
}
