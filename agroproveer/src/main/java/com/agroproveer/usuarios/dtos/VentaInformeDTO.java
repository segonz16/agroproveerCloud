package com.agroproveer.usuarios.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VentaInformeDTO {
    private Long id;
    private String cliente;
    private List<VentaDetalleDTO> detalles;


    public BigDecimal getTotal() {
        return detalles.stream()
                .map(VentaDetalleDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}

