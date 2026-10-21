package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.dto.cliente.*;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.DuplicateResourceException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.ClienteMapper;
import com.zheiry.barbersystem.repositories.ClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClienteSaidaDto cadastroCliente(ClienteDto dto) throws BadRequestException{

        // Verifica se o e-mail informado já está em uso
        if (repository.existsByEmail(dto.email())){
            throw new DuplicateResourceException("E-mail já existente!");
        }
        // Verifica se o telefone informado já pertence a outro barbeiro.
        if (repository.existsByTelefone(dto.telefone())){
            throw new DuplicateResourceException("Telefone já existente!");
        }

        // Valida se a senha bate com o campo de confirmação de senha.
        if (!dto.senha().equals(dto.confirmaSenha())){
            throw new BadRequestException("Senha não condiz com a confirmação de senha!");
        }

        // MAPEAMENTO: Converte os dados do DTO de entrada para a Entidade de banco de dados
        ClienteEntity entity = ClienteMapper.toEntity(dto);

        // SEGURANÇA: Criptografa a senha antes de salvar no banco
        String senhaCriptografada = passwordEncoder.encode(dto.senha());
        entity.setSenha(senhaCriptografada);

        // REGRA DE NEGÓCIO: Define por padrão o perfil do usuário como Cliente
        entity.setNivelAcesso(NivelAcesso.CLIENTE);

        // REGRA DE NEGÓCIO: Define por padrão o perfil do usuário como ATIVO
        entity.setStatus(Status.ATIVO);

        // PERSISTÊNCIA: Grava o novo barbeiro no banco de dados e recupera a entidade salva (com ID gerado)
        ClienteEntity salva = repository.save(entity);

        // RETORNO: Converte a entidade salva para o DTO de saída e o retorna para a camada superior (Controller)
        return ClienteMapper.toSaidaDto(salva);
    }


    // Lista para o Administrador (DTO completo)
    public List<ClienteSaidaDto> listarPorStatusAdmin(Status status) {
        List<ClienteEntity> clientes = repository.findByStatus(status);
        return ClienteMapper.toSaidaDtoList(clientes);
    }

    // Lista para o Barbeiro (só ATIVO + DTO resumido)
    public List<ClienteSaidaResumoDto> listarAtivosParaBarbeiro() {
        List<ClienteEntity> clientes = repository.findByStatus(Status.ATIVO);
        return ClienteMapper.toSaidaResumoDtoList(clientes);
    }



    // Buscar por Nome.
    // ADMINISTRADO - Lista completa.
    // BARBEIRO - Lista com informacoes necessarias.
    public List<?> buscarPorNome(String nome) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_BARBEIRO"));

        if (!isStaff) {
            throw new AccessDeniedException("Apenas administrador ou barbeiro podem buscar clientes por nome!");
        }

        List<ClienteEntity> clientes = repository.findByNomeContainingIgnoreCase(nome);

        boolean isBarbeiro = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BARBEIRO"));

        if (isBarbeiro){
            return ClienteMapper.toSaidaResumoDtoList(clientes);
        }

        return ClienteMapper.toSaidaDtoList(clientes);
    }


    // Atualizar informações de cadastro.
    @Transactional
    public ClienteSaidaDto atualizarInformacoesCliente(Long id, ClienteAtualizaDto dto){
        // Busca o cliente. se nao existir lança excessao.
        ClienteEntity cliente = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));
        // Pega o email do usuario autenticado (vem do JWT).
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        // Só permite atualizar se o email for o mesmo do cliente.
        if (!Objects.equals(cliente.getEmail(), emailLogado)) {
            throw new AccessDeniedException("Acesso negado!");
        }
        // Valida se o novo email ja esta em uso por outro usuario.
        if (dto.email() != null && repository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new DuplicateResourceException("O e-mail informado já está sendo utilizado por outro Usuário.");
        }
        // Valida se o telefone ja esta em uso por outro usuario.
        if (dto.telefone() != null && repository.existsByTelefoneAndIdNot(dto.telefone(), id)) {
            throw new DuplicateResourceException("O telefone informado já está sendo utilizado por outro Usuário.");
        }
        // Aplica apenas os campos que vierem preenchidos do DTO.
        ClienteMapper.atualizarEntity(dto,cliente);
        // Salva e retorna Dto de saida.
        repository.save(cliente);
        return ClienteMapper.toSaidaDto(cliente);
    }

    // Alterar senha.
    @Transactional
    public void alterarSenha(Long id, ClienteAlteraSenhaDto dto){

        // Buscar cliente pelo ID.
        ClienteEntity cliente = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));
        // Pega o email do usuario logado (vem do JWT).
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        // Só permite se o email logado for o mesmo do cliente.
        if (!Objects.equals(cliente.getEmail(), emailLogado)) {
            throw new AccessDeniedException("Você só pode alterar sua propria senha!");
        }

        // Valida a senha atual.
        if (!passwordEncoder.matches(dto.senhaAtual(),cliente.getSenha())){
            throw new BadRequestException("Senha atual incorreta!");
        }
        // Valida a confirmação da nova senha.
        if (!dto.novaSenha().equals(dto.confirmaNovaSenha())){
            throw new BadRequestException("Nova senha e confirmação não conferem!");
        }
        // Impede senha igual á atual.
        if (passwordEncoder.matches(dto.novaSenha(),cliente.getSenha())){
            throw new BadRequestException("A nova senha deve ser diferente da senha atual!");
        }
        // Criptografa e salva a nova senha.
        cliente.setSenha(passwordEncoder.encode(dto.novaSenha()));
        repository.save(cliente);
    }

    // Desativar cliente - exclusão lógica
    @Transactional
    public void desativarCliente(Long id) {
        ClienteEntity cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        // Segurança: só o próprio cliente ou um ADMIN pode desativar
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")); // ajuste conforme seu JWT

        if (!cliente.getEmail().equals(emailLogado) && !isAdmin) {
            throw new AccessDeniedException("Você não tem permissão para desativar este cliente!");
        }

        // Já está inativo?
        if (cliente.getStatus() == Status.INATIVO) {
            throw new BadRequestException("Cliente já está desativado!");
        }

        cliente.setStatus(Status.INATIVO);
        repository.save(cliente);
    }

    // Reativar cliente - (controle do ADMINISTRADOR)
    @Transactional
    public void reativarCliente(Long id) {
        ClienteEntity cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        // Apenas ADMIN pode reativar
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (!isAdmin) {
            throw new AccessDeniedException("Apenas administradores podem reativar clientes!");
        }

        if (cliente.getStatus() == Status.ATIVO) {
            throw new BadRequestException("Cliente já está ativo!");
        }

        cliente.setStatus(Status.ATIVO);
        repository.save(cliente);
    }


}
