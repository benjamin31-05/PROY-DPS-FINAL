package com.utp.artesaniamvc.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.utp.artesaniamvc.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class VentasPDFService {

    @Autowired
    private VentaService ventaService;

    public ByteArrayInputStream generarReporteVentasPDF(String local, Date fechaInicio, Date fechaFin) throws DocumentException {
        // Obtener lista de ventas según filtros
        List<DetallePedido> ventas;
        if (local != null && !local.isEmpty()) {
            ventas = ventaService.obtenerListaVentasPorLocalAdmin(local, fechaInicio, fechaFin);
        } else {
            ventas = ventaService.obtenerListaTodasVentasAdmin(fechaInicio, fechaFin);
        }

        // Configurar formato de moneda y fecha
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Crear documento PDF
        Document document = new Document(PageSize.A4.rotate()); // Horizontal
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Título del reporte
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
        Paragraph titulo = new Paragraph("Reporte de Ventas", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        // Subtítulo con filtros
        Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
        String subtitulo = String.format("Periodo: %s al %s | Local: %s", 
            formatoFecha.format(fechaInicio), 
            formatoFecha.format(fechaFin), 
            local != null && !local.isEmpty() ? local : "Todos los Locales"
        );
        Paragraph filtroParrafo = new Paragraph(subtitulo, subtituloFont);
        filtroParrafo.setAlignment(Element.ALIGN_CENTER);
        document.add(filtroParrafo);
        document.add(Chunk.NEWLINE);

        // Crear tabla de ventas
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2, 1, 1, 1, 2, 2, 2});

        // Encabezados
        String[] headers = {"ID", "Producto", "Cantidad", "Precio Unit.", "Total", "Fecha Venta", "Contacto del Cliente", "Local"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(headerCell);
        }

        // Agregar datos de ventas
        double totalVentas = 0;
        int totalProductos = 0;
        for (DetallePedido venta : ventas) {
            table.addCell(String.valueOf(venta.getId()));
            table.addCell(venta.getProducto().getNombreProducto());
            table.addCell(String.valueOf(venta.getCantidad()));
            table.addCell(formatoMoneda.format(venta.getPrecio()));
            table.addCell(formatoMoneda.format(venta.getTotal()));
            table.addCell(formatoFecha.format(venta.getPedido().getFechaCreacion()));
            table.addCell(venta.getPedido().getUsuario().getUsername());
            table.addCell(venta.getLocal().getNombre());

            totalVentas += venta.getTotal();
            totalProductos += venta.getCantidad();
        }

        document.add(table);

        // Resumen
        document.add(Chunk.NEWLINE);
        Font resumenFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Paragraph resumen = new Paragraph("Resumen", resumenFont);
        resumen.setAlignment(Element.ALIGN_LEFT);
        document.add(resumen);

        PdfPTable resumenTable = new PdfPTable(2);
        resumenTable.setWidthPercentage(50);
        resumenTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        resumenTable.addCell("Total de Ventas:");
        resumenTable.addCell(formatoMoneda.format(totalVentas));
        resumenTable.addCell("Total de Productos Vendidos:");
        resumenTable.addCell(String.valueOf(totalProductos));

        document.add(resumenTable);

        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}