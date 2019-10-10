package top.keepempty;

import android.util.Log;

import java.io.IOException;

import top.keepempty.sph.library.SerialPortJNI;

/**
 * Created by cc
 * On 2019/10/10.
 */
public class SerHelper {

    private static String TAG = "SerHelper";


    private volatile static SerHelper singleton;

    private SerHelper() {
    }

    public static SerHelper getSingleton() {
        if (singleton == null) {
            synchronized (SerHelper.class) {
                if (singleton == null) {
                    singleton = new SerHelper();
                }
            }
        }
        return singleton;
    }

    private volatile static boolean mIsOpen;
    private ReceiveThread mReceiveThread = null;
    private volatile String receiveData = "";


    public boolean open() {
        int i = SerialPortJNI.openPort("dev/ttyUSB0", 19200, 8, 1, 'E');

        if (i == 1) {
            Log.d(TAG, "打开成功---" );

            mIsOpen = true;
            getSerialPort();
        } else {
            mIsOpen = false;
        }
        return mIsOpen;


    }


    /**
     * 发送数据
     * 通过串口，发送数据到单片机
     *
     * @param data 要发送的数据
     */
    public void sendSerialPort(String data) {
        Log.d(TAG, "send----" + data);
        if (!mIsOpen) {
            return;
        }

        try {
            data = data.trim();
            byte[] sendData = DataUtils.HexToByteArr(data);
            SerialPortJNI.writePort(sendData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getSerialPort() {
        if (mReceiveThread == null) {

            mReceiveThread = new ReceiveThread();
        }
        if (!mReceiveThread.isAlive()) {
            if (mReceiveThread.getState() == Thread.State.NEW) {
                mReceiveThread.start();
            } else {
                mReceiveThread = new ReceiveThread();
                mReceiveThread.start();

            }
        }

    }

    /**
     * 接收串口数据的线程
     */

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (mIsOpen) {
                try {

                    byte[] bytes = SerialPortJNI.readPort(1024);

                    if (bytes != null) {
                        String readString = DataUtils.ByteArrToHex(bytes,0,bytes.length);
                        receiveData = receiveData + readString.trim();
                        if (receiveData.length() >= 22) {
                            Log.d(TAG, "receiveData---" + receiveData);
                            receiveData = "";
                        }


                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


}
