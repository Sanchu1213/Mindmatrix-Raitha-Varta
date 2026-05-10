import csv

def generate_market_repo():
    commodity_map = {
        'Paddy(Dhan)(Common)': 'paddy',
        'Wheat': 'wheat',
        'Tomato': 'tomato',
        'Onion': 'onion',
        'Potato': 'potato',
        'Brinjal': 'brinjal',
        'Cabbage': 'cabbage',
        'Carrot': 'carrot',
        'Maize': 'maize',
        'Apple': 'apple',
        'Banana': 'banana',
        'Grapes': 'grapes',
        'Lemon': 'lemon',
        'Orange': 'orange',
        'Cotton': 'cotton',
        'Soyabean': 'soybean',
        'Green Chilli': 'green_chilli',
        'Coconut': 'coconut',
        'Arecanut(Betelnut/Supari)': 'arecanut',
        'Coffee': 'coffee'
    }

    records = []
    try:
        with open('my_farming_data.csv', 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                commodity = row.get('commodity')
                if commodity in commodity_map:
                    crop_id = commodity_map[commodity]
                    state = row.get('state', '').replace('"', '').replace("'", "")
                    district = row.get('district', '').replace('"', '').replace("'", "")
                    market = row.get('market', '').replace('"', '').replace("'", "")
                    min_p = row.get('min_price', '0')
                    max_p = row.get('max_price', '0')
                    modal_p = row.get('modal_price', '0')
                    
                    records.append(
                        f'MarketRecord("{crop_id}", "{state}", "{district}", "{market}", {min_p}, {max_p}, {modal_p})'
                    )
    except Exception as e:
        print("Error reading CSV:", e)
        return

    kotlin_code = """package com.example.raitha_varta.data

data class MarketRecord(
    val cropId: String,
    val state: String,
    val district: String,
    val market: String,
    val minPrice: Int,
    val maxPrice: Int,
    val modalPrice: Int
)

object MarketDataset {
    val allRecords = listOf(
""" + ",\n".join("        " + r for r in records) + """
    )
}
"""
    with open('app/src/main/java/com/example/raitha_varta/data/MarketDataset.kt', 'w', encoding='utf-8') as f:
        f.write(kotlin_code)

    print("Successfully generated MarketDataset.kt with all location data!")

if __name__ == "__main__":
    generate_market_repo()
