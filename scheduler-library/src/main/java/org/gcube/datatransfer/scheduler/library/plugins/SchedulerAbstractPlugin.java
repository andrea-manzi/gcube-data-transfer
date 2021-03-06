package org.gcube.datatransfer.scheduler.library.plugins;


import org.gcube.common.clients.fw.plugin.Plugin;
import org.gcube.datatransfer.scheduler.library.utils.Constants;

public abstract class SchedulerAbstractPlugin<S,P> implements Plugin<S,P> {

	public final String name;
	
	public SchedulerAbstractPlugin(String name) {
		this.name=name;
	}
	
	@Override
	public String serviceClass() {
		return Constants.SERVICE_CLASS;
	}
	
	@Override
	public String serviceName() {
		return Constants.SERVICE_NAME;
	}
	
	@Override
	public String namespace() {
		return Constants.NAMESPACE;
	}
	
	@Override
	public String name() {
		return Constants.SCHEDULER_PORT_TYPE_NAME;
	}
	
}