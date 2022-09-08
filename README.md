# BLE-Devices
Android application for Bluetooth Low Energy devices

Specifications:
  - minSdk 21
  - targetSdk 32
  - Java version > 1.8

IDE and System: 
  - Android Studio Chipmunk | 2021.2.1 Patch 1
  - Build #AI-212.5712.43.2112.8609683, built on May 18, 2022
  - Runtime version: 11.0.12+7-b1504.28-7817840 amd64
  - VM: OpenJDK 64-Bit Server VM by Oracle Corporation
  - Windows 10
  - GC: G1 Young Generation, G1 Old Generation
  - Cores: 2

Non-Bundled Plugins: 
  - com.nasller.CodeGlancePro (1.3.8-Last2021.3.x)
  - com.herbert.george.dart.extensions (0.0.1+3)
  - Gitflow (0.7.7)
  - org.ice1000.tt (0.11.0)
  - org.jetbrains.kotlin (212-1.7.10-release-333-AS5457.46)
  - com.developerphil.adbidea (1.6.6)
  - br.com.dynamiclight.android-master-tools (0.3)

Uses permission on Manifest
  <uses-permission android:name="android.permission.BLUETOOTH" />
  <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

- Non-MVC and MVVM model
- Simple implementation of RecyclerView, allows to get the device depending on the touch events handled by the RecyclerView adapter


TODO:
 - Improve the UI:
      - change icons depending on BLE device
      - change Main activity to fragment
      - adding fragments activities to show BLE characteristics
      - applying material design
      
 - Send data to BLE device 
 - Read data from BLE device 
 - Synchronize stats in case of Health, Hearing Aid, A2dp, Headset and HID profile
 - Add functionalities to modify the Bluetooth status
  
