package com.yl.middleware;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class IOHelper {
    private Context mContext;

    public IOHelper(Context mContext) {
        this.mContext = mContext;
    }

    /*
     * 这里定义的是一个文件保存的方法，写入到文件中，所以是输出流
     * */
    public void save(String filename, String filecontent) throws Exception {
        FileOutputStream output = mContext.openFileOutput(filename, Context.MODE_APPEND);

        OutputStreamWriter osw = new OutputStreamWriter(output, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(filecontent + "\t\n");

        //注意关闭的先后顺序，先打开的后关闭，后打开的先关闭
        bw.close();
        osw.close();
        output.close();
    }

    /*
     * 这里定义的是文件读取的方法
     * */
    public ArrayList read(String filename) throws IOException {
        //打开文件输入流
        FileInputStream input = mContext.openFileInput(filename);
        InputStreamReader isr = new InputStreamReader(input, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String line = "";
        ArrayList<String> result = new ArrayList<String>();

        while ((line = br.readLine()) != null) {
            result.add(line);
        }

        br.close();
        isr.close();
        input.close();
        return result;
    }

    public void saveExternalFile(String filename, String filecontent) throws Exception {
        String filePath = Environment.getExternalStorageDirectory() + "/" + filename;
        File myFile = new File(filePath);
        try {
            FileOutputStream fos = new FileOutputStream(myFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(filecontent + "\t\n");
            osw.flush();
            fos.flush();
            osw.close();
            fos.close();

        } catch (FileNotFoundException e) {
//            Log.e("文件没找到", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
//            Log.e("io错误", e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList readExternalFile(String filename) throws IOException {
        String filePath = Environment.getExternalStorageDirectory() + "/" + filename;
        File myFile = new File(filePath);

        try {
            FileInputStream fis = new FileInputStream(myFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            ArrayList<String> result = new ArrayList<String>();

            while ((line = br.readLine()) != null) {
                result.add(line);
            }

            br.close();
            isr.close();
            fis.close();
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * 将对象保存到文件中
     */
    public void saveObjToFile(Object o, String fileName) {
        try {
            //FileOutputStream output = mContext.openFileOutput(fileName, Context.MODE_PRIVATE | Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
            FileOutputStream output = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            //写对象流的对象
            ObjectOutputStream oos = new ObjectOutputStream(output);
            oos.writeObject(o);
            oos.close();
            output.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * 从文件中读出对象，并且返回Object对象
     */
    public Object getObjFromFile(String fileName) {
        try {
            FileInputStream input = mContext.openFileInput(fileName);
            ObjectInputStream ois = new ObjectInputStream(input);
            Object o = (Object) ois.readObject();
            ois.close();
            input.close();
            return o;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    /*
     * 从文件中读出对象，并且返回Object对象
     */
    public Object getObjFromFileJava(File targetFile) {
        try {
            FileInputStream fin = new FileInputStream(targetFile);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            fin.close();
            return (Object) buffer;


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


//    /*
//     * 将EncryptionParameters对象保存到文件中
//     */
//    public void saveObjToFile(EncryptionParameters o, String fileName) {
//        try {
//            FileOutputStream output = mContext.openFileOutput(fileName, Context.MODE_PRIVATE | Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
//            //写对象流的对象
//            ObjectOutputStream oos = new ObjectOutputStream(output);
//            oos.writeObject(o);
//            oos.close();
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    /*
//     * 从文件中读出对象，并且返回EncryptionParameters对象
//     */
//    public EncryptionParameters getObjFromFile(String fileName) {
//        try {
//            FileInputStream input = mContext.openFileInput(fileName);
//            ObjectInputStream ois = new ObjectInputStream(input);
//            EncryptionParameters o = (EncryptionParameters) ois.readObject();
//            return o;
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        return null;
//    }
}
