package ru.mgvk.dlcontrol;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

/**
 *  Класс, отвечающий за логгирование
 *
 * Created by mihail on 02.07.15.
 *
 */
public class Logs {

    //Переменные
    String LogString = " ", TagString = " "; //Переменные тэга и лога для потока
    HashMap <Integer,String> logmap;
    int logcount =0;

    MainActivity Mact;


    Logs(Context context){
        Mact = (MainActivity) context;

    }

    Logs(){

    }

    public void StartLogging(){ //Запуск потока логгирования
        Thread thread = new Thread(new LogThread());
        thread.start();
    }

    public  void add(String Tag,String Log){  //Добавление логов для вывода в потоке
      //TODO написать метод добавления данных в поток
        logcount++;
        logmap.put(logcount,Tag);
        logmap.put(100 * logcount, Log);
    }

    public void update(int index,String Tag,String Log){ //Обновление существующих логов в потоке (с указанием индекса)
        //TODO придумать, что делать с индексами
        String s =logmap.get(index);
        String ss = logmap.get(100*index);



    }

    class LogThread implements  Runnable{

        LogThread(){

        }
        @Override
        public void run(){
            try{
                for(;;){
                    //TODO вывод содержимого dmxmas в лог
                    Log.d("DmxThread"," "); // Логгирование dmxmas
                    for(int i=0;i<logcount;i++){
                        //TODO дописать
                    }

                    Log.d("TagString","LogString"); // Потоковый вывод логов (добавляются в методе add())
                    Thread.sleep(45);
                }
            }catch (Exception e){

            }

        }

    }
}