package com.phonereplay.tasklogger.exception;

import android.content.Context;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultUEH;

    public MyExceptionHandler(Context context) {
        this.context = context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler(); // Mantenha o manipulador padrão
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace(); // Loga a exceção

        // Reenvie para o manipulador padrão
        if (defaultUEH != null) {
            defaultUEH.uncaughtException(thread, ex);
        } else {
            System.exit(2); // Encerra o aplicativo
        }
    }
}
