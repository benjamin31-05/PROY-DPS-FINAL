package com.utp.artesaniamvc.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.utp.artesaniamvc.model.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.springframework.stereotype.Service;

@Service
public class PdfGeneratorService {

    public void generarComprobantePDF(Pedido pedido, HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // Fuente para el título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            // Fuente para información general
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            // Logo o encabezado de la empresa (si tienes)
            Paragraph titulo = new Paragraph("BOLETA DE VENTA", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            // Información de la empresa (puedes personalizar)
            Paragraph empresa = new Paragraph("HELADERIA BREEZE\nRUC: 12345678901\nDirección: Av. Castilla 231", infoFont);
            empresa.setAlignment(Element.ALIGN_CENTER);
            document.add(empresa);

            document.add(Chunk.NEWLINE);

            // Obtener el local de recogida desde el primer detalle del pedido
            Local localRecogida = pedido.getDetalles().get(0).getLocal();

            // Información del pedido
            Paragraph infoPedido = new Paragraph();
            infoPedido.add(new Chunk("Número de Pedido: ", infoFont));
            infoPedido.add(new Chunk(pedido.getNumero(), FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            document.add(infoPedido);

            Paragraph fechaPedido = new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(pedido.getFechaCreacion()), infoFont);
            document.add(fechaPedido);

            // Datos del cliente
            Paragraph cliente = new Paragraph("Cliente: " + pedido.getUsuario().getNombre(), infoFont);
            cliente.add(Chunk.NEWLINE);
            cliente.add("Email: " + pedido.getUsuario().getEmail());
            cliente.add(Chunk.NEWLINE);
            cliente.add("Local de recogida: " + localRecogida.getNombre());
            document.add(cliente);

            document.add(Chunk.NEWLINE);

            // Detalles del pedido en tabla
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Encabezados de la tabla
            String[] headers = {"Producto", "Precio", "Tamaño", "Cantidad", "Subtotal"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Filas de detalles
            for (DetallePedido detalle : pedido.getDetalles()) {
                table.addCell(detalle.getNombre());
                table.addCell("S/ " + String.format("%.2f", detalle.getPrecio()));
                table.addCell(detalle.getTamano().getNombre());
                table.addCell(String.valueOf(detalle.getCantidad()));
                table.addCell("S/ " + String.format("%.2f", detalle.getTotal()));
            }

            document.add(table);

            // Resumen de totales
            Paragraph totalParrafo = new Paragraph();
            totalParrafo.add(new Chunk("Subtotal: ", infoFont));
            totalParrafo.add(new Chunk("S/ " + String.format("%.2f", pedido.getTotal() / 1.18), FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            totalParrafo.add(Chunk.NEWLINE);
            totalParrafo.add(new Chunk("IGV (18%): ", infoFont));
            totalParrafo.add(new Chunk("S/ " + String.format("%.2f", pedido.getTotal() * 0.18 / 1.18), FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            totalParrafo.add(Chunk.NEWLINE);
            totalParrafo.add(new Chunk("Total: ", infoFont));
            totalParrafo.add(new Chunk("S/ " + String.format("%.2f", pedido.getTotal()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLUE)));

            document.add(totalParrafo);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
