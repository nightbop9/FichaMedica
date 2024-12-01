package com.api.fichamed.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		// Verifica se já existe um paciente com o CPF (por exemplo)
		if (repository.existsByCpf(paciente.getCpf())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("CPF já cadastrado. Por favor, insira um CPF diferente.");
		}
		if (repository.existsByEmail(paciente.getEmail())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado. Por favor, insira um email diferente.");
		}
		if (repository.existsByTelefone(paciente.getTelefone())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Telefone já cadastrado. Por favor, insira um telefone diferente.");
		}

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
	public ResponseEntity<?> atualizar(
		@PathVariable Long id,
		@Valid FichamedDTO user,
		BindingResult result,
		@RequestParam(value = "file", required = false) MultipartFile arquivo) {

		// verificar se o paciente existe
		FichamedModel paciente = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Erro ao atualizar: paciente não encontrado com ID: " + id));

		// verificar conflitos de dados
		if (repository.existsByCpfAndIdNot(user.cpf(), id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("CPF já cadastrado.");
		}
		if (repository.existsByEmailAndIdNot(user.email(), id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado.");
		}
		if (repository.existsByTelefoneAndIdNot(user.telefone(), id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Número já cadastrado.");
		}

	// setando novos dados
		paciente.setNome(user.nome());
		paciente.setEmail(user.email());
		paciente.setTelefone(user.telefone());
		paciente.setCpf(user.cpf());

		try {
			// gerenciar a pasta de imagens
			File pasta = new File(caminhoImagens);
			if (!pasta.exists()) {
				pasta.mkdir();
			}

			//caso receba arquivo, salvar com o id do paciente
			//deletar imagem antiga
			if (arquivo != null && !arquivo.isEmpty()) {
				// Deletar a imagem antiga
				String imagemAntiga = paciente.getNomeImagem();
				if (imagemAntiga != null && !imagemAntiga.isBlank()) {
					Path caminhoImagemAntiga = Paths.get(caminhoImagens + imagemAntiga);
					Files.deleteIfExists(caminhoImagemAntiga); // Deleta a imagem antiga
				}

			//salvar nova
			if (arquivo != null && !arquivo.isEmpty()) {
				byte[] bytes = arquivo.getBytes();
				String nomeArquivo = String.valueOf(paciente.getId()) + arquivo.getOriginalFilename();
				Path caminho = Paths.get(caminhoImagens + nomeArquivo);
				Files.write(caminho, bytes);

				paciente.setNomeImagem(nomeArquivo);
			}
		}
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o arquivo.");
		}

		// salvar   alterações no banco
		try {
			repository.save(paciente);
			return ResponseEntity.status(HttpStatus.OK).body("Paciente atualizado com sucesso.");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Algum campo já existe no banco de dados.");
		}
	}
	
	@DeleteMapping("/deletar/{id}")
@ResponseBody
public ResponseEntity<?> deletar(@PathVariable Long id) {
    try {
        // 
        FichamedModel paciente = repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "paciente não encontrado com ID: " + id));

        // deletar a imagem associada ao paciente, se existir
        String imagemPaciente = paciente.getNomeImagem();
        if (imagemPaciente != null && !imagemPaciente.isBlank()) {
            Path caminhoImagem = Paths.get(caminhoImagens + imagemPaciente);
            try {
                Files.deleteIfExists(caminhoImagem);
                System.out.println("Imagem do paciente deletada: " + imagemPaciente);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao deletar a imagem do paciente.");
            }
        }

        // deletar o paciente no banco de dados
        repository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Paciente deletado com sucesso.");
    } catch (ResponseStatusException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getReason());
    }
}

@DeleteMapping("/deletar-em-massa")
public ResponseEntity<Map<String, String>> deletarEmMassa(@RequestBody List<Long> ids) {
    Map<String, String> response = new HashMap<>();

    try {
        if (ids == null || ids.isEmpty()) {
            response.put("message", "Nenhum ID fornecido.");
            return ResponseEntity.badRequest().body(response);
        }

        // Processa cada ID
        for (Long id : ids) {
            Optional<FichamedModel> pacienteOptional = repository.findById(id);

            if (pacienteOptional.isPresent()) {
                FichamedModel paciente = pacienteOptional.get();
                String imagemPaciente = paciente.getNomeImagem();

                if (imagemPaciente != null && !imagemPaciente.isBlank()) {
                    Path caminhoImagem = Paths.get(caminhoImagens + imagemPaciente);
                    try {
                        Files.deleteIfExists(caminhoImagem);
                    } catch (IOException e) {
                        response.put("message", "Erro ao deletar a imagem: " + imagemPaciente);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }
            }
        }

        // Deleta os pacientes do banco
        repository.deleteAllById(ids);

        response.put("message", "Pacientes deletados com sucesso.");
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        response.put("message", "Erro ao processar a requisição.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

}
