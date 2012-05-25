
package com.coleman.kingword.wordlist;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.coleman.kingword.R;
import com.coleman.util.Log;

public class WordlistTabActivity extends TabActivity implements TabHost.OnTabChangeListener {
    private static final String TAG = WordlistTabActivity.class.getName();

    private TabHost mTabHost;

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordlist_tab);

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

    @Override
    public void onTabChanged(String tabId) {

    }
}
