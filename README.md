# MediCare - Medication Reminder Android App

A clean and efficient medication reminder application built for Android using Java. MediCare helps users manage their medication schedules and set reliable reminders.

## Features

### 🏠 Main Dashboard
- Clean, medical-inspired interface with navigation cards
- Overview of active medicines and today's reminders
- Health statistics display (active medicines, reminders, adherence rate)
- Quick access to all main features

### 📊 Dashboard
- Personalized greeting based on time of day
- Statistics cards showing:
  - Active medicines count
  - Today's medicines
  - Reminders scheduled for today
  - Adherence rate tracking
- Upcoming reminders preview
- Complete list of all active medicines
- Medicine cards with detailed information and quick actions

### 💊 Medicine Management
- **Add New Medicines**: Comprehensive form with:
  - Medicine name and dosage
  - Frequency selection (once daily, twice daily, etc.)
  - Medicine type (tablet, capsule, liquid, etc.)
  - Custom reminder times with dynamic management
  - Start and end dates
  - Optional notes
- **Dynamic Reminder Times**: 
  - Automatic time suggestions based on frequency
  - Add/remove custom times
  - Visual time count tracking
- **Medicine Cards**: Display all medicine details with options to delete or view info


### 🔔 Smart Notifications
- Scheduled reminders based on medicine times
- Persistent notifications with medicine details
- Vibration patterns for attention
- Automatic rescheduling after device reboot
- Background service for reliable delivery

### 🗄️ Data Management
- SQLite database for local storage
- Pre-populated sample data (Aspirin, Vitamin D, Omega-3)
- Secure data handling
- Efficient queries and data operations

## Technical Architecture

### Core Components
- **MainActivity**: Main navigation hub with clean interface
- **DashboardActivity**: Medicine overview and statistics
- **AddMedicineActivity**: Medicine creation with dynamic forms

### Data Layer
- **Medicine Model**: Complete medication data structure
- **DatabaseHelper**: SQLite operations and sample data
- **Medicine Adapters**: RecyclerView adapters for medicine display

### Services & Utilities
- **ReminderScheduler**: Alarm management for notifications
- **MedicineReminderReceiver**: Notification handling
- **BootReceiver**: Reminder restoration after reboot

### UI Components
- Material Design theming
- Custom medical color palette (blues, cyans, purples)
- Responsive layouts for different screen sizes
- Card-based design with smooth interactions
- Time management interface for medicine schedules

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0+)
- Java 8+

### Installation
1. Clone or download the project files
2. Open Android Studio
3. Select "Open an Existing Project"
4. Navigate to the `MediCare` folder and select it
5. Wait for Gradle sync to complete
6. Run the app on an emulator or physical device

### Configuration
- **Notifications**: The app requests notification permissions on Android 13+
- **Alarms**: Uses exact alarms for precise reminder scheduling

## File Structure

```
MediCare/
├── app/
│   ├── build.gradle                 # App-level dependencies
│   └── src/main/
│       ├── AndroidManifest.xml      # App permissions and components
│       ├── java/com/medicare/app/
│       │   ├── MainActivity.java    # Main navigation activity
│       │   ├── DashboardActivity.java
│       │   ├── AddMedicineActivity.java
│       │   ├── AIAssistantActivity.java
│       │   ├── models/
│       │   │   ├── Medicine.java    # Medicine data model
│       │   │   └── ChatMessage.java
│       │   ├── database/
│       │   │   └── DatabaseHelper.java # SQLite operations
│       │   ├── adapters/            # RecyclerView adapters
│       │   ├── receivers/           # Notification receivers
│       │   └── utils/               # Helper utilities
│       └── res/
│           ├── layout/              # XML layouts
│           ├── values/              # Colors, strings, themes
│           └── drawable/            # Icons and backgrounds
├── build.gradle                    # Project-level configuration
├── settings.gradle                 # Module settings
└── README.md
```

## Key Features in Detail

### Medicine Scheduling
- **Flexible Frequencies**: Support for standard schedules (daily, twice daily, etc.) and custom intervals
- **Smart Time Management**: Automatic time suggestions with manual override capability
- **Visual Feedback**: Real-time count of scheduled times with validation

### AI Integration
- **Context-Aware**: AI responses consider user's current medication list
- **Medical Focus**: Specialized prompts for medication-related queries
- **Safety First**: Clear disclaimers about consulting healthcare providers
- **Mock Responses**: Built-in responses for demo purposes, easily replaceable with real AI API

### Notification System
- **Reliable Scheduling**: Uses Android's AlarmManager for precise timing
- **Persistent Reminders**: Survive app closure and device reboot
- **Rich Notifications**: Include medicine name, dosage, and scheduled time
- **User-Friendly**: Clear actions and easy dismissal

### Data Security
- **Local Storage**: All data stored locally using SQLite
- **No External Dependencies**: Core functionality works offline
- **Privacy Focused**: No personal health data transmitted without explicit user action

## Customization Options

### Theming
- Medical-grade color scheme with blue, cyan, and purple accents
- Material Design 3 components throughout
- Easy color customization in `colors.xml`
- Responsive design for tablets and phones

### Functionality Extensions
- Add medicine categories or drug classifications
- Implement medication interaction checking
- Add photo capture for medicine identification
- Integrate with health platforms or APIs
- Add family member management
- Implement dose tracking and adherence analytics

## Dependencies

- **AndroidX Libraries**: Core Android components
- **Material Design**: UI components and theming
- **RecyclerView**: Efficient list displays
- **CardView**: Medicine card layouts
- **Gson**: JSON parsing for data operations

## Safety & Compliance

- **Medical Disclaimers**: Clear warnings about consulting healthcare providers
- **No Medical Advice**: App provides information only, not medical recommendations
- **Privacy Focused**: Local data storage, no cloud synchronization by default
- **Accessibility**: Designed with accessibility guidelines in mind

## Future Enhancements

- **Cloud Backup**: Optional data synchronization
- **Wearable Integration**: Smartwatch notifications
- **Barcode Scanning**: Medicine identification
- **Prescription Import**: Parse doctor prescriptions
- **Family Sharing**: Multiple user profiles
- **Analytics Dashboard**: Advanced adherence tracking
- **Healthcare Provider Integration**: Share data with doctors
- **Voice Commands**: Hands-free operation

This comprehensive medication reminder app provides a solid foundation for personal health management while maintaining focus on user safety and data privacy.