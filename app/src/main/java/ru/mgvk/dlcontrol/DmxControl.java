package ru.mgvk.dlcontrol;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

public class DmxControl extends MainActivity {

    // Константы
    byte OPEN_DMX = 1;
    byte ART_NET = 2;
    byte DEMO = 3;
    byte DMX_STATUS = 4;


    // Переменные
    boolean initialized[] = {false, false, false};
    Thread opendmx_thread;
    Thread artnet_thread;
    Thread dmxstatus_thread;

    boolean dmxstatus_works = false;
    public ConcurrentHashMap<Integer, ConcurrentHashMap<String, Object>> PrtclMap; //Список всех протоколов
    // Imports
    MainActivity MAct;
    OpenDmx openDmx;
    DmxArtNet artNet;

    // Special
    Context context;


    public DmxControl() {
//        openDmx = new OpenDmx();
//        opendmx_thread = new Thread(openDmx.new openDmx_Thread());
//        //artNet = new ArtNet();
//        //artNet_thread = new Thread(artNet.new artNet_Thread());
    }

    public DmxControl(Context parentcontext) {

        context = parentcontext;

        openDmx = new OpenDmx(context);
        opendmx_thread = new Thread(openDmx.new openDmx_Thread());
        artNet = new DmxArtNet(context);
        artnet_thread = new Thread(artNet.new artNet_Thread());
        dmxstatus_thread = new Thread(new DmxStatusThread());
        MAct = (MainActivity) parentcontext;
        PrtclMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, Object>>();


        ConcurrentHashMap<String, Object> opendmx = new ConcurrentHashMap<String, Object>();
        opendmx.put(MAct.getString(R.string.opendmx), opendmx_thread);
        ConcurrentHashMap<String, Object> artnet = new ConcurrentHashMap<String, Object>();
        artnet.put(MAct.getString(R.string.artnet), artnet_thread);
        PrtclMap.put((int) OPEN_DMX, opendmx);
        PrtclMap.put((int) ART_NET, artnet);


    }

    /**
     * Запуск нужного протокола
     *
     * @param protocol OPEN_DMX,
     *                 ART_NET,
     *                 DEMO
     */

    public boolean Start(int protocol) { //Выбор из констант
        boolean ret = false;
        switch (protocol) {
            case 1: //OpenDmx
            {
                try {
                    if (!openDmx.works) {
                        if (openDmx.startinit(false)) {
                            opendmx_thread.start();
                            ret = true;
                        } else {
                            ret = false;

                        }
                    }
                } catch (Exception e) {
                    Log.e("Protocol_Start", "OpenDMX " + e.getMessage());
                }
                break;

            }
            case 2: //ArtNet
            {
                try {
                    if (!artNet.works && !artnet_thread.isAlive()) { // TODO: 13.12.15 не пашет isAlive
                        artnet_thread.start();
                        ret = true;

                    } else {
                        artNet.works = true;
                        ret = true;
                    }
                } catch (Exception e) {
                    Log.e("Protocol_Start", "ArtNet " + e.getMessage());
                    ret = false;
                }
                break;
            }
            case 3: //demo
            {
                initialized[2] = true;
                break;
            }
            case 4: //dmx status
            {
                try {
                    if (!dmxstatus_works) {
                        dmxstatus_thread.start();
                        ret = true;
                    }
                } catch (IllegalThreadStateException e) {
                    dmxstatus_works = true;
                } catch (Exception e) {
                    Log.e("Protocol_Start", "Dmx_Status" + e);
                }

                break;
            }

        }
        return ret;
    }

    public void Start() {


    }

    /**
     * Управление протоколами.
     *
     * @param protocol индекс протокола
     * @return true, если протокол успешно остановлен
     */

    public boolean Stop(int protocol) {
// TODO: 29.11.15  дописать управление протоколами
        boolean res = false;
        try {
            switch (protocol) {
                case 1: //Open-Dmx
                {
                    if (opendmx_thread != null) {

                        openDmx.works = false;
                        res = true;
                        openDmx.ftDev.close();
                    }
                    break;
                }
                case 2: //Atr-net
                {
                    if (artnet_thread != null) {
                        artNet.works = false;
                        res = true;
                    }
                    break;
                }
                case 3: //demo
                {

                    res = true;
                    break;
                }
                case 4: //dmx status
                {
                    if (dmxstatus_thread != null) {
                        dmxstatus_works = false;
                        Log.d("DmxStatus", "works:" + dmxstatus_works);
                        Thread.sleep(200);
                        res = true;
                    }

                    break;
                }

            }

        } catch (Exception e) {
            Log.e("DmxThreadStop", "" + e);
            res = false;
        }
        return res;
    }


    /**
     * @return true, если запущен хоть один протокол,
     * false, если ничего не запущено
     */

    boolean started() {
        boolean b = false;
        for (boolean aStarted : initialized) {
            b |= aStarted;
        }
        return b;
    }

    class DmxStatusThread implements Runnable {


        @Override
        public void run() {
            dmxstatus_works = true;
            for (; ; ) {
                try {
                    if (dmxstatus_works) {

                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String s = "";
                                    for (int i = 0; i < 512; i++) {
                                        s += MAct.conv(MAct.dmxmas[i]) + " ";
                                    }
                                    MAct.DmxStatusTxt.setText(s);
                                }
                        });

                        Thread.sleep(250);
                    } else {
                        Thread.sleep(250);
                    }

                } catch (Exception e) {
                    Log.e("DmxStatus", "Error " + String.valueOf(e));
                }

            }
        }
    }


}