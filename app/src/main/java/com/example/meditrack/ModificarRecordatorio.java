package com.example.meditrack;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

public class ModificarRecordatorio extends AppCompatActivity {

    private EditText et_medicamento, et_hora, et_cantidad, et_dosis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE); //Preferencia del usuario
        boolean oscuro = prefs.getBoolean("modoOscuro", false);

        //Aplicar el tema seleccionado
        if (oscuro) {
            setTheme(R.style.AppTheme_Oscuro);
        } else {
            setTheme(R.style.AppTheme_Claro);
        }

        //Se llama a la actividad y se ajusta a la pantalla para que el layout quede acorde
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modificar_recordatorio);

        //Obtenemos las referencias de los EditText donde se editaran los datos del recordatorio
        et_medicamento = (EditText)findViewById(R.id.txt_medicamento);
        et_cantidad = (EditText)findViewById(R.id.txt_cantidad);
        et_hora = (EditText)findViewById(R.id.txt_horario);
        et_dosis = (EditText)findViewById(R.id.txt_dosis);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Recibimos el codigo del recordatorio a editar
        int codigo = getIntent().getIntExtra("codigo", -1);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 6);
        SQLiteDatabase db = admin.getReadableDatabase(); //Modo lectura

        Cursor cursor = db.rawQuery("SELECT medicamento, hora, cantidad, dosis, fechaRegistro FROM Recordatorio WHERE codigo=" + codigo, null);
        cursor.moveToFirst();
        //Buscamos el registro cuyo codigo coincida con el que se recibio por intent y se traen las columnas necesarias para setearlas en los editText

        //Seteamos los valores traidos de la base de datos del respectivo recordatorio
        et_medicamento.setText(cursor.getString(0));
        et_hora.setText(cursor.getString(1));
        et_cantidad.setText(String.valueOf(cursor.getDouble(2)));
        et_dosis.setText(String.valueOf(cursor.getDouble(3)));

        cursor.close();
        db.close();
    }
    //MODIFICAR
    public void Btn_Modificar(View view){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 6);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase(); //Modo escritura

        int codigo = getIntent().getIntExtra("codigo", - 1); //Recuperamos el codigo del recordatorio a modificar
        //Se leen los valores ingresados en los EditText
        String medicamento = et_medicamento.getText().toString();
        String cantidad = et_cantidad.getText().toString();
        String hora = et_hora.getText().toString();
        int horaINT = 0;
        if(!hora.isEmpty()){
            horaINT = Integer.parseInt(hora);
        }
        String dosis = et_dosis.getText().toString();

        //Validaciones respectivas para modificar el recordatorio
        if(!medicamento.isEmpty() && !cantidad.isEmpty() && !hora.isEmpty() && !dosis.isEmpty()){
            if(!cantidad.equals("0") && !cantidad.equals("0.") && !cantidad.equals("0.0") && !hora.equals("0") && !dosis.equals("0") && !dosis.equals("0.") && !dosis.equals("0.0")){
                if(horaINT <= 24){
                    ContentValues registro = new ContentValues();
                    registro.put("medicamento", medicamento);
                    registro.put("hora", hora);
                    registro.put("cantidad", cantidad);
                    registro.put("dosis", dosis);

                    //Hacemos la actualización a la tabla en la base de datos
                    BaseDeDatos.update("Recordatorio", registro, "codigo=" + codigo, null);
                    BaseDeDatos.close();

                    Toast.makeText(this, "Recordatorio modificado correctamente", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "El intervalo de horas no puede ser mayor a 24", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Los valores deben ser mayor a 0", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Debe llenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }
    //BOTON VOLVER
    public void Btn_Volver(View view){
        Intent volver = new Intent(this, MainActivity.class);
        startActivity(volver);
    }
}