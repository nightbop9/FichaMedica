package com.api.fichamed.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.api.fichamed.dto.FichamedDTO;
import com.api.fichamed.model.FichamedModel;
import com.api.fichamed.repository.FichamedRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("paciente")
public class FichamedController {
	
	
	private final String caminhoImagens = "src/main/resources/static/uploads/";
	private final String caminhoImagemOrigem = "src/main/resources/static/images/user.png";

	@Autowired
	FichamedRepository repository;

	@GetMapping
    public String exibirIndex() {
        return "index"; // O Spring Boot vai procurar pelo arquivo src/main/resources/templates/index.html
    }
	
	@GetMapping("/listar")
	@ResponseBody
	public List<FichamedModel> listarPacientes() {
		return repository.findAll();
	}

//	@GetMapping("/listar")
//	public String listarPacientes(Model model) {
//        model.addAttribute("pacientes", repository.findAll());
//        return "index"; 
//    }
    

	@GetMapping("/listar/{id}")
	@ResponseBody
	public ResponseEntity<FichamedModel> listarUm(@PathVariable Long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado com ID: " + id));
	}

	@PostMapping("/cadastrar")
	@ResponseBody
	public ResponseEntity<?> cadastrar(@Valid FichamedDTO user, BindingResult result,
			@RequestParam("file") MultipartFile arquivo) {
		FichamedModel paciente = new FichamedModel(user);

		repository.save(paciente);

		try {
			
			File pasta = new File(caminhoImagens);
			if(!pasta.exists()) {
				pasta.mkdir();
			}
			
			File destino = new File(caminhoImagens + "user.png");

			if(!destino.exists()){
				try{
					Path origemPath = Paths.get(caminhoImagemOrigem);
					
					Files.copy(origemPath, destino.toPath());
					System.out.println("Imagem padrão de usuário copiada para: " + destino.getPath());
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Erro ao copiar o arquivo: " + e.getMessage());
				}
			}
			
			if(!arquivo.isEmpty()) {
				byte[] bytes = arquivo.getBytes();
				Path caminho = Paths.get(caminhoImagens+String.valueOf(paciente.getId())+arquivo.getOriginalFilename());
				Files.write(caminho, bytes);
				
				paciente.setNomeImagem(String.valueOf(paciente.getId())+arquivo.getOriginalFilename());
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		try {
			repository.save(paciente);
			return ResponseEntity.status(HttpStatus.CREATED).body("Paciente cadastrado com sucesso.");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Algum campo já existe no banco de dados.");
		}
	}

	@PutMapping("/atualizar/{id}")
	@ResponseBody
	public ResponseEntity<?> atualizar(@RequestBody FichamedDTO user, @PathVariable Long id) {
		FichamedModel paciente = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Erro ao atualizar: paciente não encontrado com ID: " + id));
		if (repository.existsByCpf(paciente.getCpf())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("CPF já cadastrado.");
		}
		if (repository.existsByEmail(paciente.getEmail())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado.");
		}
		if (repository.existsByTelefone(paciente.getTelefone())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Número já cadastrado.");
		}
		

		paciente.setNome(user.nome());
		paciente.setEmail(user.email());
		paciente.setTelefone(user.telefone());
		paciente.setCpf(user.cpf());
		paciente.setNomeImagem(user.nomeImagem());
		repository.save(paciente);
		return ResponseEntity.status(200).body("Paciente atualizado com sucesso.");
	}

	@DeleteMapping("/deletar/{id}")
	@ResponseBody
	public ResponseEntity<?> deletar(@PathVariable Long id) {
		try {
			repository.findById(id).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado com ID: " + id));
			repository.deleteById(id);
			return ResponseEntity.status(200).body("Paciente deletado com sucesso.");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getReason());
		}

	}
}