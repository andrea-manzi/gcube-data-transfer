package org.gcube.datatransfer.portlets.sm.user.client;

import java.util.ArrayList;
import java.util.List;

import org.gcube.datatransfer.portlets.sm.user.client.popup.PopUpMongoDB;
import org.gcube.datatransfer.portlets.sm.user.shared.StorageManagerServiceAsync;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent.BeforeLoadHandler;
import com.sencha.gxt.data.shared.loader.ChildTreeStoreBinding;
import com.sencha.gxt.data.shared.loader.DataReader;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent.LoadExceptionHandler;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class TreeLoaderBuilder {
	
	public TreeLoaderBuilder(TreeGrid<BaseDto> tree) {
		
		this.tree = tree;
	}
	
	public void buildMongoDBProxy () {
		
		proxy = new RpcProxy<BaseDto, String>() {
			 
		      @Override
		      public void load(BaseDto loadConfig, AsyncCallback<String> callback) {
		        
		    	  	String path = loadConfig == null ? root : loadConfig.getName();
		    	  
		    	  	StorageManagerServiceAsync service = StorageManagerServiceHolder.getInstance();
			    	  
		    	  	PopUpMongoDB params = PopUpMongoDB.getInstance(GuiStateHolder.side(tree));
		    	  
					service.getFileListOfMongoDB(
							params.get(ServiceParameter.CLASS),
							params.get(ServiceParameter.NAME),
							params.get(ServiceParameter.OWNER),
							params.get(ServiceParameter.ACCESS_TYPE),
							params.get(ServiceParameter.AREA_TYPE),
							path, 
							Common.scope,
							callback
						);
		      }
		};
	}
	
	public void buildUploadProxy () {
		
		proxy = new RpcProxy<BaseDto, String>() {
			 
		      @Override
		      public void load(BaseDto loadConfig, AsyncCallback<String> callback) {
		    	  
		    	  	StorageManagerServiceAsync service = StorageManagerServiceHolder.getInstance();
		    	  	service.listFiles(StorageManagerPortlet.instance.pathForTempUploads, callback);
		      }
		};
	}
	
	public void buildNoServiceProxy () {
		
		proxy = new RpcProxy<BaseDto, String>() {
			 
		      @Override
		      public void load(BaseDto loadConfig, AsyncCallback<String> callback) {
		    	  
		    	  	callback.onSuccess(new String());
		      }
		};
	}
	
	public void buildWorkspaceDBProxy () {
		
		proxy = new RpcProxy<BaseDto, String>() {
			 
		      @Override
		      public void load(BaseDto loadConfig, AsyncCallback<String> callback) {
		        
		    	  StorageManagerServiceAsync service = StorageManagerServiceHolder.getInstance();
		    	  
		    	  String folderId = loadConfig == null ? null : loadConfig.getIdInWorkspace();
		    	  boolean needTheParent = false;//folderId != null;
		    	  
		    	  service.getWorkspaceFolder(Common.jsonWorkspace, folderId, needTheParent, callback);
		      }
		};
	}
	
	public void buildServiceReader() {
		
		reader = new DataReader<List<BaseDto>, String>() {

			@Override
			public List<BaseDto> read(Object loadConfig, String data) {
				
				List<BaseDto> ret = new ArrayList<BaseDto>();
				
				if (data == null) return ret;
				
				FolderDto fd = (FolderDto) FolderDto.createSerializer().deSerialize(data, FolderDto.class.getName());
				List<FolderDto> children = fd.getChildren();
				
				if (children == null) return ret;
				
				for (FolderDto c : children) {
					// if the name is empty it's a dummy
					if (c.getName().isEmpty()) continue;
					// if the only child has an empty name it is a dummy
					if (c.getChildren().size() == 1 && c.getChildren().get(0).getName().isEmpty()) c.getChildren().clear();
					ret.add(c);
				}
				
				return ret;
			}
		};
	}
	
	public void buildListReader(final List<BaseDto> ret) {
		
		reader = new DataReader<List<BaseDto>, String>() {

			@Override
			public List<BaseDto> read(Object loadConfig, String data) {
				
				return ret;
			}
		};
	}
	
	public TreeLoader<BaseDto> get() {
		
		TreeLoader<BaseDto> loader = new TreeLoader<BaseDto>(proxy, reader) {
		    @Override
		    public boolean hasChildren(BaseDto parent) {
		    	
		    	// check in the store
		    	return tree.getTreeStore().hasChildren(parent);
		    }
	    };
	    
	    final Side side = GuiStateHolder.side(tree);
	    
	    loader.addBeforeLoadHandler(new BeforeLoadHandler<BaseDto>() {

			@Override
			public void onBeforeLoad(BeforeLoadEvent<BaseDto> event) {
				
				LoadingIconMultiton.getInstance(side).startLodingIcon();
			}
		
	    });
	    
	    // handle errors
	    loader.addLoadExceptionHandler(new LoadExceptionHandler<BaseDto>() {

			@Override
			public void onLoadException(LoadExceptionEvent<BaseDto> event) {
				
				Info.display("Warning", "Items were not loaded");
				LoadingIconMultiton.getInstance(side).stopLodingIcon();
			}
		});
	    
	    // bind data
	    ChildTreeStoreBinding<BaseDto> handler = new ChildTreeStoreBinding<BaseDto>(tree.getTreeStore()) {
	    	
	    	@Override
	    	public void onLoad(LoadEvent<BaseDto, List<BaseDto>> event) {
	    		
	    		super.onLoad(event);
				LoadingIconMultiton.getInstance(side).stopLodingIcon();
	    	}
	    };
	    
	    loader.addLoadHandler(handler);
		
		return loader;
	}
	
	private TreeGrid<BaseDto> tree = null;
	private static final String root = "/"; 
	
	private RpcProxy<BaseDto, String> proxy = null;
	private DataReader<List<BaseDto>, String> reader = null;
}
