
package com.coleman.kingword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.wordlist.AbsSubVisitor;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;
import com.coleman.kingword.wordlist.IgnoreListVisitor;
import com.coleman.kingword.wordlist.NewListVisitor;
import com.coleman.kingword.wordlist.ReviewListVisitor;
import com.coleman.kingword.wordlist.SubListActivity;
import com.coleman.kingword.wordlist.SubListVisitor;
import com.coleman.kingword.wordlist.WordVisitor;
import com.coleman.kingword.wordlist.WordlistTabActivity;
import com.coleman.kingword.wordlist.model.SubWordList;
import com.coleman.log.Log;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;
import com.coleman.util.DictDataProcessingCenter;
import com.coleman.util.ToastUtil;

public class CoreActivity extends Activity implements OnItemClickListener, OnClickListener,
        OnLongClickListener {
    public static final String OPEN_TAB = "open_tab";

    private static final String TAG = CoreActivity.class.getName();

    private static Log Log = Config.getLog();

    private static final int SET_COLOR = 0;

    private ProgressBar progressBarDay, progressBarNight;

    private TextView textView;

    private ListView listView;

    private ArrayList<DictData> list = new ArrayList<DictData>();

    private ParaphraseAdapter adapter;

    private AbsSubVisitor sublistVisitor;

    private LinearLayout container;

    private WordVisitor wordVisitor;

    /**
     * upgrade and degrade are not support by user anymore.
     */
    private Button viewWord, addOrRemove, ignoreOrNot;

    private TextView continueView, loopView;

    private Button countBtn;

    private int continueHitCount;

    private Anim anim;

    private Button btnSpeak;

    private boolean autoSpeak;

    private PlayControl playControl;

    /**
     * 用来标识关闭当前Activity是否会打开TabListActivity
     */
    private boolean openTabList = false;

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

    public CountdownManager countdownManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_list);
        anim = Anim
                .getAnim(AppSettings.getInt(AppSettings.ANIM_TYPE_KEY, Anim.ANIM_FADE.getType()));
        initView();

        if (!DictManager.getInstance().isCurLibInitialized()) {
            DictManager.getInstance().initLibrary(this);
            startService(new Intent(this, DictLoadService.class));
        }

        Intent intent = getIntent();
        int sliceListType = intent.getByteExtra("type", AbsSubVisitor.TYPE);
        switch (sliceListType) {
            case SubListVisitor.TYPE:
                SubWordList info = getIntent().getParcelableExtra("subinfo");
                Log.d(TAG, "info:" + info);
                SubListVisitor subVisitor = new SubListVisitor();
                subVisitor.setSubInfo(info);
                sublistVisitor = subVisitor;
                new ExpensiveTask(ExpensiveTask.INIT_SUB_WORD_LIST).execute();
                break;
            case NewListVisitor.TYPE:
                sublistVisitor = new NewListVisitor();
                new ExpensiveTask(ExpensiveTask.INIT_NEW_WORD_BOOK_LIST).execute();
                break;
            case IgnoreListVisitor.TYPE:
                sublistVisitor = new IgnoreListVisitor();
                new ExpensiveTask(ExpensiveTask.INIT_SCAN_LIST).execute();
                break;
            case ReviewListVisitor.TYPE:
                AppSettings.saveInt(AppSettings.STARTED_TOTAL_TIMES_KEY,
                        AppSettings.getInt(AppSettings.STARTED_TOTAL_TIMES_KEY, 1) + 1);
                this.openTabList = intent.getBooleanExtra(OPEN_TAB, true);
                sublistVisitor = new ReviewListVisitor();
                new ExpensiveTask(ExpensiveTask.INIT_REVIEW_LIST).execute();
                break;
            default:
                finish();
                break;
        }
        playControl = new PlayControl();

    }

    private void startPreload() {
        if (sublistVisitor != null) {
            sublistVisitor.preload();
        }
    }

    private void stopPreload() {
        if (sublistVisitor != null) {
            sublistVisitor.stopPreload();
        }
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
        if (!(sublistVisitor instanceof SubListVisitor)) {
            menu.removeItem(R.id.menu_review);
        }
        if (playControl.ongoing) {
            menu.findItem(R.id.menu_play).setTitle(R.string.stop);
        } else {
            menu.findItem(R.id.menu_play).setTitle(R.string.play);
        }
        if (!playControl.usable) {
            menu.removeItem(R.id.menu_play);
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
                if (playControl.ongoing) {
                    playControl.stop();
                    ToastUtil.show(getString(R.string.play_stop));
                }
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
            case R.id.menu_play:
                if (playControl.ongoing) {
                    playControl.stop();
                } else {
                    playControl.play();
                }
                break;
            case R.id.menu_listmode:
                Intent it = new Intent(this, CoreActivity2.class);
                it.putExtra("sub", sublistVisitor);
                startActivity(it);
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
        intent.putExtra("type", ReviewListVisitor.TYPE);
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
    protected void onStop() {
        // playControl.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // backup this time study progress
        // writeCacheObject();
        // shutdown the text-to-speech
        playControl.shutdown();

        // stop preload
        stopPreload();

        // ///////////////////////////////////////////
        // WordInfoHelper.backupWordInfoDB(this, false);
        // ///////////////////////////////////////////
        super.onDestroy();
    }

    private void startSubListActivity() {
        // write result to SubListActivity
        Intent intent = new Intent(this, SubListActivity.class);
        intent.putExtra(TWordList._ID, ((SubListVisitor) sublistVisitor).getWordListID());
        intent.putExtra(SubListActivity.SCREEN_INDEX,
                ((SubListVisitor) sublistVisitor).getSubList().screenIndex);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playControl.ongoing) {
            ToastUtil.show(getString(R.string.stop_first));
            return;
        }
        if (list.size() == 1) {
            wordVisitor.setPass(true);
            wordVisitor.viewPlus(this);
            new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
        } else if (list.get(position).equals(wordVisitor.getDictData(this))) {
            if (!(wordVisitor.getCurrentStatus() instanceof InitState)) {
                wordVisitor.setPass(true);
            }
            continueHitCount++;
            if (continueHitCount >= 3) {
                continueView.setVisibility(View.VISIBLE);
                continueView.setText(String.format(getString(R.string.continue_hit_count),
                        continueHitCount));
            }
            wordVisitor.viewPlus(this);
            new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
        } else {
            wordVisitor.setPass(false);
            continueHitCount = 0;
            continueView.setVisibility(View.INVISIBLE);
            wordVisitor.errorPlus(this);
            new ExpensiveTask(ExpensiveTask.ADD_NEW).execute();
            Toast.makeText(this, getString(R.string.miss), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLoopCount() {
        // loop display control
        loopView.setText(String.format(getString(R.string.loop_info),
                sublistVisitor.getViewMethod(), sublistVisitor.getCurrentIndex(),
                sublistVisitor.getCount()));
    }

    @Override
    public void onClick(View v) {
        if (playControl.ongoing) {
            ToastUtil.show(getString(R.string.stop_first));
            return;
        }
        switch (v.getId()) {
            case R.id.viewWord:
                if (viewWord.getText().equals(getString(R.string.view_more))) {
                    new ExpensiveTask(ExpensiveTask.VIEW_MORE).execute();
                    if (wordVisitor.getCurrentStatus() instanceof InitState) {
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
                    wordVisitor.removeIgnore(this);
                    ignoreOrNot.setText(R.string.ignore);
                }
                break;
            case R.id.countdown:
                slideRightOut(countBtn);
                break;
            case R.id.btn_speak:
                playControl.speak(wordVisitor.item.word);
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onLongClick(View v) {
        if (playControl.ongoing) {
            ToastUtil.show(getString(R.string.stop_first));
            return false;
        }
        switch (v.getId()) {
            case R.id.btn_speak:
                if (!autoSpeak) {
                    btnSpeak.setBackgroundResource(R.drawable.speak_auto);
                    playControl.speak(wordVisitor.item.word);
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
    public void finish() {
        if (sublistVisitor instanceof ReviewListVisitor) {
            if (openTabList) {
                startActivity(new Intent(this, WordlistTabActivity.class));
            }
        } else if (sublistVisitor instanceof SubListVisitor) {
            ((SubListVisitor) sublistVisitor).update(this);
            startSubListActivity();
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {
        finish();
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
                if (wordVisitor.isNewWord()) {
                    textView.setText(Html.fromHtml("<b><i>"
                            + wordVisitor.getWord(CoreActivity.this) + "</b></i>"));
                } else {
                    textView.setText(wordVisitor.getWord(CoreActivity.this));
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
                        if (!sublistVisitor.allComplete()) {
                            if (autoSpeak) {
                                if (playControl.ongoing) {
                                    // 自动播放看成用户点击的同等操作
                                    wordVisitor.setPass(true);
                                    wordVisitor.viewPlus(CoreActivity.this);
                                    playControl.speak(null);
                                }
                            }
                        }
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
                if (wordVisitor.isNewWord()) {
                    textView.setText(Html.fromHtml("<b><i>"
                            + wordVisitor.getWord(CoreActivity.this) + "</b></i>"));
                } else {
                    textView.setText(wordVisitor.getWord(CoreActivity.this));
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
                        if (!sublistVisitor.allComplete()) {
                            if (autoSpeak) {
                                if (playControl.ongoing) {
                                    // 自动播放看成用户点击的同等操作
                                    wordVisitor.setPass(true);
                                    wordVisitor.viewPlus(CoreActivity.this);
                                    playControl.speak(null);
                                }
                            }
                        }
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

    private void lookupInDict(WordVisitor worditem) {
        _buflist.clear();
        _buflist.addAll(sublistVisitor.getDictData(this, worditem));
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
        switch (sublistVisitor.getType()) {
            case SubListVisitor.TYPE: {
                ((SubListVisitor) sublistVisitor).update(this);
                String str1 = ((SubListVisitor) sublistVisitor).getCorrectPercentage() + "%";
                String str2 = ((SubListVisitor) sublistVisitor)
                        .getSubListLevelString(CoreActivity.this);
                title = getString(R.string.view_sub_complete_title);
                msg = String.format(getString(R.string.show_complete_study), str1, str2);
                break;
            }
            case ReviewListVisitor.TYPE:
                title = getString(R.string.view_review_complete_title);
                msg = getString(R.string.review_complete);
                break;
            case NewListVisitor.TYPE:
                title = getString(R.string.view_newbook_complete_title);
                msg = getString(R.string.study_new_word_book_end);
                break;
            case IgnoreListVisitor.TYPE:
                title = getString(R.string.view_ignore_complete_title);
                msg = getString(R.string.study_ignore_list_end);
                break;
            default:
                break;
        }

        if (playControl.ongoing) {
            playControl.stop();
            playControl.speak(getString(R.string.play_complete) + "," + msg);
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
                if (wordVisitor.showSymbol()) {
                    String text = data.getDataAndSymbol();
                    if (list.size() > 1) {
                        // Log.v(TAG, "===coleman-debug-before text:" + text);
                        text = DictDataProcessingCenter.process4MultiSelect(text);
                        // Log.v(TAG, "===coleman-debug-after text:" + text);
                    }
                    tv.setText(text.indexOf("<font") != -1 ? Html.fromHtml(text) : text);
                } else {
                    String text = data.getDatas();
                    if (list.size() > 1) {
                        // Log.v(TAG, "===coleman-debug-before text:" + text);
                        text = DictDataProcessingCenter.process4MultiSelect(text);
                        // Log.v(TAG, "===coleman-debug-after text:" + text);
                    }
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
            if (wordVisitor.isNewWord()) {
                textView.setText(Html.fromHtml("<b><i>" + wordVisitor.getWord(CoreActivity.this)
                        + "</b></i>"));
            } else {
                textView.setText(wordVisitor.getWord(CoreActivity.this));
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            container.post(new SwapViews());
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private class ExpensiveTask extends AsyncTask<Void, Void, Bundle> {

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
                    boolean b = wordVisitor.removeFromNew(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("removenew", b);
                    break;
                }
                case ADD_NEW: {
                    boolean b = wordVisitor.addNew(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("addnew", b);
                    break;
                }
                case VIEW_MORE:
                    _buflist.clear();
                    _buflist.add(wordVisitor.getDetail(CoreActivity.this));
                    break;
                case VIEW_RAW:
                    lookupInDict(wordVisitor);
                    break;
                case INIT_REVIEW_LIST:
                case INIT_SCAN_LIST:
                case INIT_NEW_WORD_BOOK_LIST:
                case INIT_SUB_WORD_LIST:
                    bundle = new Bundle();
                    sublistVisitor.loadWordList(CoreActivity.this);
                    if (!sublistVisitor.allComplete()) {
                        wordVisitor = sublistVisitor.getCurrentWord();
                        lookupInDict(wordVisitor);
                        bundle.putBoolean("complete", false);
                    } else {
                        bundle.putBoolean("complete", true);
                    }
                    if (sublistVisitor instanceof SubListVisitor) {
                        ((SubListVisitor) sublistVisitor).update(CoreActivity.this);
                    }
                    break;
                case UPDATE:
                    bundle = new Bundle();
                    if (!sublistVisitor.allComplete()) {
                        wordVisitor.clear();
                        lookupInDict(wordVisitor);
                        bundle.putBoolean("next", true);
                    } else {
                        bundle.putBoolean("next", false);
                    }
                    break;
                case LOOKUP:
                    bundle = new Bundle();
                    if (!sublistVisitor.allComplete()) {
                        wordVisitor = sublistVisitor.getNext();
                        lookupInDict(wordVisitor);
                        bundle.putBoolean("next", true);
                    } else {
                        bundle.putBoolean("next", false);
                    }
                    if (sublistVisitor instanceof SubListVisitor) {
                        ((SubListVisitor) sublistVisitor).update(CoreActivity.this);
                    }
                    break;
                case UPGRADE: {
                    boolean b = wordVisitor.upgrade(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("upgrade", b);
                    break;
                }
                case DEGRADE: {
                    boolean b = wordVisitor.degrade(CoreActivity.this);
                    bundle = new Bundle();
                    bundle.putBoolean("upgrade", b);
                    break;
                }
                case IGNORE: {
                    boolean b = wordVisitor.ignore(CoreActivity.this);
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
                    if (wordVisitor.isNewWord()) {
                        textView.setText(Html.fromHtml("<b><i>"
                                + wordVisitor.getWord(CoreActivity.this) + "</b></i>"));
                    } else {
                        textView.setText(wordVisitor.getWord(CoreActivity.this));
                    }
                    break;
                }
                case ADD_NEW: {
                    boolean b = result.getBoolean("addnew");
                    Toast.makeText(CoreActivity.this,
                            b ? getString(R.string.add_success) : getString(R.string.add_failed),
                            Toast.LENGTH_SHORT).show();
                    addOrRemove.setText(R.string.remove_from_new);
                    if (wordVisitor.isNewWord()) {
                        textView.setText(Html.fromHtml("<b><i>"
                                + wordVisitor.getWord(CoreActivity.this) + "</b></i>"));
                    } else {
                        textView.setText(wordVisitor.getWord(CoreActivity.this));
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
                case INIT_SCAN_LIST:
                case INIT_NEW_WORD_BOOK_LIST:
                case INIT_SUB_WORD_LIST: {
                    boolean isCompleteStudy = result.getBoolean("complete");
                    if (!isCompleteStudy) {
                        startPreload();
                        countdownManager = new CountdownManager(handler, sublistVisitor.getCount(),
                                sublistVisitor.getCountDown());

                        list.clear();
                        list.addAll(_buflist);
                        if (wordVisitor.isNewWord()) {
                            textView.setText(Html.fromHtml("<b><i>"
                                    + wordVisitor.getWord(CoreActivity.this) + "</b></i>"));
                        } else {
                            textView.setText(wordVisitor.getWord(CoreActivity.this));
                        }
                        adapter.notifyDataSetChanged();
                        progressBarDay.setProgress(sublistVisitor.getProgress());
                        progressBarNight.setProgress(sublistVisitor.getProgress());
                        viewWord.setText(R.string.view_more);
                        if (wordVisitor.isAddToNew()) {
                            addOrRemove.setText(R.string.remove_from_new);
                        } else {
                            addOrRemove.setText(R.string.add_new);
                        }
                        if (wordVisitor.isIgnore()) {
                            ignoreOrNot.setText(R.string.remove_from_ignore);
                        } else {
                            ignoreOrNot.setText(R.string.ignore);
                        }
                        updateLoopCount();
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
                    Log.d(TAG, "progress:" + sublistVisitor.getProgress());
                    updateLoopCount();
                    boolean hasNext = result.getBoolean("next");
                    if (hasNext) {
                        progressBarDay.setProgress(sublistVisitor.getProgress());
                        progressBarNight.setProgress(sublistVisitor.getProgress());
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
                        if (wordVisitor.isAddToNew()) {
                            addOrRemove.setText(R.string.remove_from_new);
                        } else {
                            addOrRemove.setText(R.string.add_new);
                        }
                        if (wordVisitor.isIgnore()) {
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
                    if (!sublistVisitor.allComplete()) {
                        if (autoSpeak) {
                            if (!playControl.ongoing) {
                                playControl.speak(wordVisitor.item.word);
                            }
                        }
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
                    updateLoopCount();
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
                    if (!sublistVisitor.allComplete()) {
                        if (autoSpeak) {
                            if (playControl.ongoing) {
                                // 自动播放看成用户点击的同等操作
                                wordVisitor.setPass(true);
                                wordVisitor.viewPlus(CoreActivity.this);
                                playControl.speak(null);
                            }
                        }
                    }
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

    /**
     * 用于自动朗读单元单词
     * 
     * @author coleman
     * @version [version, Jul 10, 2012]
     * @see [relevant class/method]
     * @since [product/module version]
     */
    private class PlayControl implements OnInitListener, OnUtteranceCompletedListener {
        private TextToSpeech tts;

        private boolean ongoing = false;

        private Handler handler = new Handler();

        private boolean usable = false;

        public PlayControl() {
            tts = new TextToSpeech(CoreActivity.this, this);
        }

        public void speak(String word) {
            if (!usable) {
                return;
            }
            try {
                String tmp = TextUtils.isEmpty(word) ? wordVisitor.item.word
                        + ".\n"
                        + DictDataProcessingCenter.process4TTS(wordVisitor.getDictData(
                                CoreActivity.this).getDatas()) : word;
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, tmp);
                tts.speak(tmp, TextToSpeech.QUEUE_ADD, map);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        public void play() {
            if (!usable) {
                return;
            }
            // ===start fix bug when launch a review activity, can not auto play
            int code = tts.setOnUtteranceCompletedListener(this);
            Log.i(TAG, "===coleman-debug-play-set code: " + code);
            // ===end
            ongoing = true;
            autoSpeak = true;
            btnSpeak.setBackgroundResource(R.drawable.speak_auto);
            setUnlocked();
            wordVisitor.setPass(true);
            wordVisitor.viewPlus(CoreActivity.this);
            speak(null);
        }

        public void stop() {
            if (!usable) {
                return;
            }
            ongoing = false;
            setLocked();
        }

        public void shutdown() {
            if (!usable) {
                return;
            }
            stop();
            tts.shutdown();
        }

        private void setUnlocked() {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            win.setAttributes(winParams);
        }

        private void setLocked() {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.flags &= (~WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    & ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    & ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON & ~WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            win.setAttributes(winParams);
        }

        @Override
        public void onUtteranceCompleted(final String utteranceId) {
            if (ongoing) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new ExpensiveTask(ExpensiveTask.LOOKUP).execute();
                        Log.i(TAG, "===coleman-debug-utteranceId: " + utteranceId);
                    }
                });
            }
        }

        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                usable = true;
                btnSpeak.setVisibility(View.VISIBLE);
                int result = tts.setLanguage(Locale.CHINESE); // 设置发音语言
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) // 判断语言是否可用
                {
                    Log.w(TAG, "Chinese language is not available");
                } else {
                    Log.w(TAG, "Chinese language set successful");
                }
                int code = tts.setOnUtteranceCompletedListener(this);
                Log.i(TAG, "===coleman-debug-code: " + code);
            } else {
                btnSpeak.setVisibility(View.GONE);
            }
        }
    }
}
