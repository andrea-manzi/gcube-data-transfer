package org.gcube.datatransfer.portlets.sm.user.client;

import org.gcube.datatransfer.portlets.sm.user.shared.StorageManagerService;
import org.gcube.datatransfer.portlets.sm.user.shared.StorageManagerServiceAsync;

import com.google.gwt.core.client.GWT;

public class StorageManagerServiceHolder {
	
	private StorageManagerServiceHolder() { }
	
	private static final StorageManagerServiceAsync INSTANCE = GWT.create(StorageManagerService.class);
	
	public static StorageManagerServiceAsync getInstance() {
		return StorageManagerServiceHolder.INSTANCE;
	}
}
