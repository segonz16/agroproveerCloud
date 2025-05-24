package com.agroproveer.usuarios.rest;

import com.agroproveer.usuarios.service.InformeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/informe")
public class InformeController {


    private final InformeService informeService;

    public InformeController(InformeService informeService) {
        this.informeService = informeService;
    }

    @GetMapping(value = "/reporte", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> obtenerInforme(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        try {
            if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
                return ResponseEntity.ok(informeService.generarInformeJSON());
            } else {
                String xml = informeService.generarInformeXML();
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(xml);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error generando informe");
        }
    }
}
