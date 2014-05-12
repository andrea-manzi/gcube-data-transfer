package org.gcube.datatransfer.portlets.sm.user.client.popup;

import java.util.EnumMap;
import java.util.Map;

import org.gcube.datatransfer.portlets.sm.user.client.Common;
import org.gcube.datatransfer.portlets.sm.user.client.GuiStateHolder;
import org.gcube.datatransfer.portlets.sm.user.client.ServiceParameter;
import org.gcube.datatransfer.portlets.sm.user.client.Side;
import org.gcube.datatransfer.portlets.sm.user.client.StorageManagerPortlet;
import org.gcube.datatransfer.portlets.sm.user.client.StorageType;
import org.gcube.datatransfer.portlets.sm.user.client.TreeLoaderBuilder;
import org.gcube.datatransfer.portlets.sm.user.shared.obj.BaseDto;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.CollapseEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.CollapseEvent.CollapseHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN), Michał Simon (CERN)
 *
 */

public class PopUpMongoDB extends FocusPanel{

	// access to one of the instances
	static public PopUpMongoDB getInstance(Side side) {
		
		// get the instance
		PopUpMongoDB instance = instances.get(side);
		// if it's null vrate it
		if (instance == null) {
			instance = new PopUpMongoDB(side);
			instances.put(side, instance);
		}
		
		return instance;
	}
	
	// access to the parameters that have been set
	public String get(ServiceParameter param) {
		
		return params.get(param);
	}
	
	// pops up the window
	public void popUpNow() {
		

		if (Common.isAdmin) {
			// let the admin choose		
			Common.foc = this;
			// set it to false so the value changes and the event is triggered
			defaultValuesStorage.setValue(false);
			defaultValuesStorage.setValue(true, true);
			
			StorageManagerPortlet.instance.mainDialogBox.popUpSomePanel(this);
			
		} else {
			
			
			StorageManagerPortlet.instance.sourceAndDestination.setStorageType(side, StorageType.MONGODB);
			
			// and use default values for others
			params.put(ServiceParameter.CLASS, "data-transfer");
			params.put(ServiceParameter.NAME, "scheduler-portlet");
			params.put(ServiceParameter.ACCESS_TYPE, "PRIVATE");
			params.put(ServiceParameter.AREA_TYPE, "Persistent");
			params.put(ServiceParameter.OWNER, Common.resourceName);
			
			load();
		}
	}

	// private constructor
	private PopUpMongoDB(Side side) {
		
		this.side = side; 
		create();
	}

	// initializes the GUI components
	private void create() {
		
		FramedPanel panel = new FramedPanel();
		panel.setHeadingText("Storage Manager Settings");
		panel.setWidth(300);
		panel.setBodyStyle("background: none; padding: 5px");
		VerticalLayoutContainer p = new VerticalLayoutContainer();


		smServiceName = new TextField();
		smServiceName.setAllowBlank(false);
		smServiceName.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				Info.display(
						"Service Name Changed",
						"Service Name changed to " + event.getValue() == null ? "blank" : event.getValue()
					);
			}
		});

		p.add(new FieldLabel(smServiceName, "Service Name"), new VerticalLayoutData(1, -1));


		smServiceClass = new TextField();
		smServiceClass.setAllowBlank(false);
		smServiceClass.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				Info.display(
						"Service Class Changed",
						"Service Class changed to " + event.getValue() == null ? "blank" : event.getValue());
			}
		});

		p.add(new FieldLabel(smServiceClass, "Service Class"), new VerticalLayoutData(1, -1));

		smAccessType = new SimpleComboBox<String>(
				new LabelProvider<String>() {
					public String getLabel(String item) {
						return item.toString().substring(0, 1) + item.toString().substring(1).toLowerCase();
					}
			});
		
		smAccessType.setTriggerAction(TriggerAction.ALL);
		smAccessType.setEditable(false);
		smAccessType.add("SHARED");
		smAccessType.add("PUBLIC");
		smAccessType.add("PRIVATE");
		// Add a handler to change the data source
		smAccessType.addCollapseHandler(new CollapseHandler() {
			public void onCollapse(CollapseEvent event) {
				// set the same values in the main form
				if (smAccessType.getCurrentValue() == null) return;

				String v = smAccessType.getCurrentValue() == null ? "nothing" : smAccessType.getCurrentValue();
				Info.display("Selected", "You selected " + v);
			}
		});

		smAccessType.setAllowBlank(true);
		smAccessType.setForceSelection(true);
		
		p.add(new FieldLabel(smAccessType, "Access Type"), new VerticalLayoutData(1, -1));
		
		smAreaType = new SimpleComboBox<String>(new StringLabelProvider<String>());
		smAreaType.setTriggerAction(TriggerAction.ALL);
		smAreaType.setEditable(false);
		smAreaType.add("Persistent");
		smAreaType.add("Volatile");
		// Add a handler to change the data source
		smAreaType.addCollapseHandler(new CollapseHandler() {
			public void onCollapse(CollapseEvent event) {
				// set the same values in the main form
				if (smAreaType.getCurrentValue() == null) return;

				String v = smAreaType.getCurrentValue() == null ? "nothing" : smAreaType.getCurrentValue();
				Info.display("Selected", "You selected " + v);
			}
		});

		smAreaType.setAllowBlank(true);
		smAreaType.setForceSelection(true);

		p.add(new FieldLabel(smAreaType, "Storage Area Type"), new VerticalLayoutData(1, -1));

		//check box for default values
		defaultValuesStorage = new CheckBox();
		defaultValuesStorage.setBoxLabel("");
		defaultValuesStorage.setValue(false);
		defaultValuesStorage.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				boolean checked = event.getValue();
				if(checked){
					smServiceName.setValue("scheduler-portlet");
					smServiceClass.setValue("data-transfer");
					smAccessType.setValue("PRIVATE");
					smAreaType.setValue("Persistent");
					Info.display("Storage Manager", "Default parameters");
				}
				else{
					smServiceName.setValue("");
					smServiceClass.setValue("");
					smAreaType.setValue(null);
					smAccessType.setValue(null);
				}
			}
		});		
		p.add(new FieldLabel(defaultValuesStorage, "Default parameters"));

		// ************** buttons **************

		TextButton cancelButton = new TextButton("Cancel");
		cancelButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				StorageManagerPortlet.instance.mainDialogBox.hide();
				StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
			}
		});
		
		TextButton nextButton = new TextButton("Next");
		nextButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				onNext();
			}
		});
		
		panel.setWidget(p);
		panel.setButtonAlign(BoxLayoutPack.START);
		panel.addButton(cancelButton);
		panel.addButton(nextButton);

		// key handlers -------------
		this.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					StorageManagerPortlet.instance.mainDialogBox.hide();
					StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();
				} else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					onNext();
				}
			}
		});
		this.add(panel);
	}

	// checks that the set values are correct
	private boolean checkMongoDBFields() {
		// check if we have service name
		if (this.smServiceName == null) {
			Info.display("Warning", "You should have a Service Name !");
			return true;
		} else if (this.smServiceName.getCurrentValue() == null) {
			Info.display("Warning", "You should have a Service Name !");
			return true;
		}
		// service class
		if (this.smServiceClass == null) {
			Info.display("Warning", "You should have a Service Class !");
			return true;
		} else if (this.smServiceClass.getCurrentValue() == null) {
			Info.display("Warning", "You should have a Service Class !");
			return true;
		}
		// access type
		if (this.smAccessType == null) {
			Info.display("Warning", "You should have an Access Type !");
			return true;
		} else if (this.smAccessType.getCurrentValue() == null) {
			Info.display("Warning", "You should have an Access Type !");
			return true;
		}
		// storage area type
		if (this.smAreaType == null) {
			Info.display("Warning", "You should have a Storage Area Type !");
			return true;
		} else if (this.smAreaType.getCurrentValue() == null) {
			Info.display("Warning", "You should have a Storage Area Type !");
			return true;
		}
		return false;
	}

	// gets called if user approved the settings
	private void onNext(){
		
		if (checkMongoDBFields()) return;
		
		StorageManagerPortlet.instance.sourceAndDestination.setStorageType(side, StorageType.MONGODB);
		
		StorageManagerPortlet.instance.mainDialogBox.hide();
		StorageManagerPortlet.instance.mainDialogBox.setDialogBoxForMessages();

		params.put(ServiceParameter.CLASS, smServiceClass.getCurrentValue());
		params.put(ServiceParameter.NAME, smServiceName.getCurrentValue());
		params.put(ServiceParameter.ACCESS_TYPE, smAccessType.getCurrentValue());
		params.put(ServiceParameter.AREA_TYPE, smAreaType.getCurrentValue());
		params.put(ServiceParameter.OWNER, Common.resourceName);
				
		load();
	}
	
	void load() {
		
		TreeGrid<BaseDto> tree = GuiStateHolder.tree(side);
		TreeLoaderBuilder builder = new TreeLoaderBuilder(tree);
		builder.buildMongoDBProxy();
		builder.buildServiceReader();
		TreeLoader<BaseDto> loader = builder.get();
		GuiStateHolder.setLoader(side, loader);
		tree.setTreeLoader(loader);
		loader.load();
		GuiStateHolder.enableDropping(side);
		GuiStateHolder.enableMenu(side);
	}

	// map with instances
	static private Map<Side, PopUpMongoDB> instances = new EnumMap<Side, PopUpMongoDB>(Side.class); 
	
	// GUI components 
	private TextField smServiceName, smServiceClass;
	private SimpleComboBox<String> smAccessType;
	private SimpleComboBox<String> smAreaType;
	private CheckBox defaultValuesStorage;
	
	// left or right
	private Side side;
	
	// parameters that have been set
	private Map<ServiceParameter, String> params = new EnumMap<ServiceParameter, String>(ServiceParameter.class);
}
