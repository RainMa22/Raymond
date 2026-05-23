package me.rainma22.Raymond.Debug;

import java.io.PrintStream;

/**
 *
 */
public class Debugger {

    private static Debugger instance = null;
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

    public void log(Object x) {
        this.out.println(x);
    }
}
