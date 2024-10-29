package com.api.fichamed.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.api.fichamed.dto.FichamedDTO;
import com.api.fichamed.model.FichamedModel;
import com.api.fichamed.repository.FichaMedRepository;

@RestController
@RequestMapping("/paciente")
public class FIchamedController {
    @Autowired
    FichaMedRepository repository;

    @GetMapping
    public String mensagem(){
        return "Api Ficha Médica está funcionando! Verifique e este utilizando os endpoints.";
    }

    @GetMapping("listar")
    public List<FichamedModel> listarPacientes(){   
        return repository.findAll();
    }
    
    @GetMapping("/listar/{id}")
    public ResponseEntity<FichamedModel> listarUm(@PathVariable Long id){   
        return repository.findById(id)
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado com ID: " + id));
    }
    
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody FichamedDTO user){
    	FichamedModel paciente = new FichamedModel(user);
    	if((paciente.getNome() == null){
    		
    	}
    }
   
}
