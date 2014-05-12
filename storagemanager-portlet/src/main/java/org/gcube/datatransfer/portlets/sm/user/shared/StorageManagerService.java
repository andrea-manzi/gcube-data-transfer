package org.gcube.datatransfer.portlets.sm.user.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("storagemanager")
public interface StorageManagerService extends RemoteService {
	String listFiles(String name) throws IllegalArgumentException;
	String getWorkspaceFolder(String jsonWorkspace, String folderId, boolean needTheParent) throws IllegalArgumentException;
	String getUserAndScopeAndRole() throws IllegalArgumentException;
	String getWorkspace(String username) throws IllegalArgumentException;
	String getFileListOfMongoDB(String smServiceClassSource,
			String smServiceNameSource, String smOwnerSource,
			String smAccessTypeSource, String smAreaTypeSource, String path,
			String scope);
	void createNewFolderInMongoDB(String smServiceClassSource,
			String smServiceNameSource, String smOwnerSource,
			String smAccessTypeSource, String smAreaTypeSource, String path, String scope);
	void deleteFolderInMongoDB(String smServiceClassSource,
			String smServiceNameSource, String smOwnerSource,
			String smAccessTypeSource, String smAreaTypeSource, String path, String scope);
	Map<String, String> transfer(String serializedTransferDetails) throws IllegalArgumentException;
	void deleteUploadedFiles(String path)throws IllegalArgumentException;
	String createNewFolderInWorkspace(String serializedWorkspaceInfo, String parentFolderId,String newFolderName)throws IllegalArgumentException;
	boolean deleteInWorkspace(String serializedWorkspaceInfo, String itemIdToRemove)throws IllegalArgumentException;

}
