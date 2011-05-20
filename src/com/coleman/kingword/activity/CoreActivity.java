
package com.coleman.kingword.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.wordlist.SubWordList;

public class CoreActivity extends Activity implements OnItemClickListener {
    private static final String TAG = CoreActivity.class.getName();

    TextView textView;

    ListView listView;

    private ArrayList<DictData> list = new ArrayList<DictData>();

    ParaphraseAdapter adapter;

    private SubWordList wordlist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_list);
        int sub_id = getIntent().getIntExtra(SubWordsList._ID, -1);
        wordlist = new SubWordList(this, sub_id);
        initView();
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView1);
        listView = (ListView) findViewById(R.id.listView1);
        adapter = new ParaphraseAdapter(list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        String word = wordlist.getWord();
        textView.setText(word);
        lookupInDict(word);
    }

    private void lookupInDict(String word) {
        DictData dd = DictManager.getInstance().viewWord(this, word);
        list.add(dd);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                default:
                    break;
            }
        }
    };

    private class ParaphraseAdapter extends BaseAdapter {
        final LayoutInflater inflater;

        ArrayList<DictData> list;

        public ParaphraseAdapter(ArrayList<DictData> list) {
            this.list = list;
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = inflater.inflate(R.layout.core_item, null);
            } else {
                v = convertView;
            }
            TextView tv = (TextView) v.findViewById(R.id.textView1);
            tv.setMaxLines(100);
            tv.setMinLines(4);
            DictData data = list.get(position);
            if (data != null) {
                tv.setText(data.toString());
            }
            return v;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        list.clear();
        String word = wordlist.getNext();
        textView.setText(word);
        lookupInDict(word);
    }
}
