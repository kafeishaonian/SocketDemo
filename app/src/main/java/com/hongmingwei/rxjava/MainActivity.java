package com.hongmingwei.rxjava;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hongmingwei.rxjava.service.SocketActivity;

public class MainActivity extends AppCompatActivity implements Function{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String NAME = "name";
    private TextView button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (TextView) findViewById(R.id.text);
        button.setOnClickListener(mListener);
        findViewById(R.id.stock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, SocketActivity.class));
            }
        });
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.text:
//                    button.setText("aaaa ");
                    initActivity();
                    break;
            }
        }
    };

    /**
     * 模仿activity
     */
    private void initActivity(){
        ObservableManager.getInstance().registerObserver(NAME, this);
    }

    /**
     * 模仿fragment
     */
    private void initFragment(){

    }


    @Override
    public Object function(Object[] objects) {
        String name = "123789";

        return null;
    }
}
