package com.example.meditrack;

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

public class AgregarRecordatorio extends AppCompatActivity {

    private EditText et_medicamento, et_hora, et_dosis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_recordatorio);

        et_medicamento = (EditText)findViewById(R.id.txt_medicamento);
        et_dosis = (EditText)findViewById(R.id.txt_dosis);
        et_hora = (EditText)findViewById(R.id.txt_horario);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    //BOTON AGREGAR
    public void Btn_Agregar(View view){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        String medicamento = et_medicamento.getText().toString();
        String dosis = et_dosis.getText().toString();
        String hora = et_hora.getText().toString();

        if(!medicamento.isEmpty() && !dosis.isEmpty() && !hora.isEmpty()){
            ContentValues registro = new ContentValues();
            registro.put("medicamento", medicamento);
            registro.put("hora", hora);
            registro.put("dosis", dosis);

            BaseDeDatos.insert("recordatorios", null, registro);

            BaseDeDatos.close();

            et_medicamento.setText("");
            et_dosis.setText("");
            et_hora.setText("");

            Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show();
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