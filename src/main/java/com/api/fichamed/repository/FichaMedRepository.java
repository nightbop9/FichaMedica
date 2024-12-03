package com.api.fichamed.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.fichamed.model.FichamedModel;


public interface FichamedRepository extends JpaRepository<FichamedModel, Long> {
    //métodos para verificação do post
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByTelefone(String telefone);
    //métodos para verificação do put
    boolean existsByCpfAndIdNot(String cpf, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefoneAndIdNot(String telefone, Long id);

}
