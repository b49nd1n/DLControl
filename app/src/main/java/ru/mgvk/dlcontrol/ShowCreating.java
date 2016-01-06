package ru.mgvk.dlcontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ShowCreating extends DmxControl {

    // Переменные
    MainActivity MAct;
    Fixture fixture;

    static int NEW = 0, CURRENT = 1, NEXT = 2, PREV = 3;

    int[][][] SCmas;
    int fixturecount, functioncount;
    int currentFrameNumber = 0, FrameCount = 0, MaxFrameCount = 20;
    ArrayList<String> SClist = new ArrayList<String>();
    ArrayList<LinearLayout> layoutlist = new ArrayList<LinearLayout>();

    EditText SCframeTimeEdit, SCfadeTimeEdit;

    Integer colorFrameOff,colorFrameOn;


    Context context;

    public ShowCreating(Context parentcontext) {
        context = parentcontext;
        MAct = (MainActivity) parentcontext;
        fixture = MAct.fixture;
    }

    public ShowCreating(byte[] mas) {

    }

    public void start() {

        currentFrameNumber=FrameCount=0;
        fixturecount = MAct.fixture.getFixturesCount();
        functioncount  = MAct.fixture.getMaxFunctionsCount();
        SCmas = new int[MaxFrameCount][fixturecount][functioncount + 2];

        colorFrameOn = MAct.getResources().getColor(R.color.second);
        colorFrameOff = MAct.getResources().getColor(R.color.exp);

        addFrame();

    }

    Button createLayoutBlackout(int frameIndex){
        Button layoutBlackout = new Button(context);
        layoutBlackout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layoutBlackout.setBackgroundColor(colorFrameOff);
        layoutBlackout.setTag(frameIndex);
        layoutBlackout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFromCurrentFrame((Integer) v.getTag());
            }
        });

     return layoutBlackout;
    }



    void changeToCurrentFrame(int firstframe){
        changeFrame(firstframe,currentFrameNumber);
    }

    void changeFromCurrentFrame(int secondframe){
        changeFrame(currentFrameNumber, secondframe);
    }

    void changeFrame(int firstframe,int secondframe){

        setLayoutBlackout(firstframe);
        saveFrame(firstframe);
        currentFrameNumber = secondframe;
        openFrame();
        removeLayoutBlackout(secondframe);
        MAct.layoutRefresh();

    }

    void setLayoutBlackout(int frameIndex){
        for(int i=0;i<4;i++){
            layoutlist.get(frameIndex).getChildAt(i).setEnabled(false);
        }
        layoutlist.get(frameIndex).setBackgroundColor(colorFrameOff);
    }

    void removeLayoutBlackout(int frameIndex){
        for(int i=0;i<4;i++){
            layoutlist.get(frameIndex).getChildAt(i).setEnabled(true);
        }
        layoutlist.get(frameIndex).setBackgroundColor(colorFrameOn);
    }




    LinearLayout l; //временная переменная лэйаута

    void setFrameLayout() {

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        l = new LinearLayout(context);
        l.setLayoutParams(lp);
        l.setOnClickListener(new onFrameClickListener());

        ArrayList<View> viewlist = new ArrayList<View>();

        TextView text_frame_number = new TextView(context);
        text_frame_number.setText(String.valueOf(currentFrameNumber));
        text_frame_number.setGravity(Gravity.CENTER);
        text_frame_number.setTextSize(25);
        viewlist.add(0, text_frame_number);
        EditText text_frame_time = new EditText(context);
        viewlist.add(1, text_frame_time);
        EditText text_fade_time = new EditText(context);
        viewlist.add(2, text_fade_time);
        Button button_frame_delete = new Button(context);
        viewlist.add(3, button_frame_delete);

        for (View view : viewlist) {
            view.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1));
            l.addView(view);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MAct.SCFrameLayout.addView(l);
            }
        });


        l.setTag(currentFrameNumber);

        layoutlist.add(currentFrameNumber, l);
        setTextEdit();
        printFrameNumber();

        if(FrameCount>1) {
            setLayoutBlackout(currentFrameNumber);
        }
    }

    class onFrameClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            changeFromCurrentFrame((Integer) v.getTag());
        }
    }


    void addFrame() {
        addFrame(1);
    }

    /**
     * Добавление новой сцены
     *
     * @param a 0, если нужна нулевая сцена
     */

    void addFrame(int a) {

        int curfr = currentFrameNumber;

        if (FrameCount > 0) {
            saveFrame(currentFrameNumber);
        }

        if (FrameCount < MaxFrameCount) {
            currentFrameNumber = FrameCount++;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, MAct.getString(R.string.SCframe_max_err), Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        setFrameLayout();
        setDefaultTimes();
        if (a == 0) {
            for (int i = 0; i < MAct.dmxmas.length; i++) {
                MAct.dmxmas[i] = 0;
            }
            MAct.layoutRefresh();
        }
        if(FrameCount>1){
            changeToCurrentFrame(curfr);
        }

    }

    public class onAddFrameClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            addFrame();

        }

    }


    void deleteFrame(int frame) {
        // TODO Сделать поиск макс. кол-ва функций в
        // методе start
        for (int i = 0; i < fixturecount; i++) { // Обнуление удаляемой сцены
            // в массиве
            for (int j = 2; j <= functioncount + 1; j++) {
                SCmas[frame][i][j] = 0;
            }
        }
        MAct.SCFrameLayout.removeViewAt(frame);
    }


    public class onDelFrameClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (FrameCount == 1) {
                return;
            } else {
                changeFromCurrentFrame((--FrameCount)-1);
                currentFrameNumber = (FrameCount)-1;
                deleteFrame(FrameCount);

            }
        }
    }

    /**
     * Сохранение текущего состояния в массив временного шоу
     *
     * @param frameNumber - индекс сцены
     */
    void saveFrame(int frameNumber) {
        int lastdmx = 0;
        for (int i = 0; i < fixturecount; i++) { // Сохранение текущего
            // состояния в массив
            int functioncount = (fixture.DmxMap.get(String.format("%3d", i + 1))) / 1000;
            for (int j = 2; j <= functioncount + 1; j++) {
                SCmas[frameNumber][i][j] = MAct.dmxmas[j - 2 + lastdmx];
            }
            lastdmx += functioncount;
        }
        saveFrameTime(frameNumber);
    }

    /**
     * Открытие сцены под индексом {@link #currentFrameNumber}
     */
    void openFrame() {
        int lastdmx = 0;
        for (int i = 0; i < fixturecount; i++) { // "Открытие" следующей
            // сцены - запись из SCmas в MAct.dmxmas
            int functioncount = (fixture.DmxMap.get(String.format("%3d", i + 1))) / 1000;
            for (int j = 2; j <= functioncount + 1; j++) {
                MAct.dmxmas[j - 2 + lastdmx] = (byte) (SCmas[currentFrameNumber][i][j]);
            }
            lastdmx += functioncount;
        }
        setTextEdit();
    }

    /**
     * Вывод номера сцены
     */
    void printFrameNumber() {
        ((TextView) layoutlist.get(currentFrameNumber).getChildAt(0))
                .setText(String.valueOf(currentFrameNumber));

    }

    /**
     * Запись времени сцены и времени fade в массив
     *
     * @param frame индекс сцены
     */
    void saveFrameTime(int frame) {
        try {
            SCmas[frame][0][0] = Integer.valueOf(SCframeTimeEdit.getText()
                    .toString());
            SCmas[frame][0][1] = Integer.valueOf(SCfadeTimeEdit.getText()
                    .toString());
        } catch (Exception e) {
            SCmas[frame][0][0] = 500;
            SCmas[frame][0][1] = 0;
        }
    }

    /**
     *
     *
     */
    void setTextEdit() {
        SCframeTimeEdit = (EditText) layoutlist.get(currentFrameNumber).getChildAt(1);
        SCframeTimeEdit.setOnKeyListener(new onFrameTimeEditListener());
        SCfadeTimeEdit = (EditText) layoutlist.get(currentFrameNumber).getChildAt(2);
        SCfadeTimeEdit.setOnKeyListener(new onFadeEditListener());
    }

    void setDefaultTimes() {
        SCframeTimeEdit.setText(String.valueOf(1000));
        SCfadeTimeEdit.setText(String.valueOf(500));
    }


    public class onFrameTimeEditListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            try {
				SCmas[currentFrameNumber][0][0] = Integer.valueOf(SCframeTimeEdit
						.getText().toString());
            } catch (Exception e) {
                SCmas[currentFrameNumber][0][0] = 500;

            }

            return false;
        }
    }

    public class onFadeEditListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            try {
				SCmas[currentFrameNumber][0][1] = Integer.valueOf(SCfadeTimeEdit
						.getText().toString());
            } catch (Exception e) {
                SCmas[currentFrameNumber][0][1] = 0;
            }

            return false;
        }

    }

    public class onSaveShowClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            final EditText SCnameEdit = new EditText(context);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Введите название шоу")
                    .setCancelable(false)
                    .setPositiveButton("Сохранить",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    saveFrame(currentFrameNumber);
                                    saveFrameTime(currentFrameNumber);
                                    for (int i = 0; i < fixture.functlist.size(); i++) {
                                        String str = fixture.functlist.get(i);
                                        if (!SClist.contains(str)) {
                                            SClist.add(str);
                                        }
                                    }

                                    ShowSave(SCnameEdit.getText().toString());
                                    SCexit();
                                }
                            });
            builder.setNegativeButton("Выйти без сохранения",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder truebuilder = new AlertDialog.Builder(
                                    context);
                            truebuilder
                                    .setTitle("Вы уверены?")
                                    .setCancelable(true)
                                    .setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    SCexit();

                                                }
                                            });

                            AlertDialog alert = truebuilder.create();
                            alert.show();

                        }
                    });

            builder.setView(SCnameEdit);

            AlertDialog alert = builder.create();
            alert.show();
            SCnameEdit.setText("Новое шоу 1");

        }

    }

    /**
     * Выход из режима создания шоу
     */
    void SCexit() {

        for (int i = 0; i < 512; i++) {
            MAct.dmxmas[i] = 0;
        }

        MAct.SCFrameLayout.removeAllViews();


        MAct.fControl.getshows();

        MAct.SCmodeON = false;
        MAct.Manualsw.setChecked(false);
    }


    public void ShowSave(String showname) {

        @SuppressWarnings("static-access")
        String path = MAct.fControl.PATH;

        File file = new File(path + showname + ".shw");
        try {
            file.createNewFile();
        } catch (IOException e) {
            Toast.makeText(context, "Невозможно сохранить шоу!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(file);

            String n = "\n";
            String s = FrameCount + n;

            for (String f : SClist) {
                s += f + " ";
            }
            s = s.substring(0, s.length() - 1) + n;
            writer.print(s);
            s="";
            for (int frame = 1; frame <= FrameCount; frame++) {
                s = SCmas[frame][0][0] + n + SCmas[frame][0][1] + n;
                for (int fixture = 0; fixture < fixturecount; fixture++) {
                    for (int function = 2; function <= functioncount + 1; function++) {
                        s += SCmas[frame][fixture][function] + ";";
                    }
                    s += n;
                }
                writer.print(s);
            }
            writer.close();

        } catch (FileNotFoundException e) {
            Toast.makeText(context, "Ошибка записи в файл", Toast.LENGTH_SHORT)
                    .show();
        }
    }


}
