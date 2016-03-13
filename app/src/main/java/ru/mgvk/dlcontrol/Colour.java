package ru.mgvk.dlcontrol;

import android.content.Context;

import java.util.ArrayList;

public class Colour {

	//Переменные
		public static boolean exist = false;
		public static int channel=0;
		static ArrayList<ArrayList<Integer>> goboList = new ArrayList<ArrayList<Integer>>();
		
		//Импорты
		MainActivity MAct;
		Fixture Fixt;
		DmxControl DevInit;
		ShowCreating showCreating;
		
		Context context;
		
		public Colour(Context parentcontext){
		
			MAct = (MainActivity) parentcontext;
			Fixt = MAct.fixture;
			DevInit = MAct.dmxControl;
			showCreating = MAct.showCreating;
		}
		
		public Colour(){
			
		}
		
		public static void addAll(String s){
			
			String ch = "";
			ArrayList<Integer> list = new ArrayList<Integer>();
			for(int i=0;i<s.length();i++){
				if(s.charAt(i)!=';'){
					ch+=s.charAt(i);
				}else{
					list.add(Integer.valueOf(ch));
					ch="";
				}
				
			}
			if(list.size()>0){
				exist = true;
				goboList.add(list);
			}
		}
		
		public void add(){
			
		}
		
		public static void setChannel(int ch){
			channel = ch;
		}
		
		public static int getCount(int fixtnumber){
			return goboList.get(fixtnumber).size();
		}
		
		
		/**
		 * i - номер прибора
		 * j - номер значения
		 * 
		 * @param i
		 * @param j
		 * @return
		 */
		public static int getGoboValue(int i, int j){
			return goboList.get(i).get(j);
		}
		
		public static boolean exist(){
			return exist;
		}
		
	
}
