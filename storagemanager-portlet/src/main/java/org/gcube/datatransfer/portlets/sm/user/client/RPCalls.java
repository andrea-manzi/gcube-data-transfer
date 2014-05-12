package org.gcube.datatransfer.portlets.sm.user.client;


import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpMongoDB;
import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpUploadFile;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.info.Info;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN), Michal Simon (CERN)
 *
 */

public class RPCalls {

	/*
	 * getUserAndScope input: Nothing -- returns: Nothing Remote Procedure Call:
	 * getting the user and scope from the portal liferay
	 */
	public void getUserAndScopeAndRole() {
		
		StorageManagerPortlet.storagemanagerService.getUserAndScopeAndRole(new AsyncCallback<String>() {
			
			public void onFailure(Throwable caught) {
				Info.display("Remote Procedure Call","Failure retrieving user and scope");
				Common.gettingUserAndScope = false;

			}
			
			public void onSuccess(String userAndScope) {
				
//				userAndScope = "andrea.manzi--/gcube--true";
//				userAndScope = "andrea.manzi--/gcube--false";
				
				if (userAndScope == null) {
					Info.display("Warning",
							"user and scope were not loaded properly");
					Common.gettingUserAndScope = false;
				} else {
					Common.gettingUserAndScope = true;
					String[] parts = userAndScope.split("--");
					Common.defaultResourceName = parts[0];
					Common.defaultScope = parts[1];
					if (parts[2].compareTo("true") == 0) {
						Common.isAdmin = true;
						
						//StorageManagerPortlet.instance.mainToolbar.rightSideF.enable();
						//StorageManagerPortlet.instance.mainToolbar.leftSideF.enable();
					}
					
					Common.resourceName=parts[0];
					//refresh
					PopUpUploadFile.getInstance(Side.LEFT).refreshUsername();
					PopUpUploadFile.getInstance(Side.RIGHT).refreshUsername();
					StorageManagerPortlet.instance.pathForTempUploads="/tmp/storagemanager-portlet/"+Common.resourceName;

					Common.scope=parts[1];
					Info.display("Message", "user:" + parts[0] + " - scope:"
							+ parts[1]);
					
					if (Common.isAdmin) {
						StorageManagerPortlet.instance.mainToolbar.enableAdminInterface();
					}
					else {
						// load storage area
						PopUpMongoDB.getInstance(Side.RIGHT).popUpNow();
					}
				}
			}
		});
	}

	/*
	 * getWorkspace input: Nothing -- returns: Nothing Remote Procedure Call:
	 * getting the workspace as a json object
	 */
	public void getWorkspace() {
		StorageManagerPortlet.storagemanagerService.getWorkspace(Common.resourceName,
				new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Info.display("Warning", "Workspace was not loaded");
			}

			public void onSuccess(String result) {
				Common.jsonWorkspace = result;
				if (Common.jsonWorkspace == null) {
					Info.display("Message",
							"Workspace was not loaded in the first place");
				}
			}
		});
	}

	//get a local folder (used when you upload files)
	public void getLocalFolder(String path) {
//		if(StorageManagerPortlet.instance.loadingIconForLeft==null)StorageManagerPortlet.instance.loadingIconForLeft=StorageManagerPortlet.instance.createLoadingIcon();
//		StorageManagerPortlet.instance.startLoadingIcon(StorageManagerPortlet.instance.sourceAndDestination.leftTree,StorageManagerPortlet.instance.loadingIconForLeft);
//
//		// currentDataSourcePath=path;
//		StorageManagerPortlet.storagemanagerService.listFiles(StorageManagerPortlet.instance.pathForTempUploads, 
//				new AsyncCallback<String>() {
//			public void onFailure(Throwable caught) {
//				StorageManagerPortlet.instance.stopLoadingIcon(StorageManagerPortlet.instance.loadingIconForLeft);
//				Info.display("Warning", "Items were not loaded");
//				StorageManagerPortlet.instance.mainToolbar.leftSide.setValue(StorageManagerPortlet.instance.mainToolbar.lastCombo1Value,true);
//			}
//			public void onSuccess(String folderResult) {
//				StorageManagerPortlet.instance.stopLoadingIcon(StorageManagerPortlet.instance.loadingIconForLeft);
//				Common.folderResLeft = folderResult;
//				Common.folderLeft = null;
//				StorageManagerPortlet.instance.sourceAndDestination.leftStore=null;
//				StorageManagerPortlet.instance.redrawCenter();
//				StorageManagerPortlet.instance.mainToolbar.lastCombo1Value = StorageManagerPortlet.instance.mainToolbar.leftSide.getValue();
//				if (Common.folderResLeft == null)
//					Info.display("Message", "Items were not loaded");
//				else
//					Info.display("Message", "Items were loaded");
//			}
//		});
	}

	public void deleteUploadedFiles(boolean refreshSource_){
//		final String finalPathToDelete = StorageManagerPortlet.instance.pathForTempUploads;
//		final boolean refreshSource=refreshSource_;
//
//		StorageManagerPortlet.storagemanagerService.deleteUploadedFiles(finalPathToDelete, new AsyncCallback<Void>() {
//			public void onFailure(Throwable caught) {
//				Info.display("Warning", "Failed to delete the uploaded files");
//			}
//			public void onSuccess(Void result) {
//				Info.display("","Uploaded files have been deleted");
//				StorageManagerPortlet.instance.popUpUploadFile.localFilesDeleted=true;
//				if(refreshSource){
//					StorageManagerPortlet.instance.rpCalls.getLocalFolder(StorageManagerPortlet.instance.pathForTempUploads);
//					Common.refreshLeftTimer.schedule(1000);				
//				}
//			}
//		});
	}

}
