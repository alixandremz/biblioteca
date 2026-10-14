package com.alixandremz.biblioteca;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.alixandremz.biblioteca.repository.AutorRepository;
import com.alixandremz.biblioteca.repository.EmprestimoRepository;
import com.alixandremz.biblioteca.repository.LivroRepository;

@SpringBootTest
@AutoConfigureMockMvc
class BibliotecaApiTests {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AutorRepository autores;

    @Autowired
    private LivroRepository livros;

    @Autowired
    private EmprestimoRepository emprestimos;

    @BeforeEach
    void limpar() {
        emprestimos.deleteAll();
        livros.deleteAll();
        autores.deleteAll();
    }

    private long extrairId(String resposta) {
        Matcher matcher = ID.matcher(resposta);
        matcher.find();
        return Long.parseLong(matcher.group(1));
    }

    private long criarAutor(String nome) throws Exception {
        String resposta = mvc.perform(post("/autores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"nacionalidade\":\"Brasil\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extrairId(resposta);
    }

    private long criarLivro(String titulo, String isbn, long autorId) throws Exception {
        String resposta = mvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"" + titulo + "\",\"isbn\":\"" + isbn
                                + "\",\"anoPublicacao\":2020,\"autores\":[" + autorId + "]}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extrairId(resposta);
    }

    private long emprestar(long livroId, String leitor) throws Exception {
        String resposta = mvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"livroId\":" + livroId + ",\"nomeLeitor\":\"" + leitor + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extrairId(resposta);
    }

    @Test
    void criaAutorComStatus201() throws Exception {
        mvc.perform(post("/autores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Machado de Assis\",\"nacionalidade\":\"Brasil\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Machado de Assis"));
    }

    @Test
    void criaLivroComAutor() throws Exception {
        long autorId = criarAutor("Machado de Assis");
        mvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Dom Casmurro\",\"isbn\":\"111\",\"anoPublicacao\":1899,\"autores\":["
                                + autorId + "]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.disponivel").value(true))
                .andExpect(jsonPath("$.autores.length()").value(1))
                .andExpect(jsonPath("$.autores[0].nome").value("Machado de Assis"));
    }

    @Test
    void buscaLivroInexistenteRetorna404() throws Exception {
        mvc.perform(get("/livros/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void emprestaLivroEDeixaIndisponivel() throws Exception {
        long autorId = criarAutor("Autor");
        long livroId = criarLivro("Livro A", "100", autorId);

        emprestar(livroId, "João");

        mvc.perform(get("/livros/{id}", livroId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false));
    }

    @Test
    void naoEmprestaLivroJaEmprestado() throws Exception {
        long autorId = criarAutor("Autor");
        long livroId = criarLivro("Livro A", "100", autorId);
        emprestar(livroId, "João");

        mvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"livroId\":" + livroId + ",\"nomeLeitor\":\"Maria\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void devolveLivroETornaDisponivel() throws Exception {
        long autorId = criarAutor("Autor");
        long livroId = criarLivro("Livro A", "100", autorId);
        long emprestimoId = emprestar(livroId, "João");

        mvc.perform(post("/emprestimos/{id}/devolucao", emprestimoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.devolvidoEm").exists());

        mvc.perform(get("/livros/{id}", livroId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(true));
    }

    @Test
    void naoDevolveDuasVezes() throws Exception {
        long autorId = criarAutor("Autor");
        long livroId = criarLivro("Livro A", "100", autorId);
        long emprestimoId = emprestar(livroId, "João");
        mvc.perform(post("/emprestimos/{id}/devolucao", emprestimoId)).andExpect(status().isOk());

        mvc.perform(post("/emprestimos/{id}/devolucao", emprestimoId))
                .andExpect(status().isConflict());
    }

    @Test
    void emprestaLivroInexistenteRetorna404() throws Exception {
        mvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"livroId\":9999,\"nomeLeitor\":\"João\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void livroSemAutoresRetorna400() throws Exception {
        mvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Sem autor\",\"isbn\":\"200\",\"anoPublicacao\":2020,\"autores\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.autores").exists());
    }

    @Test
    void autorInexistenteAoClarLivroRetorna404() throws Exception {
        mvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Livro\",\"isbn\":\"300\",\"anoPublicacao\":2020,\"autores\":[9999]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void isbnDuplicadoRetorna409() throws Exception {
        long autorId = criarAutor("Autor");
        criarLivro("Livro A", "400", autorId);

        mvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Livro B\",\"isbn\":\"400\",\"anoPublicacao\":2021,\"autores\":["
                                + autorId + "]}"))
                .andExpect(status().isConflict());
    }

    @Test
    void listaELetraPorTituloComPaginacao() throws Exception {
        long autorId = criarAutor("Autor");
        criarLivro("Dom Casmurro", "500", autorId);
        criarLivro("Memórias Póstumas", "501", autorId);
        criarLivro("Quincas Borba", "502", autorId);

        mvc.perform(get("/livros").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.conteudo.length()").value(2));

        mvc.perform(get("/livros").param("titulo", "borba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("Quincas Borba"));
    }

    @Test
    void listaHistoricoDeEmprestimos() throws Exception {
        long autorId = criarAutor("Autor");
        long livroId = criarLivro("Livro A", "600", autorId);
        long emprestimoId = emprestar(livroId, "João");
        mvc.perform(post("/emprestimos/{id}/devolucao", emprestimoId)).andExpect(status().isOk());

        mvc.perform(get("/livros/{id}/emprestimos", livroId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nomeLeitor").value("João"))
                .andExpect(jsonPath("$[0].devolvidoEm").exists());
    }
}
