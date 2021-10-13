package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.Optional;

import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;

public interface IArgHolder {
	
	public void accept(ArgWrapper wrapper);
	public ArgWrapper getWrapper();
	
	public boolean isItemStackArg();
	
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container);
	
}
