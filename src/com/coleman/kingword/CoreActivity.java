
package com.coleman.kingword;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coleman.kingword.dict.DictLoadService;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.ebbinghaus.receiver.KingWordReceiver;
import com.coleman.kingword.inspirit.countdown.CountdownManager;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;
import com.coleman.kingword.wordlist.WordAccessor;
import com.coleman.kingword.wordlist.SubWordListAccessor;
import com.coleman.kingword.wordlist.WordlistTabActivity;
import com.coleman.kingword.wordlist.model.SubWordList;
import com.coleman.log.Log;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

public class CoreActivity extends Activity implements OnItemClickListener, OnClickListener,
        OnInitListener, OnLongClickListener {
    private static final String TAG = CoreActivity.class.getName();

    private static Log Log = Config.getLog();

    private static final int SET_COLOR = 0;

    private ProgressBar progressBarDay, progressBarNight;

    private TextView textView;

    private ListView listView;

    private ArrayList<DictData> list = new ArrayList<DictData>();

    private ParaphraseAdapter adapter;

    private SubWordListAccessor sublistAccessor;

    private LinearLayout container;

    private WordAccessor nextWordItem;

    /**
     * upgrade and degrade are not support by user anymore.
     */
    private Button viewWord, addOrRemove, ignoreOrNot;

    private TextView continueView, loopView;

    private Button countBtn;

    private int continueHitCount;

    private byte sliceListType;

    private Anim anim;

    private Button btnSpeak;

    private boolean autoSpeak;

    private TextToSpeech tts;

    public static enum Anim {
        ANIM_3D(0), ANIM_FADE(1), ANIM_SLIDE(2);
        private final int type;

        private Anim(int animType) {
            this.type = animType;
        }

        public int getType() {
            return type;
        }

        public static Anim getAnim(int animType) {
            switch (animType) {
                case 0:
                    return ANIM_3D;
                case 1:
                    return ANIM_FADE;
                case 2:
                    return ANIM_SLIDE;
                default:
                    return ANIM_3D;
            }
        }
    }

    private CountdownManager countdownManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_list);
        anim = Anim
                .getAnim(AppSettings.getInt(AppSettings.ANIM_TYPE_KEY, Anim.ANIM_FADE.getType()));
        initView();

        Intent intent = getIntent();
        sliceListType = intent.getByteExtra("type", SubWordListAccessor.NULL_TYPE);
        switch (sliceListType) {
            case SubWordListAccessor.SUB_WORD_LIST:
                SubWordList info = getIntent().getParcelableExtra("subinfo");
                Log.d(TAG, "info:" + info);
                sublistAccessor = new SubWordListAccessor(info);
                new ExpensiveTask(ExpensiveTask.INIT_SUB_WORD_LIST).execute();
                break;
            case SubWordListAccessor.NEW_WORD_BOOK_LIST:
                sublistAccessor = new SubWordListAccessor(sliceListType);
                new ExpensiveTask(ExpensiveTask.INIT_NEW_WORD_BOOK_LIST).execute();
                break;
            case SubWordListAccessor.SCAN_LIST:
                sublistAccessor = new SubWordListAccessor(sliceListType);
                new ExpensiveTask(ExpensiveTask.INIT_SCAN_LIST).execute();
                break;
            case SubWordListAccessor.REVIEW_LIST:
                AppSettings.saveInt(AppSettings.STARTED_TOTAL_TIMES_KEY,
                        AppSettings.getInt(AppSettings.STARTED_TOTAL_TIMES_KEY, 1) + 1);
                DictManager.getInstance().initLibrary(this);
                startService(new Intent(this, DictLoadService.class));
                sublistAccessor = new SubWordListAccessor(sliceListType);
                new ExpensiveTask(ExpensiveTask.INIT_REVIEW_LIST).execute();
                break;
            case SubWordListAccessor.RECOVERY_LIST:
                DictManager.getInstance().initLibrary(this);
                startService(new Intent(this, DictLoadService.class));
                new ExpensiveTask(ExpensiveTask.INIT_RECOVERY_LIST).execute();
                break;
            default:
                finish();
                break;
        }

        tts = new TextToSpeech(this, this);
    }

    @Override
    protected void onPause() {
        if (countdownManager != null) {
            countdownManager.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (countdownManager != null) {
            countdownManager.start();
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_COLOR) {
            if (resultCode == RESULT_OK) {
                setReadMode();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.core_option, menu);
        if (countBtn.getVisibility() == View.VISIBLE) {
            menu.findItem(R.id.menu_countdown).setTitle(R.string.countdown_conceal);
        } else {
            menu.findItem(R.id.menu_countdown).setTitle(R.string.countdown_show);
        }
        if (sliceListType == SubWordListAccessor.REVIEW_LIST) {
            menu.removeItem(R.id.menu_review);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_anim:
                showAnimSelect();
                break;
            case R.id.menu_review:
                broadcastReview();
                break;
            case R.id.menu_countdown:
                if (countBtn.getVisibility() == View.VISIBLE) {
                    slideRightOut(countBtn);
                } else {
                    slideRightIn(countBtn);
                }
                break;
            case R.id.menu_color_set:
                startActivityForResult(new Intent(this, ColorSetActivityAsDialog.class), SET_COLOR);
                break;
            default:
                break;
        }
        return true;
    }

    private void showAnimSelect() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        anim = Anim.ANIM_3D;
                        break;
                    case 1:
                        anim = Anim.ANIM_FADE;
                        break;
                    case 2:
                        anim = Anim.ANIM_SLIDE;
                        break;
                    default:// ignore
                        break;
                }
                AppSettings.saveInt(AppSettings.ANIM_TYPE_KEY, anim.getType());
            }
        };
        new AlertDialog.Builder(this).setItems(R.array.anim_select, listener).show();
    }

    /**
     * try to review.
     */
    private void broadcastReview() {
        Intent intent = new Intent(this, KingWordReceiver.class);
        intent.putExtra("type", SubWordListAccessor.REVIEW_LIST);
        sendBroadcast(intent);
    }

    private int selectMode;// 0 day 1 night 2 custom

    private int textColor, bgColor, selectColor;

    private void setReadMode() {
        selectMode = AppSettings.getInt(AppSettings.SELECT_COLOR_MODE_KEY, 0);
        textColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][0], Color.BLACK);
        bgColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][1], Color.WHITE);
        selectColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][2], Color.GRAY);

        findViewById(R.id.container).setBackgroundColor(bgColor);
        textView.setTextColor(textColor);

        countBtn.setTextColor(textColor);

        viewWord.setTextColor(textColor);
        addOrRemove.setTextColor(textColor);
        ignoreOrNot.setTextColor(textColor);

        continueView.setTextColor(textColor);
        loopView.setTextColor(textColor);

        if (selectMode == 1) {
            viewWord.setBackgroundResource(R.drawable.btn_bg_night);
            addOrRemove.setBackgroundResource(R.drawable.btn_bg_night);
            ignoreOrNot.setBackgroundResource(R.drawable.btn_bg_night);

            findViewById(R.id.linearLayout2).setBackgroundResource(R.drawable.bottom_bar_night);

            progressBarDay.setVisibility(View.GONE);
            progressBarNight.setVisibility(View.VISIBLE);

            countBtn.setBackgroundResource(R.drawable.countdown_night);

        } else {
            viewWord.setBackgroundResource(android.R.drawable.btn_default);
            addOrRemove.setBackgroundResource(android.R.drawable.btn_default);
            ignoreOrNot.setBackgroundResource(android.R.drawable.btn_default);

            findViewById(R.id.linearLayout2).setBackgroundResource(android.R.drawable.bottom_bar);

            progressBarDay.setVisibility(View.VISIBLE);
            progressBarNight.setVisibility(View.GONE);

            countBtn.setBackgroundResource(R.drawable.countdown_day);
        }
        // make the list view update too
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // backup this time study progress
        writeCacheObject();

        // shutdown the text-to-speech
        tts.shutdown();
        // ///////////////////////////////////////////
        // WordInfoHelper.backupWordInfoDB(this, false);
        // ///////////////////////////////////////////
        super.onDestroy();
    }

    private void readCacheObject() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream("/sdcard/kingword/obj.save"));
            sublistAccessor = (SubWordListAccessor) ois.readObject();
            countdownManager = (CountdownManager) ois.readObject();
            countdownManager.setHandler(handler);
            Log.d(TAG, "------------------------load cache");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
            } catch (Exception e2) {
            }
        }
    }

    private boolean needWriteCache = true;

    private void writeCacheObject() {
        if (needWriteCache
                && (sliceListType == SubWordListAccessor.SUB_WORD_LIST || sliceListType == SubWordListAccessor.RECOVERY_LIST)) {
            new Thread() {
                public void run() {
                    ObjectOutputStream oos = null;
                    try {
                        oos = new ObjectOutputStream(new FileOutputStream(
                                "/sdcard/kingword/obj.save"));
                        oos.writeObject(sublistAccessor);
                        oos.writeObject(countdownManager);
                        AppSettings.saveBoolean(AppSettings.SAVE_CACHE_KEY, true);
                        Log.d(TAG, "------------------------save cache");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            oos.close();
                        } catch (Exception e2) {
                        }
                    }

                };
            }.start();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (list.size() == 1) {
            nextWordItem.setPass(true);
            nextWordItem.studyOrReview(this);
            updateLoopCount();
            new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
        } else if (list.get(position).equals(nextWordItem.getDictData(this))) {
            if (!(nextWordItem.getCurrentStatus() instanceof InitState)) {
                nextWordItem.setPass(true);
            }
            continueHitCount++;
            if (continueHitCount >= 3) {
                continueView.setVisibility(View.VISIBLE);
                continueView.setText(String.format(getString(R.string.continue_hit_count),
                        continueHitCount));
            }
            updateLoopCount();
            nextWordItem.studyOrReview(this);
            new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
        } else {
            nextWordItem.setPass(false);
            continueHitCount = 0;
            continueView.setVisibility(View.INVISIBLE);
            nextWordItem.errorPlus(this);
            Toast.makeText(this, getString(R.string.miss), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLoopCount() {
        if (sublistAccessor.getListType() != SubWordListAccessor.SUB_WORD_LIST) {
            return;
        }
        // loop display control
        if (!sublistAccessor.allComplete()) {
            sublistAccessor.loopCount++;
            if (sublistAccessor.loopCount == sublistAccessor.getCount()
                    - sublistAccessor.getIgnoreCount()) {
                sublistAccessor.loopIndex++;
                Toast.makeText(
                        this,
                        String.format(getString(R.string.enter_next_loop),
                                sublistAccessor.getLoopIndex() + 1), Toast.LENGTH_SHORT).show();
                sublistAccessor.loopCount = 0;
            }
        }
        Log.d(TAG, "wordlist.getCount():" + sublistAccessor.getCount()
                + "   wordlist.getIgnoreCount()" + sublistAccessor.getIgnoreCount());
        loopView.setText(String.format(getString(R.string.loop_info),
                sublistAccessor.getLoopIndex() + 1, sublistAccessor.getLoopCount() + 1,
                sublistAccessor.getCount() - sublistAccessor.getIgnoreCount()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewWord:
                if (viewWord.getText().equals(getString(R.string.view_more))) {
                    new ExpensiveTask(ExpensiveTask.VIEW_MORE).execute();
                    if (nextWordItem.getCurrentStatus() instanceof InitState) {
                        viewWord.setText(R.string.view_raw);
                    } else {
                        viewWord.setText(R.string.view_select);
                    }
                } else {
                    new ExpensiveTask(ExpensiveTask.VIEW_RAW).execute();
                    viewWord.setText(R.string.view_more);
                }
                break;
            case R.id.add_or_remove:
                if (addOrRemove.getText().equals(getString(R.string.add_new))) {
                    new ExpensiveTask(ExpensiveTask.ADD_NEW).execute();
                } else {
                    new ExpensiveTask(ExpensiveTask.REMOVE_FROM_NEW).execute();
                }
                break;
            case R.id.ignore_or_not:
                if (ignoreOrNot.getText().equals(getString(R.string.ignore))) {
                    new ExpensiveTask(ExpensiveTask.IGNORE).execute();
                } else {
                    nextWordItem.removeIgnore(this);
                    ignoreOrNot.setText(R.string.ignore);
                }
                break;
            case R.id.countdown:
                slideRightOut(countBtn);
                break;
            case R.id.btn_speak:
                playVoice(nextWordItem.item.word);
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.btn_speak:
                if (!autoSpeak) {
                    btnSpeak.setBackgroundResource(R.drawable.speak_auto);
                    playVoice(nextWordItem.item.word);
                } else {
                    btnSpeak.setBackgroundResource(R.drawable.speak);
                }
                autoSpeak = !autoSpeak;
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            btnSpeak.setVisibility(View.VISIBLE);
        } else {
            btnSpeak.setVisibility(View.GONE);
        }
    }

    private void playVoice(String word) {
        try {
            tts.speak(word, TextToSpeech.QUEUE_ADD, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        needWriteCache = false;
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (sliceListType == SubWordListAccessor.REVIEW_LIST) {
            startActivity(new Intent(this, WordlistTabActivity.class));
        }
        super.finish();
    }

    private void slideRightIn(final Button view) {
        view.setClickable(false);
        view.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setClickable(true);
            }
        });
        view.startAnimation(anim);
    }

    private void slideRightOut(final View view) {
        view.setClickable(false);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                view.setClickable(true);
            }
        });
        view.startAnimation(anim);
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView1);
        listView = (ListView) findViewById(R.id.listView1);
        container = (LinearLayout) findViewById(R.id.root_container);
        viewWord = (Button) findViewById(R.id.viewWord);
        addOrRemove = (Button) findViewById(R.id.add_or_remove);
        ignoreOrNot = (Button) findViewById(R.id.ignore_or_not);
        countBtn = (Button) findViewById(R.id.countdown);
        btnSpeak = (Button) findViewById(R.id.btn_speak);

        progressBarDay = (ProgressBar) findViewById(R.id.progressBarDay1);
        progressBarNight = (ProgressBar) findViewById(R.id.progressBarNight1);
        continueView = (TextView) findViewById(R.id.continueHitView);
        loopView = (TextView) findViewById(R.id.loopView);

        listView.setOnItemClickListener(this);

        addOrRemove.setOnClickListener(this);
        viewWord.setOnClickListener(this);
        ignoreOrNot.setOnClickListener(this);
        countBtn.setOnClickListener(this);
        btnSpeak.setOnClickListener(this);

        btnSpeak.setOnLongClickListener(this);

        adapter = new ParaphraseAdapter();
        listView.setAdapter(adapter);

        // set the read mode night or day
        setReadMode();

        if (selectMode == 1) {
            listView.setEmptyView(findViewById(R.id.progressBarNight2));
        } else {
            listView.setEmptyView(findViewById(R.id.progressBarDay2));
        }
    }

    /**
     * Setup a new 3D rotation on the container view.
     * 
     * @param position the item that was clicked to show a picture, or -1 to
     *            show the list
     * @param start the start angle at which the rotation must begin
     * @param end the end angle of the rotation
     */
    private void apply3DAnim(float start, float end) {
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

    private void applyFadeAnim() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_fade_out);
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
                if (nextWordItem.isNewWord()) {
                    textView.setText(Html.fromHtml("<b><i>"
                            + nextWordItem.getWord(CoreActivity.this) + "</b></i>"));
                } else {
                    textView.setText(nextWordItem.getWord(CoreActivity.this));
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                Animation a = AnimationUtils.loadAnimation(CoreActivity.this, R.anim.slide_fade_in);
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
                        listView.setEnabled(true);
                    }
                });
                textView.startAnimation(a);
                btnSpeak.startAnimation(a);
                listView.startAnimation(a);
            }
        });
        textView.startAnimation(anim);
        btnSpeak.startAnimation(anim);
        listView.startAnimation(anim);
    }

    private void applySlideAnim() {
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
                if (nextWordItem.isNewWord()) {
                    textView.setText(Html.fromHtml("<b><i>"
                            + nextWordItem.getWord(CoreActivity.this) + "</b></i>"));
                } else {
                    textView.setText(nextWordItem.getWord(CoreActivity.this));
                }
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
                        listView.setEnabled(true);
                    }
                });
                container.startAnimation(a);
            }
        });
        container.startAnimation(anim);
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

    private void lookupInDict(WordAccessor worditem) {
        _buflist.clear();
        _buflist.addAll(sublistAccessor.getDictData(this, worditem));
    }

    public static final byte SUBLIST_REACH_END = 0;

    public static final byte INSPIRIT_COMPLETE_STUDY = 1;

    public static final byte INSPIRIT_CONTINUE_HIT = 2;

    public static final byte INSPIRIT_CONTINUE_NO_ACTION = 3;

    public static final byte INSPIRIT_ACHIEVEMETN_REPORT = 4;

    public static final int UPDATE_REMAINDER_TIME = 5;

    /**
     * Used to report a subwordlist msg or an inspirit msg.
     */
    private final Handler handler = new Handler() {
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
                case UPDATE_REMAINDER_TIME:
                    countBtn.setText(countdownManager
                            .getRemainderTimeShortFormatted(CoreActivity.this));
                    countdownManager.update();
                    // Log.d(TAG, "remainder time: " +
                    // countdownManager.getRemainderTimeFormatted(CoreActivity.this));
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
        String title = "", msg = "";
        switch (sliceListType) {
            case SubWordListAccessor.RECOVERY_LIST:
            case SubWordListAccessor.SUB_WORD_LIST: {
                sublistAccessor.update(this);
                String str1 = sublistAccessor.getCorrectPercentage() + "%";
                String str2 = sublistAccessor.getSubListLevelString(CoreActivity.this);
                title = getString(R.string.view_sub_complete_title);
                msg = String.format(getString(R.string.show_complete_study), str1, str2);
                break;
            }
            case SubWordListAccessor.REVIEW_LIST:
                title = getString(R.string.view_review_complete_title);
                msg = getString(R.string.review_complete);
                break;
            case SubWordListAccessor.NEW_WORD_BOOK_LIST:
                title = getString(R.string.view_newbook_complete_title);
                msg = getString(R.string.study_new_word_book_end);
                break;
            case SubWordListAccessor.SCAN_LIST:
                title = getString(R.string.view_ignore_complete_title);
                msg = getString(R.string.study_ignore_list_end);
                break;
            default:
                break;
        }
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
                .setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
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
            LinearLayout layout = (LinearLayout) v.findViewById(R.id.itemLayout);
            TextView tv = (TextView) v.findViewById(R.id.textView1);
            tv.setTextColor(textColor);
            BGDrawable bg = new BGDrawable();
            layout.setBackgroundDrawable(bg);
            tv.setTypeface(mFace);
            DictData data = list.get(position);
            if (data != null) {
                if (nextWordItem.showSymbol()) {
                    String text = data.getDataAndSymbol();
                    tv.setText(text.indexOf("<font") != -1 ? Html.fromHtml(text) : text);
                } else {
                    String text = data.getDatas();
                    tv.setText(text.indexOf("<font") != -1 ? Html.fromHtml(text) : text);
                }
            }
            return v;
        }
    }

    private class BGDrawable extends StateListDrawable {
        public BGDrawable() {
            addState(new int[] {
                android.R.attr.state_pressed
            }, new ColorDrawable(selectColor));
            addState(new int[] {
                android.R.attr.state_selected
            }, new ColorDrawable(selectColor));
            addState(new int[] {
                -android.R.attr.state_selected
            }, new ColorDrawable(bgColor));
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
            textView.setText(nextWordItem.getWord(CoreActivity.this));
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            container.post(new SwapViews());
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private class ExpensiveTask extends AsyncTask<Void, Void, Bundle> {

        public static final byte INIT_RECOVERY_LIST = -9;

        public static final byte INIT_REVIEW_LIST = -8;

        public static final byte INIT_SCAN_LIST = -7;

        public static final byte INIT_NEW_WORD_BOOK_LIST = -6;

        public static final byte REMOVE_FROM_NEW = -5;

        public static final byte ADD_NEW = -4;

        public static final byte VIEW_RAW = -3;

        public static final byte VIEW_MORE = -2;

        private static final byte INIT_SUB_WORD_LIST = -1;

        private static final byte LOOKUP = 0;

        private static final byte UPGRADE = 1;

        private static final byte DEGRADE = 2;

        private static final byte IGNORE = 3;

        private static final byte UPDATE = 4;

        private byte taskType;

        public ExpensiveTask(byte type) {
            this.taskType = type;
        }

        @Override
        protected void onPreExecute() {
            switch (taskType) {
                case REMOVE_FROM_NEW:
                    break;
                case ADD_NEW:
                    break;
                case VIEW_MORE:
                    break;
                case VIEW_RAW:
                    break;
                case INIT_REVIEW_LIST:
                    if (list != null) {
                        list.clear();
                        adapter.notifyDataSetChanged();
                    }
                    findViewById(R.id.linearLayout1).setVisibility(View.INVISIBLE);
                    findViewById(R.id.linearLayout2).setVisibility(View.GONE);
                    countBtn.setVisibility(View.INVISIBLE);
                    break;
                case INIT_RECOVERY_LIST:
                case INIT_SCAN_LIST:
                case INIT_NEW_WORD_BOOK_LIST:
                case INIT_SUB_WORD_LIST:
                    findViewById(R.id.linearLayout1).setVisibility(View.INVISIBLE);
                    findViewById(R.id.linearLayout2).setVisibility(View.GONE);
                    countBtn.setVisibility(View.INVISIBLE);
                    break;
                case UPDATE:
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
            switch (taskType) {
                case REMOVE_FROM_NEW: {
                    boolean b = nextWordItem.removeFromNew(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("removenew", b);
                    break;
                }
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
                case INIT_REVIEW_LIST:
                    bundle = new Bundle();
                    sublistAccessor.loadReviewWordList(CoreActivity.this);
                    sliceListType = SubWordListAccessor.REVIEW_LIST;
                    if (!sublistAccessor.allComplete()) {
                        nextWordItem = sublistAccessor.getCurrentWord();
                        lookupInDict(nextWordItem);
                        bundle.putBoolean("complete", false);
                    } else {
                        bundle.putBoolean("complete", true);
                    }
                    break;
                case INIT_RECOVERY_LIST:
                    readCacheObject();
                    bundle = new Bundle();
                    if (!sublistAccessor.allComplete()) {
                        nextWordItem = sublistAccessor.getCurrentWord();
                        lookupInDict(nextWordItem);
                        bundle.putBoolean("complete", false);
                    } else {
                        bundle.putBoolean("complete", true);
                    }
                    break;
                case INIT_SCAN_LIST:
                case INIT_NEW_WORD_BOOK_LIST:
                case INIT_SUB_WORD_LIST:
                    bundle = new Bundle();
                    sublistAccessor.loadWordList(CoreActivity.this);
                    if (!sublistAccessor.allComplete()) {
                        nextWordItem = sublistAccessor.getCurrentWord();
                        lookupInDict(nextWordItem);
                        bundle.putBoolean("complete", false);
                    } else {
                        bundle.putBoolean("complete", true);
                    }
                    break;
                case UPDATE:
                    bundle = new Bundle();
                    if (!sublistAccessor.allComplete()) {
                        nextWordItem.clear();
                        lookupInDict(nextWordItem);
                        bundle.putBoolean("next", true);
                    } else {
                        bundle.putBoolean("next", false);
                    }
                    break;
                case LOOKUP:
                    bundle = new Bundle();
                    if (!sublistAccessor.allComplete()) {
                        nextWordItem = sublistAccessor.getNext();
                        lookupInDict(nextWordItem);
                        if (autoSpeak) {
                            playVoice(nextWordItem.item.word);
                        }
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
            switch (taskType) {
                case REMOVE_FROM_NEW: {
                    boolean b = result.getBoolean("removenew");
                    Toast.makeText(
                            CoreActivity.this,
                            b ? getString(R.string.remove_success)
                                    : getString(R.string.remove_failed), Toast.LENGTH_SHORT).show();
                    addOrRemove.setText(R.string.add_new);
                    if (nextWordItem.isNewWord()) {
                        textView.setText(Html.fromHtml("<b><i>"
                                + nextWordItem.getWord(CoreActivity.this) + "</b></i>"));
                    } else {
                        textView.setText(nextWordItem.getWord(CoreActivity.this));
                    }
                    break;
                }
                case ADD_NEW: {
                    boolean b = result.getBoolean("addnew");
                    Toast.makeText(CoreActivity.this,
                            b ? getString(R.string.add_success) : getString(R.string.add_failed),
                            Toast.LENGTH_SHORT).show();
                    addOrRemove.setText(R.string.remove_from_new);
                    if (nextWordItem.isNewWord()) {
                        textView.setText(Html.fromHtml("<b><i>"
                                + nextWordItem.getWord(CoreActivity.this) + "</b></i>"));
                    } else {
                        textView.setText(nextWordItem.getWord(CoreActivity.this));
                    }
                    break;
                }
                case VIEW_MORE:
                    applySlideIn();
                    break;
                case VIEW_RAW:
                    applySlideIn();
                    break;
                case INIT_REVIEW_LIST:
                case INIT_RECOVERY_LIST:
                case INIT_SCAN_LIST:
                case INIT_NEW_WORD_BOOK_LIST:
                case INIT_SUB_WORD_LIST: {
                    boolean isCompleteStudy = result.getBoolean("complete");
                    if (!isCompleteStudy) {
                        // if recovery list, the countdown manager will be
                        // deserialized
                        if (taskType != INIT_RECOVERY_LIST) {
                            countdownManager = new CountdownManager(handler,
                                    sublistAccessor.getCount());
                        }

                        list.clear();
                        list.addAll(_buflist);
                        if (nextWordItem.isNewWord()) {
                            textView.setText(Html.fromHtml("<b><i>"
                                    + nextWordItem.getWord(CoreActivity.this) + "</b></i>"));
                        } else {
                            textView.setText(nextWordItem.getWord(CoreActivity.this));
                        }
                        adapter.notifyDataSetChanged();
                        progressBarDay.setProgress(sublistAccessor.getProgress());
                        progressBarNight.setProgress(sublistAccessor.getProgress());
                        viewWord.setText(R.string.view_more);
                        if (nextWordItem.isAddToNew()) {
                            addOrRemove.setText(R.string.remove_from_new);
                        } else {
                            addOrRemove.setText(R.string.add_new);
                        }
                        if (nextWordItem.isIgnore()) {
                            ignoreOrNot.setText(R.string.remove_from_ignore);
                        } else {
                            ignoreOrNot.setText(R.string.ignore);
                        }
                        if (taskType == INIT_SUB_WORD_LIST || taskType == INIT_RECOVERY_LIST) {
                            loopView.setText(String.format(getString(R.string.loop_info),
                                    sublistAccessor.getLoopIndex() + 1,
                                    sublistAccessor.getLoopCount() + 1, sublistAccessor.getCount()
                                            - sublistAccessor.getIgnoreCount()));
                        }
                        findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
                        findViewById(R.id.linearLayout2).setVisibility(View.VISIBLE);
                        countBtn.setVisibility(View.VISIBLE);
                    } else {
                        /** @TODO prompt that no word to be reviewed. */
                        progressBarDay.setProgress(100);
                        progressBarNight.setProgress(100);
                        handler.sendEmptyMessage(SUBLIST_REACH_END);
                    }
                    break;
                }
                case UPDATE:
                case LOOKUP:
                    Log.d(TAG, "progress:" + sublistAccessor.getProgress());
                    boolean hasNext = result.getBoolean("next");
                    if (hasNext) {
                        progressBarDay.setProgress(sublistAccessor.getProgress());
                        progressBarNight.setProgress(sublistAccessor.getProgress());
                        switch (anim) {
                            case ANIM_3D:
                                apply3DAnim(0, 90);
                                break;
                            case ANIM_FADE:
                                applyFadeAnim();
                                break;
                            case ANIM_SLIDE:
                                applySlideAnim();
                                break;
                        }
                        if (nextWordItem.isAddToNew()) {
                            addOrRemove.setText(R.string.remove_from_new);
                        } else {
                            addOrRemove.setText(R.string.add_new);
                        }
                        if (nextWordItem.isIgnore()) {
                            ignoreOrNot.setText(R.string.remove_from_ignore);
                        } else {
                            ignoreOrNot.setText(R.string.ignore);
                        }
                        viewWord.setText(R.string.view_more);
                    } else {
                        progressBarDay.setProgress(100);
                        progressBarNight.setProgress(100);
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
                    break;
                }
                case DEGRADE: {
                    boolean b = result.getBoolean("upgrade");
                    Toast.makeText(
                            CoreActivity.this,
                            b ? getString(R.string.degrade_success)
                                    : getString(R.string.degrade_failed), Toast.LENGTH_SHORT)
                            .show();
                    break;
                }
                case IGNORE: {
                    boolean b = result.getBoolean("upgrade");
                    Toast.makeText(
                            CoreActivity.this,
                            b ? getString(R.string.ignore_success)
                                    : getString(R.string.ignore_failed), Toast.LENGTH_SHORT).show();
                    if (sublistAccessor.getListType() == SubWordListAccessor.SUB_WORD_LIST) {
                        loopView.setText(String.format(getString(R.string.loop_info),
                                sublistAccessor.getLoopIndex() + 1,
                                sublistAccessor.getLoopCount() + 1, sublistAccessor.getCount()
                                        - sublistAccessor.getIgnoreCount()));
                    }
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
