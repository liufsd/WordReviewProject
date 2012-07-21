
package com.coleman.kingword.wordlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.content.Context;

import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.wordlist.FiniteStateMachine.CompleteState;
import com.coleman.kingword.wordlist.FiniteStateMachine.FiniteState;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;
import com.coleman.kingword.wordlist.FiniteStateMachine.MultipleState;
import com.coleman.log.Log;
import com.coleman.util.Config;
import com.coleman.util.MyApp;
import com.coleman.util.ThreadUtils;

public abstract class AbsSubVisitor implements Serializable {
    private static final long serialVersionUID = 7418355503075825496L;

    protected ArrayList<WordVisitor> list = new ArrayList<WordVisitor>();;

    private static final String TAG = AbsSubVisitor.class.getName();

    public static final byte TYPE = -1;

    protected byte type = TYPE;

    private static Log Log = Config.getLog();

    /**
     * the pointer of the word index in the sublist.
     */
    protected int p;

    /**
     * 关键性能点：
     * <p>
     * 用来标识上次顺序遍历没有完成学习的单词的位置，加快判断是否所有单词都已学习完的速度。
     * 
     * @Optimize
     */
    protected int lastMark;

    protected ViewMethod method;

    private boolean preload;

    protected AbsSubVisitor() {
        method = new ViewMethod("");
    }

    public abstract void loadWordList(Context context);

    public int getProgress() {
        if (list.size() == 0) {
            return 0;
        } else if (list.size() == 1) {
            return 100;
        }

        if (allComplete()) {
            return 100;
        }

        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            WordVisitor wa = list.get(i);
            sum += wa.getStudyIndex();
        }
        int total = (method.getStates().length - 1) * list.size();

        return sum * 100 / total;
    }

    public boolean allComplete() {
        if (list.size() == 0) {
            return true;
        }
        for (int i = lastMark; i < list.size(); i++) {
            WordVisitor wa = list.get(i);
            if (!wa.isComplete()) {
                lastMark = i;
                return false;
            }
        }
        for (int i = 0; i < lastMark; i++) {
            WordVisitor wa = list.get(i);
            if (!wa.isComplete()) {
                lastMark = i;
                return false;
            }
        }
        return true;
    }

    public ArrayList<DictData> getDictData(Context context, WordVisitor item) {
        return item.getDictData(context, list);
    }

    /**
     * call the method must make sure that isComplete is called first.
     */
    public WordVisitor getCurrentWord() {
        if (allComplete()) {
            return list.get(p);
        }
        WordVisitor item = list.get(p);
        if (!item.isComplete()) {
            return item;
        } else {
            return getNext();
        }
    }

    /**
     * call this method must make sure that hasNext is called first!!!
     */
    public WordVisitor getNext() {
        p = p + 1 > list.size() - 1 ? 0 : p + 1;
        WordVisitor accessor = list.get(p);
        if (!accessor.isComplete()) {
            return accessor;
        } else {
            // if the next word is already completed, then try to find the next
            // word after index of p
            int tmp = p + 1 > list.size() - 1 ? 0 : p + 1;
            for (int i = tmp; i < list.size(); i++) {
                WordVisitor it = list.get(i);
                if (!it.isComplete()) {
                    p = i;
                    return it;
                }
            }
            // if not found next word after index p, then try to find the next
            // word from the index 0
            for (int i = 0; i < tmp; i++) {
                WordVisitor it = list.get(i);
                if (!it.isComplete()) {
                    p = i;
                    return it;
                }
            }
            return list.get(tmp);
        }
    }

    public int getCurrentIndex() {
        return p + 1;
    }

    public byte getType() {
        return type;
    }

    public String getViewMethod() {
        Log.i(TAG, "===coleman-debug-getCurrentWord(): " + getCurrentWord().toString());
        return getCurrentWord().getViewMethod();
    }

    public int getCount() {
        return list.size();
    }

    public int getCountDown() {
        return 0;
    }

    public void preload() {
        preload = true;
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = p, c = 0; c < list.size() && preload; i = i + 1 >= list.size() ? 0
                        : i + 1, c++) {
                    list.get(i).preload(MyApp.context);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                preload = false;
            }
        });
    }

    public void stopPreload() {
        preload = false;
    }

    public static class ViewMethod implements Serializable {
        private static final long serialVersionUID = 4298484064348653821L;

        private int stateTypes[];

        public ViewMethod(FiniteState states[]) {
            if (states == null || states.length == 0) {
                this.stateTypes = new int[] {
                        InitState.TYPE, CompleteState.TYPE
                };
            } else {
                ArrayList<FiniteState> list = new ArrayList<FiniteState>();
                for (FiniteState finiteState : states) {
                    if (!(finiteState instanceof CompleteState)) {
                        list.add(finiteState);
                    }
                }
                if (list.size() == 0) {
                    list.add(new InitState());
                }
                list.add(new CompleteState());
                stateTypes = new int[list.size()];
                for (int i = 0; i < states.length; i++) {
                    stateTypes[i] = list.get(i).getType();
                }
                list.clear();
                list = null;
            }
        }

        public ViewMethod(int types[]) {
            checkAndSetTypes(types);
        }

        public ViewMethod(String typeStr) {
            String regular = "[\\d]+(,[\\d]+)*";
            Pattern p = Pattern.compile(regular);
            if (typeStr == null || !p.matcher(typeStr).matches()) {
                typeStr = InitState.TYPE + "," + CompleteState.TYPE;
            }
            String types[] = typeStr.split(",");
            int itypes[] = new int[types.length];
            for (int i = 0; i < itypes.length; i++) {
                itypes[i] = Integer.valueOf(types[i]);
            }
            checkAndSetTypes(itypes);
        }

        private void checkAndSetTypes(int[] itypes) {
            ArrayList<Integer> list = new ArrayList<Integer>();
            if (itypes == null || itypes.length == 0) {
                this.stateTypes = new int[] {
                        InitState.TYPE, CompleteState.TYPE
                };
            } else {
                for (int i = 0; i < itypes.length; i++) {
                    if (itypes[i] != CompleteState.TYPE) {
                        list.add(itypes[i]);
                    }
                }
                if (list.size() == 0) {
                    list.add(InitState.TYPE);
                }
                list.add(CompleteState.TYPE);
                stateTypes = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    stateTypes[i] = list.get(i);
                }
            }
        }

        public FiniteState[] getStates() {
            ArrayList<FiniteState> list = new ArrayList<FiniteState>();
            for (int i = 0; i < stateTypes.length; i++) {
                switch (stateTypes[i]) {
                    case InitState.TYPE:
                        list.add(new InitState());
                        break;
                    case MultipleState.TYPE:
                        list.add(new MultipleState());
                        break;
                    case CompleteState.TYPE:
                        list.add(new CompleteState());
                        break;
                    default:
                        break;
                }
            }
            return list.toArray(new FiniteState[0]);
        }

        public int[] getStateTypes() {
            return stateTypes;
        }

        public String getTypeString() {
            String typeStr = "";
            for (int i = 0; i < stateTypes.length; i++) {
                if (i < stateTypes.length - 1) {
                    typeStr += stateTypes[i] + ",";
                } else {
                    typeStr += stateTypes[i];
                }
            }
            return typeStr;
        }
    }
}
