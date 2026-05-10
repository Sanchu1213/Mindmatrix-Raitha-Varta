import urllib.request
import json

with open('local.properties', 'r') as f:
    lines = f.readlines()
    
api_key = ""
for line in lines:
    if "GEMINI_API_KEY" in line:
        api_key = line.split('=')[1].strip()

# Test 1: Vision (gemini-2.5-flash)
url_flash = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={api_key}"
payload_flash = {
    "contents": [{"role": "user", "parts": [{"text": "Hello"}]}],
    "generationConfig": {"temperature": 0.4}
}
try:
    req = urllib.request.Request(url_flash, data=json.dumps(payload_flash).encode('utf-8'), headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as response:
        print("Flash OK:", json.loads(response.read().decode('utf-8'))['candidates'][0]['content']['parts'][0]['text'][:20])
except Exception as e:
    print(f"Flash Error: {e}")

# Test 2: Pro (gemini-2.5-pro)
url_pro = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key={api_key}"
payload_pro = {
    "contents": [{"role": "user", "parts": [{"text": "Hello"}]}],
    "generationConfig": {"temperature": 0.7, "maxOutputTokens": 8192}
}
try:
    req = urllib.request.Request(url_pro, data=json.dumps(payload_pro).encode('utf-8'), headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as response:
        print("Pro OK:", json.loads(response.read().decode('utf-8'))['candidates'][0]['content']['parts'][0]['text'][:20])
except Exception as e:
    print(f"Pro Error: {e}")
