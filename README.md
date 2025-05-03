# 📚 Oqu Toqu — next-generation educational assistant

**Oqu Toqu** is a mobile application built to simplify academic search and support students in accessing research content. The project integrates modern AI technologies, custom-built libraries, and a clean architecture for scalability and offline functionality.

---

## ✨ Features

- 📄 **AI Chat (Gemini)** — allows students to ask questions and receive detailed answers.
- 🔍 **Article Search** using **Google Scholar API (SerpAPI)** with display of key data (title, description, DOI).
- 📥 **PDF fetching** via custom [`scihubparser`](https://github.com/QuanyshK/scihubparser) library.
- 🧠 **Clean Architecture** — clear separation into `data`, `domain`, and `app` layers for testability and maintainability.
- 🔌 **Integration with Django backend** — handles user management and server-side logic.
- 🔐 **Firebase Authentication (Google Sign-In)** — fast and secure login.
- 🧪 **Unit testing** across layers using `mockk` and `kotlinx.coroutines.test`.
- 🌐 **Offline mode** — messages are cached with Room when offline and synced later.
- 🧩 **Koin Dependency Injection** — simple and scalable DI.
- 🧭 **Jetpack Compose UI** — modern declarative UI approach.

---

## ⚠️ Disclaimer on Sci-Hub

This project uses the [`scihubparser`](https://github.com/QuanyshK/scihubparser) library **strictly for educational and demonstrational purposes**.  
We **do not encourage or endorse** bypassing paywalls or violating copyright laws.

> Using Sci-Hub may be illegal in your country and could pose security and ethical risks.  
> You are solely responsible for your actions if you choose to use it.

---

## 🛠️ Tech Stack

| Category              | Technology / Library                                |
|----------------------|------------------------------------------------------|
| UI                   | Jetpack Compose, Material 3                          |
| Architecture         | Clean Architecture, Koin                             |
| Authentication       | Firebase Auth (Google Sign-In), Django backend       |
| AI                   | Gemini API                                           |
| Article Search       | SerpAPI, [scihubparser](https://github.com/QuanyshK/scihubparser) |
| Database & Caching   | Room (SQLite)                                        |
| Networking           | Retrofit, OkHttp, LoggingInterceptor                 |
| Testing              | JUnit, mockk, kotlinx.coroutines.test                |

---

## 📸 Demo Video
