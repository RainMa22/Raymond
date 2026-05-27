package me.rainma22.Raymond.Debug;

import java.io.PrintStream;

/**
 *
 */
public class Debugger {

    private static Debugger instance = null;
    public static final int VERY_VERBOSE = 2;
    public static final int VERBOSE = 1;
    public static final int LOG = 0;
    public static final int WARNING = -1;
    public static final int ERROR = -2;
    private int logLevel = 0;
    private PrintStream out = System.out;

    private Debugger() {
    }

    public static Debugger getInstance() {
        if (instance == null) {
            instance = new Debugger();
        }
        return instance;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public void setLogLevel(int newLevel) {
        logLevel = newLevel;
    }

    public void log(Object x) {
        log(x, LOG);
    }

    public void log(Object x, int logLevel) {
        if (logLevel <= this.logLevel) {
            this.out.println(x);
        }
    }
}
