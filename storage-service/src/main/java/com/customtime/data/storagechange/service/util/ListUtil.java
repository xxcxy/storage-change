package com.customtime.data.storagechange.service.util;

import java.util.List;

public class ListUtil {
	
	public static int effectSize(List<? extends Object> list){
		if(list==null)
			return 0;
		int i = 0;
		for(Object obj:list)
			if(obj!=null)
				i++;
		return i;
	}
	public static <E> E getEffect(List<E> list,int index){
		int size = list.size();
		if(index>size||index<0)
			return null;
		for(E e:list){
			if(e!=null){
				if(0==index)
					return e;
				index--;
			}
		}
		return null;
	}
	
	public static <E> E setNull(List<E> list,int index){
		if(index>=list.size())
			return null;
		E e = list.get(index);
		list.remove(index);
		list.add(index, null);
		return e;
	}
	
	public static <E> int addEffect(List<E> list,E e){
		int re = -1;
		for(int i=0,len=list.size();i<len;i++){
			if(list.get(i)==null){
				re = i;
				list.remove(i);
				list.add(i,e);
				break;
			}
		}
		if(re==-1){
			re = list.size();
			list.add(e);
		}
		return re;
	}
	
	public static boolean isEmpty(List<?> list){
		if(list==null)
			return true;
		else if(list.isEmpty())
			return true;
		else
			return false;
	}
}
