package com.example.meditrack;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AgregarMedicamento extends AppCompatActivity {

    private EditText etm,etc;
    private Spinner sp_u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_medicamento);

        etm = (EditText)findViewById(R.id.et_Agremedica);
        etc = (EditText)findViewById(R.id.et_CantMedi);
        sp_u = (Spinner)findViewById(R.id.Sp_UniMedidas);

        String [] UniMedidas = {"Kilogramos","Gramos","Miligramos","Litros","Militros"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,UniMedidas);
        sp_u.setAdapter(adapter);

        }
    public void btn_AgregarMedi(View view){
        Toast.makeText(this,"Medicamento agregado con exito",Toast.LENGTH_SHORT).show();

    }


}