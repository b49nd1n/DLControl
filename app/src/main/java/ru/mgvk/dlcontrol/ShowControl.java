package ru.mgvk.dlcontrol;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class of show playing
 * Created by mihail on 29.10.15.
 */
public class ShowControl {

    // Переменные
    static Thread show_thread;
    static String showname;
    boolean stop = false;
    static boolean stoped = false;
    boolean newShow = false;
    BufferedReader showfile;
    volatile int[][][] showmas;
    int framecount = 0;
    int functcount = 1;
    static int fixturecount = 0;
    ArrayList<String> functmas = new ArrayList<String>();
    public ConcurrentHashMap<String, Integer> DmxMap;

    //Константы
    public byte SHOW_START=1;
    public byte SHOW_PAUSE =2;
    public byte SHOW_RESUME=3;

    Context context;

    static MainActivity MAct;
    DmxControl dmxControl;
    Fixture fixture;

    ShowControl(){ //empty constructor
    }

    ShowControl(Context parentcontext){
        context = parentcontext;

        MAct = (MainActivity) parentcontext;
        dmxControl = MAct.dmxControl;
        fixture =  MAct.fixture;
        show_thread = new Thread(new Show_Thread());
        show_thread.setName("ShowThread");
    }

    /**
     * Метод возвращающий состояние шоу-потока.
     *
     * @return true - если шоу запущено
     *         false - если остановлено
     */

    public boolean works(){
        return !stop;
    }


    /**
     * @param i 1 = start {@link #SHOW_START}
     *          2 = pause {@link #SHOW_PAUSE}
     *          3 = resume {@link #SHOW_RESUME}
     **/
    public boolean showThreadControl(int i) {

        boolean res=false;
        try {
            if (i == SHOW_START) {
                show_thread.start();
                stop= false;
                res = true;
            }
            if (i == SHOW_PAUSE) {

                if (show_thread.isAlive()) {
                    stop = true;
                    // Thread thread_stop = new Thread(new Runnable() {
                    //
                    // @Override
                    // public void run() {
                    // try {
                    // Thread.sleep(50);
                    // } catch (Exception e) {
                    // Log.e("Error ", "" + e);
                    // }
                    // for (int j = 0; j < 512; j++) {
                    // MAct.dmxmas[j] = 0;
                    // }
                    // }
                    //
                    // });
                    // thread_stop.run();

                    while (!stoped) {
                        Thread.sleep(100);
                    }
                    for (int j = 0; j < 512; j++) {
                        MAct.dmxmas[j] = 0;
                    }
                    res = true;

                }

            }
            if (i == SHOW_RESUME) {
                stop = false;
                res = true;
            }
        }catch (Exception e){
            Log.e("ShowthreadControl",""+e);
            res = false;
        }
        return res;
    }

    void Control(String showfilename, int fixtcount) throws Exception {
        int fixturecount = fixtcount;
        functmas.clear();
        functcount = 1;
        try {
            showfilename += ".shw";

            if (fixture.DmxMap == null) {
                MAct.errorDialog(context,R.string.error,R.string.restart);
                Toast.makeText(context, "Ошибка открытия шоу!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            showfile = MAct.fControl.OpenFile(showfile, showfilename);
            framecount = Integer.valueOf(showfile.readLine());
            String s = showfile.readLine();

            functmas.add(0, "");
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == ' ') {
                    functcount++;
                    functmas.add("");
                } else {
                    String a = functmas.get(functcount - 1) + s.charAt(i);
                    functmas.set(functcount - 1, a);
                }
            }
            showmas = new int[framecount + 1][fixturecount][functcount + 2];
            String c = "";
            int m = 0;
            byte k;


            ShowLoad(fixturecount, c);

        } catch (Exception e) {  // TODO: 30.11.15 R.String!!!
            Toast.makeText(context, "Ошибка загрузки шоу " + e,
                    Toast.LENGTH_SHORT).show();
            Log.e("ShowLoading", "" + e);
            return;
        }

        showfile.close(); // TODO: 30.11.15 R.String!!!
        Toast.makeText(context, "Загрузка шоу завершена", Toast.LENGTH_SHORT)
                .show();

        // Thread show_thread = new Thread(new Dmx_Show_Thread());
        // show_thread.start();
        if (!show_thread.isAlive()) {
            showThreadControl(SHOW_START);
        }
    }

    private void ShowLoad(int fixturecount, String c) throws IOException {
        String s;
        byte k;
        int m;
        for (int frame = 1; frame <= framecount; frame++) {
            s = showfile.readLine();
            showmas[frame][0][0] = Integer.valueOf(s);
            s = showfile.readLine();
            if (!s.contains(";")) {
                showmas[frame][0][1] = Integer.valueOf(s);
                k = 1;
            } else {
                k = 0;
            }
            for (int fixture = 0; fixture < fixturecount; fixture++) {
                if (k == 1) {
                    s = showfile.readLine();
                }
                k = 1;
                s += ' ';
                m = 0;
                for (int function = 2; function <= functcount + 1; function++) {
                    while ((m < s.length()) && (s.charAt(m) != ';')) {
                        c += s.charAt(m);
                        m++;
                    }
                    try {
                        if ((!c.contains(";"))
                                && (!c.contains(" ") && (c.length() != 0))) {
                            showmas[frame][fixture][function] = Integer
                                    .valueOf(c);
                        } else {
                            showmas[frame][fixture][function] = 0;
                            c = "";
                        }
                        c = "";
                        m++;
                    } catch (Exception e) {
                        Log.e("ShowLoadingError", "" + e);
                        Toast.makeText(context, "Error: " + e,
                                Toast.LENGTH_SHORT).show();
                    }
            }

            }

        }
    }

    /************************************ Show Start *******************************************/

    public void showstart(String shw) {
        showname = shw;

        if ((show_thread != null)) {
            dmxControl.Start();
            if (dmxControl.started()) {
                for (int i = 0; i < 512; i++) {
                    MAct.dmxmas[i] = 0;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {

                ShowThreadStart(showname);
            } catch (Exception e) {
                // TODO: 30.11.15 R.String!!!
                Toast.makeText(context,
                        "Невозмножно запустить шоу! Ошибка загрузки!",
                        Toast.LENGTH_SHORT).show();
            }
            showThreadControl(1);

        } else { // TODO: 30.11.15 R.String!!!
            Toast.makeText(context, "Невозмножно запустить шоу!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void ShowThreadStart(String shw) {
        try {
            showname = shw;
            // TODO: 09.12.15 исправить кол-во приборов (считает +1)
            fixturecount = MAct.FixtChoose.getCount()-1;
            Control(showname, fixturecount);
        } catch (Exception e) {
            Log.e("ShowControlError", "" + e);
        }
    }


    public class Show_Thread implements Runnable {
        int a;
        String s;
        int count = 0;
        int time = 0;
        Thread t;
        boolean Kostil = false;
        Show_Thread() {
            t = Thread.currentThread();
            t.setPriority(Thread.NORM_PRIORITY);
        }

        public void run() {
            int l = 50;
            try{
                for (;;) {
                    int startt = (int) System.currentTimeMillis();
                    for (int frame = 1; frame <= framecount; frame++) {
                        int pause = showmas[frame][0][0] / l;
                        for (int fixture = 0; fixture < fixturecount; fixture++) {
                            s = String.format("%3d", fixture + 1) + " ";

                            for (int function = 2; function <= functcount + 1; function++) {
                                try {
                                    a = MAct.fixture.DmxMap.get(s + functmas.get(function - 2));
                                    count = showmas[frame][fixture][function]
                                            - showmas[frame - 1][fixture][function];
                                    if (count != 0) {
                                        time = Math.abs(showmas[frame][0][1]
                                                / count);
                                        if (time != 0) {
                                            Thread Set_frame = new Thread(
                                                    new set_frame(a, count, time));
                                            Set_frame.setName("FrameThread "
                                                    + (a + 1));
                                            Set_frame.start();
                                        } else {
                                            MAct.dmxmas[a] = (byte) showmas[frame][fixture][function];
                                        }
                                    } else {

                                        MAct.dmxmas[a] = (byte) showmas[frame][fixture][function];
                                    }

                                } catch (Exception e) {

                                    Log.e("Show_Thread ERROR",
                                            "" + e + " " + e.getLocalizedMessage()
                                                    + " " + a);

                                }
                            }
                        }
                        try {
                            // значение l установлено в начале метода, значение с -
                            // в начале цикла фрейма
                            for (int i = 0; i < pause; i++) {
                                if (stop) {
                                    byte stopmas[] = new byte[512];
                                    System.arraycopy(MAct.dmxmas, 0, stopmas, 0, 512);
                                    stoped = true;
                                    while (stop) {
                                        Thread.sleep(l);
                                    }
                                    Log.d("NewShow", "" + newShow);
                                    if (!newShow) {
                                        System.arraycopy(stopmas, 0, MAct.dmxmas, 0, 512);
                                    } else {
                                        for (int j = 0; j < 512; j++) {
                                            MAct.dmxmas[j] = 0;
                                        }
                                    }

                                    for (int fixture = 0; fixture < fixturecount; fixture++) {
                                        s = String.format("%3d", fixture + 1) + " ";
                                        for (int function = 2; function <= functcount + 1; function++) {
                                            a = MAct.fixture.DmxMap.get(s
                                                    + functmas.get(function - 2));
                                            MAct.dmxmas[a] = (byte) showmas[1][fixture][function];
                                        }
                                    }
                                    Kostil = true;
                                    newShow = false;
                                    Thread.sleep(1000);
                                    stoped = false;
                                    break;
                                }
                                Thread.sleep(l);
                            }
                        } catch (Exception e) {
                            Log.e("ShowThread Pause",String.valueOf(e));
                        }

                        if (Kostil) {
                            break;
                        }
                    }
                    if (Kostil) {
                        for (int fixture = 0; fixture < fixturecount; fixture++) {
                            System.arraycopy(showmas[1][fixture], 1, showmas[0][fixture], 1, functcount + 1);
                        }
                        Kostil = false;
                    } else {
                        for (int fixture = 0; fixture < fixturecount; fixture++) {
                            System.arraycopy(showmas[framecount][fixture], 1, showmas[0][fixture], 1, functcount + 1);
                        }
                    }

                    int stopt = (int) System.currentTimeMillis();
                    Log.d("Time!", "" + (stopt - startt));
                }
            }catch(Exception e){
                Log.e("ShowThread",""+e);
            }

        }

    }

    public class set_frame implements Runnable {

        int channel;
        int time;
        int count;
        int k = 1;
        public set_frame(int ch, int ct, int t) {
            channel = ch;
            time = t;
            count = Math.abs(ct);
            k = count / ct;
        }

        public void run() {
            for (int i = 0; i < count; i++) {
                if (stop) {
                    break;
                }
                MAct.dmxmas[channel] += k;
                try {

                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
