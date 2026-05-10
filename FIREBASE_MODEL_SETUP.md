# Firebase Model Upload Instructions for Raitha Varta

To complete the crop disease detection setup, you need to upload your trained TensorFlow Lite model to Firebase Console:

## Step 1: Prepare Your Model
1. Ensure you have a trained TensorFlow Lite model (.tflite file) for crop disease detection
2. The model should classify the following diseases:
   - Healthy
   - Blight
   - Brown Spot
   - Leaf Curl
   - Mosaic Virus
   - Powdery Mildew
   - Rust
   - Yellow Vein Mosaic

## Step 2: Upload to Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `raithra-varta`
3. In the left sidebar, navigate to **ML Kit** → **Custom Models**
4. Click **Add custom model**
5. Enter model name: `crop_disease_model`
6. Upload your .tflite file
7. (Optional) Upload labels file if you want to store labels in Firebase too
8. Click **Publish**

## Step 3: Verify Model Availability
After publishing, the model will be available for download by your app using:
- Model name: `crop_disease_model`
- Download type: `LOCAL_MODEL_UPDATE_IN_BACKGROUND` (available over any network)

## Step 4: Test the Implementation
Once the model is uploaded:
1. Run the app on an Android device/emulator
2. Navigate to the Crop Disease Detection screen
3. Take or select a crop image
4. Click "Analyze with AI"
5. The app will download the model from Firebase (if not cached) and run inference
6. Results will show disease name, severity, confidence percentage, and treatment recommendations

## Network Behavior Notes:
- The app now allows model downloads over both WiFi and mobile data for better accessibility
- Model downloads still occur in the background to avoid blocking UI
- Background updates ensure farmers always have the latest disease detection model
- Fallback to local asset model ensures functionality even without internet connectivity