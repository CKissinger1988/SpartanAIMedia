# SpartanAI Media

SpartanAI Media is a high-performance, ad-free streaming application built on modern Android principles. Designed with zero technical debt and adhering strictly to Clean Architecture, it delivers a premium, secure, and absolute privacy-first media experience.

## Core Features

- **Ad-Free Streaming Engine:** Utilizing `ExoPlayer` wrapped with a custom `ResolvingDataSource`, the app intercepts and blocks known ad-serving domains and trackers at the network level, ensuring pristine, uninterrupted playback.
- **100% Untraceable Downloads:** Completely bypasses the Android system `DownloadManager`. Media chunks are streamed securely via an OkHttp coroutine pipeline, saved as obfuscated binary `.blob` files in isolated, non-exported internal app storage (`filesDir`), and user-agents are spoofed to prevent system and network fingerprinting.
- **Tor & Proxychains Support:** Route all application traffic—including metadata scraping and untraceable downloads—through SOCKS5 or HTTP proxies, with native support for Tor networks via Orbot (port 9050).
- **P2P Encrypted Beaming:** Uses Android `NsdManager` for local peer discovery and raw TCP sockets with AES-GCM encryption to beam downloaded media files securely between devices without internet access, complete with real-time transfer progress.
- **Watch Party Sync:** A real-time, WebSocket-powered synchronized playback experience. Users can join virtual rooms, sync video playback, send messages, and share animated emoji reactions.
- **Intelligent Recommendations:** An AI-powered, history-aware recommendation engine that dynamically updates the "More Like This" shelves based on user interaction progress and genre affinities.
- **Biometric Security & Anonymous Profiles:** Protects sensitive watch history and downloads using device biometrics (`BIOMETRIC_STRONG`). Users can create "Anonymous Shadow" profiles that silo downloads and network configurations independently.

## Getting Started

1. Clone the repository: `git clone https://github.com/CKissinger1988/SpartanAIMedia.git`
2. Open the project in Android Studio (Jellyfish or newer recommended).
3. Build and Run: Make sure you use a device or emulator running API 31+ for full features (like Picture-in-Picture and Material You dynamic coloring).

## Build Requirements
- JDK 17+
- Android SDK API 37
- Gradle 8.x

## License
Confidential and Proprietary to SpartanAI. 
