package ru.mgvk.dlcontrol;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends Activity {

    // Переменные
    public Boolean configured,
            extmode = false; //Состояние расширенного режима

    boolean mplay = false, SCmodeON = false, FCmodeON = false;
    public volatile byte[] dmxmas = new byte[512];
    public volatile ConcurrentHashMap<Object, Object> configmap = new ConcurrentHashMap<Object, Object>();
    int starttime = 0, stoptime = 0, lastposition = 0;
    Thread extModeThread = new Thread(new extModeThread());

    AlarmManager mgr;
    /**
     * ***********
     */
    CheckBox chkbox,DmxStatusChkbox;
    Switch Manualsw;
    TextView SCframeTimeTxt,
            SCfadeTimeTxt, SCframeNumTxt, DmxStatusTxt;
    Spinner FixtChoose, ShowChoose;
    LinearLayout ManualLayout, ProgramLayout,SCLayout, FCLayout, FC2Layout, SCFrameLayout;
    ScrollView ScrollLayout;
    LayoutParams lpView;
    ImageButton StartBtn, GoboBtn;
    Button SCaddFrameBtn, SCdelFrameBtn, SCnextFrameBtn, SCprevFrameBtn,
            SCsaveShowBtn, FCaddFunctBtn, FCdelFunctBtn, FCsaveFixtBtn, ShowLoadBtn;

    /**
     * ***********
     */

    // Константы

    PendingIntent RESTART_INTENT;

    //Константы конфигурации
    public int DEFAULT_PROT = 1;
    public int FIXTURES_COUNT = 2;


    // Special
    Context context;
    Context Activity;
    ArrayAdapter<String> spinnerAdapter;

    // Imports
    DmxControl dmxControl;
    Fixture fixture;
    ShowCreating showCreating;
    FixtureCreating fixtureCreating;
    MainActivity MAct = this;
    ShowControl showControl;
    FilesControl fControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        configured = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);

        try {
            this.registerReceiver(mUsbReceiver, filter);
        } catch (Exception e) {
            Log.e("RegisterReciever", "" + e);
        }
        Activity = this;
        RESTART_INTENT = PendingIntent.getActivity(((ContextWrapper) Activity)
                .getBaseContext(), 0, new Intent(getIntent()), PendingIntent.FLAG_ONE_SHOT);

        ScrollLayout = (ScrollView) findViewById(R.id.manualscroll);
        ManualLayout = (LinearLayout) findViewById(R.id.manuallayout);

        ProgramLayout = (LinearLayout) findViewById(R.id.programlayout);

        SCFrameLayout = (LinearLayout) findViewById(R.id.SCFrameLayout);
        SCLayout = (LinearLayout) findViewById(R.id.SCLayout);
        SCLayout.setVisibility(LinearLayout.GONE);
//        SCLayout.setEnabled(false); // TODO: 31.12.15 нафига отключать?!


        Manualsw = (Switch) findViewById(R.id.manualsw);

        FixtChoose = (Spinner) findViewById(R.id.fixtchoose);
        FixtChoose.setOnItemSelectedListener(new FixtureSelection());

        ShowChoose = (Spinner) findViewById(R.id.programchoose);
        ShowChoose.setOnItemSelectedListener(new SCstart());

        Manualsw.setOnCheckedChangeListener(new onManualCheckedChangeListener());

        StartBtn = (ImageButton) findViewById(R.id.startbtn);
        StartBtn.setImageResource(R.drawable.play);
        StartBtn.setOnClickListener(new onStartClickButton());
        StartBtn.setLongClickable(true);
        StartBtn.setOnLongClickListener(new onStartPressButton());
        ShowLoadBtn = (Button) findViewById(R.id.showloadbtn);
        ShowLoadBtn.setOnClickListener(new onShowLoadBtnClick());

        chkbox = new CheckBox(context);
        chkbox.setText(getString(R.string.defprot));

        DmxStatusChkbox = new CheckBox(context);
        DmxStatusChkbox.setText(getString(R.string.action_DmxStatus));
        DmxStatusChkbox.setOnCheckedChangeListener(new onDmxStatusChkBoxCheckListener());
        DmxStatusChkbox.setChecked(false);
        DmxStatusTxt = (TextView) findViewById(R.id.DmxStatusTxt);


        fixture = new Fixture(this);
        dmxControl = new DmxControl(this);
        fControl = new FilesControl(this);
//        dmxControl.Start(dmxControl.OPEN_DMX);

        showCreating = new ShowCreating(this);
        fixtureCreating = new FixtureCreating(this);
        showControl = new ShowControl(this);

        //Блок showCreating

        SCaddFrameBtn = (Button) findViewById(R.id.SCaddFrameBtn);
        SCaddFrameBtn.setOnClickListener(showCreating.new onAddFrameClickListener());
        SCdelFrameBtn = (Button) findViewById(R.id.SCdelFrameBtn);
        SCdelFrameBtn.setOnClickListener(showCreating.new onDelFrameClickListener());
        SCsaveShowBtn = (Button) findViewById(R.id.SCsaveShowBtn);
        SCsaveShowBtn.setOnClickListener(showCreating.new onSaveShowClickListener());
        //
//        SCframeTimeTxt = (TextView) findViewById(R.id.SCframeTimeTxt);
//        SCfadeTimeTxt = (TextView) findViewById(R.id.SCfadeTimeTxt);
//        SCframeNumTxt = (TextView) findViewById(R.id.SCframeNumTxt);

//        SCframeTimeEdit = (EditText) findViewById(R.id.SCframeTimeEdit);
//        SCframeTimeEdit.setOnKeyListener(showCreating.new onFrameTimeEditListener());
//        SCfadeTimeEdit = (EditText) findViewById(R.id.SCfadeTimeEdit);
//        SCfadeTimeEdit.setOnKeyListener(showCreating.new onFadeEditListener());

        //Блок fixtureCreating

        FCLayout = (LinearLayout) findViewById(R.id.FCLayout);
        FCLayout.setVisibility(LinearLayout.GONE);
        FCLayout.setEnabled(false);

        FC2Layout = (LinearLayout) findViewById(R.id.FC2Layout);

        FCaddFunctBtn = (Button) findViewById(R.id.FCaddFunctBtn);
        FCaddFunctBtn.setOnClickListener(fixtureCreating.new onAddFuncClickListener());
        FCdelFunctBtn = (Button) findViewById(R.id.FCdelFunctBtn);
        FCdelFunctBtn.setOnClickListener(fixtureCreating.new onDelFunctClickListener());
        FCsaveFixtBtn = (Button) findViewById(R.id.FCsaveFixtBtn);
        FCsaveFixtBtn.setOnClickListener(fixtureCreating.new onSaveFixtClickListener());

        lpView = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onDestroy() {
        try{
            showControl.showThreadControl(showControl.SHOW_PAUSE);
            dmxControl.Stop((Integer) (configmap.get(DEFAULT_PROT)));
            this.unregisterReceiver(mUsbReceiver);
        }catch (Exception e){
            Log.e("DLControl_exit",String.valueOf(e));
            System.exit(0);
        }
        super.onDestroy();


    }



    /**
     * Диалог выбора протокола при запуске приложения. Если в файле config.conf
     * прописано значение по-умолчанию, то диалог не показывается,
     * а запускается указанный протокол.
     */
    void protChoose() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("al dialog", "" + configmap.get(DEFAULT_PROT));
                if (configmap.get(DEFAULT_PROT) == null || (Integer) configmap.get(DEFAULT_PROT) == 0) {
                    CharSequence[] pnames = new CharSequence[dmxControl.PrtclMap.size()];
                    for (int i = 0; i < dmxControl.PrtclMap.size(); i++) {
                        pnames[i] = dmxControl.PrtclMap.get(i + 1).keySet().toString();
                    }
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.chprottitle))
                            .setCancelable(false)
                            .setSingleChoiceItems(pnames, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    configmap.put(DEFAULT_PROT, which + 1);
                                }
                            })
                            .setView(chkbox)
                            .setNegativeButton(getString(R.string.exit),
                                    new onDialogClick())
                            .setPositiveButton(getString(R.string.entrance), new onDialogClick());
                    AlertDialog al = builder.create();
                    al.show();
                } else {
                    dmxControl.Start((Integer) configmap.get(DEFAULT_PROT));
                }
            }
        });


    }


    class onDialogClick implements DialogInterface.OnClickListener {


        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE: //
                    int pos = (Integer) configmap.get(DEFAULT_PROT);
                    if (pos != 0) {
                        dmxControl.Start(pos);
                        if (chkbox.isChecked()) {
                            fControl.saveConfig();
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, getString(R.string.chprottitle) + "!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    break;

                case Dialog.BUTTON_NEGATIVE:
                    System.exit(0);
                    break;

            }
        }
    }


    /**
     * Обработчик кнопки PLAY
     */

    public class onStartPressButton implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {

//TODO Найти ошибку
// здесь->
            runOnUiThread(extModeThread);

            return true;
        }

    }

    class extModeThread implements Runnable {

        @Override
        public void run() {

//                Thread.sleep(2000);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    for (int i = 1; i <= 50; i++) {
                        StartBtn.setAlpha(5 * i);
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Log.e("BTNAlpha", "err " + e);
                        }
                    }
//                        StartBtn.setAlpha(200);
                    if (StartBtn.isPressed()) {
                        if (!extmode) {
                            extmode = true;
                            Toast.makeText(context,
                                    "Включен расширенный режим!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            extmode = false;
                            Toast.makeText(context,
                                    "Расширенный режим выключен!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


//            if (StartBtn.isPressed()) {
//                if (!extmode) {
//                    extmode = true;
//                    Toast.makeText(context, "Включен расширенный режим!", Toast.LENGTH_SHORT).show();
//                } else {
//                    extmode = false;
//                    Toast.makeText(context, "Расширенный режим выключен!", Toast.LENGTH_SHORT).show();
//                }
//
//            }

        }
    }

    public class onStartClickButton implements View.OnClickListener {

        @Override
        public void onClick(View v) {


            if (!mplay) {
                try {
                    dmxControl.Start((Integer) configmap.get(DEFAULT_PROT));
                    showControl.ShowThreadStart(getshowname());
                    StartBtn.setImageResource(R.drawable.stop);
                    mplay = true;

                } catch (Exception e) {
                    Toast.makeText(context, "Ошибка старта Dmx потока",
                            Toast.LENGTH_SHORT).show();
                    Log.e("DmxThread", "StartError: " + e.getMessage());
                }

            } else {
                if (!showControl.stop) {
                    StartBtn.setImageResource(R.drawable.play);
                    try {

                        if (stoptime != 0) { //задержка, исключающая конфликт шоу
                            stoptime = (int) System.currentTimeMillis();
                            if (stoptime - starttime < 1000) {

                                Thread.sleep(1000 - (stoptime - starttime));
                            }
                        }
                        starttime = (int) System.currentTimeMillis();
                        showControl.showThreadControl(showControl.SHOW_PAUSE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    StartBtn.setImageResource(R.drawable.stop);
                    try {
                        stoptime = (int) System.currentTimeMillis();
                        if ((stoptime - starttime) < 1000) {
                            Thread.sleep(1000 - (stoptime - starttime));
                        }
                        showControl.showThreadControl(showControl.SHOW_RESUME);
                        starttime = (int) System.currentTimeMillis();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }

    /**
     * Обработчик кнопки "Загрузка шоу"
     */

    public class onShowLoadBtnClick implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                if (showControl.showThreadControl(showControl.SHOW_PAUSE)) {
                    showControl.newShow = true;
                    StartBtn.setImageResource(R.drawable.play);
                    showControl.showstart(ShowChoose.getSelectedItem().toString());
                    Thread.sleep(100);
                }

            } catch (Exception e) {
                Log.e("ShowLoadButton", "" + e);
            }
        }
    }


    /**
     * Обработчик переключателя "Ручного режима"
     */
    public class onManualCheckedChangeListener implements
            CompoundButton.OnCheckedChangeListener {

        boolean wasStoped = true;   //показывает, было ли остановлено шоу
                                    // перед переключением на ручной режим

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            DmxStatusTxt.setText("");
            if (isChecked) {

                try {
                    StartBtn.setVisibility(View.VISIBLE);
                    if (!dmxControl.started()) {
                        if (!dmxControl.Start((Integer) configmap.get(DEFAULT_PROT))) {
                            Manualsw.setChecked(false);
                            return;
                        }
                    }
                    if(showControl.works()){
                        showControl.showThreadControl(showControl.SHOW_PAUSE);
                        wasStoped=false;
                    }else{
                        wasStoped=true;
                    }
//                        StartBtn.setImageResource(R.drawable.stop);
//                        mplay = true;

                } catch (Exception e) {
                    // TODO: 07.12.15 R.string
                    Toast.makeText(context, "Ошибка старта Dmx потока",
                            Toast.LENGTH_SHORT).show();
                    Log.e("DmxThread", "StartError: " + e.getMessage());
                }
//                    SystemClock.sleep(100); // *******//

                ProgramLayout.setVisibility(LinearLayout.GONE);
                ScrollLayout.setVisibility(LinearLayout.VISIBLE);
                FixtChoose.setVisibility(LinearLayout.VISIBLE);
                if (SCmodeON) {
                    SCLayout.setVisibility(LinearLayout.VISIBLE);
                    SCLayout.setEnabled(true);
                    StartBtn.setVisibility(View.GONE);
                }
                if (FCmodeON) {
                    FCLayout.setVisibility(LinearLayout.VISIBLE);
                    FCLayout.setEnabled(true);
                    StartBtn.setVisibility(ImageButton.GONE);
                }


            } else {
                ScrollLayout.setVisibility(LinearLayout.GONE);
                FixtChoose.setVisibility(Spinner.GONE);
                ProgramLayout.setVisibility(LinearLayout.VISIBLE);
                SCLayout.setVisibility(LinearLayout.GONE);
                FCLayout.setVisibility(LinearLayout.GONE);
                StartBtn.setVisibility(ImageButton.VISIBLE);
                try {
                    if (showControl.show_thread.isAlive()&&!wasStoped) {
                        showControl.showThreadControl(showControl.SHOW_RESUME);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

    }

    /**
     * Работа со слайдером изменения DMX-значения
     *
     * @author Michael_Admin
     */

    class SeekChangeListener implements SeekBar.OnSeekBarChangeListener,View.OnTouchListener {

        String Name = "";
        int ch = 0;
        //TextView text;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            dmxmas[ch-1] = (byte) progress;
            DmxStatusTxt.setText(" Адрес Dmx канала: " + String.valueOf(ch) +
                    " Dmx Значение: " + String.valueOf(progress));
// DmxStatusTxt.setText(String.valueOf(progress));
            //вывод значений DMX и номера канала в Textview.

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Name = String.format("%3d %s",
                    FixtChoose.getSelectedItemPosition() + 1,seekBar.getTag(seekBar.getId())); // Возврат полного "имени" функции конкретного слайдера
            ch = fixture.getadress(Name); // Возврат номера канала
            //		text = (TextView) seekBar.getTag(ManualLayout.getId());
//            DmxStatusTxt.setText(String.valueOf(seekBar.getProgress()));
            DmxStatusTxt.setText(" Адрес Dmx канала: " + String.valueOf(ch) + " Dmx Значение: " + String.valueOf(seekBar.getProgress()));
            //вывод значений DMX и номера канала в Textview.
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

//            	text.setText((CharSequence) seekBar.getTag(seekBar.getId()));
//            DmxStatusTxt.setText("");
            dmxControl.dmxstatus_works=DmxStatusChkbox.isChecked(); //включение "dmx-статуса"

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction()==MotionEvent.ACTION_DOWN){
                dmxControl.dmxstatus_works=false; //отключение "dmx-статуса"
                Name = String.format("%3d",
                        FixtChoose.getSelectedItemPosition() + 1)
                        + " "
                        + v.getTag(v.getId()); // Возврат полного "имени" функции конкретного слайдера
                ch = fixture.getadress(Name); // Возврат номера канала
                //		text = (TextView) seekBar.getTag(ManualLayout.getId());
//                DmxStatusTxt.setText(" Адрес Dmx канала: " + (ch) + " Dmx Значение: " + ((SeekBar) v).getProgress());
                //вывод значений DMX и номера канала в Textview.


            }

            return false;
        }
    }


    /**
     * Обработчик нажатий кнопок ГОБО
     *
     * @author Michael_Admin
     */
    class onGoboBtnClickListener implements View.OnClickListener {
        TextView txt;

        @Override
        public void onClick(View v) {

            int a = (Integer) v.getTag(ManualLayout.getId());
            dmxmas[(Integer) v.getTag(v.getId())] = (byte) a;

            txt = (TextView) v.getTag(Manualsw.getId());

            txt.setText("Dmx значение Gobo " + a);

            //
            //


            if (GoboBtn != null) {
                GoboBtn.setBackgroundResource(R.drawable.rama);
            }
            v.setBackgroundResource(R.drawable.rama2);
            GoboBtn = (ImageButton) v;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            txt.setText("Gobo");
                        }
                    });

                }
            }, 2000);
        }

    }

    /**
     * Обработчик выбора прибора
     *
     * @author Michael_Admin
     */

    class FixtureSelection implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            // if(Manualsw.isChecked()){
            layoutRefresh();
            // }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    /**
     * Обновление слайдеров и TextView к этим слайдерам
     */
    public void layoutRefresh() {
        int position = FixtChoose.getSelectedItemPosition();
        ManualLayout.removeAllViews();
        int functioncount = (fixture.getadress(String.format("%3d", position + 1)) - 1);
        int lastdmx = functioncount - (functioncount / 1000) * 1000;
        functioncount /= 1000;


        if (fixture.Gobo.getGoboCount(position) != 0) {
            TextView gobotext = new TextView(context);
            LinearLayout goboLayout = new LinearLayout(context);
            LayoutParams scrlp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            HorizontalScrollView goboscr = new HorizontalScrollView(context);
            goboscr.setHorizontalFadingEdgeEnabled(true);
            goboscr.setHorizontalScrollBarEnabled(false);
            goboscr.setLayoutParams(scrlp);
            LayoutParams goboBtnParams = new LayoutParams(50, 50);

            goboLayout.setLayoutParams(lpView);
            goboLayout.setOrientation(LinearLayout.HORIZONTAL);

            gobotext.setLayoutParams(lpView);
            gobotext.setText("GOBO");
            goboscr.addView(goboLayout);
            ManualLayout.addView(gobotext);
            ManualLayout.addView(goboscr);
            for (int i = 0; i < fixture.Gobo.getCount(position); i++) {
                ImageButton gobobtn = new ImageButton(context);
                gobobtn.setLayoutParams(goboBtnParams);
                gobobtn.setTag(gobobtn.getId(), fixture.Gobo.channel);
                gobobtn.setTag(ManualLayout.getId(), fixture.Gobo.getGoboValue(
                        FixtChoose.getSelectedItemPosition(), i));
                gobobtn.setTag(Manualsw.getId(), gobotext);
                gobobtn.setBackgroundResource(R.drawable.rama);
                goboLayout.addView(gobobtn);
                gobobtn.setOnClickListener(new onGoboBtnClickListener());

            }

        }

        for (int i = 0; i < functioncount; i++) {

            TextView textview = new TextView(context);
            SeekBar seekbar = new SeekBar(context);

            seekbar.setTag(seekbar.getId(), fixture.functlist.get(lastdmx + i));
            seekbar.setTag(seekbar.getId(), fixture.functlist.get(lastdmx + i));

            textview.setLayoutParams(lpView);
            seekbar.setLayoutParams(lpView);

            seekbar.setTag(ManualLayout.getId(), textview);
            textview.setText(fixture.functlist.get(lastdmx + i));

            ManualLayout.addView(textview);
            ManualLayout.addView(seekbar);
            seekbar.setMax(0);
            seekbar.setMax(255);
            seekbar.setProgress(0);
            seekbar.setProgress(conv(MAct.dmxmas[lastdmx + i]));

            seekbar.setOnSeekBarChangeListener(new SeekChangeListener());
//            seekbar.setOnTouchListener(new SeekChangeListener());

        }
    }

    /**
     * Обработчик выбора "Создать новое шоу...", вызов функций создания шоу.
     *
     * @author Michael_Admin
     */

    public class SCstart implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            if ((id == (ShowChoose.getCount() - 1))) {
                ShowChoose.setSelection(lastposition);
                SCmodeON = true;
                Manualsw.setChecked(true);
                showCreating.start();
            } else {
                lastposition = position;
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    public class onDmxStatusChkBoxCheckListener implements CheckBox.OnCheckedChangeListener{


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (dmxControl.dmxstatus_works) { //если статус включен
                dmxControl.Stop(dmxControl.DMX_STATUS);
                DmxStatusTxt.setText("");
            } else { //если выключен
                dmxControl.Start(dmxControl.DMX_STATUS);
                DmxStatusTxt.setText("");
            }

        }
    }

    public String getshowname() {
        return ShowChoose.getSelectedItem().toString();
    }


    /**
     * ************************** USB broadcast receiver ***************************
     */
    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() { // Описание
        // BroadcastReciever
        @Override
        public void onReceive(Context context, Intent intent) {
            String TAG = "FragL";
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.i(TAG, "DETACHED...");

            }
        }
    };



    /**
     * Сообщение об ошибке  с заголовком title и сообщением mess
     *
     * @param Activity основной контекст
     * @param title    заголовок сообщения
     * @param mess     тело сообщения
     */
    public void errorDialog(final Context Activity, String title, String mess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity);
        builder.setTitle(title)
                .setMessage(mess)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mgr = (AlarmManager) Activity
                                .getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC,
                                System.currentTimeMillis() + 1000,
                                RESTART_INTENT);
                        System.exit(2);

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void errorDialog(final Context Activity, int t, int e) {
        errorDialog(Activity, getString(t), getString(e));
    }


    /**
     * Экшн бар меню
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        menu.getItem(3).setActionView(DmxStatusChkbox);
//        MenuItem item = menu.getItem(300);
//        item.setCheckable(true);
        return true;
    }

    /**
     * Обработчик пунктов меню Экшн бара
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret;
        int id = item.getItemId();
        if (id == R.id.action_SCadd) {
            ret = true;
            SCmodeON = true;
            Manualsw.setChecked(true);
            showCreating.start();

        } else if (id == R.id.action_Fixtadd) {
            ret = true;
            FCmodeON = true;
            Manualsw.setChecked(true);
            fixture.add();

        } else
// if (id == R.id.action_DMXStatus) {
//
//            ret = true;
//            // TODO: 07.12.15 протестить
////            if(item.isChecked()){ // TODO: 03.12.15 Сделан костыль. Исправить на галочку
////                dmxControl.Start(dmxControl.DMX_STATUS);
////
////            }else{dmxControl.Stop(dmxControl.DMX_STATUS);}
//
//        } else
            {
            ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }

    int conv(int a) {
        if (a >= 0) {
            return a;
        } else {
            return (a + 256);
        }

    }


}
