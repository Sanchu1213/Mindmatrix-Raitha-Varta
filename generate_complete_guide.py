from fpdf import FPDF

class PDF(FPDF):
    def header(self):
        self.set_font('helvetica', 'B', 16)
        self.cell(0, 10, 'Raitha Varta - Complete Project Understanding Guide', border=False, align='C')
        self.ln(12)

    def chapter_title(self, num, title):
        self.set_font('helvetica', 'B', 14)
        self.set_fill_color(200, 220, 255)
        self.cell(0, 8, f'{num}. {title}', border=False, fill=True)
        self.ln(10)

    def chapter_body(self, text):
        self.set_font('helvetica', '', 11)
        self.multi_cell(0, 6, text)
        self.ln()

pdf = PDF()
pdf.add_page()
pdf.set_auto_page_break(auto=True, margin=15)

# 1. Project Title
pdf.chapter_title('1', 'Project Title')
pdf.chapter_body('Raitha Varta\nSmart Farming Assistance Application')

# 2. What is Raitha Varta?
pdf.chapter_title('2', 'What is Raitha Varta?')
pdf.chapter_body('Raitha Varta is a smart agriculture mobile application developed to help farmers with:\n\n- Crop guidance\n- Farming tips\n- Weather/farming awareness\n- Crop-based information\n- Smart farming support\n- Farmer success stories\n- Multi-language support\n\nThe app is designed mainly for rural farmers to access useful farming knowledge easily through a mobile phone.')

# 3. Why This Project Was Created
pdf.chapter_title('3', 'Why This Project Was Created')
pdf.chapter_body('Problem Statement\nFarmers often face problems like:\n\n- Lack of proper crop guidance\n- Crop diseases\n- Low awareness of modern farming methods\n- Language barriers\n- Difficulty accessing expert farming information\n- Low digital literacy\n\nSolution\nRaitha Varta provides:\n\n- Simple farming tips\n- Crop-specific guidance\n- Easy navigation\n- Local language support\n- Smart farming awareness')

# 4. Main Features of the App
pdf.chapter_title('4', 'Main Features of the App')
features = [
    ('Daily Farming Tips', 'Helps farmers learn better farming practices daily. Displays tips in card format with image and short explanation. Example: "Use drip irrigation during summer to reduce water wastage."'),
    ('Crop Categories', 'Allows users to view crop-specific information. Categories include Paddy, Areca Nut, Coconut, Tomato, and others. Filters tips based on crop and displays related guidance.'),
    ('Success Stories', 'Motivates farmers using real-life examples. Shows farmer achievements, improvement methods, and encourages modern farming.'),
    ('Multi-Language Support', 'Helps rural farmers use the app easily. Languages: English, Kannada (future: others). Implemented via Android string resources.'),
    ('Firebase Integration', 'Stores and manages app data online - user data, tips database, crop information, images, syncing.'),
    ('Smart Farming Features', 'Promotes modern techniques - water-saving methods, organic farming awareness, soil care tips, irrigation suggestions.'),
    ('Expert Ask (Prototype/Future Enhancement)', 'Planned AI-based crop disease support. Upload image, detect disease, get suggestions. Currently UI only, AI model integration pending.'),
]
for name, desc in features:
    pdf.chapter_body(f'- {name}:\n  {desc}\n')

# 5. Frontend Development
pdf.chapter_title('5', 'Frontend Development')
pdf.chapter_body('Technologies Used:\n- XML\n- Android Studio\n- Kotlin (Java)\n\nResponsibilities:\n- User interface design and navigation\n- Card views, buttons, image handling\n- Crop display pages and language switching\n- Splash screen\n\nUI Components Used:\n- RecyclerView, CardView, ScrollView, Buttons, ImageView, TextView')

# 6. Backend Development
pdf.chapter_title('6', 'Backend Development')
pdf.chapter_body('Backend Used:\n- Firebase (Realtime Database / Firestore)\n\nResponsibilities:\n- Stores app data, retrieves farming tips, manages crop information, handles cloud storage, synchronizes real-time data')

# 7. Database Used
pdf.chapter_title('7', 'Database Used')
pdf.chapter_body('Firebase Database / Firestore stores:\n- Crop names\n- Farming tips\n- Images\n- Success stories\n- User information')

# 8. Dataset Used
pdf.chapter_title('8', 'Dataset Used')
pdf.chapter_body('Sources likely include:\n- CSV files (e.g., my_farming_data.csv)\n- Manually collected farming data\n- Government agriculture resources\n- Internet farming references\n\nDataset includes crop names, descriptions, farming methods, disease prevention tips, water management tips, etc.')

# 9. Tools & Technologies
pdf.chapter_title('9', 'Tools & Technologies Used')
pdf.chapter_body('''
| Technology | Purpose |
|------------|---------|
| Android Studio | App development |
| Firebase | Backend & Database |
| XML | UI design |
| Kotlin / Java | App logic |
| CSV Dataset | Crop data |
| AI APIs (experimental) | Expert Ask feature |
''')

# 10. Architecture Flow
pdf.chapter_title('10', 'Architecture Flow')
pdf.chapter_body('User opens the app -> Frontend displays UI -> App requests data from Firebase -> Firebase returns crop/tip data -> Frontend renders information to the user')

# 11. Challenges Faced
pdf.chapter_title('11', 'Challenges Faced')
pdf.chapter_body('- Firebase integration issues\n- AI API limitations (rate limits, model access)\n- Dataset formatting and consistency\n- UI responsiveness across devices\n- Multi-language implementation\n- Image handling for Expert Ask')

# 12. Future Enhancements
pdf.chapter_title('12', 'Future Enhancements')
pdf.chapter_body('- Full AI crop disease detection (Gemini/other models)\n- Voice assistant for farmers\n- Real-time weather integration\n- Market price prediction\n- Government scheme notifications\n- Chat support with experts\n- Offline mode\n- GPS-based farming suggestions')

# 13. Advantages of the Project
pdf.chapter_title('13', 'Advantages of the Project')
pdf.chapter_body('- Farmer-friendly UI\n- Easy access to farming knowledge\n- Supports digital agriculture\n- Encourages smart farming practices\n- Improves awareness\n- Multi-language accessibility')

# 14. Conclusion
pdf.chapter_title('14', 'Conclusion')
pdf.chapter_body('Raitha Varta is a smart agriculture assistance application designed to support farmers with crop guidance, daily farming tips, and smart agricultural practices. Combining Android development with Firebase cloud services, it offers an easy-to-use platform for rural users, aiming to improve farming awareness and encourage technology-driven agriculture.')

# Output PDF
output_path = 'Raitha_Varta_Complete_Guide.pdf'
pdf.output(output_path)
print(f"PDF generated successfully: {output_path}")
