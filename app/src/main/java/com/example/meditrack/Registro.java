package com.example.meditrack;

public class Registro {
    public int id;

    public String medicamento;

    public double dosis;

    public String fechaToma;

    @Override
    public String toString() {
        return "Medicamento: " + medicamento + " - " + "Dosis: " + dosis + " - " + "Fecha de consumo: " + fechaToma;
    }

}
