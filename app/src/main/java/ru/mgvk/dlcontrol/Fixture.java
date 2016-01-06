package ru.mgvk.dlcontrol;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Fixture extends DmxControl {

	// Переменные
	BufferedReader fixt;
	int lastdmx = 0;
	public int functcount = 0;
	public volatile ConcurrentHashMap<String, Integer> DmxMap = new ConcurrentHashMap<String, Integer>();
	public ArrayList<String> functlist = new ArrayList<String>();
	int fixturecount = 0;
    int maxfunctioncount =0;

	// Imports
	MainActivity MAct;
	ShowCreating SC;
	public Gobo Gobo;

	// Special
	Context context;

	public Fixture() {
	}

	public Fixture(Context parentcontext) {
		MAct = new MainActivity();
//		SC = MAct.showCreating;
		context = parentcontext;
		Gobo = new Gobo(context);

	}

	/**
	 * Стартовая функция режима создания прибора
	 * 
	 */
	
	public void add(){
		
	}
	
	/**
	 * Инициализация приборов при запуске (вызывается из MainActivity)
	 * 
	 * @param name Список названий
	 * @return
	 */
	
	boolean add(String[] name) {
		boolean rc = false;


		fixturecount = name.length-1;
		Gobo.init(fixturecount);
		Log.d("FixtureListLength", "" + fixturecount);

		for (int i = 0; i < fixturecount; i++) {

			fixt = (new FilesControl()).OpenFile(fixt, name[i].substring(4, name[i].length()));
			if (fixt != null) {
				try {
					functcount = Integer.valueOf(fixt.readLine());
                    if(functcount>maxfunctioncount){
                        maxfunctioncount = functcount;
                    }

				} catch (IOException e) {
					e.printStackTrace();
				}

				if (DmxAdress(name[i], i)) {
					rc = true;
				} else {
					Toast.makeText(context, "Ошибка установки DMX каналов",
							Toast.LENGTH_SHORT).show();
					rc = false;
				}
				try {
					fixt.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(context, "Не найдена конфигурация прибора!",
						Toast.LENGTH_SHORT).show();
				rc = false;
			}
		}
		// showCreating.setVars(-1, functcount);
		lastdmx = 0;
		return rc;
	}

	private boolean DmxAdress(String FixtName, int fixtnumber) {
		DmxMap.put(FixtName.substring(0, 3), functcount * 1000 + lastdmx);
		Log.d("DmxChannelsCount", "" + functcount);
		int gobocount = 0;
		for (int i = 0; i < functcount; i++) {
			try {
				String s = fixt.readLine();
				int j = 0;
				String functname = "";
				while (s.charAt(j) != ' ') {
					functname += s.charAt(j);
					j++;
				}
				int fixturechstart = 0;
				if (s.charAt(j + 1) != '-') {
					fixturechstart = Integer.valueOf(s.substring(j + 1,
							s.length()))
							- 1 + lastdmx;
				} else {
					Toast.makeText(
							context,
							"Проверьте конфигурацию прибора " + FixtName + " !",
							Toast.LENGTH_SHORT).show();
					return false;
				}
				Log.d("DmxConfig:", functname + "|" + s.charAt(j + 1) + "|"
						+ fixturechstart);
				if (fixturechstart >= 0) {

					DmxMap.put(FixtName.substring(0, 4) + functname,
							fixturechstart);

					if (functname.equals("gobo")) {
						gobocount++;
						s = fixt.readLine();
						Gobo.addAll(fixtnumber, s, fixturecount);
						Gobo.setChannel(fixturechstart);
						DmxMap.put(FixtName.substring(0, 3),
								(functcount-gobocount) * 1000 + (lastdmx));

						// if ((lastdmx + gobocount) == 0) {
						// gobocount++;
						// DmxMap.put(FixtName.substring(0, 3),
						// (functcount - gobocount) * 1000 + (lastdmx));
						// lastdmx--;
						// } else {
						// gobocount++;
						// lastdmx--;
						// DmxMap.put(FixtName.substring(0, 3),
						// (functcount - gobocount) * 1000 + (lastdmx));
						// }
					} else {
						functlist.add(functname);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		lastdmx += functcount-gobocount;
		return true;
	}

	public int getadress(String s) {
		return DmxMap.get(s) + 1;
	}

	public int getFixturesCount() {
		return fixturecount;
	}
    public int getMaxFunctionsCount(){
        return maxfunctioncount;

    }
}
