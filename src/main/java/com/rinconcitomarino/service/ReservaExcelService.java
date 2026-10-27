package com.rinconcitomarino.service;

import com.rinconcitomarino.model.Reserva;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReservaExcelService {
    public byte[] generar(List<Reserva> reservas) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            var hoja = libro.createSheet("Reservas");
            String[] columnas = {"Cliente", "DNI", "Correo", "Telefono", "Personas", "Fecha", "Hora", "Estado"};
            Row encabezado = hoja.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                encabezado.createCell(i).setCellValue(columnas[i]);
                hoja.setColumnWidth(i, 22 * 256);
            }
            int indice = 1;
            for (Reserva reserva : reservas) {
                Row fila = hoja.createRow(indice++);
                fila.createCell(0).setCellValue(reserva.getNombreCompleto());
                fila.createCell(1).setCellValue(reserva.getDni());
                fila.createCell(2).setCellValue(reserva.getEmail());
                fila.createCell(3).setCellValue(reserva.getTelefono());
                fila.createCell(4).setCellValue(reserva.getPersonas());
                fila.createCell(5).setCellValue(reserva.getFecha().toString());
                fila.createCell(6).setCellValue(reserva.getHora());
                fila.createCell(7).setCellValue(reserva.getEstado().getEtiqueta());
            }
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
