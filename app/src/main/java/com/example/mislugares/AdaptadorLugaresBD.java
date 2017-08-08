package com.example.mislugares;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by Jesús Tomás on 18/04/2017.
 */

public class AdaptadorLugaresBD extends AdaptadorLugares {

    protected Cursor cursor;

    public AdaptadorLugaresBD(Context contexto, Lugares lugares,
                              Cursor cursor) {
        super(contexto, lugares);
        this.cursor = cursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public Lugar lugarPosicion(int posicion) {
        cursor.moveToPosition(posicion);
        return LugaresBD.extraeLugar(cursor);
    }

    public int idPosicion(int posicion) {
        cursor.moveToPosition(posicion);
        return cursor.getInt(0);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Lugar lugar = lugarPosicion(position);
        personalizaVista(holder, lugar);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
}
