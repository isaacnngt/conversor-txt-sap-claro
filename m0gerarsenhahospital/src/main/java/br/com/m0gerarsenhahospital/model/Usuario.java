// ===============================
// model/Usuario.java
// ===============================
package br.com.m0gerarsenhahospital.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @NotBlank(message = "Login é obrigatório")
    @Size(min = 3, max = 100, message = "Login deve ter entre 3 e 100 caracteres")
    @Column(name = "login_usuario", unique = true, nullable = false, length = 100)
    private String loginUsuario;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Column(nullable = false, length = 255)
    private String senha;

    @Column(name = "sn_administrador", nullable = false, length = 1)
    private char snAdministrador = 'N';

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "usuario_que_cadastrou")
    private Long usuarioQueCadastrou;

    @Column(nullable = false)
    private boolean ativo = true;

    // Construtores
    public Usuario() {}

    public Usuario(String email, String loginUsuario, String senha, char snAdministrador, Long usuarioQueCadastrou) {
        this.email = email;
        this.loginUsuario = loginUsuario;
        this.senha = senha;
        this.snAdministrador = snAdministrador;
        this.usuarioQueCadastrou = usuarioQueCadastrou;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLoginUsuario() { return loginUsuario; }
    public void setLoginUsuario(String loginUsuario) { this.loginUsuario = loginUsuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public char getSnAdministrador() { return snAdministrador; }
    public void setSnAdministrador(char snAdministrador) { this.snAdministrador = snAdministrador; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public Long getUsuarioQueCadastrou() { return usuarioQueCadastrou; }
    public void setUsuarioQueCadastrou(Long usuarioQueCadastrou) { this.usuarioQueCadastrou = usuarioQueCadastrou; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    // Métodos utilitários
    public boolean isAdministrador() {
        return this.snAdministrador == 'S';
    }

    public String getTipoUsuario() {
        return this.snAdministrador == 'S' ? "Administrador" : "Comum";
    }

    // Métodos de ciclo de vida JPA
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (dataCriacao == null) {
            dataCriacao = now;
        }
        if (dataAtualizacao == null) {
            dataAtualizacao = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", loginUsuario='" + loginUsuario + '\'' +
                ", snAdministrador=" + snAdministrador +
                ", ativo=" + ativo +
                ", dataCriacao=" + dataCriacao +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;

        Usuario usuario = (Usuario) o;

        return id != null ? id.equals(usuario.id) : usuario.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}