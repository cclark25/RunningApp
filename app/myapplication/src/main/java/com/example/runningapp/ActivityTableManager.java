package com.example.runningapp;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Vector;

class ActivityRow {
    Context context;
    public TableRow row;
    public ActivityRow(Context context){
        this.context = context;
        this.row = new TableRow(context);
        TextView txt = new TextView(context);
        txt.setText("Row #" + 1);
        this.row.addView(txt);
    }
}

public class ActivityTableManager {
    TableLayout table;
    Context context;
    Vector<ActivityRow> rows = new Vector<ActivityRow>();

    public ActivityTableManager(Context context, TableLayout table){
        this.table = table;
        this.context = context;

        for(int i =0; i < 100; i++) {
            ActivityRow r = new ActivityRow(context);
            this.rows.add(r);
            table.addView(r.row);
        }
    }
}
