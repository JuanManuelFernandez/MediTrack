package com.example.meditrack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.AlertDialog;

import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.view.View;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View vista = getLayoutInflater().inflate(R.layout.activity_agregar_medicamento, null);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CambiarTitulo();

        final ArrayList<String>[] listaMedicamentos = new ArrayList[]{ObtenerRecordatorios()};

        ListView listView = findViewById(R.id.listaMedicamentos);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.medicamento,
                R.id.textMedicamento,
                listaMedicamentos[0]
        );
        listView.setAdapter(adapter);

        //CLICKEAR ITEMS EN LA LISTA
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String itemSeleccionado = listaMedicamentos[0].get(position);

            String codigoSeleccionado = itemSeleccionado.split(" - ")[0];

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Acciones")
                    .setMessage("¿Qué deseas hacer?")
                    .setPositiveButton("Modificar", (dialog, which) -> {
                        //MODIFICAR
                        Intent modificar = new Intent(this, ModificarRecordatorio.class);
                        modificar.putExtra("codigo", Integer.parseInt(codigoSeleccionado));
                        startActivity(modificar);
                    })
                    .setNegativeButton("Eliminar", (dialog, which) -> {
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4);
                        SQLiteDatabase db = admin.getWritableDatabase();

                        // Eliminar
                        db.delete("Recordatorio", "medicamento=?", new String[]{itemSeleccionado.split(" - ")[1]});
                        db.close();

                        listaMedicamentos[0] = ObtenerRecordatorios();
                        ArrayAdapter<String> nuevoAdapter = new ArrayAdapter<>(
                                MainActivity.this,
                                R.layout.medicamento,
                                R.id.textMedicamento,
                                listaMedicamentos[0]
                        );
                        listView.setAdapter(nuevoAdapter);
                    })
                    .show();
        });
    }
    //FUNCIÓN QUE SE LLAMA CADA VEZ QUE MAIN VUELVE A ESTAR VISIBLE
    @Override
    protected void onResume(){
        super.onResume();
        actualizarTextViewRecordatorio();
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

                lista.add(codigo + " - " + medicamento + " - " + hora + " horas" + " - " + " (x" + dosis + ")" + " - " + "Restantes: " + cantidad);
            } while (cursor.moveToNext());
        }

        cursor.close();
        BaseDeDatos.close();

        return lista;
    }
    private void CambiarTitulo() {
        TextView titulo = findViewById(R.id.textView3); //OBTENER REFERENCIA

        String horaActual = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        int hora = Integer.parseInt(horaActual);

        // Cambiar el texto según la hora
        if (hora >= 6 && hora < 12) {
            titulo.setText("¡Buenos días!");
        } else if (hora >= 12 && hora < 20) {
            titulo.setText("¡Buenas tardes!");
        } else {
            titulo.setText("¡Buenas noches!");
        }
    }
    private void actualizarTextViewRecordatorio() {
        TextView textViewRecordatorio = findViewById(R.id.textViewRecordatorio);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4);
        SQLiteDatabase db = admin.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT medicamento, dosis, hora, fechaRegistro FROM Recordatorio", null);

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

            //Se asignan los valores al stringbuilder para poder cambiar el recordatorio
            if (horaActual.equals(horaToma)) {
                recordatorios.append("Tomar ")
                        .append(medicamento)
                        .append(" x ")
                        .append(dosis)
                        .append(" dosis\n");

                // Actualizar fechaRegistro para que se reprograme
                long nuevaFechaRegistro = proximaToma;
                ContentValues values = new ContentValues();
                values.put("fechaRegistro", nuevaFechaRegistro);
                db.update("Recordatorio", values, "medicamento=?", new String[]{medicamento});
            }
        }

        if (recordatorios.length() > 0) {
            textViewRecordatorio.setText(recordatorios.toString());

            // Limpiar después de 5 minutos
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                textViewRecordatorio.setText("");
            }, 5 * 60 * 1000);
        } else {
            textViewRecordatorio.setText("No hay recordatorios activos en esta hora");
        }

        cursor.close();
        db.close();
    }

    //BOTON AGREGAR
    public void Btn_Agregar(View view){
        Intent agregar = new Intent(this, AgregarMedicamento.class);
        startActivity(agregar);
    }
}