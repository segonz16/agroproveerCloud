package com.agroproveer.usuarios.service;

import com.agroproveer.usuarios.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InformeService {

    private final ProductoRepository productoRepository;

    public InformeService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }
    static class ProductoInformeDTO {
        private String nombre;
        private BigDecimal precio;
        private int cantidad;

        public ProductoInformeDTO(String nombre, BigDecimal precio, int cantidad) {
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
        }

        public BigDecimal getSubtotal() {
            return precio.multiply(BigDecimal.valueOf(cantidad));
        }

        public String getNombre() { return nombre; }
        public BigDecimal getPrecio() { return precio; }
        public int getCantidad() { return cantidad; }
    }

    public String generarInformeXML() throws Exception {
        List<ProductoInformeDTO> productos = cargarProductosDesdeBD();

        BigDecimal total = productos.stream()
                .map(ProductoInformeDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("inventario");
        root.setAttribute("total", total.toString());
        doc.appendChild(root);

        for (ProductoInformeDTO p : productos) {
            Element prod = doc.createElement("producto");
            prod.setAttribute("nombre", p.getNombre());
            prod.setAttribute("precio", p.getPrecio().toString());
            prod.setAttribute("cantidad", String.valueOf(p.getCantidad()));
            prod.setAttribute("subtotal", p.getSubtotal().toString());

            String porcentaje = total.compareTo(BigDecimal.ZERO) > 0
                    ? p.getSubtotal().multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP).toString() + "%"
                    : "0%";
            prod.setAttribute("porcentaje", porcentaje);

            root.appendChild(prod);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

    public List<Map<String, Object>> generarInformeJSON() {
        List<ProductoInformeDTO> productos = cargarProductosDesdeBD();

        BigDecimal total = productos.stream()
                .map(ProductoInformeDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return productos.stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nombre", p.getNombre());
            item.put("precio", p.getPrecio());
            item.put("cantidad", p.getCantidad());
            item.put("subtotal", p.getSubtotal());
            item.put("porcentaje", total.compareTo(BigDecimal.ZERO) > 0
                    ? p.getSubtotal().multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP).toString() + "%"
                    : "0%");
            return item;
        }).collect(Collectors.toList());
    }

    private List<ProductoInformeDTO> cargarProductosDesdeBD() {
        return productoRepository.findAll().stream()
                .map(p -> new ProductoInformeDTO(
                        p.getNombre(),
                        p.getPrecio(),
                        p.getCantidadDisponible()
                ))
                .collect(Collectors.toList());
    }
}

