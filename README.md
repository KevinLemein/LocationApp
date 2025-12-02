# LocationApp - Setup Instructions

## Prerequisites
- Android Studio
- Android device with GPS

## Setup

1. **Open Terminal or Command Prompt**
```bash
   cd AndroidStudioProjects
```
2. **Clone the repository**
```bash
   git clone https://github.com/KevinLemein/LocationApp.git
```

2. **Open in Android Studio**
   - Open Android Studio
   - Click "Open" 
   - Select the cloned `LocationApp` folder
   - Wait for Gradle sync

3. **Run**
   - Connect your Android device via USB
   - Enable USB Debugging on your device
   - Click the green Run button (▶)
   - Grant location permissions when prompted
   - Go outside and walk around to see location updates

## Note
- Remeber to change your name
- Also the styling is shit, hio jifanyie

## Troubleshooting
- **Build fails**: File → Invalidate Caches and Restart
- **No location**: Enable GPS in device settings
