package com.example.meditrack;

import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.widget.Toast;

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
            CambiarTitulo();
            actualizarTextViewRecordatorio();
            handler.postDelayed(this, 500); // 500 = 0,5
        }
    };
    ConstraintLayout mainLayout;
    Switch switchModoOscuro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE); //guardar la preferencia del usuario
        //SharedPreferences = sistema de almacenamiento
        boolean oscuro = prefs.getBoolean("modoOscuro", false);

        //Setear el tema en base al valor de la variable booleana oscuro
        if (oscuro) {
            setTheme(R.style.AppTheme_Oscuro);
        } else {
            setTheme(R.style.AppTheme_Claro);
        }

        //Carga de la interfaz
        super.onCreate(savedInstanceState);
        //Se aplica el layout en base a lo seteado anteriormente
        setContentView(R.layout.activity_main);

        //Guarda la selección del switch
        mainLayout = findViewById(R.id.main);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);

        switchModoOscuro.setChecked(oscuro);

        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("modoOscuro", isChecked).apply();
            recreate();
        }); //Guarda si el usuario activa o desactiva el check para actualizar el tema en tiempo real

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });//Ajusta el tamaño del layout para que no se vea superpuesto con las barras de los dispositivos

        actualizarListaMedicamentos();

        ListView listView = findViewById(R.id.listaMedicamentos);

        // CLICK en items de la lista
        listView.setOnItemClickListener((parent, view, position, id) -> { //Devuelve el objeto en la posición clickeada
            //Listener para detectar el click
            Recordatorio seleccionado = (Recordatorio) parent.getItemAtPosition(position); //Se utiliza el objeto recordatorio directamente en la selección

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Acciones")
                    .setMessage("¿Qué deseas hacer?")
                    //Modificar
                    .setPositiveButton("Modificar", (dialogInterface, which) -> {
                        Intent modificar = new Intent(this, ModificarRecordatorio.class); //Caso de que el usuario seleccione modificar se usa el intent para movernos entre actividades
                        modificar.putExtra("codigo", seleccionado.codigo); //Pasamos el codigo a la otra actividad para que la app sepa que recordatorio vamos a modificar
                        startActivity(modificar);
                    })
                    //Eliminar
                    .setNegativeButton("Eliminar", (dialogInterface, which) -> {
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4); //Abrimos la base de datos
                        SQLiteDatabase db = admin.getWritableDatabase(); //Modo escritura
                        db.delete("Recordatorio", "codigo=?", new String[]{String.valueOf(seleccionado.codigo)}); //Realizamos un delete utilizando el codigo del objeto seleccionado
                        db.close();
                        actualizarListaMedicamentos();
                    })
                    .create();

            dialog.show();

            //Cambiamos el color de los botones en base al tema seleccionado (claro/oscuro)
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
    //Metodo que devuelve una lista de objetos de Recordatorio
    private ArrayList<Recordatorio> ObtenerRecordatorios() {
        ArrayList<Recordatorio> lista = new ArrayList<>(); //ArrayList vacio para guardar los datos
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4);
        SQLiteDatabase BaseDeDatos = admin.getReadableDatabase(); //Modo lectura

        Cursor cursor = BaseDeDatos.rawQuery("SELECT codigo, medicamento, hora, cantidad, dosis FROM Recordatorio", null);
        //Cursos = puntero que permite recorrer filas por fila los resultados

        if (cursor.moveToFirst()) { //Si existe (si hay datos) posicionamos el cursor en la primera fila
            do {
                Recordatorio r = new Recordatorio();
                //Asignamos los valores de la lista a cada atributo del objeto
                r.codigo = cursor.getInt(0);
                r.medicamento = cursor.getString(1);
                r.hora = cursor.getString(2);
                r.cantidad = cursor.getInt(3);
                r.dosis = cursor.getInt(4);

                lista.add(r); //Terminada de leer la fila, añadimos el objeto a la lista anteriormente vacia
            } while (cursor.moveToNext()); //Avanza hasta la siguiente fila
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
        ArrayList<Recordatorio> lista = ObtenerRecordatorios(); //Llamamos a la función ObtenerRecordatorios que contiene la lista de objetos registrados

        ListView listView = findViewById(R.id.listaMedicamentos); //Buscamos la lista de medicamentos creada en nuestro layout
        ArrayAdapter<Recordatorio> adapter = new ArrayAdapter<>(
                this, //Actividad actual (main)
                R.layout.medicamento, //Layout personalizado
                R.id.textMedicamento, //TextView que se mostrara
                lista //Lisa de objetos de Recordatorio
        );
        listView.setAdapter(adapter); //Conectamos el adaptador a la lista y mostramos todos los registros en pantalla
    }
    boolean ventanaAbierta = false;
    private void CrearVentanaRecordatorio(final String medicamento, final int dosis, final int cantActual) {
    //Variables final para evitar posibles reasignaciones
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Acciones")
                .setMessage("¿Confirmas la toma de " + medicamento + " x " + dosis + " dosis?")
                .setPositiveButton("Confirmar", (dialogInterface, which) -> {
                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(MainActivity.this, "administracion", null, 4);
                    SQLiteDatabase dbWritable = admin.getWritableDatabase();

                    int nuevaCant = cantActual - dosis;

                    if (nuevaCant <= 0) {
                        dbWritable.delete("Recordatorio", "medicamento=?", new String[]{medicamento});
                    } else {
                        ContentValues values = new ContentValues();
                        values.put("fechaRegistro", System.currentTimeMillis());
                        values.put("cantidad", nuevaCant);
                        dbWritable.update("Recordatorio", values, "medicamento=?", new String[]{medicamento});
                    }

                    dbWritable.close();
                    actualizarListaMedicamentos();
                    Toast.makeText(MainActivity.this, "Toma confirmada", Toast.LENGTH_SHORT).show();
                    ventanaAbierta = false;
                    dialogInterface.dismiss();
                })
                .create();
        dialog.show();

        //Cambiamos el color de los botones en base al tema seleccionado (claro/oscuro)
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.botones_recuadro, value, true);
        int color = value.data;

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
    }
    private long tiempoExpiracionRecordatorio = 0;
    private void actualizarTextViewRecordatorio() {
        TextView textViewRecordatorio = findViewById(R.id.textViewRecordatorio);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4);
        SQLiteDatabase db = admin.getReadableDatabase(); //Modo lectura

        Cursor cursor = db.rawQuery("SELECT medicamento, dosis, hora, fechaRegistro, cantidad FROM Recordatorio", null);
        //Traemos las columnas necesarias para calcular cada toma

        StringBuilder recordatorios = new StringBuilder(); //Mensaje a mostrar
        long ahora = System.currentTimeMillis(); //Hora actual

        //Recorreemos cada fila y extraemos los valores
        while (cursor.moveToNext()) {
            String medicamento = cursor.getString(0);
            int dosis = cursor.getInt(1);
            int intervaloHoras = cursor.getInt(2);
            long fechaRegistro = cursor.getLong(3);
            int cantidad = cursor.getInt(4);

            long proximaToma = fechaRegistro + (intervaloHoras * 60L * 60L * 1000L);
            //Calculamos el tiempo exacto en milisegundos y lo sumamos a la fecha registrada del recordatorio y así obtenemos el momento exacto en donde se debe hacer la proxima toma

            //Convertimos ambas horas en formato HH:MM
            String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(ahora));
            String horaToma = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(proximaToma));

            //Si coinciden significa que es la hora de la toma
            if (horaActual.equals(horaToma) && !ventanaAbierta) {
                //Construcción de los mensajes

                //Caso de que sea la ultima toma, se mostrara un mensaje distinto
                if(cantidad <= 1){
                    recordatorios.append("Tomar ultima dosis de ")
                            .append(medicamento)
                            .append(" x ")
                            .append(dosis)
                            .append(" dosis");
                }
                else {
                    recordatorios.append("Tomar ")
                            .append(medicamento)
                            .append(" x ")
                            .append(dosis)
                            .append(" dosis");
                }
                CrearVentanaRecordatorio(medicamento, dosis, cantidad);

                ventanaAbierta = true;

                //Tiempo de expiración (Pasados 5 minutos se borra el mensaje)
                tiempoExpiracionRecordatorio = ahora + (5 * 60 * 1000);
                textViewRecordatorio.setText(recordatorios.toString());
            }
        }
        //Si no hay recordatorios programados para la hora, se muestra el siguiente mensaje
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