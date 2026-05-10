from fpdf import FPDF

class PDF(FPDF):
    def header(self):
        self.set_font('helvetica', 'B', 15)
        self.cell(0, 10, 'Raitha Varta - Comprehensive Project Analysis', border=False, align='C')
        self.ln(15)

    def chapter_title(self, title):
        self.set_font('helvetica', 'B', 12)
        self.set_fill_color(200, 220, 255)
        self.cell(0, 8, title, border=False, fill=True)
        self.ln(10)

    def chapter_body(self, body):
        self.set_font('helvetica', '', 11)
        # Using multi_cell instead of write for better text wrapping
        self.multi_cell(0, 6, body)
        self.ln()

# Create PDF
pdf = PDF()
pdf.add_page()
pdf.set_auto_page_break(auto=True, margin=15)

# 1. Project Overview
pdf.chapter_title('1. Project Overview: What it is & Why it exists')
overview_text = (
    "Raitha Varta (Farmer's News/Information) is an advanced, AI-powered Android mobile application "
    "designed to revolutionize modern agriculture for farmers, particularly in the state of Karnataka. "
    "\n\nWhy it exists:\n"
    "Farmers often face unpredictable challenges such as crop diseases, fluctuating market prices, and "
    "unreliable weather patterns. Traditional expert consultation is slow and often inaccessible. Raitha Varta "
    "solves this by putting a digital agricultural scientist in the farmer's pocket. It aims to bridge the "
    "information gap by providing real-time, highly localized, and bilingual (English and Kannada) guidance, "
    "helping farmers maximize yield and profitability."
)
pdf.chapter_body(overview_text)

# 2. Core Functions and Features
pdf.chapter_title('2. Core Functions & Features')
features_text = (
    "1. Expert Ask (Multimodal AI Consultant): Farmers can upload photos of their crops. The system uses "
    "Gemini 2.5 AI to instantly identify the crop, diagnose diseases, and provide a structured, bilingual report "
    "detailing organic and chemical treatments.\n\n"
    "2. Real-Time Market Prices: Integrates with Agmarknet (data.gov.in) to fetch live Mandi prices across "
    "Karnataka, displaying price trends and historical data to help farmers decide when and where to sell.\n\n"
    "3. AI Crop & Yield Prediction: Uses advanced AI logic to predict expected crop yields, analyze potential "
    "market demand, and forecast localized disease risks based on current weather and soil inputs.\n\n"
    "4. Crop Recommendation System: Suggests the most profitable crops to plant based on soil type, farm size, "
    "water availability, and seasonal data.\n\n"
    "5. Agricultural News & Schemes: Aggregates the latest farming news and government schemes, allowing farmers "
    "to stay updated and bookmark important articles."
)
pdf.chapter_body(features_text)

# 3. Technology Stack & Architecture
pdf.chapter_title('3. Technology Stack & Tools')
tech_text = (
    "The application follows a modern MVVM (Model-View-ViewModel) architecture.\n\n"
    "Frontend (Mobile App):\n"
    "- Platform: Android (Native)\n"
    "- Language: Kotlin\n"
    "- UI Framework: Jetpack Compose (Material Design 3)\n"
    "- Navigation: Jetpack Navigation Compose\n"
    "- Image Rendering: Coil and Glide\n\n"
    "Backend & Cloud Services:\n"
    "- Cloud Database: Firebase (Firestore, Realtime Database)\n"
    "- APIs: Ktor and Retrofit for remote network calls\n"
    "- AI Integration 1: Google Gemini GenAI SDK / REST API (Gemini 2.5 Flash & Pro) for image analysis and text generation.\n"
    "- AI Integration 2: OpenAI API (for predictive modeling and structural logic)\n"
    "- On-Device ML: TensorFlow Lite & Firebase ML Model Downloader\n\n"
    "Local Storage & State Management:\n"
    "- Database: Room Database (SQLite) for offline caching of market prices, chat history, and daily tips.\n"
    "- Asynchronous execution: Kotlin Coroutines and StateFlow.\n"
    "- Dependency Injection: Dagger-Hilt.\n\n"
    "Tools:\n"
    "- IDE: Android Studio\n"
    "- Build System: Gradle (Kotlin DSL)\n"
    "- Version Control: Git"
)
pdf.chapter_body(tech_text)

# 4. Datasets Used
pdf.chapter_title('4. Datasets & APIs Used')
dataset_text = (
    "The application relies on both static datasets and live remote APIs to function:\n\n"
    "1. my_farming_data.csv: A comprehensive historical dataset of farming records integrated into the project. "
    "It contains historical crop performance, soil compatibility, and weather conditions used to train or simulate "
    "the AI predictive models.\n\n"
    "2. MarketDataset.kt / MockDataset.kt: Internal fallback mock datasets that ensure the app remains fully "
    "functional even if the user loses internet connectivity or the primary API servers go down.\n\n"
    "3. Live Datasets (APIs):\n"
    "   - Agmarknet API (data.gov.in): Real-time daily market pricing data.\n"
    "   - OpenWeatherMap API: Real-time climate and weather data for the predictive engines."
)
pdf.chapter_body(dataset_text)

# 5. Conclusion
pdf.chapter_title('5. Development Summary')
conclusion_text = (
    "Raitha Varta is a highly sophisticated, production-ready Android application. By migrating away from legacy "
    "AI frameworks to robust direct REST API integrations with Gemini 2.5, the application achieves enterprise-level "
    "reliability. The seamless integration of Jetpack Compose for the UI, Room for offline capabilities, and "
    "multilingual support makes it an invaluable, highly scalable tool for empowering rural farmers."
)
pdf.chapter_body(conclusion_text)

# Output PDF
try:
    pdf.output('Raitha_Varta_Project_Analysis.pdf')
    print("PDF generated successfully.")
except Exception as e:
    print(f"Error generating PDF: {e}")
