package com.example.meditrack;

import android.content.ContentValues;
import android.content.SharedPreferences;
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

public class AgregarMedicamento extends AppCompatActivity {

    private EditText et_medicamento, et_hora, et_cantidad, et_dosis;

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
        setContentView(R.layout.activity_agregar_medicamento);

        et_medicamento = findViewById(R.id.txt_medicamento);
        et_cantidad = findViewById(R.id.txt_cantidad);
        et_hora = findViewById(R.id.txt_horario);
        et_dosis = findViewById(R.id.txt_dosis);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    //BOTON AGREGAR
    public void Btn_Agregar(View view){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 4);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String medicamento = et_medicamento.getText().toString();
        String cantidad = et_cantidad.getText().toString();
        String hora = et_hora.getText().toString();
        int horaINT = Integer.parseInt(hora);
        String dosis = et_dosis.getText().toString();

        if(!medicamento.isEmpty() && !cantidad.isEmpty() && !hora.isEmpty()){
            if(!cantidad.equals("0") && !dosis.equals("0") && !hora.equals("0")){
                if(horaINT <= 24){
                    ContentValues registro = new ContentValues();
                    registro.put("medicamento", medicamento);
                    registro.put("hora", hora);
                    registro.put("cantidad", cantidad);
                    registro.put("dosis", dosis);
                    registro.put("fechaRegistro", System.currentTimeMillis());

                    BaseDeDatos.insert("Recordatorio", null, registro);

                    BaseDeDatos.close();

                    et_medicamento.setText("");
                    et_cantidad.setText("");
                    et_hora.setText("");
                    et_dosis.setText("");

                    Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show();
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