
package com.coleman.kingword.wordlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.coleman.kingword.dict.stardict.DictData;

import android.content.Context;
import android.os.Message;

/**
 * Finite state machine is composite of an InitState, an group of states, some
 * input messages, some output messages, and the state transition function.
 */
public class FiniteStateMachine {

    private FiniteState mCurrentState;

    private FiniteStateEngine mStateEngine = new FiniteStateEngine();

    private ArrayList<FiniteState> mStateList = new ArrayList<FiniteState>();

    private FiniteState initState = new InitState();

    private FiniteState mulState = new MultipleState();

    private FiniteState completeState = new CompleteState();

    private SliceWordList sliceList;

    public FiniteStateMachine(SliceWordList sliceList) {
        this.sliceList = sliceList;

        setInitialState(initState);

        addState(initState);
        addState(mulState);
        addState(completeState);
    }

    public boolean isComplete() {
        return mCurrentState instanceof CompleteState;
    }

    public boolean isPassView() {
        return initState.pass;
    }

    public boolean isPassMultiple() {
        return mulState.pass;
    }

    protected void addState(FiniteState state) {
        mStateList.add(state);
    }

    public FiniteState getCurrentState() {
        return mCurrentState;
    }

    public FiniteState getInitState() {
        return initState;
    }

    public void sendMessage(Message msg) {
        mStateEngine.handleMessage(msg);
    }

    public void sendEmptyMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        mStateEngine.handleMessage(msg);
    }

    public ArrayList<DictData> getDictData(Context context, WordItem item, ArrayList<WordItem> list) {
        return mCurrentState.getDictData(context, item, list);
    }

    private void setInitialState(FiniteState state) {
        mCurrentState = state;
    }

    public abstract class FiniteState {
        private Random ran = new Random();

        protected FiniteState nextState;

        protected boolean counted;

        protected boolean pass;

        protected FiniteState() {
            counted = false;
            pass = false;
        }

        protected abstract FiniteState getNext();

        /**
         * Called when a state is entered.
         */
        protected void enter() {
        }

        /**
         * Called when a state is exited.
         */
        protected void exit() {
            counted = true;
            pass = true;
        }

        protected void reset() {
            pass = false;
        }

        protected ArrayList<DictData> getDictData(Context context, WordItem item,
                ArrayList<WordItem> list) {
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

        protected void addRandomDictData(Context context, ArrayList<WordItem> list,
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

        public boolean isCounted() {
            return counted;
        }

    }

    public class InitState extends FiniteState {
        @Override
        protected void exit() {
            if (!counted) {
                sliceList.passViewCount++;
            }
            super.exit();
        }

        protected ArrayList<DictData> getDictData(Context context, WordItem item,
                ArrayList<WordItem> list) {
            ArrayList<DictData> datalist = new ArrayList<DictData>();
            if (counted) {
                DictData data = item.getDetail(context);
                if (data.getDatas().indexOf("not found!") != -1) {
                    data = item.getDictData(context);
                }
                datalist.add(data);
            } else {
                DictData data = item.getDictData(context);
                if (data.getDatas().indexOf("not found!") != -1) {
                    data = item.getDetail(context);
                }
                datalist.add(data);
            }
            return datalist;
        }

        @Override
        protected FiniteState getNext() {
            return mulState;
        }
    }

    public class MultipleState extends FiniteState {
        @Override
        protected void exit() {
            if (!counted) {
                sliceList.passMulCount++;
                switch (sliceList.listType) {
                    case SliceWordList.SUB_WORD_LIST:
                        break;
                    case SliceWordList.NEW_WORD_BOOK_LIST:
                        break;
                    case SliceWordList.SCAN_LIST:
                        break;
                    case SliceWordList.REVIEW_LIST:
                        sliceList.passViewCount++;
                        break;
                    default:
                        throw new RuntimeException("Not support word list type!");
                }
            }
            super.exit();
        }

        protected ArrayList<DictData> getDictData(Context context, WordItem item,
                ArrayList<WordItem> list) {
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

        @Override
        protected FiniteState getNext() {
            return completeState;
        }
    }

    public class CompleteState extends FiniteState {
        @Override
        protected FiniteState getNext() {
            return null;
        }
    }

    private class FiniteStateEngine implements IFSMCommand {
        private FiniteStateEngine() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEXT:
                    transitionTo(mCurrentState.getNext());
                    break;
                case RESET:
                    reset();
                    break;
                case COMPLETE:
                    complete();
                    break;
                case LAST:
                    setInitialState(mulState);
                    break;
                default:
                    break;
            }
        }

        private void transitionTo(FiniteState state) {
            mCurrentState.exit();
            mCurrentState = state;
            mCurrentState.enter();
        }

        private void reset() {
            for (FiniteState finiteState : mStateList) {
                finiteState.reset();
            }
            mCurrentState = initState;
        }

        private void complete() {
            for (FiniteState finiteState : mStateList) {
                finiteState.exit();
            }
            mCurrentState = completeState;
        }
    }

    public static interface IFSMCommand {
        int NEXT = 1;

        int RESET = 2;

        int COMPLETE = 3;

        int LAST = 4;
    }
}
