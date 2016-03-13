package ru.mgvk.dlcontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Класс управления файлами
 * Created by mihail on 10.11.15.
 */
public class FilesControl extends MainActivity {


    String configfilename = "config.conf";
    public static final String FILE_FOLDER = "DLControl";
    public static final String PATH = android.os.Environment
            .getExternalStorageDirectory()
            + java.io.File.separator
            + FILE_FOLDER + java.io.File.separator;


    String[] FixtMas;

    Context context;
    BufferedReader configfile;
    String config; // Переменная для считывания значений из конфиг-файла

    MainActivity MAct;


    public FilesControl() {

    }

    public FilesControl(Context parentcontext) {
        context = parentcontext;
        MAct = (MainActivity) context;
        filesControl();
    }

    void filesControl() {
        File dir = new File(PATH);
        if (!(dir.isDirectory() && dir.exists())) {
            Toast.makeText(context, "Не найдена папка конфигураций",
                    Toast.LENGTH_SHORT).show();
            if (!dir.mkdirs()) {
                // TODO: 05.02.16 обработать случай ошибки создания папки
                new AlertDialog.Builder(context)
                        .setTitle(MAct.getString(R.string.unexpected_err))
                        .setPositiveButton(MAct.getString(R.string.exit),(dialog, which) -> {System.exit(0);})
                        .create()
                        .show();
            }
            Log.i("DLControlDir", "Dir is made");
        }

        try {  //Загрузка конфигов
            configfile = OpenFile(configfile, configfilename);
            if (configfile == null) {
                //Сообщение об ошибке
                runOnUiThread(() ->
                        new AlertDialog.Builder(context)
                                .setTitle(MAct.getString(R.string.error))
                                .setMessage(MAct.getString(R.string.err_config))
                                .setCancelable(false)
                                .setNegativeButton(MAct.getString(R.string.exit),
                                        (dialog, which) -> {
                                            System.exit(0);
                                        })
                                .setPositiveButton(MAct.getString(R.string.demo_mode),
                                        (dialog, which) -> {
                                            try {

                                                int t[] = {R.string.configfile, R.string.fixtfile, R.string.showfile};
                                                int f[] = {R.string.demo_config, R.string.demo_fixt, R.string.demo_show};
                                                for (int i = 0; i < 3; i++) {
                                                    String s = MAct.getString(t[i]); // Демо-конфиг
                                                    File file = new File(PATH + MAct.getString(f[i]));
                                                    PrintWriter writer = new PrintWriter(file);
                                                    writer.print(s);
                                                    writer.close();
                                                }

                                                Log.i("DemoFile", "Demo files created");

                                                filesControl();

                                            } catch (Exception e) {
                                                Log.e("DemoFile", "DemoFileCopyErr " + e);
                                            }
                                        })
                                .create()
                                .show());

            } else {
                configfile.mark(1000);
                Log.i("ConfigFileLoading", "Config file loaded");
                Config();
                //Todo исправить косяк при автогенерации файлов и их считывании
                getshows();
                MAct.protChoose();
            }
        } catch (Exception e) {
            Log.e("ConfigFileLoading", "" + e);

        }


    }

    /**
     * Загрузка списка шоу
     */

    public void getshows() {
        try {
            File path = new File(PATH);
            File[] files = path.listFiles();
            ArrayList<String> fileslist = new ArrayList<String>();
            for (File f : files) {
                String s = String.valueOf(f.getName());
                if (s.contains(".shw")) {
                    fileslist.add(s.substring(0, s.length() - 4));
                }
            }
            fileslist.add("Создать новое шоу..."); //Todo r.string

            MAct.spinnerAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, fileslist);
            MAct.spinnerAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            MAct.ShowChoose.setAdapter(MAct.spinnerAdapter);
        } catch (Exception e) {
            Log.e("GetShows", "" + e);

        }
    }

    public void Config() {
        try {
            configfile.reset();
            config = configfile.readLine();
            int a = (Integer.valueOf(config));
            MAct.configmap.put(MAct.FIXTURES_COUNT, a);
            FixtMas = new String[a + 1];

            FixtMas[a] = MAct.getString(R.string.action_FixtAdd);

            for (int i = 0; i < a; i++) {
                FixtMas[i] = String.format("%3d", i + 1) + " "
                        + configfile.readLine();
            }


            MAct.fixture.add(FixtMas);

            MAct.spinnerAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, FixtMas);
            MAct.spinnerAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            MAct.FixtChoose.setAdapter(MAct.spinnerAdapter);

            String s = configfile.readLine();
            configfile.close();
            if (s == null) {
                MAct.configmap.put(MAct.DEFAULT_PROT, 0);  //флаг "протокол по умолчанию"
            } else {
                a = Integer.valueOf(s);
                Log.d("configmap", "" + a);
                MAct.configmap.put(MAct.DEFAULT_PROT, a);//флаг "протокол по умолчанию"
                Log.d("configmap", "" + MAct.configmap.get(MAct.DEFAULT_PROT));
            }
        } catch (IOException e) {
            Log.e("ConfigFile", "FileReadingError: " + e);
        }

    }


    public boolean saveConfig() {
        boolean res;

        File f = new File(PATH + configfilename);
        PrintWriter writer;
        try {

            if (!f.delete()) {
                Log.e("SaveConfig", "Error deleting file");
            }
            if (!f.createNewFile()) {
                Log.e("SaveConfig", "Error creating file " + f.getName());
                res = false;
            } else {
                Log.d("SaveConfig", "File" + f.getName() + "created");

                int fc = (Integer) MAct.configmap.get(MAct.FIXTURES_COUNT);
                writer = new PrintWriter(f);
                writer.println(fc);

                for (int i = 0; i < fc; i++) {
                    writer.println(FixtMas[i].substring(4));
                }

                writer.println(MAct.configmap.get(MAct.DEFAULT_PROT));

                writer.close();
                res = true;
            }
        } catch (Exception e) {
            Log.e("ConfigSave", "" + e);
            MAct.errorDialog(MAct, R.string.error, R.string.err_config);
            res = false;
        }

        return res;
    }


    public BufferedReader OpenFile(BufferedReader br, String file) {
        try {
            br = new BufferedReader(new FileReader(PATH + file));
        } catch (Exception e) {
            Log.e("OpenFile()", "File{" + file + "} reading error: " + e);
            Toast.makeText(context, "Проверьте файлы конфигурации!",
                    Toast.LENGTH_SHORT).show();
            return br;
        }
        Log.i("OpenFile()", "File{" + file + "} is loaded");
        return br;
    }


}
