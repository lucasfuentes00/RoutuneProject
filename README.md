# Routune 🎵

**Routine** is an Android application designed for developing musicians, offering a complete set of tools to support every stage of learning and practice.  
It allows you to organize practice sessions, provides rhythmic tools, assists with tuning, and includes interactive methods to strengthen musical ear training.

📺 **Demo video**: [Watch on YouTube](https://youtu.be/6gepTbVJLnA)

![Interface](RoutuneProject/assets/Interface.png)

---

## ✨ Features

### 🎯 Practice Routines
- Create and manage routines for different instruments.
- Store metadata about progress and development.
- Persistent settings to resume where you left off.
![Rutine](RoutuneProject/assets/Rutine.png)


### 🥁 Training Loops
Two sections:  
- **Search** – Find loops on **Freesound API** by keyword, preview, and play them in a loop.  
- **Sounds** – Load preselected drum loops by ID and play them at the project’s tempo.  
- Load your favorite drum loops to practice with different rhythms and speeds.
- Integration with [**Freesound API**](https://freesound.org/) to search and loop new sounds for inspiration.
- Tempo control synced with the metronome.
![Loops](RoutuneProject/assets/Loops.png)


### 🎸 Chromatic Tuner
Real-time pitch detection from the microphone, processed locally.  
Using **JTransforms** (FFT) and the **YIN algorithm**, the app detects the fundamental frequency, calculates the closest note, and shows its deviation in cents.  
A visual bar and color feedback (green for ±10 cents) help ensure precise tuning.

- Real-time pitch detection from the microphone.
- Fundamental frequency analysis via FFT.
- Helps tune any instrument accurately.
![Tuner](RoutuneProject/assets/Tuner.png)

### ⏱️ Metronome
- Fully adjustable: tempo, time signature, sound, and more.
- Smooth pendulum-style animation.
- Sound playback synchronized with BPM.
![Metronome](RoutuneProject/assets/Metronome.png)


### 🎮 Mini-games for Ear Training
- Interactive games to practice theoretical concepts and perfect pitch recognition.
![Games](RoutuneProject/assets/Games.png)

---

## 🛠️ External Libraries & APIs

### Core Architecture
- **ViewModel** – Manage UI-related data across configuration changes.
- **LiveData** – Observable data holder to update UI automatically.
- **SharedPreferences** – Persistent key-value storage.
- **Gson** – Convert objects to/from JSON.
- **RecyclerView** + custom **Adapter** – Efficient list rendering.

### Audio Processing & Playback
- **AudioRecord** – Real-time audio capture.
- **kotlinx.coroutines** – Background audio processing.
- **org.jtransforms.fft.DoubleFFT_1D** – Fast Fourier Transform for frequency analysis.
- **MediaPlayer** – Metronome sound playback with adjustable speed.
- **SoundPool** – Loop creation and management.

### Animation & UI
- **ObjectAnimator** + **AnimatorListenerAdapter** – Pendulum animation for metronome.
- **Handler** + **Looper** – Synchronize animations with tempo.
- **LinearInterpolator**, **RotateAnimation** – Smooth animations.

### APIs
- **Freesound API** – Search and download audio loops.

---
## 🎨 Theme Support
The app supports both light and dark themes, adapting automatically to the system setting. This ensures a consistent look, comfortable viewing in any lighting, and proper contrast for readability.
![Themes](RoutuneProject/assets/Themes.png)


## 📚 References & Credits

1. [JTransforms – Java FFT library](https://github.com/wendykierp/JTransforms)  
2. [LibGDX – AndroidSound.java](https://github.com/libgdx/libgdx/blob/master/backends/gdx-backend-android/src/com/badlogic/gdx/backends/android/AndroidSound.java)  
3. [Freesound API Documentation](https://freesound.org/docs/api/)  
4. [Stack Overflow – Base frequency detection with JTransforms](https://stackoverflow.com/questions/21464801/android-jtransforms-finding-and-understanding-base-frequency)  
5. [Chroma Project – Audio analysis tools](https://github.com/adrielcafe/chroma)  
6. [M. Délèze – Musical note frequency calculation](http://www.deleze.name/marcel/en/physique/musique/index.html)  
7. [Android Developers – MediaPlayer documentation](https://developer.android.com/reference/android/media/MediaPlayer)  
8. [Stack Overflow – Looping ObjectAnimator animations](https://stackoverflow.com/questions/13814503/how-to-loop-objectanimator-animation-in-android)  
9. [Freesound – Metronome sound 60 BPM](https://freesound.org/)  

---

## ▶️ Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/routine-app.git´´´

2. Open the project in Android Studio.

3. Add your Freesound API key in the configuration file.

4. Build and run the app on your device or emulator.

---

## 📄 License
This project is released under the MIT License.

---

## 🛠️ Version & Status
- **Current** version: Prototype  
This app is in prototype mode — features are functional but still under development and may change. 

Feedback and contributions are welcome!

---

Made with ❤️ and 🎶 for musicians in practice.