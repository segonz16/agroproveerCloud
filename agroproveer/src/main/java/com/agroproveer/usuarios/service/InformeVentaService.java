package com.agroproveer.usuarios.service;

import com.agroproveer.usuarios.dtos.VentaDetalleDTO;
import com.agroproveer.usuarios.dtos.VentaInformeDTO;
import com.agroproveer.usuarios.models.Producto;
import com.agroproveer.usuarios.models.Venta;
import com.agroproveer.usuarios.models.VentaProducto;
import com.agroproveer.usuarios.repository.ProductoRepository;
import com.agroproveer.usuarios.repository.VentaProductoRepository;
import com.agroproveer.usuarios.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InformeVentaService {

    private final VentaRepository ventaRepository;
    private final VentaProductoRepository ventaProductoRepository;
    private final ProductoRepository productoRepository;

    public InformeVentaService(VentaRepository ventaRepo, VentaProductoRepository vpRepo, ProductoRepository pRepo) {
        this.ventaRepository = ventaRepo;
        this.ventaProductoRepository = vpRepo;
        this.productoRepository = pRepo;
    }

    public String generarInformeXML() throws Exception {
        List<Venta> ventas = ventaRepository.findAll();

        List<VentaInformeDTO> informes = ventas.stream().map(venta -> {
            List<VentaProducto> detalles = ventaProductoRepository.findByVentaId(venta.getId());
            List<VentaDetalleDTO> detalleDTOs = detalles.stream().map(dp -> {
                String nombreProducto = productoRepository.findById(dp.getId())
                        .map(Producto::getNombre).orElse("Desconocido");

                return new VentaDetalleDTO(nombreProducto, dp.getCantidad(), dp.getPrecioUnitario());
            }).collect(Collectors.toList());

            return new VentaInformeDTO(venta.getId(), venta.getNombreCompleto(), detalleDTOs);
        }).collect(Collectors.toList());

        BigDecimal totalGeneral = informes.stream()
                .map(VentaInformeDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("ventas");
        root.setAttribute("total", totalGeneral.toString());
        doc.appendChild(root);

        for (VentaInformeDTO venta : informes) {
            Element ventaElement = doc.createElement("venta");
            ventaElement.setAttribute("id", String.valueOf(venta.getId()));
            ventaElement.setAttribute("cliente", venta.getCliente());
            ventaElement.setAttribute("total", venta.getTotal().toString());

            String porcentaje = totalGeneral.compareTo(BigDecimal.ZERO) > 0
                    ? venta.getTotal().multiply(BigDecimal.valueOf(100)).divide(totalGeneral, 2, RoundingMode.HALF_UP).toString() + "%"
                    : "0%";
            ventaElement.setAttribute("porcentaje", porcentaje);

            for (VentaDetalleDTO det : venta.getDetalles()) {
                Element prod = doc.createElement("producto");
                prod.setAttribute("nombre", det.getNombreProducto());
                prod.setAttribute("cantidad", String.valueOf(det.getCantidad()));
                prod.setAttribute("precio_unitario", det.getPrecioUnitario().toString());
                prod.setAttribute("subtotal", det.getSubtotal().toString());
                ventaElement.appendChild(prod);
            }

            root.appendChild(ventaElement);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public List<Map<String, Object>> generarInformeJSON() {
        List<Venta> ventas = ventaRepository.findAll();

        List<VentaInformeDTO> informes = ventas.stream().map(venta -> {
            List<VentaProducto> detalles = ventaProductoRepository.findByVentaId(venta.getId());
            List<VentaDetalleDTO> detalleDTOs = detalles.stream().map(dp -> {
                String nombreProducto = productoRepository.findById(dp.getId())
                        .map(Producto::getNombre).orElse("Desconocido");
                return new VentaDetalleDTO(nombreProducto, dp.getCantidad(), dp.getPrecioUnitario());
            }).toList();

            return new VentaInformeDTO(venta.getId(), venta.getNombreCompleto(), detalleDTOs);
        }).toList();

        BigDecimal totalGeneral = informes.stream()
                .map(VentaInformeDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return informes.stream().map(venta -> {
            Map<String, Object> ventaMap = new LinkedHashMap<>();
            ventaMap.put("id", venta.getId());
            ventaMap.put("cliente", venta.getCliente());
            ventaMap.put("total", venta.getTotal());
            ventaMap.put("porcentaje", totalGeneral.compareTo(BigDecimal.ZERO) > 0
                    ? venta.getTotal().multiply(BigDecimal.valueOf(100)).divide(totalGeneral, 2, RoundingMode.HALF_UP).toString() + "%"
                    : "0%");

            List<Map<String, Object>> productos = venta.getDetalles().stream().map(det -> {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("nombre", det.getNombreProducto());
                p.put("cantidad", det.getCantidad());
                p.put("precio_unitario", det.getPrecioUnitario());
                p.put("subtotal", det.getSubtotal());
                return p;
            }).toList();

            ventaMap.put("productos", productos);
            return ventaMap;
        }).toList();
    }

}

