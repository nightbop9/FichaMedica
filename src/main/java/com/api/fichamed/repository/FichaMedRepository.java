package com.api.fichamed.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.fichamed.model.FichamedModel;

public interface FichaMedRepository extends JpaRepository<FichamedModel, Long> {

}
