package org.liujing.jeditplugin.v2;

import java.util.*;
import java.io.*;
import static org.liujing.jeditplugin.v2.ProjectController.*;

/**
Asynchronized search
*/
public interface SearchResponseHandler{
	public void searchResponsed(FileItem newFound, SortedSet<FileItem> allFound);
	
	public void searchDone(SortedSet<FileItem> allFound);
}
