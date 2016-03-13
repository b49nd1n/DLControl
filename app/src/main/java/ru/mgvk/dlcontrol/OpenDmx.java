package ru.mgvk.dlcontrol;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

/**
 * OpenDmx Class
 * Created by mihail on 29.10.15.
 */
public class OpenDmx {

    // Константы
    int baudRate = 250000; /* baud rate */
    byte stopBit = 2; /* 1:1stop bits, 2:2 stop bits */
    byte dataBit = 8; /* 8:8bit, 7: 7bit */
    byte parity = 0; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    byte flowControl = 0; /* 0:none, 1: flow control(CTS,RTS) */

    // Переменные
    int DevCount = -1;
    boolean works =false;
//    public ConcurrentHashMap<String, Integer> DmxMap;

    // Imports
    D2xxManager ftD2xx;
    FT_Device ftDev;
    MainActivity MAct;
    DmxControl dmxControl;

    // Special
    Context context;

    public OpenDmx(){

    }

    public OpenDmx(Context parentcontext){
        context = parentcontext;
        baudRate = 250000; /* baud rate */
        stopBit = 2; /* 1:1stop bits, 2:2 stop bits */
        dataBit = 8; /* 8:8bit, 7: 7bit */
        parity = 0; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
        flowControl = 0; /* 0:none, 1: flow control(CTS,RTS) */

        MAct = (MainActivity) parentcontext;
        dmxControl = MAct.dmxControl;

        // Переменные
        DevCount = -1;
        try {
            ftD2xx = D2xxManager.getInstance(parentcontext);
            if (ftD2xx.setVIDPID(0x0403, 0xada1)) {
                Log.d("ftD2xx", "setVIDPID completed");
            }
        } catch (D2xxManager.D2xxException e) {
            Log.e("D2xxManagerException",""+e);
        }




    }


    /***************************** Device DmxControl *****************************
     *
     * @param reconnect false, если устройство подключается первый раз
     * @return true, если устройство удачно настроено
     */

    public boolean startinit(boolean reconnect) { // Запуск инициализации устройства
        boolean ret = false;
        if(reconnect){

            // TODO: 30.11.15 Протестить обновление интента

            try {
                IntentFilter filter = new IntentFilter();
                filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                filter.setPriority(500);
                context.registerReceiver(MAct.mUsbReceiver, filter);
            } catch (Exception e) {
                Log.e("RegisterReciever", "" + e);
            }
        }
        Log.d("Initialization", "Started");

        if (ConnectFunction()) {

            // TODO: 30.11.15 R.String!!!!!
            if (!SetConfig(baudRate, dataBit, stopBit, parity, flowControl)) {
                Toast.makeText(context, "Ошибка настройки устройства!",
                        Toast.LENGTH_SHORT).show();
                ret = false;
            } else {
                Toast.makeText(context, "Устройство успешно настроено!",
                        Toast.LENGTH_SHORT).show();
                Log.d("Config", "Config done");
                ret = true;
            }
        }else{
            Toast.makeText(context, "Проверьте соединение с устройством!",
                    Toast.LENGTH_SHORT).show();
            ret = false;
        }
        if(ret){
            MAct.dmxControl.initialized[MAct.dmxControl.OPEN_DMX] = true;
        }
        return ret;
    }



    /***************************** Device Configuration ******************************/
    private boolean SetConfig(int baud, byte dataBits, byte stopBits, // Конфигурация
                              // устройства
                              byte parity, byte flowControl) {

        boolean ret = ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET)
                &&ftDev.setBaudRate(baud)
                &&ftDev.setDataCharacteristics( dataBits, stopBits,
                parity)
                &&ftDev.setFlowControl(flowControl, (byte) 0x0b, (byte) 0x0c);

        return true;
    }

    /******************************** Device Connect *********************************/
    private boolean ConnectFunction() { // Подключение устройства
        int openIndex = 0;
        if (DevCount > 0) {
            Log.d("DevCount", "DevCount>0");
            return true;
        }

        try {
            if (ftD2xx == null) {
                Log.e("ConnectFunction", "D2XXManager is null");
                return false;
            }
            DevCount = ftD2xx.createDeviceInfoList(context);

        if (DevCount > 0) {
            ftDev = ftD2xx.openByIndex(context, openIndex);

            if (ftDev == null) {
                Toast.makeText(context, "Ошибка настройки устройства!",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            if (ftDev.isOpen()) {
                return true;
            }
        } else {
            Log.e("j2xx", "DevCount <= 0");
            return false;
        }

        } catch (Exception e) {
            Log.e("DevCount", "Error:" + e.getMessage());
        }

        return false;

    }

    /***************************** Dmx Thread Description ****************************/
    public class openDmx_Thread implements Runnable { // Описание потока DmxThread
        openDmx_Thread() {
            Thread t = new Thread(this);
            t.setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void run() {
            try {
                works = true;
                ftDev.clrRts();
                ftDev.purge((byte) 1);
                ftDev.purge((byte) 2);

                ftDev.setLatencyTimer((byte) 16);
                byte[] a = {0};

                works=true;
                while (works) {
                    ftDev.setBreakOn();
                    ftDev.setBreakOff();
                    ftDev.write(a, 1);
//					Log.d("MAct.dmxmas", "" + MAct.dmxmas[0] + " "
//							+ MAct.dmxmas[1] + " " + MAct.dmxmas[2] + " "
//							+ MAct.dmxmas[3] + " " + MAct.dmxmas[4] + " "
//							+ MAct.dmxmas[5] + " " + MAct.dmxmas[6] + " "
//							+ MAct.dmxmas[7] + " " + MAct.dmxmas[8] + " "
//							+ MAct.dmxmas[9]);
                    ftDev.write(MAct.dmxmas, 512);
                    try {
                        Thread.sleep(45);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                works=false;
                Log.e("jx22", "openDmx_Thread" + e);
            }
        }
    }
}
