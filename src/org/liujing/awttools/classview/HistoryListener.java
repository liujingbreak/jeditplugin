package org.liujing.awttools.classview;
import java.util.EventListener;
public interface HistoryListener<T> extends EventListener{
	public void onHistoryLink(T event);
}
