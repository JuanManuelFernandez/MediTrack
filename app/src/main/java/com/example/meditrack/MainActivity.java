package com.example.meditrack;

import android.os.Bundle;
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
        View vista = getLayoutInflater().inflate(R.layout.activity_agregar_recordatorio, null);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textRecordatorio = findViewById(R.id.textRecordatorio);

        mostrarRecordatorio(textRecordatorio, "Ibuprofeno", "20:21");

        CambiarTitulo();

        ArrayList<String> listaMedicamentos = new ArrayList<>();
        listaMedicamentos.add("Ibuprofeno - 20:21");
        listaMedicamentos.add("Paracetamol - 08:00");
        listaMedicamentos.add("Amoxicilina - 12:30");
        listaMedicamentos.add("Omeprazol - 07:00");
        listaMedicamentos.add("Aspirina - 09:15");
        listaMedicamentos.add("Metformina - 18:00");
        listaMedicamentos.add("Losartán - 22:00");
        listaMedicamentos.add("Atorvastatina - 21:00");
        listaMedicamentos.add("Clonazepam - 23:00");
        listaMedicamentos.add("Vitamina D - 10:00");
        listaMedicamentos.add("Hierro - 14:00");
        listaMedicamentos.add("Ibuprofeno - 20:21");
        listaMedicamentos.add("Paracetamol - 08:00");
        listaMedicamentos.add("Amoxicilina - 12:30");
        listaMedicamentos.add("Omeprazol - 07:00");
        listaMedicamentos.add("Aspirina - 09:15");
        listaMedicamentos.add("Metformina - 18:00");
        listaMedicamentos.add("Losartán - 22:00");
        listaMedicamentos.add("Atorvastatina - 21:00");
        listaMedicamentos.add("Clonazepam - 23:00");
        listaMedicamentos.add("Vitamina D - 10:00");
        listaMedicamentos.add("Hierro - 14:00");

        // Configurar ListView
        ListView listView = findViewById(R.id.listaMedicamentos);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.medicamento,
                R.id.textMedicamento,
                listaMedicamentos
        );
        listView.setAdapter(adapter);

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
        Intent agregar = new Intent(this, AgregarRecordatorio.class);
        startActivity(agregar);
    }

    public void Btn_RediMedica(View view){
        Intent RediMedi = new Intent(this, AgregarMedicamento.class);
        startActivity(RediMedi);

    }
}