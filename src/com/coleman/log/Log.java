
package com.coleman.log;

public interface Log {
    void v(String TAG, Object msg);

    void d(String TAG, Object msg);

    void i(String TAG, Object msg);

    void w(String TAG, Object msg);

    void e(String TAG, Object msg);

    void setLevel(Level level);

    /**
     * Specified if show on console.
     * 
     * @param printed true show otherwise false.
     */
    void setPrintable(boolean printed);

    public static enum Level {
        verbose(2), debug(3), info(4), warning(5), error(6), off(7);
        public final int value;

        private Level(int v) {
            this.value = v;
        }

        public static Level getLevel(int value) {
            Level l = verbose;
            switch (value) {
                case 2:
                    l = verbose;
                    break;
                case 3:
                    l = debug;
                    break;
                case 4:
                    l = info;
                    break;
                case 5:
                    l = warning;
                    break;
                case 6:
                    l = error;
                    break;
                case 7:
                    l = off;
                    break;
                default:
                    break;
            }
            return l;
        }
    }

}
