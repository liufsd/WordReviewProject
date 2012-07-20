
package com.coleman.kingword.wordlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.Context;
import android.os.Message;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.ojm.core.Observable;
import com.coleman.util.MyApp;

/**
 * Finite state machine is composite of an InitState, an group of states, some
 * input messages, some output messages, and the state transition function.
 */
public class FiniteStateMachine extends Observable implements Serializable {

    private static final long serialVersionUID = -8116244691305999296L;

    private FiniteStateEngine mStateEngine;

    public FiniteStateMachine(int types[], int initState) {
        mStateEngine = new FiniteStateEngine(types, initState);
    }

    public boolean isComplete() {
        return mStateEngine.isComplete();
    }

    public FiniteState getCurrentState() {
        return mStateEngine.getCurrentState();
    }

    public void sendMessage(Message msg) {
        mStateEngine.handleMessage(msg);
        setChanged();
        notifyObservers(null);
    }

    public void sendEmptyMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        mStateEngine.handleMessage(msg);
        setChanged();
        notifyObservers(null);
    }

    public int getCurrentIndex() {
        return getCurrentState().index;
    }

    public ArrayList<DictData> getDictData(Context context, WordAccessor item,
            ArrayList<WordAccessor> list) {
        return getCurrentState().getDictData(context, item, list);
    }

    public String getViewMethod() {
        return getCurrentState().getViewMethod();
    }

    public static abstract class FiniteState implements Serializable {
        private static final long serialVersionUID = 6453121803787047807L;

        private Random ran = new Random();

        protected FiniteState nextState;

        protected boolean pass;

        /**
         * 标识状态在State List中的位置，用来保存每个单词的学习状态。
         */
        protected int index;

        protected String viewMethod = "";

        protected FiniteState() {
            pass = false;
        }

        protected String getViewMethod() {
            return viewMethod;
        }

        /**
         * Called when a state is entered.
         */
        protected void enter() {
        }

        /**
         * Called when a state is exited.
         */
        protected void exit() {
            pass = true;
        }

        protected void reset() {
            pass = false;
        }

        protected ArrayList<DictData> getDictData(Context context, WordAccessor item,
                ArrayList<WordAccessor> list) {
            ArrayList<DictData> datalist = new ArrayList<DictData>();
            return datalist;
        }

        protected void shuffle(ArrayList<DictData> list) {
            if (list.size() <= 1) {
                return;
            } else {
                Collections.shuffle(list);
            }
        }

        protected void addRandomDictData(Context context, ArrayList<WordAccessor> list,
                ArrayList<DictData> datalist) {
            int index = ran.nextInt(list.size());
            int i = 0;
            while (datalist.contains(list.get(index).getDictData(context))) {
                index = index + 1 > list.size() - 1 ? 0 : index + 1;
                i++;
                // to avoid deadlock if there are two or more same words.
                if (i == list.size()) {
                    break;
                }
            }
            datalist.add(list.get(index).getDictData(context));
        }

    }

    public static class InitState extends FiniteState {
        private static final long serialVersionUID = -1336813785311805550L;

        public static final int TYPE = 0;

        public InitState() {
            super();
            viewMethod = MyApp.context.getString(R.string.view_word);
        }

        protected ArrayList<DictData> getDictData(Context context, WordAccessor item,
                ArrayList<WordAccessor> list) {
            ArrayList<DictData> datalist = new ArrayList<DictData>();
            DictData data = item.getDictData(context);
            datalist.add(data);
            return datalist;
        }

    }

    public static class MultipleState extends FiniteState {
        private static final long serialVersionUID = 3630980301775392193L;

        public static final int TYPE = 1;

        public MultipleState() {
            super();
            viewMethod = MyApp.context.getString(R.string.view_multi);
        }

        protected ArrayList<DictData> getDictData(Context context, WordAccessor item,
                ArrayList<WordAccessor> list) {
            ArrayList<DictData> datalist = new ArrayList<DictData>();
            datalist.add(item.getDictData(context));
            int size = list.size();
            for (int i = 0; i < size - 1; i++) {
                if (i > 2) {
                    break;
                } else {
                    addRandomDictData(context, list, datalist);
                }
            }
            shuffle(datalist);
            return datalist;
        }

    }

    public static class CompleteState extends FiniteState {

        private static final long serialVersionUID = 1872280077484642327L;

        public static final int TYPE = 100;

        public CompleteState() {
            super();
            viewMethod = MyApp.context.getString(R.string.view_complete);
        }
    }

    private static class FiniteStateEngine implements IFSMCommand, Serializable {
        private static final long serialVersionUID = 762743574880645974L;

        private ArrayList<FiniteState> mStateList = new ArrayList<FiniteState>();

        private FiniteState mCurrentState;

        private FiniteStateEngine(int[] types, int initStIdx) {
            for (int i = 0; i < types.length; i++) {
                switch (types[i]) {
                    case InitState.TYPE:
                        addState(new InitState());
                        break;
                    case MultipleState.TYPE:
                        addState(new MultipleState());
                        break;
                    case CompleteState.TYPE:
                        addState(new CompleteState());
                        break;
                    default:
                        break;
                }
            }
            for (int i = 0; i < mStateList.size(); i++) {
                mStateList.get(i).index = i;
                if (i != mStateList.size() - 1) {
                    mStateList.get(i).nextState = mStateList.get(i + 1);
                } else {
                    mStateList.get(i).nextState = null;
                }
            }
            setInitialState(initStIdx);
        }

        public boolean isComplete() {
            return mCurrentState instanceof CompleteState;
        }

        protected void addState(FiniteState curState) {
            mStateList.add(curState);
        }

        public FiniteState getCurrentState() {
            return mCurrentState;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEXT:
                    transitionTo(mCurrentState.nextState);
                    break;
                case RESET:
                    reset();
                    break;
                case COMPLETE:
                    complete();
                    break;
                default:
                    break;
            }
        }

        private void transitionTo(FiniteState state) {
            if (!(mCurrentState instanceof CompleteState)) {
                mCurrentState.exit();
                mCurrentState = state;
                mCurrentState.enter();
            }
        }

        private void reset() {
            for (FiniteState finiteState : mStateList) {
                finiteState.reset();
            }
            mCurrentState = mStateList.get(0);
        }

        private void setInitialState(int stateIndex) {
            if (stateIndex >= 0 && stateIndex < mStateList.size()) {
                mCurrentState = mStateList.get(stateIndex);
            } else {
                mCurrentState = mStateList.get(0);
            }

        }

        private void complete() {
            for (FiniteState finiteState : mStateList) {
                if (!(finiteState instanceof CompleteState)) {
                    finiteState.exit();
                }
            }
            mCurrentState = mStateList.get(mStateList.size() - 1);
            mCurrentState.enter();
        }
    }

    public static interface IFSMCommand {
        int NEXT = 1;

        int RESET = 2;

        int COMPLETE = 3;
    }
}
