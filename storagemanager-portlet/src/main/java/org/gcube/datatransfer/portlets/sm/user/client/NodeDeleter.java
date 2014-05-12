package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.List;

import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpMongoDB;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.info.Info;

public class NodeDeleter extends Reloader {

	public NodeDeleter(List<BaseDto> items, Side side) {
		
		super(GuiStateHolder.tree(side), side);
		
		this.side = side;
		this.items = items;
		addCallbackCount(items.size());
	}

	public <T> AsyncCallback<T> deleteAsyncCallback(final BaseDto parent) {
		
		return new AsyncCallback<T>() {
			
			public void onFailure(Throwable caught) {
			
				LoadingIconMultiton.getInstance(side).stopLodingIcon();
				Info.display("Warning", "Failed to delete folder");
			}
			
			public void onSuccess(T result) {
				
				if (result instanceof Boolean && result != null && (Boolean)result) {
					onFailure(null);
					return;
				}

				// reload the parent
				reload(parent);
				// stop loading icon
				LoadingIconMultiton.getInstance(side).stopLodingIcon();
			}
		};
	}
	
	public void delete() {
		
		for (BaseDto item : items) {
			
			BaseDto parent = GuiStateHolder.tree(side).getTreeStore().getParent(item);

			LoadingIconMultiton.getInstance(side).startLodingIcon();
			
			StorageType type = GuiStateHolder.get(side);
			
			switch (type) {
				
			case MONGODB:
				deleteFromMongoDB(item, parent);
				break;
				
			case WORKSPACE:
				deleteFromWokspace(item, parent);
				break;
				
			default:
				// for other types we do nothing
				break;
			}
		}
	}
	
	public void deleteFromMongoDB(BaseDto item, BaseDto parent) {
		
		PopUpMongoDB params = PopUpMongoDB.getInstance(side);
		
		StorageManagerPortlet.storagemanagerService.deleteFolderInMongoDB(
				params.get(ServiceParameter.CLASS),
				params.get(ServiceParameter.NAME),
				params.get(ServiceParameter.OWNER),
				params.get(ServiceParameter.ACCESS_TYPE),
				params.get(ServiceParameter.AREA_TYPE),
				item.getName(),
				Common.scope, 
				this.<Void>deleteAsyncCallback(parent)
			);
	}
	
	public void deleteFromWokspace(BaseDto item, BaseDto parent) {
		
		StorageManagerPortlet.storagemanagerService.deleteInWorkspace(
				null, 
				item.getIdInWorkspace(), 
				this.<Boolean>deleteAsyncCallback(parent)
			);
		
	}
	
	private Side side;
	private List<BaseDto> items;
}
