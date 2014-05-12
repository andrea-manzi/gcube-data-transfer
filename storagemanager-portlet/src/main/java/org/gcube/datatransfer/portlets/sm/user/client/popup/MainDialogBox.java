package org.gcube.datatransfer.portlets.sm.user.client.popup;

import org.gcube.datatransfer.portlets.sm.user.client.Common;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

/**
 *	
 * @author Nikolaos Drakopoulos(CERN)
 *
 */

public class MainDialogBox extends DialogBox{
	
	private MainDialogBox instance;
	public MainDialogBox getInstance(){
		return instance;
	}
	
	public MainDialogBox(){	
		instance=this;
	}
	
	
	public void setDialogBoxForMessages() {
		this.setAnimationEnabled(true);
		this.getElement().getStyle().setZIndex(100);
		this.setText("[+]");
	}
	
	/*
	 * printMsgInDialogBox input: String with the message -- returns: Nothing It
	 * shows the 'dialogBoxGen' which contains the input string message
	 */
	public void printMsgInDialogBox(String message) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendEscapedLines(message);
		FramedPanel panel = new FramedPanel();
		panel.setHeadingText("");
		panel.setWidth(500);
		panel.setBodyStyle("background: none; padding: 5px");
		HTML html = new HTML();
		html.setHTML(builder.toSafeHtml());
		panel.add(html);
		panel.setButtonAlign(BoxLayoutPack.START);
		TextButton closeButton = new TextButton("Close");
		closeButton.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				instance.hide();
			}
		});

		// adding the button
		panel.addButton(closeButton);
		// key handlers -------------
		Common.foc = new FocusPanel();
		Common.foc.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER
						|| event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					instance.hide();
				}
			}
		});
		Common.foc.add(panel);
		// --------------

		this.setWidget(Common.foc);
		this.center();
		if (Common.focusTimer == null) {
			Timer focusTimer = new Timer() {
				@Override
				public void run() {
					Common.foc.setFocus(true);
				}
			};
			focusTimer.schedule(200);
		}
		else Common.focusTimer.schedule(200);
	}
	
	/*
	 * createDialogBox input: Widget -- returns: DialogBox It hides the
	 * 'dialogBoxGen' and returns a new DialogBox containing the input widget
	 */
	public void popUpSomePanel(Widget widg) {
		this.hide(); //
		this.setText("[+]");
		this.setAnimationEnabled(true);
		this.getElement().getStyle().setZIndex(50);
		this.setWidget(widg);
		this.center();
		if (Common.focusTimer == null) {
			Timer focusTimer = new Timer() {
				@Override
				public void run() {
					Common.foc.setFocus(true);
				}
			};
			focusTimer.schedule(200);
		}
		else Common.focusTimer.schedule(200);
	}

	
}
