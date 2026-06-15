# About SpartanAI Media

SpartanAI Media is the result of a rigorous, AI-driven development lifecycle targeting a high-performance Android media streaming ecosystem. The application was constructed under a "YOLO" (Zero-Interruption) mandate, demonstrating the power of autonomous architectural decisions, comprehensive implementation, and continuous self-correction.

## Design Philosophy

- **Zero-Debt Implementation:** Instead of accumulating legacy hacks, the application resolves root issues immediately. Every component from the Database (Room) to the Dependency Injection framework (Koin) was carefully curated to work harmoniously on modern Android environments.
- **Privacy as a Primitive:** Media consumption must be private. Features like "Anonymous Shadows", biometric security, and 100% untraceable background downloads ensure that the user's data belongs solely to them. 
- **Untraceable & Censorship Resistant:** Media downloads completely bypass Android's system `DownloadManager`, preventing OS-level tracking and notifications. Files are stored as obfuscated binary blobs in internal storage. The entire network layer supports Tor routing (via SOCKS5) and custom HTTP proxies, enabling users to bypass geographical and network restrictions silently.
- **Decentralized Capabilities:** With AES-GCM encrypted P2P file transfers and serverless synchronization logic, the app empowers users to share and experience media securely and locally, without relying on centralized, surveillance-heavy servers.

## Technical Highlights

- **UI Layer:** Jetpack Compose with Material 3, heavily customized for a cinematic, immersive look (Bento grids, immersive player views with double-tap to seek, and fluid animations).
- **Domain Layer:** Kotlin Flows drive a purely reactive pipeline. From search queries to network discovery and download progress, every state change is propagated instantly to the UI without blocking the main thread.
- **Data Layer:** 
  - Room Database with SQLCipher for encrypted at-rest storage.
  - OkHttp WebSockets for real-time Watch Party signaling.
  - Custom OkHttp Coroutine Pipeline for stealth downloads, masking IP via Tor/SOCKS5 proxies and spoofing User-Agent headers.
  - Jsoup for parsing legacy web portals, with dynamic fallback mechanisms.
  - `CipherOutputStream` for secure local file sharing via TCP sockets.
  - Android `NsdManager` for seamless peer discovery on local networks.

Built by NOVA at SpartanAI.
