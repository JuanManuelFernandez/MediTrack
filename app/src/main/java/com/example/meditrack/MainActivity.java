package com.example.meditrack;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ListView;
import android.view.View;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refrescarRunnable = new Runnable() {
        @Override
        public void run() {
            actualizarTextViewRecordatorio();
            handler.postDelayed(this, 500); // 500 = 0,5
        }
    };
    ConstraintLayout mainLayout;
    Switch switchModoOscuro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        boolean oscuro = prefs.getBoolean("modoOscuro", false);

        if (oscuro) {
            setTheme(R.style.AppTheme_Oscuro);
        } else {
            setTheme(R.style.AppTheme_Claro);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);

        switchModoOscuro.setChecked(oscuro);

        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("modoOscuro", isChecked).apply();
            recreate();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        CambiarTitulo();
        actualizarListaMedicamentos();

        ListView listView = findViewById(R.id.listaMedicamentos);

        // CLICK en items de la lista
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String itemSeleccionado = (String) parent.getItemAtPosition(position);
            String codigoSeleccionado = itemSeleccionado.split(" - ")[0];

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Acciones")
                    .setMessage("¿Qué deseas hacer?")
                    .setPositiveButton("Modificar", (dialogInterface, which) -> {
                        // MODIFICAR
                        Intent modificar = new Intent(this, ModificarRecordatorio.class);
                        modificar.putExtra("codigo", Integer.parseInt(codigoSeleccionado));
                        startActivity(modificar);
                    })
                    .setNegativeButton("Eliminar", (dialogInterface, which) -> {
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4);
                        SQLiteDatabase db = admin.getWritableDatabase();

                        // Eliminar por nombre de medicamento
                        db.delete("Recordatorio", "medicamento=?", new String[]{itemSeleccionado.split(" - ")[1]});
                        db.close();

                        actualizarListaMedicamentos(); // refresca lista después de eliminar
                    })
                    .create();

            dialog.show();

            TypedValue value = new TypedValue();
            getTheme().resolveAttribute(R.attr.botones_recuadro, value, true);
            int color = value.data;

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(refrescarRunnable);
    }
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(refrescarRunnable);
    }
    private ArrayList<String> ObtenerRecordatorios() {
        ArrayList<String> lista = new ArrayList<>();
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4);
        SQLiteDatabase BaseDeDatos = admin.getReadableDatabase();

        Cursor cursor = BaseDeDatos.rawQuery("SELECT codigo, medicamento, hora, cantidad, dosis FROM Recordatorio", null);

        if (cursor.moveToFirst()) {
            do {
                int codigo = cursor.getInt(0);
                String medicamento = cursor.getString(1);
                String hora = cursor.getString(2);
                int cantidad = cursor.getInt(3);
                int dosis = cursor.getInt(4);

                lista.add(codigo + " - " + medicamento + " - " + hora + " hora/s" +
                        " - (x" + dosis + ")" + " - Restantes: " + cantidad);
            } while (cursor.moveToNext());
        }

        cursor.close();
        BaseDeDatos.close();

        return lista;
    }

    private void CambiarTitulo() {
        TextView titulo = findViewById(R.id.textView3);

        String horaActual = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        int hora = Integer.parseInt(horaActual);

        if (hora >= 6 && hora < 12) {
            titulo.setText("¡Buenos días!");
        } else if (hora >= 12 && hora < 20) {
            titulo.setText("¡Buenas tardes!");
        } else {
            titulo.setText("¡Buenas noches!");
        }
    }

    private void actualizarListaMedicamentos() {
        ArrayList<String> lista = ObtenerRecordatorios();

        ListView listView = findViewById(R.id.listaMedicamentos);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.medicamento,
                R.id.textMedicamento,
                lista
        );
        listView.setAdapter(adapter);
    }

    private long tiempoExpiracionRecordatorio = 0;

    private void actualizarTextViewRecordatorio() {
        TextView textViewRecordatorio = findViewById(R.id.textViewRecordatorio);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4);
        SQLiteDatabase db = admin.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT medicamento, dosis, hora, fechaRegistro, cantidad FROM Recordatorio", null);

        StringBuilder recordatorios = new StringBuilder();
        long ahora = System.currentTimeMillis();

        while (cursor.moveToNext()) {
            String medicamento = cursor.getString(0);
            int dosis = cursor.getInt(1);
            int intervaloHoras = cursor.getInt(2);
            long fechaRegistro = cursor.getLong(3);

            long proximaToma = fechaRegistro + (intervaloHoras * 60L * 60L * 1000L);

            String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(ahora));
            String horaToma = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(proximaToma));

            if (horaActual.equals(horaToma)) {
                recordatorios.append("Tomar ")
                        .append(medicamento)
                        .append(" x ")
                        .append(dosis)
                        .append(" dosis");

                long nuevaFechaRegistro = proximaToma;
                int cantActual = cursor.getInt(4);
                int nuevaCant = cantActual - dosis;

                ContentValues values = new ContentValues();
                values.put("fechaRegistro", nuevaFechaRegistro);
                values.put("cantidad", nuevaCant);
                db.update("Recordatorio", values, "medicamento=?", new String[]{medicamento});

                actualizarListaMedicamentos();

                tiempoExpiracionRecordatorio = ahora + (5 * 60 * 1000);
                textViewRecordatorio.setText(recordatorios.toString());
            }
        }
        if (tiempoExpiracionRecordatorio > ahora) {
        } else if (recordatorios.length() == 0) {
            textViewRecordatorio.setText("No hay recordatorios activos en esta hora");
        }

        cursor.close();
        db.close();
    }
    public void Btn_Agregar(View view) {
        Intent agregar = new Intent(this, AgregarMedicamento.class);
        startActivity(agregar);
    }
}