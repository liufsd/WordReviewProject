
package com.coleman.kingword.wordlist;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app2.DownloadManager;
import android.app2.DownloadManager.Request;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coleman.http.json.annotation.RequestObject;
import com.coleman.http.json.bean.RFile;
import com.coleman.http.json.bean.VersionCheckReq;
import com.coleman.http.json.bean.WordlistReq;
import com.coleman.http.json.bean.WordlistResp;
import com.coleman.http.json.bussiness.WorkManager;
import com.coleman.http.json.connection.SLRequest;
import com.coleman.http.json.connection.SLResponse;
import com.coleman.kingword.R;
import com.coleman.util.Config;
import com.coleman.util.Log;
import com.coleman.util.ToastUtil;

public class RemoteFileExplorer extends Activity implements Observer {
    private static final String TAG = RemoteFileExplorer.class.getName();

    private static final String ROOT_PATH = "/kingword/wordlist";

    private ListView lv;

    private RFile root;

    private TextView pathView;

    private ProgressBar pb;

    private RFileAdapter rAdapter = null;

    private Comparator<RFile> comparator = new Comparator<RFile>() {
        @Override
        public int compare(RFile object1, RFile object2) {
            String key1 = object1.getPath();
            String key2 = object2.getPath();
            return key1.compareTo(key2);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_file_list);
        lv = (ListView) findViewById(R.id.listView1);
        pathView = (TextView) findViewById(R.id.textView1);
        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "press button...");
                onBackPressed();
            }
        });
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        getWordlist(this);
    }

    public void getWordlist(Observer observer) {
        try {
            WordlistReq req = new WordlistReq();
            req.setType(2);
            SLRequest<WordlistReq> slReq = new SLRequest<WordlistReq>(req);
            WorkManager.getInstance().getWordlist(observer, slReq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (rAdapter == null || ROOT_PATH.equals(rAdapter.subRoot.getPath())) {
            super.onBackPressed();
        } else {
            Log.d(TAG, "root.getPath(): " + rAdapter.subRoot.getPath());
            rAdapter.subRoot = getParentFile(root, rAdapter.subRoot);
            listFiles(rAdapter.list, rAdapter.subRoot);
            rAdapter.notifyDataSetChanged();
        }
    }

    private RFile getParentFile(RFile root, RFile subRoot) {
        if (root != subRoot) {
            RFile fs[] = root.getChirldren();
            boolean find = false;
            for (RFile rFile : fs) {
                if (rFile.getIsFolder()) {
                    if (subRoot == rFile) {
                        find = true;
                        break;
                    }
                }
            }
            if (!find) {
                for (RFile rFile : fs) {
                    if (rFile.getIsFolder()) {
                        root = getParentFile(rFile, subRoot);
                    }
                }
            }
        }
        return root;
    }

    private void listFiles(List<RFile> list, RFile dir) {
        list.clear();
        if (dir.getIsFolder()) {
            RFile fs[] = dir.getChirldren();
            if (fs != null) {
                Log.d(TAG, "fs.length: " + fs.length);
                for (int i = 0; i < fs.length; i++) {
                    list.add(fs[i]);
                }
                Collections.sort(list, comparator);
            }
        }
        showFilePath(dir);
    }

    private void showFilePath(RFile dir) {
        String path = String.format(getString(R.string.remote_path), dir.getPath());
        Log.d(TAG, "path: " + path);
        pathView.setText(path.replace(ROOT_PATH, ""));
    }

    private class RFileAdapter extends BaseAdapter implements OnItemClickListener {
        private LayoutInflater inflater;

        private RFile subRoot = root;

        private ArrayList<RFile> list = new ArrayList<RFile>();

        public RFileAdapter(Context context, RFile[] rFiles) {
            inflater = LayoutInflater.from(context);
            list.clear();
            for (RFile rFile : rFiles) {
                list.add(rFile);
            }
            Collections.sort(list, comparator);
            showFilePath(subRoot);
        }

        public int getCount() {
            return list.size();
        }

        public RFile getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.file_item, null);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.textView1);
            String fileName = list.get(position).getName();
            textView.setText(fileName);
            convertView.setTag(fileName);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.imageView1);
            if (!TextUtils.isEmpty(fileName)) {
                if (list.get(position).getIsFolder()) {
                    RFile fs[] = list.get(position).getChirldren();
                    if (fs != null && fs.length > 0) {
                        imgView.setImageResource(R.drawable.folder);
                    } else {
                        imgView.setImageResource(R.drawable.folder_emp);
                    }
                } else if (fileName.toLowerCase().endsWith(".txt")) {
                    imgView.setImageResource(R.drawable.txt_file);
                } else {
                    imgView.setImageResource(R.drawable.unknow_file);
                }
            } else {
                imgView.setImageResource(R.drawable.unknow_file);
            }
            return convertView;
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String fn = String.valueOf(view.getTag());
            RFile child = getItem(position);
            if (child.getIsFolder()) {
                listFiles(list, child);
                notifyDataSetChanged();
                subRoot = child;
            } else {
                if (fn != null && fn.toLowerCase().indexOf(".txt") != -1) {
                    showDownloadTip(getItem(position));
                } else {
                    Toast.makeText(RemoteFileExplorer.this, getString(R.string.file_not_txt),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void showDownloadTip(final RFile item) {
            new AlertDialog.Builder(RemoteFileExplorer.this)
                    .setTitle(R.string.title_download_wordlist)
                    .setMessage(R.string.msg_download_wordlist)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doDownload(item);
                        }
                    }).setNegativeButton(R.string.cancel, null).show();
        }

        private void doDownload(RFile item) {
            ToastUtil.show(String.format(getString(R.string.down_wordlist_begin), item.getName()),
                    Toast.LENGTH_LONG);
            RequestObject reqObj = VersionCheckReq.class.getAnnotation(RequestObject.class);
            String filePath = item.getPath();
            filePath = URLEncoder.encode(filePath);
            String strUrl = (Config.isTestServer ? reqObj.url() : reqObj.hwUrl())
                    + "/ColemanServer/downloadFile?file=" + filePath;
            Uri remoteUri = Uri.parse(strUrl);
            Request request = new Request(remoteUri);

            File dir = Environment.getExternalStorageDirectory();
            File file = new File(dir.getAbsolutePath() + item.getPath());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // directory must be external storage directory or exception will be
            // threw file path can be specified
            String itemPath = item.getPath().startsWith("/") ? item.getPath().substring(1) : item
                    .getPath();
            request.setDestinationFromBase(dir, itemPath);
            DownloadManager mDownloadManager = new DownloadManager(getContentResolver(),
                    getPackageName());
            mDownloadManager.enqueue(request);
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (!(observable instanceof SLResponse<?>)) {
            return;
        }
        if (((SLResponse<?>) observable).getResponse() instanceof WordlistResp) {
            final WordlistResp bean = (WordlistResp) ((SLResponse<?>) observable).getResponse();
            if (data == null) {
                int rc = bean.getResultCode();
                if (rc == 0) {
                    root = bean.getRfile();
                    rAdapter = new RFileAdapter(this, root.getChirldren());
                    lv.setAdapter(rAdapter);
                    lv.setOnItemClickListener(rAdapter);
                    rAdapter.notifyDataSetChanged();
                    pb.setVisibility(View.GONE);
                } else {
                    Log.i(TAG, "===coleman-debug-bean.getDescription():" + bean.getDescription());
                }

            } else {
                Log.i(TAG, "===coleman-debug-bean.getDescription():" + bean.getDescription());
            }
        }
    }
}
