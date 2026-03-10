package com.example.meditrack;

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

    private EditText et_medicamento, et_hora, et_cantidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modificar_recordatorio);

        et_medicamento = (EditText)findViewById(R.id.txt_medicamento);
        et_cantidad = (EditText)findViewById(R.id.txt_cantidad);
        et_hora = (EditText)findViewById(R.id.txt_horario);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int codigo = getIntent().getIntExtra("codigo", -1);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase db = admin.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT medicamento, hora, cantidad FROM Recordatorio WHERE codigo=" + codigo, null);

        cursor.moveToFirst();

        et_medicamento.setText(cursor.getString(0));
        et_hora.setText(cursor.getString(1));
        et_cantidad.setText(String.valueOf(cursor.getInt(2)));

        cursor.close();
        db.close();
    }
    //MODIFICAR
    public void Btn_Modificar(View view){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        int codigo = getIntent().getIntExtra("codigo", - 1);
        String medicamento = et_medicamento.getText().toString();
        String cantidad = et_cantidad.getText().toString();
        String hora = et_hora.getText().toString();

        if(!medicamento.isEmpty() && !cantidad.isEmpty() && !hora.isEmpty()){
            ContentValues registro = new ContentValues();
            registro.put("medicamento", medicamento);
            registro.put("hora", hora);
            registro.put("cantidad", cantidad);

            int nuevo = BaseDeDatos.update("Recordatorio", registro, "codigo=" + codigo, null);

            BaseDeDatos.close();

            Toast.makeText(this, "Recordatorio modificado correctamente", Toast.LENGTH_SHORT).show();
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