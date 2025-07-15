package com.utp.artesaniamvc.service;

import org.springframework.stereotype.Service;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ComprobantePDFService {

    @Autowired
    private PagoPedidoRepository pagoPedidoRepository;

    public ByteArrayInputStream generarComprobantePDF(Integer pagoPedidoId) throws IOException, DocumentException {
        PagoPedido pagoPedido = pagoPedidoRepository.findById(pagoPedidoId)
            .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        // Fuentes
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // Título del comprobante
        Paragraph title = new Paragraph("COMPROBANTE DE PAGO", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Información del local
        DetallePedido detalle = pagoPedido.getDetallePedido();
        document.add(new Paragraph("Local: " + detalle.getLocal().getNombre(), normalFont));
        document.add(new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(pagoPedido.getFechaPago()), normalFont));
        document.add(Chunk.NEWLINE);

        // Tabla de detalles
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{3, 1, 1});

        // Encabezados
        table.addCell(new PdfPCell(new Phrase("Producto", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Cantidad", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Precio", headerFont)));

        // Detalles del producto
        table.addCell(new PdfPCell(new Phrase(detalle.getProducto().getNombreProducto(), normalFont)));
        table.addCell(new PdfPCell(new Phrase(String.valueOf(detalle.getCantidad()), normalFont)));
        table.addCell(new PdfPCell(new Phrase("S/ " + detalle.getPrecio(), normalFont)));

        document.add(table);
        document.add(Chunk.NEWLINE);

        // Totales
        Paragraph totalPedido = new Paragraph("Total Pedido: S/ " + detalle.getTotal(), normalFont);
        totalPedido.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalPedido);

        Paragraph montoPagado = new Paragraph("Monto Pagado: S/ " + pagoPedido.getMontoPagado(), normalFont);
        montoPagado.setAlignment(Element.ALIGN_RIGHT);
        document.add(montoPagado);

        Paragraph vuelto = new Paragraph("Vuelto: S/ " + pagoPedido.getVuelto(), normalFont);
        vuelto.setAlignment(Element.ALIGN_RIGHT);
        document.add(vuelto);

        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}