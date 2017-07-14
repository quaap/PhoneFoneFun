package com.quaap.phonefonefun;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView t1 = (TextView)findViewById(R.id.textView);
        TextView t2 = (TextView)findViewById(R.id.textView2);

        t1.setText(R.string.name_link);
        t2.setText(R.string.about);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
