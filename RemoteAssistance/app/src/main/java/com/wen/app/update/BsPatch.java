package com.wen.app.update;

public class BsPatch {
    static{
        System.loadLibrary("bspatch");
    }

    /**
     * A native method that is implemented by the 'bspatch' native library,
     * which is packaged with this application.
     */
    public static native void apply(String _old, String _new, String _patch);
}
