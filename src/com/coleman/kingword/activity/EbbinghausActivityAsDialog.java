
package com.coleman.kingword.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;

public class EbbinghausActivityAsDialog extends Activity implements OnClickListener {
    private TextView title, msg;

    private Button btn1, btn2;

    private byte type, reviewType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ebbinghaus_asdialog);

        title = (TextView) findViewById(R.id.title);
        msg = (TextView) findViewById(R.id.message);
        btn1 = (Button) findViewById(R.id.positive);
        btn2 = (Button) findViewById(R.id.negative);

        Intent i = getIntent();
        String titleStr = i.getStringExtra("title");
        String msgStr = i.getStringExtra("message");
        String btnStr1 = i.getStringExtra("positive");
        String btnStr2 = i.getStringExtra("negative");
        type = i.getByteExtra("type", Byte.MAX_VALUE);
        reviewType = i.getByteExtra("review_type", Byte.MAX_VALUE);

        title.setText(titleStr);
        msg.setText(msgStr);
        btn1.setText(btnStr1);
        btn2.setText(btnStr2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positive:
                final Intent i = new Intent(this, CoreActivity.class);
                i.putExtra("type", type);
                i.putExtra("review_type", reviewType);
                startActivity(i);
                finish();
                break;
            case R.id.negative:
                EbbinghausReminder.setNotifactionDelay(this, reviewType, 10);
                finish();
                break;
            default:
                break;
        }
    }

}
