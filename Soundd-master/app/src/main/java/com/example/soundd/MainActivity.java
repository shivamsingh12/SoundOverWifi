package com.example.soundd;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
//import android.text.Editable;
//import android.util.Log;
import android.util.Log;
import android.view.View;
//import android.widget.EditText;
//import android.widget.TextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import java.util.concurrent.ArrayBlockingQueue;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    ArrayBlockingQueue<byte[]> a;
//    int bufsize = AudioTrack.getMinBufferSize(48000,
//            AudioFormat.CHANNEL_OUT_STEREO,
//            AudioFormat.ENCODING_PCM_FLOAT);
//
//    AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC,
//            48000, //sample rate
//            AudioFormat.CHANNEL_OUT_STEREO, //2 channel
//            AudioFormat.ENCODING_PCM_FLOAT, // 32bit ieeefloat
//            bufsize,
//            AudioTrack.MODE_STREAM);
    /////////////////////////////////////////////////////////////
    private final int SAMPLE_RATE = 48000;
    private final int CHANNEL_COUNT = AudioFormat.CHANNEL_OUT_STEREO;
    private final int ENCODING = AudioFormat.ENCODING_PCM_FLOAT;
    int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_COUNT, ENCODING);
    AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();
    AudioFormat audioFormat = new AudioFormat.Builder()
            .setEncoding(ENCODING)
            .setSampleRate(SAMPLE_RATE)
            .build();
    AudioTrack audio = new AudioTrack(audioAttributes, audioFormat, bufferSize
            , AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
    ////////////////////////////
    PowerManager.WakeLock wl;
    volatile boolean bool1 = true;
    volatile boolean boolUnicast = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init1();
        a = new ArrayBlockingQueue<>(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                audio.flush();
                byte[] buff1 = new byte[3841];
                DatagramPacket clPacket = new DatagramPacket(buff1, buff1.length);
                MulticastSocket ms = null;
                InetAddress group = null;
                try {
                    ms = new MulticastSocket(1234);
                    group = InetAddress.getByName("224.0.0.3");
                    ms.joinGroup(group);
                    while (bool1) {
                        ms.receive(clPacket);
                        a.put(buff1);
                    }
                    ms.leaveGroup(group);
                    ms.close();
                } catch (Exception ex) {
                    if (ms != null) {
                        ms.close();
                        audio.flush();
                    }
                }
                audio.flush();
                clPacket = null;
                if (ms != null) ms.close();
                ms = null;
                group = null;
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                audio.play();
                byte[] buff1 = null;
                byte i = -128;
                //byte[] d = null;
                while (bool1) {
                    try {
                        buff1 = a.take();
                        //d = Arrays.copyOf(buff1, 3840);
                        //Log.i("buff",Arrays.toString(Arrays.copyOfRange(buff1,buff1.length-11,buff1.length-1)));
                        //ByteBuffer.wrap(buff1).asFloatBuffer().array();
                        float[] out=null;
                        out = conB2F(buff1);
                        //if (i == buff1[3840]) {
//                            float[] floatData = byte2Float(buff1);
                            audio.write(out, 0, out.length - 1,AudioTrack.WRITE_NON_BLOCKING);
                            //a.clear();
                            //Log.i("buff1",new Byte(buff1[buff1.length-1]).toString());
                        //} else i = buff1[3840];
                        Log.i("buff",Arrays.toString(Arrays.copyOfRange(out,out.length-11,out.length-1)));

                    } catch (Exception e) {
                        //Toast.makeText(getApplicationContext(), "error in take()", Toast.LENGTH_LONG).show();
                        Log.i("buff",Log.getStackTraceString(e));
                    }
                    //Log.i("buff1", new Byte(buff1[buff1.length - 1]).toString() + " " + i);
                    i++;
                }
            }
        }).start();
    }

    public void btnClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        a.clear();
                        Thread.sleep(50);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), " Queue : error in thread sleep spam", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).start();
        Toast.makeText(getApplicationContext(), "Queue Cleared", Toast.LENGTH_SHORT).show();
    }

    public void init1() {
        wl = ((PowerManager) getSystemService(
                Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "srdg:hgvjb");
        wl.acquire();
    }

    public void onDestroy() {
        super.onDestroy();
    }


    //Testing crashylytics; this method crashes o purpose
//    public void crasher(View v){
//        throw new RuntimeException("crashed lol");
//    }

    public float[] conB2F(byte[] by){
        float[] fl = new float[by.length/4];
        int fllen = 0;
        ByteBuffer data = ByteBuffer.wrap(by).order(ByteOrder.LITTLE_ENDIAN);
        while (fllen < fl.length) {
            fl[fllen] = data.getFloat();
            fllen++;
        }
        return  fl;
    }

    public void exit(View view) {
        bool1 = false;
        wl.release();
        audio.release();
        audio = null;
        wl = null;
        a = null;
        System.exit(0);
    }

    public void multicaster(View view) {
        Button btn = findViewById(R.id.Multiple);
        boolUnicast = !boolUnicast;

        if (!boolUnicast) {
            btn.setText("Multiple");
        } else {
            btn.setText("Single");
        }
    }

}
