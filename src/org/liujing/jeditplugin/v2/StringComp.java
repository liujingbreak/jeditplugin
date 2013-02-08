package org.liujing.jeditplugin.v2;

import java.util.*;
import java.io.*;

public class StringComp implements Comparator<String>, Serializable{
	public int compare(String s1, String s2){
		return compareString(s1, s2);
	}
	
	public static int compareString(String s1, String s2){
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		int i = 0;
		for(; i< s1.length(); i++){
			if(i >= s2.length())
				return 1;
			int r = s1.charAt(i) - s2.charAt(i);
			if(r != 0 )
				return r;
		}
		if( i< s2.length() ){
			return -1;
		}
		return 0;
	}
}
