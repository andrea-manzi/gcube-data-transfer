package org.gcube.datatransfer.portlets.sm.user.client.panels;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface LogoResource extends ClientBundle {

	public static final LogoResource INSTANCE = GWT.create(LogoResource.class);
	
	@Source("gStorageManager.gif")
	ImageResource logo();
}
