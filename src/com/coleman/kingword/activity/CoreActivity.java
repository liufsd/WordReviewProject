
package com.coleman.kingword.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
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
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.wordinfo.WordInfoHelper;
import com.coleman.kingword.wordinfo.WordInfoVO;
import com.coleman.kingword.wordlist.SubWordList;
import com.coleman.kingword.wordlist.WordItem;

public class CoreActivity extends Activity implements OnItemClickListener, OnClickListener {
    private static final String TAG = CoreActivity.class.getName();

    private ProgressBar progressBar;

    private TextView textView;

    private ListView listView;

    private ArrayList<DictData> list = new ArrayList<DictData>();

    private ParaphraseAdapter adapter;

    private long sub_id;

    private SubWordList wordlist;

    private RelativeLayout container;

    private WordItem nextWordItem;

    private WordInfoVO nextWordInfo;

    private Button upgrade, degrade, ignore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_list);
        sub_id = getIntent().getLongExtra(SubWordsList._ID, -1);
        new ExpensiveTask(ExpensiveTask.INIT_QUERY).execute();
        initView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (list.get(position).equals(nextWordItem.getDictData(this))) {
            Log.d(TAG, "choose correct++++++++++++++++++++++++++++++");
            if (list.size() == 1) {
                nextWordItem.setPassView();
            } else if (list.size() == 2) {
                nextWordItem.setPassAlternative(true);
            } else if (list.size() == 4) {
                nextWordItem.setPassMultiple(true);
            }
            new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
        } else {
            if (list.size() == 2) {
                nextWordItem.setPassAlternative(false);
            } else if (list.size() == 4) {
                nextWordItem.setPassMultiple(false);
            }
            new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
            Log.d(TAG, "choose not correct++++++++++++++++++++++++++++++");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                new ExpensiveTask(ExpensiveTask.UPGRADE).execute();
                break;
            case R.id.button2:
                new ExpensiveTask(ExpensiveTask.DEGRADE).execute();
                break;
            case R.id.button3:
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
        upgrade = (Button) findViewById(R.id.button1);
        degrade = (Button) findViewById(R.id.button2);
        ignore = (Button) findViewById(R.id.button3);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        listView.setOnItemClickListener(this);
        upgrade.setOnClickListener(this);
        degrade.setOnClickListener(this);
        ignore.setOnClickListener(this);

        adapter = new ParaphraseAdapter(list);
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

    private void lookupInDict(WordItem worditem) {
        list.clear();
        list.addAll(wordlist.getDictData(this, worditem));
        nextWordInfo = WordInfoHelper.getWordInfo(this, worditem.word);
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
                    break;
                case INSPIRIT_COMPLETE_STUDY:
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
            DictData data = list.get(position);
            if (data != null) {
                tv.setText(data.toString());
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
        private byte type;

        private static final byte INIT_QUERY = -1;

        private static final byte LOOKUP = 0;

        private static final byte UPGRADE = 1;

        private static final byte DEGRADE = 2;

        private static final byte IGNORE = 3;

        public ExpensiveTask(byte type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case INIT_QUERY:
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
                case INIT_QUERY:
                    bundle = new Bundle();
                    wordlist = new SubWordList(CoreActivity.this, sub_id);
                    if (!wordlist.isComplete()) {
                        nextWordItem = wordlist.getCurrentWord();
                        lookupInDict(nextWordItem);
                        bundle.putBoolean("complete", false);
                    } else {
                        bundle.putBoolean("complete", true);
                    }
                    break;
                case LOOKUP:
                    bundle = new Bundle();
                    if (wordlist.hasNext()) {
                        nextWordItem = wordlist.getNext();
                        lookupInDict(nextWordItem);
                        bundle.putBoolean("next", true);
                    } else {
                        bundle.putBoolean("next", false);
                    }
                    break;
                case UPGRADE: {
                    boolean b = WordInfoHelper.upgrade(CoreActivity.this, nextWordItem.word);
                    bundle = new Bundle();
                    bundle.putBoolean("upgrade", b);
                    break;
                }
                case DEGRADE: {
                    boolean b = WordInfoHelper.degrade(CoreActivity.this, nextWordItem.word);
                    bundle = new Bundle();
                    bundle.putBoolean("upgrade", b);
                    break;
                }
                case IGNORE: {
                    boolean b = WordInfoHelper.ignore(CoreActivity.this, nextWordItem.word);
                    wordlist.ignore(CoreActivity.this, nextWordItem);
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
                case INIT_QUERY:
                    boolean isComplete = result.getBoolean("complete");
                    if (!isComplete) {
                        textView.setText(nextWordItem.word);
                        adapter.notifyDataSetChanged();
                        progressBar.setProgress(wordlist.getProgress());
                        upgrade.setEnabled(nextWordInfo.canUpgrade());
                        degrade.setEnabled(nextWordInfo.canDegrade());
                    } else {
                        handler.sendEmptyMessage(SUBLIST_REACH_END);
                    }
                    break;
                case LOOKUP:
                    Log.d(TAG, "progress:" + wordlist.getProgress());
                    boolean hasNext = result.getBoolean("next");
                    if (hasNext) {
                        progressBar.setProgress(wordlist.getProgress());
                        listView.setEnabled(true);
                        applyRotation(0, 90);
                        upgrade.setEnabled(nextWordInfo.canUpgrade());
                        degrade.setEnabled(nextWordInfo.canDegrade());
                    } else {
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
                    upgrade.setEnabled(nextWordInfo.canUpgrade());
                    degrade.setEnabled(nextWordInfo.canDegrade());
                    break;
                }
                case DEGRADE: {
                    boolean b = result.getBoolean("upgrade");
                    Toast.makeText(
                            CoreActivity.this,
                            b ? getString(R.string.degrade_success)
                                    : getString(R.string.degrade_failed), Toast.LENGTH_SHORT)
                            .show();
                    upgrade.setEnabled(nextWordInfo.canUpgrade());
                    degrade.setEnabled(nextWordInfo.canDegrade());
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
