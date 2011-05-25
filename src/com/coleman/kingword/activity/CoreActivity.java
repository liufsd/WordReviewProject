
package com.coleman.kingword.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.wordlist.SubWordList;
import com.coleman.kingword.wordlist.SubWordList.SubInfo;
import com.coleman.kingword.wordlist.WordItem;
import com.coleman.util.FileAccessor;

public class CoreActivity extends Activity implements OnItemClickListener, OnClickListener {
    private static final String TAG = CoreActivity.class.getName();

    private ProgressBar progressBar;

    private TextView textView;

    private ListView listView;

    private ArrayList<DictData> list = new ArrayList<DictData>();

    private ParaphraseAdapter adapter;

    private SubWordList wordlist;

    private RelativeLayout container;

    private WordItem nextWordItem;

    /**
     * upgrade and degrade are not support by user anymore.
     */
    private Button viewmore, viewraw, upgrade, degrade, addnew, ignore;

    private TextView continueView;

    private int continueHitCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_list);
        SubInfo info = getIntent().getParcelableExtra("subinfo");
        Log.d(TAG, "info:" + info);
        wordlist = new SubWordList(info);
        initView();
        new ExpensiveTask(ExpensiveTask.INIT_QUERY).execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (!wordlist.allComplete()) {
            File file = new File(getFilesDir(), "tmp");
            try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (list.size() == 1) {
            nextWordItem.setPassView();
            nextWordItem.studyPlus(this);
            new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
        } else if (list.get(position).equals(nextWordItem.getDictData(this))) {
            if (list.size() == 2) {
                nextWordItem.setPassAlternative(true);
            } else if (list.size() == 4) {
                nextWordItem.setPassMultiple(true);
            }
            continueHitCount++;
            if (continueHitCount >= 3) {
                continueView.setVisibility(View.VISIBLE);
                continueView.setText(String.format(getString(R.string.continue_hit_count),
                        continueHitCount));
            }
            nextWordItem.studyPlus(this);
            new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
        } else {
            if (list.size() == 2) {
                nextWordItem.setPassAlternative(false);
            } else if (list.size() == 4) {
                nextWordItem.setPassMultiple(false);
            }
            continueHitCount = 0;
            continueView.setVisibility(View.INVISIBLE);
            nextWordItem.errorPlus(this);
            Toast.makeText(this, getString(R.string.miss), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewmore:
                new ExpensiveTask(ExpensiveTask.VIEW_MORE).execute();
                viewmore.setVisibility(View.GONE);
                viewraw.setVisibility(View.VISIBLE);
                break;
            case R.id.viewraw:
                new ExpensiveTask(ExpensiveTask.VIEW_RAW).execute();
                viewmore.setVisibility(View.VISIBLE);
                viewraw.setVisibility(View.GONE);
                break;
            case R.id.upgrade:
                new ExpensiveTask(ExpensiveTask.UPGRADE).execute();
                break;
            case R.id.addnew:
                new ExpensiveTask(ExpensiveTask.ADD_NEW).execute();
                break;
            case R.id.degrade:
                new ExpensiveTask(ExpensiveTask.DEGRADE).execute();
                break;
            case R.id.ignore:
                new ExpensiveTask(ExpensiveTask.IGNORE).execute();
                break;
            default:
                break;
        }

    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView1);
        listView = (ListView) findViewById(R.id.listView1);
        container = (RelativeLayout) findViewById(R.id.container);
        viewmore = (Button) findViewById(R.id.viewmore);
        viewraw = (Button) findViewById(R.id.viewraw);
        upgrade = (Button) findViewById(R.id.upgrade);
        degrade = (Button) findViewById(R.id.degrade);
        addnew = (Button) findViewById(R.id.addnew);
        ignore = (Button) findViewById(R.id.ignore);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        continueView = (TextView) findViewById(R.id.continueHitView);

        viewraw.setVisibility(View.GONE);
        upgrade.setVisibility(View.GONE);
        degrade.setVisibility(View.GONE);

        listView.setOnItemClickListener(this);
        listView.setEmptyView(findViewById(R.id.progressBar2));

        addnew.setOnClickListener(this);
        viewmore.setOnClickListener(this);
        viewraw.setOnClickListener(this);
        upgrade.setOnClickListener(this);
        degrade.setOnClickListener(this);
        ignore.setOnClickListener(this);

        adapter = new ParaphraseAdapter();
        listView.setAdapter(adapter);

    }

    /**
     * Setup a new 3D rotation on the container view.
     * 
     * @param position the item that was clicked to show a picture, or -1 to
     *            show the list
     * @param start the start angle at which the rotation must begin
     * @param end the end angle of the rotation
     */
    private void applyRotation(float start, float end) {
        // Find the center of the container
        final float centerX = container.getWidth() / 2.0f;
        final float centerY = container.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, centerX, centerY,
                310.0f, true);
        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView());
        container.startAnimation(rotation);
    }

    private void applySlideIn() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                list.clear();
                list.addAll(_buflist);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                Animation a = AnimationUtils
                        .loadAnimation(CoreActivity.this, R.anim.slide_right_in);
                a.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                listView.startAnimation(a);
            }
        });
        listView.startAnimation(anim);
    }

    private ArrayList<DictData> _buflist = new ArrayList<DictData>();

    private void lookupInDict(WordItem worditem) {
        _buflist.clear();
        _buflist.addAll(wordlist.getDictData(this, worditem));
    }

    public static final byte SUBLIST_REACH_END = 0;

    public static final byte INSPIRIT_COMPLETE_STUDY = 1;

    public static final byte INSPIRIT_CONTINUE_HIT = 2;

    public static final byte INSPIRIT_CONTINUE_NO_ACTION = 3;

    public static final byte INSPIRIT_ACHIEVEMETN_REPORT = 4;

    /**
     * Used to report a subwordlist msg or an inspirit msg.
     */
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SUBLIST_REACH_END:
                    showSubListReachEndDialog();
                    break;
                case INSPIRIT_COMPLETE_STUDY:
                    showCompleteStudyDialog();
                    break;
                case INSPIRIT_CONTINUE_HIT:
                    break;
                case INSPIRIT_CONTINUE_NO_ACTION:
                    break;
                case INSPIRIT_ACHIEVEMETN_REPORT:
                    break;
                default:
                    break;
            }
        }

    };

    private void showSubListReachEndDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.show_sub_list_reach_end)
                .setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    private void showCompleteStudyDialog() {
        String str1 = wordlist.getCorrectPercentage() + "%";
        String str2 = wordlist.computeStudyResult(CoreActivity.this);
        new AlertDialog.Builder(this)
                .setMessage(String.format(getString(R.string.show_complete_study), str1, str2))
                .setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    private class ParaphraseAdapter extends BaseAdapter {
        final LayoutInflater inflater;

        Typeface mFace = Typeface.createFromAsset(getAssets(), "font/seguibk.ttf");

        public ParaphraseAdapter() {
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
            tv.setTypeface(mFace);
            DictData data = list.get(position);
            Log.d(TAG, "data:" + data);
            if (data != null) {
                if (!nextWordItem.isPassView()) {
                    tv.setText(data.getDataAndSymbol());
                } else {
                    tv.setText(data.getDatas());
                }
            }
            return v;
        }

    }

    /**
     * This class listens for the end of the first half of the animation. It
     * then posts a new action that effectively swaps the views when the
     * container is rotated 90 degrees and thus invisible.
     */
    private final class DisplayNextView implements Animation.AnimationListener {

        private DisplayNextView() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            list.clear();
            list.addAll(_buflist);
            textView.setText(nextWordItem.word);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            container.post(new SwapViews());
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private class ExpensiveTask extends AsyncTask<Void, Void, Bundle> {

        public static final byte ADD_NEW = -4;

        public static final byte VIEW_RAW = -3;

        public static final byte VIEW_MORE = -2;

        private static final byte INIT_QUERY = -1;

        private static final byte LOOKUP = 0;

        private static final byte UPGRADE = 1;

        private static final byte DEGRADE = 2;

        private static final byte IGNORE = 3;

        private byte type;

        public ExpensiveTask(byte type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case ADD_NEW:
                    break;
                case VIEW_MORE:
                    break;
                case VIEW_RAW:
                    break;
                case INIT_QUERY:
                    textView.setVisibility(View.INVISIBLE);
                    break;
                case LOOKUP:
                    /**
                     * make sure if double clicked, ui operation will not run on
                     * background thread.
                     */
                    listView.setEnabled(false);
                    break;
                case UPGRADE: {
                    break;
                }
                case DEGRADE: {
                    break;
                }
                case IGNORE: {
                    break;
                }
                default:
                    break;
            }
        }

        @Override
        protected Bundle doInBackground(Void... params) {
            Bundle bundle = null;
            switch (type) {
                case ADD_NEW: {
                    boolean b = nextWordItem.addNew(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("addnew", b);
                    break;
                }
                case VIEW_MORE:
                    _buflist.clear();
                    _buflist.add(nextWordItem.getDetail(CoreActivity.this));
                    break;
                case VIEW_RAW:
                    lookupInDict(nextWordItem);
                    break;
                case INIT_QUERY:
                    bundle = new Bundle();
                    wordlist.load(CoreActivity.this);
                    if (!wordlist.allComplete()) {
                        nextWordItem = wordlist.getCurrentWord();
                        lookupInDict(nextWordItem);
                        bundle.putBoolean("complete", false);
                    } else {
                        bundle.putBoolean("complete", true);
                    }
                    break;
                case LOOKUP:
                    bundle = new Bundle();
                    if (!wordlist.allComplete()) {
                        nextWordItem = wordlist.getNext();
                        lookupInDict(nextWordItem);
                        bundle.putBoolean("next", true);
                    } else {
                        bundle.putBoolean("next", false);
                    }
                    break;
                case UPGRADE: {
                    boolean b = nextWordItem.upgrade(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("upgrade", b);
                    break;
                }
                case DEGRADE: {
                    boolean b = nextWordItem.degrade(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("upgrade", b);
                    break;
                }
                case IGNORE: {
                    boolean b = nextWordItem.ignore(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("upgrade", b);
                    break;
                }
                default:
                    break;
            }
            return bundle;
        }

        @Override
        protected void onPostExecute(Bundle result) {
            switch (type) {
                case ADD_NEW: {
                    boolean b = result.getBoolean("addnew");
                    Toast.makeText(CoreActivity.this,
                            b ? getString(R.string.add_success) : getString(R.string.add_failed),
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case VIEW_MORE:
                    applySlideIn();
                    break;
                case VIEW_RAW:
                    applySlideIn();
                    break;
                case INIT_QUERY:
                    textView.setVisibility(View.VISIBLE);
                    boolean isCompleteStudy = result.getBoolean("complete");
                    if (!isCompleteStudy) {
                        list.clear();
                        list.addAll(_buflist);
                        textView.setText(nextWordItem.word);
                        adapter.notifyDataSetChanged();
                        progressBar.setProgress(wordlist.getProgress());
                        upgrade.setEnabled(nextWordItem.canUpgrade());
                        degrade.setEnabled(nextWordItem.canDegrade());
                    } else {
                        progressBar.setProgress(100);
                        handler.sendEmptyMessage(SUBLIST_REACH_END);
                    }
                    break;
                case LOOKUP:
                    Log.d(TAG, "progress:" + wordlist.getProgress());
                    boolean hasNext = result.getBoolean("next");
                    if (hasNext) {
                        progressBar.setProgress(wordlist.getProgress());
                        applyRotation(0, 90);
                        upgrade.setEnabled(nextWordItem.canUpgrade());
                        degrade.setEnabled(nextWordItem.canDegrade());
                        viewmore.setVisibility(View.VISIBLE);
                        viewraw.setVisibility(View.GONE);
                    } else {
                        progressBar.setProgress(100);
                        handler.sendEmptyMessage(INSPIRIT_COMPLETE_STUDY);
                    }
                    break;
                case UPGRADE: {
                    boolean b = result.getBoolean("upgrade");
                    Toast.makeText(
                            CoreActivity.this,
                            b ? getString(R.string.upgrade_success)
                                    : getString(R.string.upgrade_failed), Toast.LENGTH_SHORT)
                            .show();
                    upgrade.setEnabled(nextWordItem.canUpgrade());
                    degrade.setEnabled(nextWordItem.canDegrade());
                    break;
                }
                case DEGRADE: {
                    boolean b = result.getBoolean("upgrade");
                    Toast.makeText(
                            CoreActivity.this,
                            b ? getString(R.string.degrade_success)
                                    : getString(R.string.degrade_failed), Toast.LENGTH_SHORT)
                            .show();
                    upgrade.setEnabled(nextWordItem.canUpgrade());
                    degrade.setEnabled(nextWordItem.canDegrade());
                    break;
                }
                case IGNORE: {
                    boolean b = result.getBoolean("upgrade");
                    Toast.makeText(
                            CoreActivity.this,
                            b ? getString(R.string.ignore_success)
                                    : getString(R.string.ignore_failed), Toast.LENGTH_SHORT).show();

                    new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
                    break;
                }
                default:
                    break;
            }
        }
    }

    /**
     * This class is responsible for swapping the views and start the second
     * half of the animation.
     */
    private final class SwapViews implements Runnable {

        public SwapViews() {
        }

        public void run() {
            final float centerX = container.getWidth() / 2.0f;
            final float centerY = container.getHeight() / 2.0f;
            Rotate3dAnimation rotation;

            rotation = new Rotate3dAnimation(270, 360, centerX, centerY, 310.0f, false);

            rotation.setDuration(500);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());
            rotation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    listView.setEnabled(true);
                }
            });
            container.startAnimation(rotation);
        }
    }

    private final class Rotate3dAnimation extends Animation {
        private final float mFromDegrees;

        private final float mToDegrees;

        private final float mCenterX;

        private final float mCenterY;

        private final float mDepthZ;

        private final boolean mReverse;

        private Camera mCamera;

        /**
         * Creates a new 3D rotation on the Y axis. The rotation is defined by
         * its start angle and its end angle. Both angles are in degrees. The
         * rotation is performed around a center point on the 2D space, definied
         * by a pair of X and Y coordinates, called centerX and centerY. When
         * the animation starts, a translation on the Z axis (depth) is
         * performed. The length of the translation can be specified, as well as
         * whether the translation should be reversed in time.
         * 
         * @param fromDegrees the start angle of the 3D rotation
         * @param toDegrees the end angle of the 3D rotation
         * @param centerX the X center of the 3D rotation
         * @param centerY the Y center of the 3D rotation
         * @param reverse true if the translation should be reversed, false
         *            otherwise
         */
        public Rotate3dAnimation(float fromDegrees, float toDegrees, float centerX, float centerY,
                float depthZ, boolean reverse) {
            mFromDegrees = fromDegrees;
            mToDegrees = toDegrees;
            mCenterX = centerX;
            mCenterY = centerY;
            mDepthZ = depthZ;
            mReverse = reverse;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            mCamera = new Camera();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final float fromDegrees = mFromDegrees;
            float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

            final float centerX = mCenterX;
            final float centerY = mCenterY;
            final Camera camera = mCamera;

            final Matrix matrix = t.getMatrix();

            camera.save();
            if (mReverse) {
                camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
            } else {
                camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
            }
            camera.rotateY(degrees);
            camera.getMatrix(matrix);
            camera.restore();

            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
        }
    }
}
