package com.agroproveer.usuarios.rest;

import com.agroproveer.usuarios.service.InformeVentaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/informe/ventas")
public class InformeVentaController {

    private final InformeVentaService informeVentaService;

    public InformeVentaController(InformeVentaService informeVentaService) {
        this.informeVentaService = informeVentaService;
    }

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getInformeXML() {
        try {
            String xml = informeVentaService.generarInformeXML();
            return ResponseEntity.ok().body(xml);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("<error>Ocurrió un error generando el XML</error>");
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getInformeJSON() {
        return ResponseEntity.ok(informeVentaService.generarInformeJSON());
    }
}
