package com.project.workorder.dao;

import com.project.workorder.model.BomComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 工單 - 物料清單 (BOM) DAO (Repository)
 */
@Repository
public interface BomComponentRepository extends JpaRepository<BomComponent, Integer> {
    @Query("SELECT bc FROM WorkOrderBomComponent bc WHERE bc.parentMaterial.materialId = :parentMaterialId")
    List<BomComponent> findByParentMaterial_MaterialId(@Param("parentMaterialId") Long parentMaterialId);
}