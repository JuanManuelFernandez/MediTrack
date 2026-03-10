package com.example.meditrack;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.AlertDialog;

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

        //REALIZAR METODO PARA ACTUALIZAR AUTOMATICAMENTE EL RECORDATORIO
        TextView textRecordatorio = findViewById(R.id.textRecordatorio);

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
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
                        SQLiteDatabase db = admin.getWritableDatabase();

                        // Eliminar
                        db.delete("Recordatorio", "medicamento=?", new String[]{itemSeleccionado.split(" - ")[0]});
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
    private ArrayList<String> ObtenerRecordatorios() {
        ArrayList<String> lista = new ArrayList<>();
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getReadableDatabase();

        Cursor cursor = BaseDeDatos.rawQuery("SELECT codigo, medicamento, hora, cantidad FROM Recordatorio", null);

        if (cursor.moveToFirst()) {
            do {
                int codigo = cursor.getInt(0);
                String medicamento = cursor.getString(1);
                String hora = cursor.getString(2);
                int cantidad = cursor.getInt(3);

                lista.add(codigo + " - " + medicamento + " - " + hora + " (x" + cantidad + ")");
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
    private void mostrarRecordatorio(TextView textRecordatorio, String medicamento, String horaProgramada) {
        String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        if (horaActual.equals(horaProgramada)) {
            textRecordatorio.setText("Recordatorio: Tomar " + medicamento);
        } else {
            textRecordatorio.setText("No hay recordatorios en este momento");
        }
    }
    //BOTON AGREGAR
    public void Btn_Agregar(View view){
        Intent agregar = new Intent(this, AgregarMedicamento.class);
        startActivity(agregar);
    }
}