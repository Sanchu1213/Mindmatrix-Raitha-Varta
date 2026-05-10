import os

crops = {
    "paddy": {"name": "Paddy", "icon": "R.drawable.rice"},
    "wheat": {"name": "Wheat", "icon": "R.drawable.wheat"},
    "ragi": {"name": "Ragi", "icon": "R.drawable.ragi"},
    "maize": {"name": "Maize", "icon": "R.drawable.corn"},
    "bajra": {"name": "Bajra", "icon": "R.drawable.cereal"},
    "jowar": {"name": "Jowar", "icon": "R.drawable.cereal"},
    "tomato": {"name": "Tomato", "icon": "R.drawable.tomato"},
    "onion": {"name": "Onion", "icon": "R.drawable.onion"},
    "potato": {"name": "Potato", "icon": "R.drawable.potato"},
    "brinjal": {"name": "Brinjal", "icon": "R.drawable.brinjal"},
    "okra": {"name": "Okra", "icon": "R.drawable.okra"},
    "carrot": {"name": "Carrot", "icon": "R.drawable.carrot"},
    "spinach": {"name": "Spinach", "icon": "R.drawable.spinach"},
    "drumstick": {"name": "Drumstick", "icon": "R.drawable.drumstick"},
    "cabbage": {"name": "Cabbage", "icon": "R.drawable.cabbage"},
    "red_chilli": {"name": "Red Chilli", "icon": "R.drawable.red_chilli"},
    "mango": {"name": "Mango", "icon": "R.drawable.mango"},
    "banana": {"name": "Banana", "icon": "R.drawable.banana"},
    "pomegranate": {"name": "Pomegranate", "icon": "R.drawable.pomegranate"},
    "grapes": {"name": "Grapes", "icon": "R.drawable.grapes"},
    "papaya": {"name": "Papaya", "icon": "R.drawable.papaya"},
    "coconut": {"name": "Coconut", "icon": "R.drawable.coconuts"},
    "arecanut": {"name": "Areca nut", "icon": "R.drawable.arecanut"},
    "sugarcane": {"name": "Sugarcane", "icon": "R.drawable.sugarcane"},
    "coffee": {"name": "Coffee", "icon": "R.drawable.coffee"},
}

tips_templates = [
    {
        "category": "Pest Alert",
        "en": "{crop} Pest Control: Inspect leaves regularly.",
        "kn": "{crop} ಕೀಟ ನಿಯಂತ್ರಣ: ಎಲೆಗಳನ್ನು ನಿಯಮಿತವಾಗಿ ಪರಿಶೀಲಿಸಿ.",
        "weather": "Humid and cloudy.",
        "stage": "Vegetative stage. Watch for common borers/insects.",
        "action": "Spray neem oil (5ml/L) or recommended insecticide.",
        "reason": "Early detection prevents major crop loss.",
        "method": "Foliar spray during evening hours.",
        "priority": "High"
    },
    {
        "category": "Fertilizer Table",
        "en": "{crop} Nutrient Management: Time for top dressing.",
        "kn": "{crop} ಪೋಷಕಾಂಶ ನಿರ್ವಹಣೆ: ಟಾಪ್ ಡ್ರೆಸ್ಸಿಂಗ್ ಸಮಯ.",
        "weather": "Clear skies with moderate temperature.",
        "stage": "Active growth phase.",
        "action": "Apply NPK fertilizers as per soil health card.",
        "reason": "Boosts robust growth and higher yield.",
        "method": "Soil application near the root zone.",
        "priority": "Medium"
    },
    {
        "category": "Weather Data",
        "en": "{crop} Irrigation Advisory: Maintain adequate moisture.",
        "kn": "{crop} ನೀರಾವರಿ ಸಲಹೆ: ಸೂಕ್ತ ತೇವಾಂಶ ಕಾಪಾಡಿ.",
        "weather": "Dry spell expected for the next 5 days.",
        "stage": "Critical growth stage requiring water.",
        "action": "Irrigate the field immediately. Avoid water stress.",
        "reason": "Water stress at this stage severely reduces quality.",
        "method": "Drip or furrow irrigation.",
        "priority": "High"
    }
]

out = """package com.example.raitha_varta.data

import com.example.raitha_varta.R
import java.util.UUID

object MockDataset {
    val massiveMockDataset = listOf(
"""

for crop_id, data in crops.items():
    name = data["name"]
    icon = data["icon"]
    
    for i, t in enumerate(tips_templates):
        tip_id = f"{crop_id}_dataset_{i}"
        
        en_text = t["en"].format(crop=name)
        kn_text = t["kn"].format(crop=name)
        
        out += f"""        Tip(
            id = "{tip_id}",
            cropId = "{crop_id}",
            cropName = "{name}",
            category = "{t['category']}",
            instructionEn = "{en_text}",
            instructionKn = "{kn_text}",
            imageRes = {icon},
            weather = "{t['weather']}",
            stage = "{t['stage']}",
            action = "{t['action']}",
            reason = "{t['reason']}",
            farmingMethod = "{t['method']}",
            priority = "{t['priority']}"
        ),
"""

out += """    )
}
"""

with open(r"d:\Raitha_Varta\app\src\main\java\com\example\raitha_varta\data\MockDataset.kt", "w", encoding="utf-8") as f:
    f.write(out)

print("MockDataset.kt created successfully.")
