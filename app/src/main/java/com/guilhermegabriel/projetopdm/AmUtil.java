package com.guilhermegabriel.projetopdm;

import android.content.Context;
import android.widget.Toast;

public class AmUtil {

    /**
     * Mostra um Toast de curta duração.
     * @param context O contexto da aplicação.
     * @param message A mensagem a ser exibida.
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Mostra um Toast de longa duração.
     * @param context O contexto da aplicação.
     * @param message A mensagem a ser exibida.
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    // Outros métodos utilitários podem ser adicionados aqui.

}
