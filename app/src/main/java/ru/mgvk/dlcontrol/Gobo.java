package ru.mgvk.dlcontrol;

import java.util.ArrayList;

import android.content.Context;

public class Gobo {

	// Переменные
	public ArrayList<Integer> goboCountList = new ArrayList<Integer>(100);
	public int channel = 0;
	ArrayList<ArrayList<Integer>> goboList = new ArrayList<ArrayList<Integer>>();
	boolean FirstTime = true; 

	// Импорты
	MainActivity MAct;
	Fixture Fixt;
	DmxControl DevInit;
	ShowCreating showCreating;

	Context context;

	public Gobo(Context parentcontext) {

		MAct = (MainActivity) parentcontext;
		Fixt = MAct.fixture;
		DevInit = MAct.dmxControl;
		showCreating = MAct.showCreating;
	}

	public Gobo() {

	}

	public void addAll(int fixtnumber, String s, int fixtcount) {

		String ch = "";
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != ';') {
				ch += s.charAt(i);
			} else {
				list.add(Integer.valueOf(ch));
				ch = "";
			}

		}
		if (list.size() > 0) {
			// if(exist.size()==0){ //TODO добавить увеличение размера листа
			// exist
			// exist.;
			// }
			goboCountList.add(fixtnumber, (goboCountList.get(fixtnumber) + 1));
			goboList.add(fixtnumber, list);
		}
	}

	public void add() {

	}

	public void setChannel(int ch) {
		channel = ch;
	}

	public int getCount(int fixtnumber) {
		return goboList.get(fixtnumber).size();
	}

	/**
	 *
	 * @param i номер прибора
	 * @param j номер значения
	 * @return Возвращает трафарет i-го прибора для j-го значения
	 */
	public int getGoboValue(int i, int j) {
		return goboList.get(i).get(j);
	}

	public void setGoboCount(int a) {

	}

	public int getGoboCount(int fixtnumber) {
		try {
			if (goboCountList.get(fixtnumber) != null) {
				return goboCountList.get(fixtnumber);
			} else
				return 0;
		} catch (Exception e) {
			return 0;
		}
	}

	public void init(int fixturecount) {   //TODO переделать!
		for (int i = 0; i < 300; i++) {
			goboCountList.add(0);
			goboList.add(null);
		}

	}
}
