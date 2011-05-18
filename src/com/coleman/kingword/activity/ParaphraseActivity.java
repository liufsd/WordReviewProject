
package com.coleman.kingword.activity;

import java.util.ArrayList;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.wordlist.WordList;
import com.coleman.kingword.wordlist.WordList.InternalWordList;
import com.coleman.kingword.wordlist.WordListManager;

import android.app.Activity;
import android.content.Context;
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

public class ParaphraseActivity extends Activity implements OnItemClickListener {
    private static final String TAG = ParaphraseActivity.class.getName();

    TextView textView;

    ListView listView;

    private ArrayList<DictData> list = new ArrayList<DictData>();

    ParaphraseAdapter adapter;

    private WordList wordlist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paraphrase_list);
        initView();
    }

    private void initView() {
        wordlist = WordListManager.getInstance()
                .getWordList(InternalWordList.POSTGRADUATE_WORDLIST);
        textView = (TextView) findViewById(R.id.textView1);
        listView = (ListView) findViewById(R.id.listView1);

        String word = wordlist.getWord();
        textView.setText(word);
        lookupInDict(word);
        adapter = new ParaphraseAdapter(list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    private void lookupInDict(String word) {
        DictData dd = DictManager.getInstance().viewWord(this, word);
        list.add(dd);
        if(adapter!=null){
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
                v = inflater.inflate(R.layout.paraphrase_item, null);
            } else {
                v = convertView;
            }
            TextView tv = (TextView) v.findViewById(R.id.textView1);
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
