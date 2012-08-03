
package com.coleman.kingword.wordlist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;

import com.coleman.kingword.CoreActivity;
import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.skin.ColorManager;
import com.coleman.log.Log;
import com.coleman.util.Config;

public class WordlistTabActivity extends TabActivity implements TabHost.OnTabChangeListener,
        OnClickListener {
    private static final String TAG = WordlistTabActivity.class.getName();

    private Button btnNew, btnIgnore, btnReview;

    private static Log Log = Config.getLog();

    private TabHost mTabHost;

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordlist_tab);
        btnNew = (Button) findViewById(R.id.button1);
        btnIgnore = (Button) findViewById(R.id.button2);
        btnReview = (Button) findViewById(R.id.button3);

        btnNew.setOnClickListener(this);
        btnIgnore.setOnClickListener(this);
        btnReview.setOnClickListener(this);

        // get current intent
        Intent intent = getIntent();

        // get the TabHost
        mTabHost = getTabHost();
        mTabHost.setOnTabChangedListener(this);

        // setup the tabs
        setupLoadedList(intent);
        setupLoadLocalWordList();
        setupLoadRemoteWordList();

        // set current tab
        setCurrentTab(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setColorMode();
    }

    @Override
    public void onTabChanged(String tabId) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                if (!DictManager.getInstance().isCurLibInitialized()) {
                    showDBInitHint();
                } else if (!WordInfoHelper.hasWordInfo(this, NewListVisitor.TYPE)) {
                    new AlertDialog.Builder(this).setTitle(R.string.system_msg_title)
                            .setMessage(R.string.no_new_word_found)
                            .setPositiveButton(android.R.string.ok, null).show();
                } else {
                    Intent intent = new Intent(this, CoreActivity.class);
                    intent.putExtra("type", NewListVisitor.TYPE);
                    startActivity(intent);
                }
                break;
            case R.id.button2:
                if (!DictManager.getInstance().isCurLibInitialized()) {
                    showDBInitHint();
                } else if (!WordInfoHelper.hasWordInfo(this, IgnoreListVisitor.TYPE)) {
                    new AlertDialog.Builder(this).setTitle(R.string.system_msg_title)
                            .setMessage(R.string.no_ignore_word_found)
                            .setPositiveButton(android.R.string.ok, null).show();
                } else {
                    Intent intent = new Intent(this, CoreActivity.class);
                    intent.putExtra("type", IgnoreListVisitor.TYPE);
                    startActivity(intent);
                }
                break;
            case R.id.button3:
                if (!DictManager.getInstance().isCurLibInitialized()) {
                    showDBInitHint();
                } else if (EbbinghausReminder.needReview(this) <= 0) {
                    new AlertDialog.Builder(this).setTitle(R.string.system_msg_title)
                            .setMessage(R.string.no_review_word_found)
                            .setPositiveButton(android.R.string.ok, null).show();
                } else {
                    Intent intent = new Intent(this, CoreActivity.class);
                    intent.putExtra("type", ReviewListVisitor.TYPE);
                    intent.putExtra(CoreActivity.OPEN_TAB, false);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    private void setColorMode() {
        ColorManager mgr = ColorManager.getInstance();
        if (mgr.getSelectMode() == 1) {
            btnNew.setBackgroundResource(R.drawable.btn_bg_night);
            btnNew.setTextColor(mgr.getTextColor());
            btnIgnore.setBackgroundResource(R.drawable.btn_bg_night);
            btnIgnore.setTextColor(mgr.getTextColor());
            btnReview.setBackgroundResource(R.drawable.btn_bg_night);
            btnReview.setTextColor(mgr.getTextColor());
            findViewById(R.id.linearLayout1).setBackgroundResource(R.drawable.bottom_bar_night);
        } else {
            btnNew.setBackgroundResource(android.R.drawable.btn_default);
            btnNew.setTextColor(mgr.getTextColor());
            btnIgnore.setBackgroundResource(android.R.drawable.btn_default);
            btnIgnore.setTextColor(mgr.getTextColor());
            btnReview.setBackgroundResource(android.R.drawable.btn_default);
            btnReview.setTextColor(mgr.getTextColor());
            findViewById(R.id.linearLayout1).setBackgroundResource(android.R.drawable.bottom_bar);
        }
    }

    private void setupLoadRemoteWordList() {
        Intent intent = new Intent(this, RemoteFileExplorer.class);
        mTabHost.addTab(mTabHost
                .newTabSpec("RemoteFileExplorer")
                .setIndicator(getString(R.string.remote_file_explorer),
                        getResources().getDrawable(R.drawable.remote)).setContent(intent));
    }

    private void setupLoadLocalWordList() {
        Intent intent = new Intent(this, FileExplorer.class);
        mTabHost.addTab(mTabHost
                .newTabSpec("FileExplorer")
                .setIndicator(getString(R.string.file_explorer),
                        getResources().getDrawable(R.drawable.local)).setContent(intent));
    }

    private void setupLoadedList(Intent i) {
        Intent intent = new Intent(this, WordListActivity.class);

        boolean external = i.getBooleanExtra(WordListActivity.EXTERNAL_FILE, false);
        String path = i.getStringExtra(WordListActivity.EXTERNAL_FILE_PATH);
        if (external) {
            intent.putExtra(WordListActivity.EXTERNAL_FILE, external);
            intent.putExtra(WordListActivity.EXTERNAL_FILE_PATH, path);
        }

        mTabHost.addTab(mTabHost
                .newTabSpec("remoteFileExplorer")
                .setIndicator(getString(R.string.wordlist_loaded),
                        getResources().getDrawable(R.drawable.download)).setContent(intent));
    }

    private void setCurrentTab(Intent intent) {
        // Dismiss menu provided by any children activities
        Activity activity = getLocalActivityManager().getActivity(mTabHost.getCurrentTabTag());
        if (activity != null) {
            activity.closeOptionsMenu();
        }
        // Choose the tab based on the inbound intent
        String componentName = intent.getComponent().getClassName();
        Log.i(TAG, "===coleman-debug-componentName: " + componentName);
        if (WordListActivity.class.getName().equals(componentName)
                || getClass().getName().equals(componentName) || componentName == null) {
            mTabHost.setCurrentTab(0);
        } else if (FileExplorer.class.getName().equals(componentName)) {
            mTabHost.setCurrentTab(1);
        } else if (RemoteFileExplorer.class.getName().equals(componentName)) {
            mTabHost.setCurrentTab(2);
        }
    }

    private void showDBInitHint() {
        new AlertDialog.Builder(this).setTitle(R.string.msg_dialog_title)
                .setMessage(R.string.init_db).setPositiveButton(R.string.ok, null).show();
    }
}
