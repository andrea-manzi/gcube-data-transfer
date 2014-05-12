package org.gcube.datatransfer.portlets.sm.user.shared.prop;

import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.FolderDto;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface BaseDtoProperties extends PropertyAccess<BaseDto> {
  
  public final ModelKeyProvider<BaseDto> key = new ModelKeyProvider<BaseDto>() {
    public String getKey(BaseDto item) {
    	// TODO if we want to use the getId() we need to dig dipper because the id is being wrongly assigned
    	// but for now the full path will do :)
    	return (item instanceof FolderDto ? "f-" : "m-") + item.getName();
    }
  };
  
  ValueProvider<BaseDto, String> name();
  ValueProvider<BaseDto, String> shortname();
  ValueProvider<BaseDto, String> owner();
  ValueProvider<BaseDto, String> type();
  ValueProvider<BaseDto, String> lastUpdate();
  ValueProvider<BaseDto, String> size();

}
