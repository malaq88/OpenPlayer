# OpenPlayer

Player de música local para Android, open source, rápido e focado em privacidade. Reproduz arquivos de áudio do dispositivo sem depender de internet.

Repositório: [github.com/malaq88/OpenPlayer](https://github.com/malaq88/OpenPlayer)

## Funcionalidades

- Biblioteca organizada em abas: **Músicas**, **Artistas**, **Álbuns**, **Gêneros**, **Pastas** e **Playlists**
- Reprodução com fila contextual (a sequência respeita a tela de origem: álbum, artista, pasta, etc.)
- Mini player e tela de reprodução com controles completos
- Playlists personalizadas com seleção e salvamento de faixas
- Reprodução em segundo plano via Media3 (ExoPlayer)
- Interface em Jetpack Compose com tema laranja
- 100% offline — sem permissão de internet

## Requisitos

- Android 6.0 (API 23) ou superior
- Android Studio com SDK Platform 37
- JDK 17+ (JBR do Android Studio recomendado)

## Como executar

1. Clone o repositório:
   ```bash
   git clone https://github.com/malaq88/OpenPlayer.git
   cd OpenPlayer
   ```
2. Abra o projeto no Android Studio.
3. Sincronize o Gradle (**Sync Project with Gradle Files**).
4. Execute em um dispositivo ou emulador com músicas locais.
5. Conceda a permissão de áudio quando solicitado.

## Build via linha de comando

```bash
./gradlew assembleDebug
```

APK de debug: `app/build/outputs/apk/debug/app-debug.apk`

Build de release (com R8/ProGuard):

```bash
./gradlew assembleRelease
```

## Stack técnica

| Camada | Tecnologia |
|--------|------------|
| UI | Jetpack Compose, Material 3 |
| Arquitetura | MVVM |
| Player | Media3 / ExoPlayer |
| Biblioteca local | MediaStore |
| Playlists | Room |
| Navegação | Navigation Compose |
| Imagens | Coil |

## Permissões

| Permissão | Uso |
|-----------|-----|
| `READ_MEDIA_AUDIO` | Leitura de músicas (Android 13+) |
| `READ_EXTERNAL_STORAGE` | Leitura de músicas (Android 12 e inferior) |
| `POST_NOTIFICATIONS` | Notificações de reprodução (opcional) |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Reprodução em segundo plano |

O app **não** solicita acesso à internet.

## Estrutura do projeto

```
app/src/main/java/com/example/openplayer/
├── data/          # MediaStore, Room, repositórios
├── player/        # MusicPlaybackService, PlaybackConnection
├── ui/            # Telas Compose, ViewModels, navegação
└── util/          # Permissões, formatadores
```

## Publicação na Play Store

Antes de publicar, considere:

- Alterar o `applicationId` de `com.example.openplayer` para o pacote definitivo
- Usar `app/play_store_512.png` como ícone da loja
- Preparar política de privacidade (o app não coleta dados)

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

Copyright (c) 2026 Antonio Malaquias

## Contribuindo

Contribuições são bem-vindas. Abra uma issue ou envie um pull request com uma descrição clara das alterações.
