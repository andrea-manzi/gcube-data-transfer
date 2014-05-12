package org.gcube.datatransfer.portlets.sm.user.client;

import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpMongoDB;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.info.Info;

public class FolderMaker extends Reloader {
	
	public FolderMaker (String dirname, BaseDto parent, Side side) {
		
		super(GuiStateHolder.tree(side), side);
		
		// set ID to -1 to indicate it was manually added
		newdir = new FolderDto(-1, (parent == null ? "/" : parent.getName()) + dirname + "/");
		this.parent = parent;
		this.side = side;
	}
	
	public BaseDto getNewDir() {
		
		return newdir;
	}
	
	public void mkdir () {

		StorageType type = GuiStateHolder.get(side);
		
		switch (type) {
		
		case MONGODB:
			mkdirInMongoDB();
			break;
			
		case WORKSPACE:
			mkdirInWorkspace();
			break;
			
		default:
			// for other storage we do nothing
			break;
		}
	}
	

	private <T> AsyncCallback<T>  mkdirAsyncCallback() {
		
		return new AsyncCallback<T>() {
			
			public void onFailure(Throwable caught) {
				// first of all reload the parent if necessary 
				if(reload) reload(parent);
				LoadingIconMultiton.getInstance(side).stopLodingIcon();
				Info.display("Warning", "Failed to create new folder");
				// run the post mkdir action if it was specified
				if (postMkDir != null) postMkDir.postFailure();
			}
			
			public void onSuccess(T result) {
				// first of all reload the parent if necessary 
				if(reload) reload(parent);
				// if the id was passed from the server set it
				if (result instanceof String) newdir.setIdInWorkspace((String)result);
				// run the post mkdir action if it was specified
				if (postMkDir != null) postMkDir.postSuccess();
				// stop the icon
				LoadingIconMultiton.getInstance(side).stopLodingIcon();
			}
		};
	}
	
	private void mkdirInMongoDB () {
		
		PopUpMongoDB params = PopUpMongoDB.getInstance(side);
		
		LoadingIconMultiton.getInstance(side).startLodingIcon();
		
		StorageManagerPortlet.storagemanagerService.createNewFolderInMongoDB(
				params.get(ServiceParameter.CLASS),
				params.get(ServiceParameter.NAME),
				params.get(ServiceParameter.OWNER),
				params.get(ServiceParameter.ACCESS_TYPE),
				params.get(ServiceParameter.AREA_TYPE),
				newdir.getName(),
				Common.scope, 
				this.<Void>mkdirAsyncCallback()
			);		
	}
	
	private void mkdirInWorkspace () {
		
		LoadingIconMultiton.getInstance(side).startLodingIcon();
		
		StorageManagerPortlet.storagemanagerService.createNewFolderInWorkspace(
				null,
				parent == null ? null : parent.getIdInWorkspace(), 
				newdir.getShortname(), 
				this.<String>mkdirAsyncCallback()
			);
	}

	public interface PostMkDir {
		
		void postFailure();
		
		void postSuccess();
	}
	
	public void setPostMkDir(PostMkDir postMkDir) {
		
		this.postMkDir = postMkDir;
	}
	
	public void setReload(boolean reload) {
		this.reload = reload;
	}
	
	private BaseDto newdir;
	private BaseDto parent;

	private Side side;
	
	private PostMkDir postMkDir = null;
	
	private boolean reload = true;
}
