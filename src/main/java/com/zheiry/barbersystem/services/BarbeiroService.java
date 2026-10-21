package com.zheiry.barbersystem.services;


import com.zheiry.barbersystem.dto.barbeiro.*;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.DuplicateResourceException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.BarbeiroMapper;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.exception.BadRequestException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BarbeiroService {

    private final BarbeiroRepository repository;
    private final PasswordEncoder passwordEncoder;

    public BarbeiroService(BarbeiroRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }


    //Realiza o cadastro de um novo barbeiro no sistema após validar todas as regras de negócio.
    // @param dto Objeto com os dados de entrada preenchidos no formulário (nome, e-mail, senhas, etc.).
    // @return BarbeiroSaidaDto Objeto de resposta sem dados sensíveis, pronto para a interface web.
    // @throws BadRequestException Lançada quando a senha e a confirmação de senha não coincidem.
    // @throws DuplicateResourceException Lançada quando o e-mail ou o telefone já constam no banco.
    //Método responsável por adicionar/cadastrar um barbeiro
    @Transactional // Garante atomicidade: se ocorrer erro, as alterações no banco são desfeitas (rollback)
    public BarbeiroSaidaDto cadastroBarbeiro(BarbeiroDto dto) throws BadRequestException {

        // Verifica se o e-mail informado já está em uso
        if(repository.existsByEmail(dto.email())){
            throw new DuplicateResourceException("E-mail já existente!");
        }

        // Verifica se o telefone informado já pertence a outro barbeiro.
        if (repository.existsByTelefone(dto.telefone())) {
            throw new DuplicateResourceException("Telefone já existente!");
        }

        // Valida se a senha bate com o campo de confirmação de senha.
        if (!dto.senha().equals(dto.confirmaSenha())){
            throw new BadRequestException("Senha não condiz com a confirmação de senha!");
        }

        // MAPEAMENTO: Converte os dados do DTO de entrada para a Entidade de banco de dados
        BarbeiroEntity entity = BarbeiroMapper.toEntity(dto);

        // SEGURANÇA: Criptografa a senha antes de salvar no banco
        String senhaCriptografada = passwordEncoder.encode(dto.senha());
        entity.setSenha(senhaCriptografada);

        // REGRA DE NEGÓCIO: Define por padrão o perfil do usuário como BARBEIRO
        entity.setNivelAcesso(NivelAcesso.BARBEIRO);

        // REGRA DE NEGÓCIO: Define por padrão o perfil do usuário como ATIVO
        entity.setStatus(Status.ATIVO);

        // PERSISTÊNCIA: Grava o novo barbeiro no banco de dados e recupera a entidade salva (com ID gerado)
        BarbeiroEntity salva = repository.save(entity);

        // RETORNO: Converte a entidade salva para o DTO de saída e o retorna para a camada superior (Controller)
        return BarbeiroMapper.toSaidaDto(salva);
    }


    // Lista para Administrador (Dto COMPLETO)
    public List<BarbeiroSaidaDto> listaPorStatusAdmin(Status status){
        List<BarbeiroEntity> barbeiros = repository.findByStatus(status);
        return BarbeiroMapper.toSaidaDtoList(barbeiros);
    }

    // Lista para Clientes e Barbeiros (Dto RESUMIDO)
    public List<BarbeiroSaidaResumoDto> listarAtivos(){
        List<BarbeiroEntity> barbeiros = repository.findByStatus(Status.ATIVO);
        return BarbeiroMapper.toSaidaResumoDtoList(barbeiros);
    }



    // Buscar por Nome
    public List<?> buscarPorNome(String nome){
        var auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_CLIENTE") || a.getAuthority().equals("ROLE_BARBEIRO"));

        if (!isStaff) {
            throw new AccessDeniedException("Acesso negado!");
        }

        List<BarbeiroEntity> barbeiros = repository.findByNomeContainingIgnoreCase(nome);

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if(isAdmin){
            return BarbeiroMapper.toSaidaDtoList(barbeiros);
        }
        return BarbeiroMapper.toSaidaResumoDtoList(barbeiros);
    }



    @Transactional
    public BarbeiroSaidaDto atualizarInformacoes(Long id, BarbeiroAtualizaDto dto){

        BarbeiroEntity barbeiro = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!Objects.equals(barbeiro.getEmail(), emailLogado)) {
            throw new AccessDeniedException("Acesso negado!");
        }

        if (dto.email() != null && repository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new DuplicateResourceException("O e-mail informado já está sendo utilizado por outro Usuário.");
        }

        if (dto.telefone() != null && repository.existsByTelefoneAndIdNot(dto.telefone(), id)) {
            throw new DuplicateResourceException("O telefone informado já está sendo utilizado por outro Usuário.");
        }

        BarbeiroMapper.atualizarEntity(dto, barbeiro);
        repository.save(barbeiro);
        return BarbeiroMapper.toSaidaDto(barbeiro);
    }

//    @Transactional
//    public BarbeiroSaidaDto atualizarInformacoesBarbeiro(BarbeiroAtualizaDto dto) {
//
//        String emailLogado = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getName();
//
//        BarbeiroEntity barbeiro = repository.findByEmail(emailLogado)
//                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));
//
//        // Validações de e-mail e telefone (iguais às suas)
//        if (dto.email() != null && repository.existsByEmailAndIdNot(dto.email(), barbeiro.getId())) {
//            throw new DuplicateResourceException("O e-mail informado já está sendo utilizado por outro Usuário.");
//        }
//
//        if (dto.telefone() != null && repository.existsByTelefoneAndIdNot(dto.telefone(), barbeiro.getId())) {
//            throw new DuplicateResourceException("O telefone informado já está sendo utilizado por outro Usuário.");
//        }
//
//        BarbeiroMapper.atualizarEntity(dto, barbeiro);
//        repository.save(barbeiro);
//
//        return BarbeiroMapper.toSaidaDto(barbeiro);
//    }


//    @Transactional
//    public BarbeiroSaidaDto atualizarInformacoesBarbeiro(Long id, BarbeiroAtualizaDto dto) {
//
//        // Busca o barbeiro. Se não existir, lança 404.
//        BarbeiroEntity barbeiro = repository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));
//
//        // Pega o e-mail do usuário autenticado (vem do JWT)
//        String emailLogado = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getName();
//
//        // Só permite se o e-mail logado for o mesmo do barbeiro
//        if (!barbeiro.getEmail().equals(emailLogado)) {
//            throw new AccessDeniedException("Você só pode atualizar as suas próprias informações!");
//        }
//
//        // Valida se o novo e-mail já está em uso por outro usuário
//        if (dto.email() != null && repository.existsByEmailAndIdNot(dto.email(), id)) {
//            throw new DuplicateResourceException("O e-mail informado já está sendo utilizado por outro Usuário.");
//        }
//
//        // Valida se o novo telefone já está em uso por outro usuário
//        if (dto.telefone() != null && repository.existsByTelefoneAndIdNot(dto.telefone(), id)) {
//            throw new DuplicateResourceException("O telefone informado já está sendo utilizado por outro Usuário.");
//        }
//
//        // Aplica apenas os campos que vieram preenchidos no DTO
//        BarbeiroMapper.atualizarEntity(dto, barbeiro);
//
//        // Salva e retorna o DTO de saída
//        repository.save(barbeiro);
//        return BarbeiroMapper.toSaidaDto(barbeiro);
//    }


    @Transactional
    public void alterarSenha(Long id, BarbeiroAlteraSenhaDto dto) {

        // Busca o barbeiro pelo ID
        BarbeiroEntity barbeiro = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        // Pega o e-mail do usuário logado (vem do JWT)
        String emailLogado = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();   // retorna o e-mail (subject do token)

        // Só permite se o e-mail logado for o mesmo do barbeiro
        if (!Objects.equals(barbeiro.getEmail(), emailLogado)) {
            throw new AccessDeniedException("Você só pode alterar a sua própria senha!");
        }
        if (!barbeiro.getEmail().equals(emailLogado)) {
            throw new AccessDeniedException("Você só pode alterar a sua própria senha!");
        }

        // Valida a senha atual
        if (!passwordEncoder.matches(dto.senhaAtual(), barbeiro.getSenha())) {
            throw new BadRequestException("Senha atual incorreta!");
        }

        // Valida confirmação da nova senha
        if (!dto.novaSenha().equals(dto.confirmaNovaSenha())) {
            throw new BadRequestException("Nova senha e confirmação não conferem!");
        }

        // Impede senha igual à atual
        if (passwordEncoder.matches(dto.novaSenha(), barbeiro.getSenha())) {
            throw new BadRequestException("A nova senha deve ser diferente da senha atual!");
        }

        // Criptografa e salva
        barbeiro.setSenha(passwordEncoder.encode(dto.novaSenha()));
        repository.save(barbeiro);
    }


    @Transactional
    public void desativarBarbeiro(Long id) {

        BarbeiroEntity barbeiro = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (!isAdmin){
            throw new AccessDeniedException("Você não tem permissão para desativar Barbeiro!");
        }

        // Evitar tentativas de desativar alguem que ja esta INATIVO
        if (barbeiro.getStatus() == Status.INATIVO) {
            throw new BadRequestException("Este barbeiro já esta desativado!");
        }

        //Realisa a exclusão lógica
        barbeiro.setStatus(Status.INATIVO);
        repository.save(barbeiro);
    }


    @Transactional
    public void reativarBarbeiro(Long id) {

        BarbeiroEntity barbeiro = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (!isAdmin){
            throw new AccessDeniedException("Você não tem permissão para ativar barbeiro!");
        }

        // Evitar reativar barbeiro que ja esta ATIVO
        if (barbeiro.getStatus() == Status.ATIVO) {
            throw new BadRequestException("Este barbeiro ja esta ativo!");
        }

        // Reativar barbeiro
        barbeiro.setStatus(Status.ATIVO);
        repository.save(barbeiro);
    }





}
