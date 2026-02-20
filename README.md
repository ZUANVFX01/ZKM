<div align="center">
  <img src="./logo/logo.jpg" width="160" height="160" alt="ZKM Corporate Logo" style="border-radius: 24px; box-shadow: 0 8px 32px rgba(0,80,198,0.15);"/>
  
  <h1 style="font-weight: 800; letter-spacing: -0.5px;">ZUAN KERNEL MANAGER</h1>
  
  <p style="font-size: 1.2em; color: #666; font-weight: 500; margin-top: -10px;">
    Enterprise-Grade Android System Optimization Suite
  </p>

  <p style="margin-top: 20px;">
    <a href="LICENSE">
      <img src="https://img.shields.io/badge/License-GPL%20v3-0050C6?style=for-the-badge&logo=gnu&logoColor=white" alt="License"/>
    </a>
    <img src="https://img.shields.io/badge/Platform-Android%2010%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform"/>
    <img src="https://img.shields.io/badge/Root%20Access-Required-EF5350?style=for-the-badge&logo=magisk&logoColor=white" alt="Root Required"/>
    <img src="https://img.shields.io/badge/Version-1.0.0%20Stable-FF9800?style=for-the-badge&logo=semantic-release&logoColor=white" alt="Release"/>
    <img src="https://img.shields.io/badge/Maintained-Zuan%20Technologies-0050C6?style=for-the-badge&logo=github&logoColor=white" alt="Maintainer"/>
  </p>
</div>

---

## 🏢 Corporate Profile

**Zuan Kernel Manager (ZKM)** merupakan solusi manajemen kernel Android enterprise-class yang dikembangkan oleh Zuan Technologies. Dibangun di atas fondasi solid dari arsitektur Rve Kernel Manager, ZKM telah berevolusi menjadi platform optimasi sistem yang komprehensif untuk perangkat Android rooted.

### Our Philosophy
> *"Empowering Performance Through Precision Engineering"*

Kami percaya bahwa kontrol mendalam terhadap sistem mobile tidak harus mengorbankan keamanan, stabilitas, atau pengalaman pengguna. ZKM menggabungkan engineering precision dengan desain modern untuk menghadirkan solusi enterprise yang accessible.

---

## 📋 Executive Summary

| Attribute | Specification |
|-----------|---------------|
| **Product Category** | System Utility & Kernel Management |
| **Target Platform** | Android 10 (API 29) - Android 15 (API 35) |
| **Architecture Support** | ARM64, ARMv7, x86_64 |
| **Root Solutions** | Magisk (24.0+), KernelSU (0.9+), APatch |
| **License Model** | Open Source (GPL-3.0) |
| **Maintenance Status** | Actively Maintained |

---

## 📸 Product Showcase

<div align="center">
  <table>
    <tr>
      <td align="center"><b>System Dashboard</b><br><sub>Real-time Monitoring</sub></td>
      <td align="center"><b>SoC Analytics</b><br><sub>Hardware Telemetry</sub></td>
      <td align="center"><b>Power Management</b><br><sub>Battery Optimization</sub></td>
      <td align="center"><b>Kernel Configuration</b><br><sub>Advanced Tuning</sub></td>
    </tr>
    <tr>
      <td><img src="https://github.com/user-attachments/assets/fa6ede6c-6231-469f-a2fc-a3fd7aea2f3e" width="200" style="border-radius: 12px;"/></td>
      <td><img src="https://github.com/user-attachments/assets/7ada5790-4b74-4de0-954b-9a11c99c408c" width="200" style="border-radius: 12px;"/></td>
      <td><img src="https://github.com/user-attachments/assets/a8e32f06-4bae-4b79-a66f-c758bd4a3ca5" width="200" style="border-radius: 12px;"/></td>
      <td><img src="https://github.com/user-attachments/assets/339c6ada-7e91-44f1-807d-b5088b7eeec0" width="200" style="border-radius: 12px;"/></td>
    </tr>
  </table>
</div>

---

## ✨ Key Features

ZKM menawarkan suite tools komprehensif yang dikategorikan dalam modul intuitif:

### 🎨 Appearance & User Interface
* **Material 3 Expressive Design** - Interface modern dengan responsive layout dan adaptive design
* **LogsView System** - Advanced log reading dengan komponen UI dinamis dan filtering capabilities
* **Themes & Visual Effects** - Fluid transitions, glassmorphism blur effects (Haze integration), dan optimized layouts untuk berbagai screen density

### ⚙️ Performance & Kernel Control
* **Enterprise Dashboard** - Monitoring real-time untuk SoC temperatures, CPU frequencies, RAM utilization, dengan data logging historis
* **CPU/GPU Tuning** - Comprehensive governor control, min/max frequency management, dan boost configuration profiles
* **Memory Management** - LMK (Low Memory Killer) tweaks, virtual memory tuning, ZRAM compression settings, dan swap management
* **Thermal & Display Control** - Deep integration dengan device thermal drivers, brightness curve calibration, refresh rate management
* **Battery & Doze Optimization** - Wakelock analysis and blocking, charging cycle control, deep sleep optimization, dan idle drain prevention

### 🛠️ Advanced System Utilities
* **Dual-Engine Kernel Flasher** - Flashing system mendukung **Horizon Logic** dan **Capntrips Architecture** dengan A/B partition support
* **Secure Terminal Emulator** - Built-in root shell dengan command history, scripting capabilities, dan environment variables management
* **Dex2oat Compiler** - On-device APK optimization untuk improved runtime performance
* **KsuWebUI Integration** - Embedded WebUI server untuk KernelSU module management tanpa browser eksternal
* **System Modding Suite** - Build.prop editor dengan syntax validation, SetEdit integration untuk database editing
* **Application Management** - Activity launcher, system app debloating dengan whitelist protection, dan disable/enable controls
* **Real-time Monitoring** - On-screen FPS counter, process resource tracking, dan system load monitoring

---

## 🔧 Technical Requirements

### Minimum System Requirements
- **Operating System**: Android 10 (Q) or higher
- **Root Access**: Magisk v24.0+, KernelSU v0.9+, or APatch
- **Storage**: 64MB available space
- **RAM**: 2GB minimum (4GB recommended untuk profiling intensif)

### Supported Architectures
```

✓ ARM64 (arm64-v8a)     - Primary Support
✓ ARMv7 (armeabi-v7a)   - Legacy Support

✓ x86_64                - Emulator Support

```

### Security Prerequisites
- **SELinux Status**: Permissive atau mode Enforcing dengan policy modifikasi
- **Bootloader Status**: Unlocked (untuk fungsi flasher)
- **SafetyNet/Play Integrity**: Bypass required untuk beberapa fitur advanced

---

## 📥 Deployment Guide

### Standard Installation
1. **Pre-Installation Check**
   - Verifikasi status root melalui `su` binary check
   - Konfirmasi kompatibilitas architecture device

2. **Package Installation**
   - Download `ZKM-vX.X.X-stable.apk` dari [Official Releases](../../releases)
   - Enable "Install from Unknown Sources" pada device settings
   - Execute installation package

3. **Permission Configuration**
   - Grant Superuser permissions pada first launch
   - Allow notifications untuk real-time monitoring alerts
   - Configure storage permissions untuk backup operations

### Enterprise Distribution
Untuk deployment massal dalam organisasi, tersedia konfigurasi MDM (Mobile Device Management) compatible package. Hubungi maintainers untuk Enterprise License Agreement.

---

## 🏆 Credits & Third-Party Integrations

ZKM dibangun di atas fondasi teknologi open-source kelas dunia. Kami mengakui kontribusi signifikan dari developer dan library berikut:

### Core Architecture Contributors

<table>
  <thead>
    <tr>
      <th width="180">Developer</th>
      <th width="300">Contribution Domain</th>
      <th width="250">Repository Source</th>
      <th>License</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center">
        <b>Rve27</b><br>
        <sub>Core Developer</sub>
      </td>
      <td>
        • Kernel Manager Core Architecture<br>
        • CPU/GPU Tuning Engines<br>
        • System Monitoring Framework
      </td>
      <td>
        <a href="https://github.com/Rve27">Rve27</a>/rve-kernel-manager
      </td>
      <td>GPL-3.0</td>
    </tr>
    <tr>
      <td align="center">
        <b>libxzr</b><br>
        <sub>System Engineer</sub>
      </td>
      <td>
        • Horizon Flasher Logic<br>
        • Boot Image Parsing<br>
        • Partition Management
      </td>
      <td>
        <a href="https://github.com/libxzr">libxzr</a>/HorizonKernelFlasher
      </td>
      <td>GPL-3.0</td>
    </tr>
    <tr>
      <td align="center">
        <b>5ec1cff</b><br>
        <sub>WebUI Specialist</sub>
      </td>
      <td>
        • KsuWebUI Standalone Implementation<br>
        • KernelSU Module Interface<br>
        • Web Server Architecture
      </td>
      <td>
        <a href="https://github.com/5ec1cff">5ec1cff</a>/KsuWebUIStandalone
      </td>
      <td>GPL-3.0</td>
    </tr>
    <tr>
      <td align="center">
        <b>Rem01Gaming</b><br>
        <sub>Platform Engineer</sub>
      </td>
      <td>
        • MediaTek SoC Logic Implementation<br>
        • Helio/Dimensity Optimization<br>
        • Vendor-specific Tweaks
      </td>
      <td>
        <a href="https://github.com/Rem01Gaming">Rem01Gaming</a>/origami-kernel-manager
      </td>
      <td>GPL-3.0</td>
    </tr>
    <tr>
      <td align="center">
        <b>helloklf</b><br>
        <sub>Kernel Utilities</sub>
      </td>
      <td>
        • Kernel Utils & Tweaks<br>
        • FPS Monitoring Logic<br>
        • Universal SoC Support
      </td>
      <td>
        <a href="https://github.com/helloklf">helloklf</a>/kernel-tweaks
      </td>
      <td>GPL-3.0</td>
    </tr>
    <tr>
      <td align="center">
        <b>capntrips</b><br>
        <sub>Flashing Engineer</sub>
      </td>
      <td>
        • Capntrips Flasher Implementation<br>
        • A/B Partition Support<br>
        • Backup/Restore Logic
      </td>
      <td>
        <a href="https://github.com/capntrips">capntrips</a>/KernelFlasher
      </td>
      <td>Apache-2.0 & GPL-3.0</td>
    </tr>
    <tr>
      <td align="center">
        <b>termux</b><br>
        <sub>Terminal Team</sub>
      </td>
      <td>
        • Terminal Emulator Engine<br>
        • Shell Environment<br>
        • Command-line Interface
      </td>
      <td>
        <a href="https://github.com/termux">termux</a>/termux-app
      </td>
      <td>Apache-2.0</td>
    </tr>
    <tr>
      <td align="center">
        <b>Kyant0</b><br>
        <sub>UI/UX Designer</sub>
      </td>
      <td>
        • Capsule iOS Navigation UI<br>
        • Liquid Glass Design System<br>
        • Animation Framework
      </td>
      <td>
        <a href="https://github.com/Kyant0">Kyant0</a>/android-liquid-glass
      </td>
      <td>Apache-2.0</td>
    </tr>
  </tbody>
</table>

### Open Source Library Stack

#### UI/UX Frameworks
* **Jetpack Compose** by Google - *Modern declarative UI toolkit for Android* (Apache-2.0)
* **Material 3 Expressive** by Google - *Extended Material Design components* (Apache-2.0)
* **Haze** by Chris Banes - *Advanced glassmorphism blur effects* (Apache-2.0)

#### System & Root Libraries
* **libsu** by topjohnwu - *Android root shell abstraction library* (Apache-2.0)
* **Coil** by Coil Team - *Image loading and caching* (Apache-2.0)
* **kotlinx.coroutines** by JetBrains - *Asynchronous programming framework* (Apache-2.0)

#### Platform Integrations
* **KernelSU** by weishu - *Kernel-based root solution integration*
* **Magisk** by topjohnwu - *Systemless root interface compatibility*

---

## 👨‍💻 ZKM Development Team

### Project Maintainers
| Role | Name | GitHub | Responsibility |
|------|------|--------|----------------|
| **Lead Developer** | Zuan | [@ZUANVFX01](https://github.com/ZUANVFX01) | Project architecture, UI/UX direction, release management |
| **Core Contributor** | Rve27 | [@Rve27](https://github.com/Rve27) | Kernel logic, performance tuning modules |

### Special Thanks
* **Community Beta Testers** - Laporan bug dan feedback UX dari komunitas XDA dan Telegram
* **Kernel Developers** - Para creator kernel custom (Predator Kernel, Nova Kernel, dll) yang menyediakan API testing
* **XDA Community** - Forum support dan resource sharing untuk Android development

---

## 🔒 Security & Compliance

### Data Privacy Protocols
- **Zero Data Collection**: ZKM tidak mengirimkan data telemetri atau analytics ke server eksternal
- **Local Processing**: Seluruh komputasi dilakukan on-device tanpa cloud dependency
- **Open Source Transparency**: Full source code audit available untuk security verification

### Root Access Management
- **Scoped Permissions**: Implementasi principle of least privilege pada root operations
- **Command Whitelisting**: Validasi strict terhadap system commands yang dieksekusi
- **Audit Logging**: Logging komprehensif untuk setiap system modification (tersedia pada LogsView)

### Compliance Standards
- **GPL-3.0 License**: Full compliance dengan open source distribution requirements
- **Apache-2.0 Components**: Proper attribution untuk library third-party
- **Security Patching**: Regular updates untuk address CVEs pada dependencies

---

## 📜 Legal & Licensing

### Primary License
This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

### Third-Party Attributions
This software contains code dari proyek open-source berikut:
- Horizon Flasher (GPL-3.0)
- Kernel Flasher by capntrips (Apache-2.0 & GPL-3.0)
- Termux (Apache-2.0)
- Android Liquid Glass (Apache-2.0)

### Disclaimer
> **WARNING**: ZKM requires root access dan melakukan modifikasi terhadap system-level parameters. Pengguna bertanggung jawab penuh atas perubahan yang dilakukan. Zuan Technologies tidak bertanggung jawab atas device damage, data loss, atau warranty void yang mungkin terjadi.

---

<div align="center" style="margin-top: 40px; padding: 20px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 12px;">

  <img src="./logo/logo.jpg" width="60" height="60" style="border-radius: 12px; margin-bottom: 15px;" alt="Zuan Technologies"/>
  
  <h3 style="margin: 10px 0; color: #2c3e50;">Zuan Technologies</h3>
  
  <p style="color: #555; font-size: 0.9em; max-width: 600px; margin: 0 auto;">
    Pioneering Android System Optimization Solutions<br>
    <sub>© 2024 Zuan Technologies. All rights reserved.</sub>
  </p>

  <p style="margin-top: 15px;">
    <a href="https://github.com/ZUANVFX01/ZKM">GitHub</a> • 
    <a href="#">Documentation</a> • 
    <a href="#">Website</a>
  </p>

</div>
