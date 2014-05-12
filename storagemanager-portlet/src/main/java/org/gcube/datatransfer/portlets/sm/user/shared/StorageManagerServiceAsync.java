package org.gcube.datatransfer.portlets.sm.user.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface StorageManagerServiceAsync {
	void listFiles(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	void getWorkspaceFolder(String jsonWorkspace, String folderId,boolean needTheParent, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	void getUserAndScopeAndRole(AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	void getWorkspace(String username, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	void getFileListOfMongoDB(String smServiceClassSource,String smServiceNameSource,String smOwnerSource,String smAccessTypeSource, String smAreaTypeSource,String path,String scope,AsyncCallback<String> callback)
			throws IllegalArgumentException;
	void createNewFolderInMongoDB(String smServiceClassSource,String smServiceNameSource,String smOwnerSource,String smAccessTypeSource, String smAreaTypeSource,String path,String scope, AsyncCallback<Void> callback)
			throws IllegalArgumentException;	
	void deleteFolderInMongoDB(String smServiceClassSource,String smServiceNameSource,String smOwnerSource,String smAccessTypeSource, String smAreaTypeSource,String path,String scope, AsyncCallback<Void> callback)
			throws IllegalArgumentException;
	void transfer(String serializedTransferDetails,
			AsyncCallback<Map<String, String>> callback);
	void deleteUploadedFiles(String path, AsyncCallback<Void> callback)
			throws IllegalArgumentException;
	void createNewFolderInWorkspace(String serializedWorkspaceInfo,
			String parentFolderId, String newFolderName,
			AsyncCallback<String> callback);
	void deleteInWorkspace(String serializedWorkspaceInfo, String itemIdToRemove, AsyncCallback<Boolean> callback)
			throws IllegalArgumentException;

}
