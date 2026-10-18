# VideoMaster Downloader (Android)

Aplicativo Android nativo em Kotlin + Jetpack Compose, com interface escura premium, entrada de links, reconhecimento de domínio e downloads de URLs diretas de mídia para `Downloads` usando o `DownloadManager` do Android.

## Situação da versão 1.0.0

- Interface Compose com tema escuro, chips de plataforma, animações e links recentes em memória. Design sem anúncios e sem SDKs publicitários.
- Recebe texto compartilhado por outros apps e links HTTPS.
- Reconhece links de YouTube, Instagram e TikTok.
- Baixa URLs diretas autorizadas com extensões comuns de vídeo/áudio.
- Abre links das plataformas no app/site oficial.
- Não tenta extrair URLs privadas de páginas, contornar DRM, autenticação ou restrições de acesso.
- A extração/conversão para MP3 ainda não está implementada.
- Links do TikTok recebem orientação explícita: baixar sem marca-d’água só é possível quando uma versão limpa autorizada é disponibilizada pela plataforma ou pelo titular; não há remoção automática de marca-d’água nesta versão.

## Limites importantes

As APIs oficiais do YouTube, Instagram e TikTok não oferecem um endpoint universal para baixar qualquer vídeo de qualquer conta nem uma garantia de arquivo sem marca-d’água para todos os links. O app não promete baixar qualquer link social nem remover marcas-d’água automaticamente. Recursos e permissões dependem da plataforma, do tipo de conteúdo, do titular e das credenciais aprovadas. Antes de implementar integrações específicas, configure credenciais oficiais e valide os termos atuais de cada API. Não inclua tokens secretos no aplicativo Android.

## Compilar

Requisitos: JDK 17, Android SDK Platform 35 e Gradle 8.9.

```bash
gradle assembleDebug
```

O APK de depuração será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Compilação automática no GitHub

O workflow `.github/workflows/android.yml` compila o APK a cada push para `main`/`master` ou manualmente em Actions e publica o arquivo como artefato `VideoMaster-debug-apk`.

## Atualizações sem desinstalar

Para instalar atualizações sobre a versão anterior, mantenha o mesmo `applicationId` e assine todas as versões distribuídas com a mesma chave de assinatura. O APK de CI é de depuração e é adequado para testes; para distribuição pública, configure uma chave de release guardada fora do repositório (GitHub Actions Secrets) e um processo de release assinado.
