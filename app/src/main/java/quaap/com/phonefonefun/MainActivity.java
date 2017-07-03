package quaap.com.phonefonefun;

/**
 *   Copyright 2017 Tom Kliethermes
 *
 *   This file is part of PhoneFoneFun.
 *
 *   PhoneFoneFun is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhoneFoneFun is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with AudioMeter.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.app.ActionBar;
import android.app.Activity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements Button.OnClickListener, SensorEventListener {

    private SensorManager mSensorManager;

    private Sensor mSensor;

    private ToneGenerator tonegen;

    private TextView display;

    private Switch switchPhone;

    private TextToVoice ttv;

    private String [] hellos;
    private String [] byes;
    private String [] convos;

    private int helloNum = 0;
    private int byeNum = 0;
    private int convoNum = 0;

    private Timer speakTimer;
    private TimerTask speaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar b = getActionBar();
        if (b!=null) b.hide();

        display = (TextView) findViewById(R.id.display);
        switchPhone = (Switch) findViewById(R.id.switchPhone);

        hellos = getResources().getStringArray(R.array.hello);
        byes = getResources().getStringArray(R.array.bye);
        convos = getResources().getStringArray(R.array.convo);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);


        findViewById(R.id.about_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(about);
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        ttv = new TextToVoice(this);
        tonegen = new ToneGenerator(AudioManager.STREAM_MUSIC, 99);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (mSensor!=null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        speakTimer = new Timer();
    }

    @Override
    protected void onPause() {
        tonegen.release();
        ttv.shutDown();
        if (mSensor!=null) {
            mSensorManager.unregisterListener(this);
        }
        speakTimer.cancel();
        super.onPause();
    }

    private int sixcount = 0;

    @Override
    public void onClick(View v) {
        String key = (String)v.getTag();
        int tone = -1;
        int time = 400;
        if (key!=null) {
            Log.d("Button", key);
            String speak = "";
            if (Character.isDigit(key.codePointAt(0))) {
                tone = Integer.parseInt(key);
                speak = key;

            } else if (key.equals("#")) {
                //tone = ToneGenerator.TONE_DTMF_P;
                sayConvo();

            } else if (key.equals("*")) {
                //tone = ToneGenerator.TONE_DTMF_S;
                sayConvo();

            } else if (key.equals("ring")) {
                tone = ToneGenerator.TONE_CDMA_LOW_L;
                time = 1000;
                key = "";

            } else if (key.equals("hello")) {
                sayHello();

            } else if (key.equals("bye")) {
                sayBye();
                display.setText("");
            }


            if (tone > -1) {
                if (switchPhone.isChecked() && !speak.isEmpty()) {
                    ttv.speak(speak);
                    if (tone==6) {
                        sixcount++;
                        if (sixcount==3) {
                            ttv.speak(getText(R.string.seis).toString());
                            sixcount=0;
                        }
                    } else {
                        sixcount=0;
                    }
                } else {
                    tonegen.startTone(tone, time);
                }

                display.append(key);
                if (display.length()>30) {
                    display.setText(display.getText().subSequence(display.length() - 30, display.length() ));
                }



            }
        }
    }

    private void sayHello() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (hellos) {
            if (!ttv.isSpeaking()) {
                ttv.speak(hellos[helloNum++]);
                if (helloNum >= hellos.length) helloNum = 0;
            }
        }
    }

    private void sayBye() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (byes) {
            if (!ttv.isSpeaking()) {
                ttv.speak(byes[byeNum++]);
                if (byeNum >= byes.length) byeNum = 0;
            }
        }
    }

    private void sayConvo() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (convos) {
            if (!ttv.isSpeaking()) {
                ttv.speak(convos[convoNum++]);
                if (convoNum >= convos.length) {
                    convoNum = 0;
                }
            }
        }
    }

    private void startConvo() {
        if (speaker == null) {
            speaker = new TimerTask() {
                @Override
                public void run() {
                    sayConvo();
                    if (Math.random()>.8) {
                        stopConvo();
                    }
                }
            };
            speakTimer.schedule(speaker, 1000, 4000);
        }
    }

    private void stopConvo() {
        if (speaker != null) {
            speaker.cancel();
            speaker = null;
            speakTimer.purge();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0) {
            startConvo();
        } else {
            stopConvo();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
