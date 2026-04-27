package com.example.meditrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class VistaRegistros extends AppCompatActivity {

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vista_registros);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ListView listView = findViewById(R.id.listaRegistros);
        // CLICK en items de la lista
        listView.setOnItemClickListener((parent, view, position, id) -> {
            //Listener para detectar el click
            Registro seleccionado = (Registro) parent.getItemAtPosition(position);

            AlertDialog dialog = new AlertDialog.Builder(VistaRegistros.this)
                    .setTitle("Acciones")
                    .setMessage("¿Qué deseas hacer?")
                    //Eliminar
                    .setNegativeButton("Eliminar", (dialogInterface, which) -> {
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 6);
                        SQLiteDatabase db = admin.getWritableDatabase(); //Modo escritura
                        db.delete("Registros", "id=?", new String[]{String.valueOf(seleccionado.id)});
                        db.close();
                        actualizarListaRegistros();
                    })
                    .create();

            dialog.show();

            //Cambiamos el color de los botones en base al tema seleccionado (claro/oscuro)
            TypedValue value = new TypedValue();
            getTheme().resolveAttribute(R.attr.botones_recuadro, value, true);
            int color = value.data;

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
        });
        actualizarListaRegistros();
    }
    private ArrayList<Registro> ObtenerRegistros() {
        ArrayList<Registro> lista = new ArrayList<>(); //ArrayList vacio para guardar los datos
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 6);
        SQLiteDatabase BaseDeDatos = admin.getReadableDatabase(); //Modo lectura

        Cursor cursor = BaseDeDatos.rawQuery("SELECT id, medicamento, dosis, fechaToma FROM Registros", null);
        //Cursos = puntero que permite recorrer filas por fila los resultados

        if (cursor.moveToFirst()) { //Si existe (si hay datos) posicionamos el cursor en la primera fila
            do {
                Registro r = new Registro();
                //Asignamos los valores de la lista a cada atributo del objeto
                r.id = cursor.getInt(0);
                r.medicamento = cursor.getString(1);
                r.dosis = cursor.getDouble(2);
                r.fechaToma = cursor.getString(3);

                lista.add(r); //Terminada de leer la fila, añadimos el objeto a la lista anteriormente vacia
            } while (cursor.moveToNext()); //Avanza hasta la siguiente fila
        }

        cursor.close();
        BaseDeDatos.close();

        return lista;
    }
    private void actualizarListaRegistros() {
        ArrayList<Registro> lista = ObtenerRegistros(); //Llamamos a la función ObtenerRecordatorios que contiene la lista de objetos registrados

        ListView listView = findViewById(R.id.listaRegistros); //Buscamos la lista de medicamentos creada en nuestro layout
        ArrayAdapter<Registro> adapter = new ArrayAdapter<>(
                this, //Actividad actual (main)
                R.layout.registro, //Layout personalizado
                R.id.textRegistro, //TextView que se mostrara
                lista //Lisa de objetos de Recordatorio
        );
        listView.setAdapter(adapter); //Conectamos el adaptador a la lista y mostramos todos los registros en pantalla
    }
    public void Btn_Volver(View view){
        Intent volver = new Intent(this, MainActivity.class);
        startActivity(volver);
    }
    public void Btn_Buscar(View view){
        EditText text = findViewById(R.id.editTextTextBusca);
        String busqueda = text.getText().toString().trim();

        if(!busqueda.isEmpty()){
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 6);
            SQLiteDatabase db = admin.getReadableDatabase(); //Modo lectura

            Cursor cursor;
            if (busqueda.matches("\\d{4}[-/]\\d{2}[-/]\\d{2}")) {
                String fechaNormalizada = busqueda.replace("/", "-");

                cursor = db.rawQuery(
                        "SELECT id, medicamento, dosis, fechaToma FROM Registros WHERE fechaToma LIKE ? COLLATE NOCASE",
                        new String[]{"%" + fechaNormalizada + "%"}
                );
            }else {
                cursor = db.rawQuery(
                        "SELECT id, medicamento, dosis, fechaToma FROM Registros WHERE medicamento LIKE ? COLLATE NOCASE",
                        new String[]{"%" + busqueda + "%"}
                );
            }

            ArrayList<Registro> lista = new ArrayList<>();
            if(cursor.moveToFirst()){
                do{
                    Registro r = new Registro();
                    r.id = cursor.getInt(0);
                    r.medicamento = cursor.getString(1);
                    r.dosis = cursor.getDouble(2);
                    r.fechaToma = cursor.getString(3);

                    lista.add(r);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

            ListView listView = findViewById(R.id.listaRegistros);
            ArrayAdapter<Registro> adapter = new ArrayAdapter<>(
                    this,
                    R.layout.registro,
                    R.id.textRegistro,
                    lista
            );
            listView.setAdapter(adapter);

            if (lista.isEmpty()) {
                Toast.makeText(this, "No se encontraron registros para: " + busqueda, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            actualizarListaRegistros();
        }

    }
}