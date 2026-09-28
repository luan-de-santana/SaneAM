# ![ic_launcher_round.webp](app/src/main/res/mipmap-hdpi/ic_launcher_round.webp) SaneAM

[![License: GPL v3](https://shields.io)](https://gnu.org)
[![Kotlin](https://shields.io)](https://kotlinlang.org)
[![Android](https://shields.io)](https://developer.android.com/)


Um aplicativo Android desenvolvido em Kotlin para apoiar a iniciativa de controle de estoque da **COMPESA**, no âmbito da **CPR Agreste Meridional** em Garanhuns-PE, servindo simultaneamente como projeto prático para a faculdade **UNINTER (Centro Universitário Internacional)**, da graduação de **Análise e Desenvolvimento de Sistemas**.

## 📌 Sobre o Projeto

O **SaneAM** nasceu para resolver o problema de saber a quantidade real dos materiais no almoxarifado e em outros depósitos da Coordenação.

Este projeto foi construído unindo o rigor acadêmico da disciplina de **Trabalho Extensionista 02 (Tecnologia Aplicada à Inclusão Digital - Projeto)** com o impacto social real exigido por projetos comunitários.

### 🚀 Principais Funcionalidades
* **☑ Entrada:** Realizar a entrada de materiais nos depósitos.
* **☑ Saída:** registrar a saída de itens para as equipes realizarem os serviços em campo.
* **☑ Acerto:** Ajustar o estoque atual em decorrência de avarias.
* **☑ Inventário:** Relação atualizada da quantidade dos materiais.

---

## 🛠️ Tecnologias e Arquitetura

O aplicativo foi desenvolvido seguindo as práticas modernas recomendadas pela Google para o ecossistema Android, integrando uma infraestrutura em nuvem robusta e reativa:

* **☑ Linguagem:** Kotlin
* **☑ Arquitetura:** MVVM (Model-View-ViewModel)
* **☑ Asincronismo e Concorrência:** Kotlin Coroutines (para chamadas assíncronas eficientes e sem travamento da UI)
* **☑ Backend as a Service (BaaS):** [Supabase](https://supabase.com)
    * **Banco de Dados:** PostgreSQL acessado via API RESTful automatizada ([PostgREST](https://postgrest.org))
    * **Autenticação:** Google Sign-In integrado diretamente via Supabase Auth
* **☑ Interface (UI):** [XML View Binding]


---

## ⚙️ Como Executar o Projeto

Para rodar o projeto localmente em sua máquina, siga os passos abaixo:

1. **Pré-requisitos:** Certifique-se de ter o [Android Studio](https://android.com) instalado (versão Ladybug ou superior).
2. **Clonar o repositório:**
   ```bash
   git clone https://github.com/luan-de-santana/SaneAM.git
   ```
3. **Abrir o projeto:** Abra o Android Studio e selecione a pasta do projeto clonado.
4. **Sincronizar o Gradle:** Aguarde o Android Studio baixar as dependências (Build -> Make Project).
5. **Executar:** Conecte um dispositivo físico ou use um Emulador e clique no botão **Run (Play)**.

---

## 📐 Diagramas UML

Os diagramas PlantUML refletem a arquitetura e os fluxos funcionais do projeto:

* [Diagrama de classes](docs/uml/diagrama-de-classes.puml)
* [Diagrama de casos de uso](docs/uml/diagrama-de-casos-de-uso.puml)

---

## 👨‍💻 Autor (Projeto Acadêmico)

**✔ Luan Pimentel de Santana** - *graduando em Análise e Desenvolvimento de Sistemas e Agente de Saneamento* - [GitHub](https://github.com/luan-de-santana)

---

## 📄 Licença

Este projeto está licenciado sob a **Licença GNU GPLv3** - veja o arquivo [LICENSE](LICENSE) para mais detalhes.

> **Atenção:** Esta é uma licença *copyleft*. Qualquer cópia, modificação ou redistribuição deste código deve, obrigatoriamente, manter o código aberto e sob a mesma licença. O uso comercial em aplicativos de código fechado (pagos ou proprietários) **não é permitido**.
