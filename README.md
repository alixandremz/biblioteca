# biblioteca

API REST de **biblioteca** feita para **estudar Spring Boot e Spring Web**.
Foco deste projeto: **relacionamentos entre entidades** no JPA e uma **regra de negócio
com estado** — um livro só pode ser emprestado se estiver na estante, e precisa ser
devolvido antes de sair de novo.

Tem três entidades: **Autor**, **Livro** e **Empréstimo**.

---

## O que estou estudando aqui

| Área | Conceitos praticados |
|------|----------------------|
| Relacionamentos JPA | `@ManyToMany` (livro ↔ autor, unidirecional com tabela de junção `livro_autor`) e `@ManyToOne` (empréstimo → livro) |
| Regra de negócio | livro indisponível não pode ser emprestado; empréstimo aberto precisa ser devolvido |
| Concorrência | lock pessimista (`@Lock(PESSIMISTIC_WRITE)` = `SELECT ... FOR UPDATE`) para evitar dois empréstimos simultâneos do mesmo livro |
| Spring Web (REST) | `@RestController`, `@GetMapping`/`@PostMapping`, `@PathVariable`, `@RequestBody`, `@RequestParam`, `ResponseEntity`, códigos HTTP (200, 201, 400, 404, 409) |
| Spring Data JPA | `JpaRepository`, query derivada `findByTituloContainingIgnoreCase`, `@Query` com `@Lock` |
| Paginação | `Pageable`, `PageRequest`, `Sort`, `Page`, invólucro `PaginaResponse` |
| Bean Validation | `@NotBlank`, `@Size`, `@NotNull`, `@NotEmpty`, `@Min`/`@Max`, `@Valid`, `@Validated` |
| Tratamento de erros | `@RestControllerAdvice`, `@ExceptionHandler`, `409 Conflict` para regra de negócio e violação de banco |
| DTOs | `record` de request/response, separados da entidade |
| Testes | `@SpringBootTest`, `MockMvc`, `jsonPath`, fluxo completo de empréstimo e devolução |

---

## Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC, Spring Data JPA, Bean Validation
- H2 em memória
- Maven (via Maven Wrapper `./mvnw`, não precisa ter Maven instalado)

---

## Modelo de dados

```
Autor  ──(muitos-para-muitos)──  Livro  ──(um-para-muitos)──  Emprestimo
         tabela livro_autor              tabela emprestimos (livro_id)
```

- **Autor**: `id`, `nome`, `nacionalidade`.
- **Livro**: `id`, `titulo`, `isbn` (único), `anoPublicacao`, `disponivel` (true = na estante),
  e a lista de `autores` (`@ManyToMany`).
- **Emprestimo**: `id`, `livro` (`@ManyToOne`), `nomeLeitor`, `emprestadoEm` e
  `devolvidoEm` (null enquanto o empréstimo está em aberto).

O estado do livro e do empréstimo andam juntos: emprestar marca `disponivel = false` e cria
um empréstimo com `devolvidoEm = null`; devolver marca `disponivel = true` e preenche `devolvidoEm`.

---

## Endpoints

| Método | Caminho | Descrição | Sucesso | Erros |
|--------|---------|-----------|---------|-------|
| GET | `/autores` | lista autores | `200` | |
| GET | `/autores/{id}` | busca um autor | `200` | `404` |
| POST | `/autores` | cria um autor | `201` | `400` |
| GET | `/livros` | lista paginada; filtro opcional por `titulo` | `200` | `400` |
| GET | `/livros/{id}` | busca um livro | `200` | `404` |
| POST | `/livros` | cria um livro (com ids de autores) | `201` | `400`, `404`, `409` |
| GET | `/livros/{id}/emprestimos` | histórico de empréstimos do livro | `200` | `404` |
| POST | `/emprestimos` | empresta um livro | `201` | `400`, `404`, `409` |
| POST | `/emprestimos/{id}/devolucao` | devolve um livro | `200` | `404`, `409` |

### Criar autor

```bash
curl -X POST http://localhost:8080/autores \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Machado de Assis","nacionalidade":"Brasil"}'
```

### Criar livro

`autores` recebe os ids de autores já cadastrados (pelo menos um).

```bash
curl -X POST http://localhost:8080/livros \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Dom Casmurro","isbn":"978-85-359-0277-5","anoPublicacao":1899,"autores":[1]}'
```

### Listar com paginação e busca

| Parâmetro | Padrão | Regra |
|-----------|--------|-------|
| `titulo` | (vazio) | filtra por parte do título, sem diferenciar maiúsculas |
| `pagina` | `0` | `>= 0` |
| `tamanho` | `10` | entre `1` e `100` |

```bash
curl "http://localhost:8080/livros?titulo=casmurro&pagina=0&tamanho=5"
```

```json
{
  "conteudo": [
    { "id": 1, "titulo": "Dom Casmurro", "isbn": "978-85-359-0277-5",
      "anoPublicacao": 1899, "disponivel": true,
      "autores": [ { "id": 1, "nome": "Machado de Assis", "nacionalidade": "Brasil" } ] }
  ],
  "pagina": 0, "tamanho": 5, "total": 1, "totalPaginas": 1
}
```

### Emprestar

```bash
curl -X POST http://localhost:8080/emprestimos \
  -H 'Content-Type: application/json' \
  -d '{"livroId":1,"nomeLeitor":"João"}'
```

Se o livro já estiver emprestado (`409 Conflict`):

```json
{ "erro": "livro indisponível (já emprestado): id 1", "campos": {} }
```

### Devolver

```bash
curl -X POST http://localhost:8080/emprestimos/1/devolucao
```

### Histórico do livro

```bash
curl http://localhost:8080/livros/1/emprestimos
```

---

## Decisões e detalhes técnicos

- **`@ManyToMany` só de um lado (unidirecional).** O `Livro` é o "dono" do relacionamento
  e controla a tabela de junção `livro_autor`. O `Autor` não conhece os livros. Isso evita
  ter que manter os dois lados sincronizados e é suficiente para o que a API faz.
- **Disponibilidade como estado, não como cálculo.** O `Livro` guarda `disponivel`
  (true/false). É atualizado junto da criação/devolução do empréstimo na **mesma
  transação**, então não há janela em que um diga uma coisa e o outro diga outra.
- **Lock pessimista para o empréstimo.** Na hora de emprestar, o livro é carregado com
  `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`). Se duas requisições tentarem
  emprestar o mesmo livro ao mesmo tempo, a segunda espera a primeira terminar e depois vê
  `disponivel = false` — sem vender o mesmo livro duas vezes.
- **`409 Conflict` para regra e banco.** Livro indisponível e empréstimo já devolvido são
  regras de negócio (409). ISBN duplicado é uma violação de `unique` no banco, capturada
  como `DataIntegrityViolationException` e também devolvida como 409.
- **DTO versão `LivroResponse` inclui os autores.** A entidade `Livro` tem `autores` como
  `LAZY`; a conversão para DTO acontece dentro da transação (`@Transactional`), então o
  carregamento preguiçoso funciona. *(Ponto de atenção: em páginas grandes isso vira o
  clássico problema **N+1** — dá para resolver com `@EntityGraph`/`join fetch`, ou com
  `@BatchSize`. Fica como exercício.)*
- **Busca e paginação** iguais ao projeto de agenda: `findByTituloContainingIgnoreCase`
  para o filtro e `PageRequest.of(pagina, tamanho, Sort.by("titulo"))`.

---

## Como rodar

Usa H2 em memória; os dados somem ao parar a aplicação.

```bash
cd biblioteca
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Console do H2 em
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:biblioteca`, usuário `sa`, sem senha).

Também há um `Dockerfile` (multi-stage, usuário sem privilégios):

```bash
docker build -t biblioteca .
docker run -p 8080:8080 biblioteca
```

---

## Testes

```bash
./mvnw test
```

Cobrem:

- criação de autor e de livro (com autores) e `Location`;
- busca de livro inexistente (`404`);
- emprestar deixa o livro indisponível; segundo empréstimo → `409`;
- devolver torna o livro disponível; devolver duas vezes → `409`;
- emprestar livro inexistente → `404`;
- livro sem autores → `400`; autor inexistente no cadastro → `404`;
- ISBN duplicado → `409`;
- listagem paginada e busca por título;
- histórico de empréstimos do livro.

---

## Próximos passos (ideias para continuar estudando)

- Resolver o **N+1** com `@EntityGraph` ou `@BatchSize` e comparar as queries no log.
- Prazo de devolução + multa por atraso (mais uma regra com estado).
- Devolução de vários livros de um leitor de uma vez.
- Trocar H2 por **PostgreSQL** + `docker-compose` e **Testcontainers** nos testes.
- Migrations com **Flyway**.
- **Spring Security** para separar bibliotecário (empresta/devolve) de leitor (consulta).
