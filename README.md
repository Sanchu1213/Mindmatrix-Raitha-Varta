# ರೈತ ವಾರ್ತಾ — Raitha Varta 🌾

> **An AI-Powered Agricultural Companion for Karnataka Farmers**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Gemini AI](https://img.shields.io/badge/AI-Gemini%202.5-8E75B2?style=flat-square&logo=google&logoColor=white)](https://ai.google.dev)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-green?style=flat-square)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue?style=flat-square)](https://developer.android.com)

---

Raitha Varta ("Farmer's News" in Kannada) is a comprehensive Android application purpose-built for farmers in Karnataka, India. It bridges the gap between modern AI technology and grassroots agriculture by delivering real-time market prices, AI-powered crop disease detection, personalised expert consultations, agricultural tips, government scheme information, and farmer success stories — all in both **Kannada (ಕನ್ನಡ) and English**.

---

## 📸 App Gallery

Raitha Varta features a modern, intuitive interface available in both **English** and **Kannada (ಕನ್ನಡ)**.

### 🌟 Onboarding & Dashboard
| **English** | **Kannada** |
|:---:|:---:|
| ![EN Welcome](./screenshots/en_1_welcome.jpg) | ![KN Welcome](./screenshots/kn_1_welcome.jpg) |
| ![EN Dashboard](./screenshots/en_2_dashboard.jpg) | ![KN Dashboard](./screenshots/kn_2_dashboard.jpg) |

### 💡 Agricultural Tips & Market Prices
| **English** | **Kannada** |
|:---:|:---:|
| ![EN Tips](./screenshots/en_3_tips.jpg) | ![KN Tips](./screenshots/kn_3_tips.jpg) |
| ![EN Market](./screenshots/en_4_market.jpg) | ![KN Market](./screenshots/kn_4_market.jpg) |

### 🤖 AI Expert & Farmer Community
| **AI Disease Analysis** | **Farmer Community** |
|:---:|:---:|
| ![KN Expert](./screenshots/kn_6_expert_analysis.jpg) | ![KN Community](./screenshots/kn_7_community.jpg) |

### 🏆 Success Stories
| **English** | **Kannada** |
|:---:|:---:|
| ![EN Stories](./screenshots/en_5_stories.jpg) | ![KN Stories](./screenshots/kn_8_stories.jpg) |

---

## ✨ Features

### 🌾 Crop Dashboard
- Browse and filter **20+ Karnataka crops** (Paddy, Ragi, Maize, Tomato, Mango, Coffee, Areca Nut, Sugarcane, Coconut, and more)
- Full-text search with real-time filtering and crop-specific navigation
- Language toggle between **English** and **ಕನ್ನಡ** on every screen

### 🌤️ Weather Integration
- Live weather data powered by the **OpenWeatherMap API**
- Location-aware conditions (temperature, humidity, description)
- Graceful offline fallback with cached data

### 💰 Market Prices
- Real-time and historical **APMC market prices** across Karnataka districts
- Search by crop or market name (e.g., Bangalore, Mysore, Hubli)
- Min / Max / Modal price display with last-updated timestamps
- Data sourced from the **data.gov.in Agmarknet API** with a rich local dataset fallback
- Supports **Areca Nut, Coffee, Paddy, Ragi, Tomato, Onion**, and many more

### 🤖 Expert Ask — AI Agricultural Advisor
- **Dual-model Gemini 2.5 pipeline**:
  - *Gemini 2.5 Flash* — fast image identification (identifies crop + visible issue)
  - *Gemini 2.5 Flash* — generates a full bilingual structured expert report
- Upload a **crop photo from gallery or camera** for instant AI diagnosis
- Text-based Q&A with a Karnataka-specialised agricultural knowledge base
- Responses delivered in **English + Kannada side-by-side**
- Consultation history saved to a local **Room database** (rename / delete supported)

### 🔬 Crop Disease Detection
- **On-device TensorFlow Lite model** (Firebase ML Model Downloader)
- Detects 8 disease classes: Healthy, Blight, Brown Spot, Leaf Curl, Mosaic Virus, Powdery Mildew, Rust, Yellow Vein Mosaic
- Shows **confidence score, severity level**, full description, symptoms, chemical treatment, and organic remedy — all bilingual
- Model downloads silently in the background (WiFi + mobile data) and falls back to a bundled local model

### 🌱 Crop Recommendation (AI)
- Input soil type, season, water availability, and farm size
- Get AI-ranked crop suggestions with expected yield, profitability rating, and cultivation tips
- Bilingual output

### 📈 AI Crop Prediction
- Three prediction modes: **Yield Forecast**, **Market Price Outlook**, **Disease Risk Assessment**
- Context-aware predictions using the farmer's registered location and crop preference
- Instant bilingual summaries

### 📰 Agricultural News
- Latest agri-news feed fetched and cached locally via **Room**
- Tap any article for a full detail view with headline, body, and source
- WorkManager background refresh keeps the feed current

### 📋 Government Schemes
- Searchable list of central and Karnataka state agricultural schemes
- One-tap detail view with eligibility, benefits, and application information
- Fully offline — data is curated and bundled

### 👥 Farmer Community
- Simulated farmer forum powered by local mock data
- Post and browse community questions in Kannada and English
- AI-assisted answers for community queries

### 🏆 Farmer Success Stories
- Inspiring real-world case studies from Karnataka districts
- Rich story cards with profile, problems faced, steps taken, and outcomes
- "NEW" badge highlights recent additions

### ⚙️ Settings
- Choose primary crop and home district
- Persistent language preference (English / Kannada) via **DataStore**
- Dark mode toggle (follows system theme by default)
- Navigation shortcuts to all major features

---

## 🏗️ Architecture & Tech Stack

```
Raitha Varta
├── UI Layer        — Jetpack Compose + Material Design 3
├── ViewModel Layer — Hilt-injected ViewModels, StateFlow
├── Data Layer      — Room DB, DataStore, Retrofit, Firebase
└── AI / ML Layer   — Google Gemini SDK, TensorFlow Lite
```

### Core Technologies

| Category | Technology |
|---|---|
| **Language** | Kotlin |
| **UI Toolkit** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Unidirectional Data Flow |
| **DI** | Dagger Hilt |
| **Navigation** | Compose Navigation |
| **Local DB** | Room (with KSP annotation processing) |
| **Preferences** | Jetpack DataStore |
| **Networking** | Retrofit + OkHttp + Kotlinx Serialization |
| **HTTP Client (AI)** | Ktor (Client + Android + OkHttp engines) |
| **AI — Text & Vision** | Google Generative AI SDK (Gemini 2.5 Flash) |
| **ML — On-device** | TensorFlow Lite + Firebase ML Model Downloader |
| **Image Loading** | Coil Compose + Glide Compose |
| **Markdown Rendering** | compose-markdown |
| **Background Work** | WorkManager |
| **Analytics / DB** | Firebase Firestore + Realtime Database |
| **Build System** | Gradle (Kotlin DSL) + KSP |

---

## 📁 Project Structure

```
app/src/main/java/com/example/raitha_varta/
├── MainActivity.kt              # Single-activity host with NavHost + BottomNav
├── RaithaVartaApp.kt            # Hilt application class
│
├── data/                        # Data layer
│   ├── AppDatabase.kt           # Room database definition
│   ├── MarketRepository.kt      # Market price data source
│   ├── NewsRepository.kt        # News articles source
│   ├── TipRepository.kt         # Farming tips + Firebase tips
│   ├── SchemeRepository.kt      # Govt schemes data
│   ├── SettingsRepository.kt    # DataStore preferences
│   ├── WeatherRepository.kt     # OpenWeatherMap integration
│   ├── FirebaseTipModelManager.kt
│   ├── MarketDataset.kt         # Bundled APMC price data
│   ├── MockDataset.kt           # Offline fallback datasets
│   └── remote/
│       ├── GeminiApiService.kt  # Gemini REST API
│       ├── MarketApiService.kt  # data.gov.in market API
│       ├── WeatherApiService.kt # OpenWeatherMap API
│       └── NewsApiService.kt
│
├── di/                          # Hilt modules
│
├── ui/                          # Presentation layer
│   ├── AppLanguage.kt           # Bilingual string definitions (EN + KN)
│   ├── HomeScreen.kt            # Daily tips dashboard
│   ├── MarketScreen.kt          # APMC market prices
│   ├── SettingsScreen.kt        # App preferences
│   ├── WeatherScreen.kt         # Weather widget
│   ├── NewsScreen.kt / NewsDetailScreen.kt
│   ├── SchemeScreen.kt          # Govt schemes
│   ├── crop/                    # Crop selection & cards
│   ├── disease/                 # TFLite disease detection
│   ├── expert/                  # Gemini AI chat + image analysis
│   ├── prediction/              # AI yield/market/disease prediction
│   ├── recommendation/          # Gemini crop recommendation
│   ├── community/               # Farmer community forum
│   ├── stories/                 # Success stories
│   ├── home/                    # Welcome / onboarding screen
│   ├── components/              # Shared composable components
│   └── theme/                   # Color, typography, Material theme
│
├── viewmodel/
│   └── CropViewModel.kt
│
├── util/                        # Utility classes
└── worker/                      # WorkManager workers
```

---

## 🔑 API Keys & Configuration

All secrets are resolved at build time via `local.properties` (or environment variables / Gradle properties). **Never commit** `local.properties` to version control.

Create or update `local.properties` in the project root:

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk

# Required API Keys
GEMINI_API_KEY=your_gemini_api_key_here
WEATHER_API_KEY=your_openweathermap_api_key_here
DATA_GOV_IN_API_KEY=your_data_gov_in_api_key_here
AGMARKNET_RESOURCE_ID=your_agmarknet_resource_id_here
PREDICTION_API_KEY=your_prediction_api_key_here
```

### Where to get each key

| Key | Service | URL |
|---|---|---|
| `GEMINI_API_KEY` | Google AI Studio | https://aistudio.google.com/app/apikey |
| `WEATHER_API_KEY` | OpenWeatherMap | https://openweathermap.org/api |
| `DATA_GOV_IN_API_KEY` | data.gov.in | https://data.gov.in/user/register |
| `AGMARKNET_RESOURCE_ID` | Agmarknet dataset | https://data.gov.in/catalog/current-daily-price-various-commodities-various-markets-mandi |

> **Note:** The app includes bundled offline datasets for market prices, tips, schemes, and success stories, so most features work without a live API key during development.

---

## 🔥 Firebase Setup

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app with package name `com.example.raitha_varta`
3. Download `google-services.json` and place it at `app/google-services.json`
4. Enable **Firestore**, **Realtime Database**, and **ML Model Hosting**

### Crop Disease TFLite Model Upload

See [`FIREBASE_MODEL_SETUP.md`](./FIREBASE_MODEL_SETUP.md) for step-by-step instructions to upload your trained `.tflite` model.

The model must be named **`crop_disease_model`** in Firebase ML and should classify:

| Class | Description |
|---|---|
| Healthy | No disease detected |
| Blight | Rapid browning and wilting |
| Brown Spot | Circular lesions with yellow halos |
| Leaf Curl | Curling from leaf edges inward |
| Mosaic Virus | Mottled green patterns |
| Powdery Mildew | White powdery surface growth |
| Rust | Orange-brown pustules |
| Yellow Vein Mosaic | Yellowing of leaf veins |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 11** or higher
- **Android SDK** with API level 24–35
- A physical or virtual Android device running **Android 7.0+**

### Build & Run

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/Raitha_Varta.git
cd Raitha_Varta

# 2. Add your API keys to local.properties (see above)

# 3. Add google-services.json to app/

# 4. Open in Android Studio and Run
# OR build from command line:
./gradlew assembleDebug
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

---

## 🌐 Supported Languages

| Language | Code | Coverage |
|---|---|---|
| English | `en` | 100% — all UI strings |
| ಕನ್ನಡ (Kannada) | `kn` | 100% — all UI strings + AI responses |

Language preference is toggled in-app and persisted across sessions via Jetpack DataStore.

---

## 📱 Supported Crops

The app provides crop-specific tips, disease detection, and market prices for:

| Crop | ಕನ್ನಡ |
|---|---|
| Paddy | ಭತ್ತ |
| Ragi | ರಾಗಿ |
| Maize | ಮೆಕ್ಕೆಜೋಳ |
| Sugarcane | ಕಬ್ಬು |
| Tomato | ಟೊಮ್ಯಾಟೊ |
| Brinjal | ಬದನೆ |
| Okra | ಬೆಂಡೆ |
| Chilli | ಮೆಣಸಿನಕಾಯಿ |
| Onion | ಈರುಳ್ಳಿ |
| Potato | ಆಲೂಗಡ್ಡೆ |
| Mango | ಮಾವು |
| Banana | ಬಾಳೆ |
| Coconut | ತೆಂಗು |
| Areca Nut | ಅಡಿಕೆ |
| Coffee | ಕಾಫಿ |
| Mulberry | ಹಿಪ್ಪುನೇರಳೆ |
| Cotton | ಹತ್ತಿ |
| Aloe Vera | ಅಲೋ ವೆರಾ |

---

## 🔒 Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | API calls, AI model download |
| `ACCESS_NETWORK_STATE` | Detect offline/online mode |
| `CAMERA` | Capture crop photos for disease analysis |
| `READ_EXTERNAL_STORAGE` | Pick images from gallery (Android ≤ 12) |
| `RECORD_AUDIO` | Voice input for expert queries |
| `ACCESS_FINE_LOCATION` | Auto-detect district for weather & market data |
| `ACCESS_COARSE_LOCATION` | Approximate location fallback |
| `POST_NOTIFICATIONS` | News/tip refresh notifications |

---

## 🗺️ Navigation Map

```
WelcomeScreen
    └─▶ CropSelectionScreen ──▶ HomeScreen (Tips)
              │
    ┌─────────┼────────────────────────────────────┐
    ▼         ▼              ▼           ▼          ▼
MarketScreen  ExpertAskScreen  StoriesApp  Community  Settings
              │   (AI Chat)
              ├─▶ CropDiseaseScreen (TFLite)
              ├─▶ CropRecommendationScreen (Gemini)
              ├─▶ AiPredictionScreen (Gemini)
              └─▶ WeatherScreen
```

---

## 🔮 Future Improvements

- **Multilingual Expansion:** Add support for more regional languages (e.g., Telugu, Tamil) to assist farmers in neighboring states.
- **Offline ML Models:** Expand the local on-device capabilities for crop recommendation without internet dependency.
- **Direct Marketplace:** Implement a peer-to-peer selling platform allowing farmers to sell directly to consumers.
- **IoT Integration:** Connect with smart soil sensors for live farm-specific data.
- **Video Content:** Add agricultural tutorial videos in the farmer community section.

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a **Pull Request**

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Hilt for all dependency injection
- Prefer `StateFlow` / `collectAsState()` for UI state
- Add bilingual (EN + KN) strings to `AppLanguage.kt` for any new UI copy

---

## 📄 License

```
MIT License

Copyright (c) 2026 Raitha Varta Project

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 🙏 Acknowledgements

- [Google Gemini API](https://ai.google.dev) — for the AI backbone powering expert consultations
- [Firebase](https://firebase.google.com) — for ML model hosting and real-time database
- [OpenWeatherMap](https://openweathermap.org) — for live weather data
- [data.gov.in / Agmarknet](https://agmarknet.gov.in) — for APMC commodity price datasets
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — for the modern UI framework
- Karnataka's farming community — the inspiration behind every feature

---

*Built with ❤️ for Karnataka's farmers — ಕರ್ನಾಟಕದ ರೈತರಿಗಾಗಿ ❤️ ನಿರ್ಮಿಸಲಾಗಿದೆ*
