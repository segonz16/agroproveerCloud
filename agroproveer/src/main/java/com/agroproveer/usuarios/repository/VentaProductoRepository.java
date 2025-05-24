package com.agroproveer.usuarios.repository;

import com.agroproveer.usuarios.models.VentaProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaProductoRepository  extends JpaRepository<VentaProducto,Long> {

    List<VentaProducto> findByVentaId(Long id);

}
