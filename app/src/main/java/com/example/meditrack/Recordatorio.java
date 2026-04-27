package com.example.meditrack;

public class Recordatorio {
    public int codigo;
    public String medicamento;
    public String hora;
    public double cantidad;
    public double dosis;

    @Override
    public String toString() {
        return medicamento + " - " + hora + " hora/s" +
                " - (x" + dosis + ")" + " - Restantes: " + cantidad;
    }
}
