# 📱 Agente de IA para Relatórios de Manutenção (Android Nativo - Kotlin + Jetpack Compose)

Aplicativo Android nativo de alto nível desenvolvido em **Kotlin** com a biblioteca **Jetpack Compose** e **Google Gemini SDK Multimodal**.

---

## 🎯 Principais Recursos

1. **Tipos de Manutenção Suportados**:
   - 🔴 **Corretiva**: Diagnóstico de causa raiz, tempo de parada (*downtime*), ações executadas e peças trocadas.
   - 🟢 **Preventiva**: Checklists operacionais, status dos componentes, agendamento de inspeções.
   - 🔵 **Preditiva**: Análise de sintomas (vibração, termografia, ultrassom, óleo) e curva P-F.
   - 🟣 **Partida Técnica / Comissionamento**: Start-up de equipamentos novos, parâmetros nominais vs. medidos, testes de carga e termo de garantia.

2. **Entradas Multimodais no Celular**:
   - **Fotos**: Captura direta via Câmera/Galeria de peças danificadas, placas e painéis.
   - **Documentos & Escopos**: Anexo de arquivos PDF/DOCX de referência.
   - **Ditado por Voz**: Integração de microfone para relato viva-voz durante a inspeção.

3. **Biblioteca de Modelos & Escopos da Empresa**:
   - Cadastre padrões específicos (ex: *Padrão Petrobras*, *Padrão Start-up*, *Padrão Inspeção Simplificada*).
   - O Agente de IA lê o escopo e garante que a saída siga exatamente o seu padrão visual e técnico.

4. **Gerador e Exportador em PDF Nativo**:
   - Gera um PDF profissional com cabeçalho da empresa, tabela de dados do ativo, badges de severidade (*Baixo, Médio, Alto, Crítico*), galeria de fotos e bloco de assinatura técnica.
   - **Compartilhamento com 1 Toque**: Envio direto em PDF via WhatsApp, E-mail ou Telegram.

---

## 🛠️ Como Abrir e Compilar no Android Studio

1. Abra o **Android Studio**.
2. Clique em **Open** e selecione a pasta:
   `/home/lucas/AgenteManutencaoAndroid`
3. Aguarde a sincronização do Gradle (`Sync Project with Gradle Files`).
4. Conecte seu celular Android via cabo USB (com Depuração USB ativa) ou use um Emulador.
5. Clique no botão **Run (Shift + F10)**.

### Para Gerar o Arquivo `.apk` Instalável:
No Android Studio, acesse o menu superior:
> **Build** > **Build APK(s)** / **Generate Signed Bundle / APK**
O arquivo `.apk` será gerado na pasta `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🔑 Como Inserir sua Chave API do Google Gemini (Opcional)

1. No aplicativo no celular, vá na aba de **Configurações** (ícone de engrenagem).
2. Cole sua chave gratuita obtida no [Google AI Studio](https://aistudio.google.com/app/apikey).
3. Caso você esteja sem a chave, o app possui um **motor de fallback inteligente** que continuará gerando relatórios com base em modelos locais.
