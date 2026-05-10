import urllib.request
import json

with open('local.properties', 'r') as f:
    lines = f.readlines()
    
api_key = ""
for line in lines:
    if "GEMINI_API_KEY" in line:
        api_key = line.split('=')[1].strip()

url = f"https://generativelanguage.googleapis.com/v1beta/models?key={api_key}"

req = urllib.request.Request(url)
try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode('utf-8'))
        for model in data.get('models', []):
            if 'flash' in model.get('name', '').lower() or 'pro' in model.get('name', '').lower():
                print(f"{model.get('name')} - {model.get('supportedGenerationMethods')}")
except Exception as e:
    print(f"Error: {e}")
