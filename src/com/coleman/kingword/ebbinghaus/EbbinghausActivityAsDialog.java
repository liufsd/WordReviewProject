
package com.coleman.kingword.ebbinghaus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.coleman.kingword.CoreActivity;
import com.coleman.kingword.R;

public class EbbinghausActivityAsDialog extends Activity implements OnClickListener {
    private TextView title, msg;

    private Button btn1, btn2;

    private byte type;

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
                i.putExtra(CoreActivity.OPEN_TAB, false);
                startActivity(i);
                finish();
                break;
            case R.id.negative:
                EbbinghausReminder.setNotifactionDelay(this, 10);
                finish();
                break;
            default:
                break;
        }
    }

}
