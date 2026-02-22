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

**Zuan Kernel Manager (ZKM)** is an enterprise-class Android kernel management solution developed by Zuan Technologies. Built upon the solid foundation of the Rve Kernel Manager architecture, ZKM has evolved into a comprehensive system optimization platform for rooted Android devices.

### Our Philosophy
> *"Empowering Performance Through Precision Engineering"*

We believe that deep control over mobile systems shouldn't compromise security, stability, or user experience. ZKM combines precision engineering with modern design to deliver an accessible enterprise solution.

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
      <td align="center"><b>Overall Dashboard</b><br><sub>Real-time Monitoring</sub></td>
      <td align="center"><b>Home</b><br><sub>Hardware Telemetry</sub></td>
      <td align="center"><b>Soc</b><br><sub>Battery Optimization</sub></td>
      <td align="center"><b>About</b><br><sub>Advanced Tuning</sub></td>
    </tr>
    <tr>
      <td><img src="https://github.com/user-attachments/assets/aa686b02-a1d6-4fc3-b3f7-388b0ea8af70" width="200" style="border-radius: 12px;"/></td>
      <td><img src="https://github.com/user-attachments/assets/851cbef4-8ed6-4dbf-84f2-b79107cd361f" width="200" style="border-radius: 12px;"/></td>
      <td><img src="https://github.com/user-attachments/assets/f82afa01-84b7-4227-971a-e6b05236e1d2" width="200" style="border-radius: 12px;"/></td>
      <td><img src="https://github.com/user-attachments/assets/36b2472f-09bc-439f-a32b-010928ae04a3" width="200" style="border-radius: 12px;"/></td>
    </tr>
  </table>
</div>

---

## ✨ Key Features

ZKM offers a comprehensive suite of tools categorized into intuitive modules:

### 🎨 Appearance & User Interface
* **Material 3 Expressive Design** - Modern interface with responsive layout and adaptive design
* **LogsView System** - Advanced log reading with dynamic UI components and filtering capabilities
* **Themes & Visual Effects** - Fluid transitions, glassmorphism blur effects (Haze integration), and optimized layouts for various screen densities

### ⚙️ Performance & Kernel Control
* **Enterprise Dashboard** - Real-time monitoring for SoC temperatures, CPU frequencies, and RAM utilization, complete with historical data logging
* **CPU/GPU Tuning** - Comprehensive governor control, min/max frequency management, and boost configuration profiles
* **Memory Management** - LMK (Low Memory Killer) tweaks, virtual memory tuning, ZRAM compression settings, and swap management
* **Thermal & Display Control** - Deep integration with device thermal drivers, brightness curve calibration, and refresh rate management
* **Battery & Doze Optimization** - Wakelock analysis and blocking, charging cycle control, deep sleep optimization, and idle drain prevention

### 🛠️ Advanced System Utilities
* **Dual-Engine Kernel Flasher** - Flashing system supporting **Horizon Logic** and **Capntrips Architecture** with A/B partition support
* **Secure Terminal Emulator** - Built-in root shell with command history, scripting capabilities, and environment variables management
* **Dex2oat Compiler** - On-device APK optimization for improved runtime performance
* **KsuWebUI Integration** - Embedded WebUI server for KernelSU module management without an external browser
* **System Modding Suite** - Build.prop editor with syntax validation, SetEdit integration for database editing
* **Application Management** - Activity launcher, system app debloating with whitelist protection, and disable/enable controls
* **Real-time Monitoring** - On-screen FPS counter, process resource tracking, and system load monitoring

---

## 🔧 Technical Requirements

### Minimum System Requirements
- **Operating System**: Android 10 (Q) or higher
- **Root Access**: Magisk v24.0+, KernelSU v0.9+, or APatch
- **Storage**: 64MB available space
- **RAM**: 2GB minimum (4GB recommended for intensive profiling)

### Supported Architectures

✓ ARM64 (arm64-v8a)     - Primary Support
✓ ARMv7 (armeabi-v7a)   - Legacy Support
✓ x86_64                - Emulator Support

### Security Prerequisites
- **SELinux Status**: Permissive or Enforcing mode with policy modifications
- **Bootloader Status**: Unlocked (required for flasher functionality)
- **SafetyNet/Play Integrity**: Bypass required for several advanced features

---

## 📥 Deployment Guide

### Standard Installation
1. **Pre-Installation Check**
   - Verify root status via `su` binary check
   - Confirm device architecture compatibility

2. **Package Installation**
   - Download `ZKM-vX.X.X-stable.apk` from the [Official Releases](../../releases)
   - Enable "Install from Unknown Sources" in device settings
   - Execute the installation package

3. **Permission Configuration**
   - Grant Superuser permissions on first launch
   - Allow notifications for real-time monitoring alerts
   - Configure storage permissions for backup operations

### Enterprise Distribution
For mass deployment within an organization, MDM (Mobile Device Management) compatible packages are available. Please contact the maintainers for an Enterprise License Agreement.

---

## 🏆 Credits & Third-Party Integrations

ZKM is built upon the foundation of world-class open-source technology. We acknowledge the significant contributions from the following developers and libraries:

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
* **Community Beta Testers** - Bug reports and UX feedback from the XDA and Telegram communities
* **Kernel Developers** - Custom kernel creators (Predator Kernel, Nova Kernel, etc.) who provided testing APIs
* **XDA Community** - Support forums and resource sharing for Android development

---

## 🔒 Security & Compliance

### Data Privacy Protocols
- **Zero Data Collection**: ZKM does not send telemetry data or analytics to external servers
- **Local Processing**: All computations are performed on-device without cloud dependency
- **Open Source Transparency**: Full source code audit available for security verification

### Root Access Management
- **Scoped Permissions**: Implementation of the principle of least privilege in root operations
- **Command Whitelisting**: Strict validation of executed system commands
- **Audit Logging**: Comprehensive logging for every system modification (available in LogsView)

### Compliance Standards
- **GPL-3.0 License**: Full compliance with open source distribution requirements
- **Apache-2.0 Components**: Proper attribution for third-party libraries
- **Security Patching**: Regular updates to address CVEs in dependencies

---

## 📜 Legal & Licensing

### Primary License
This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

### Third-Party Attributions
This software contains code from the following open-source projects:
- Horizon Flasher (GPL-3.0)
- Kernel Flasher by capntrips (Apache-2.0 & GPL-3.0)
- Termux (Apache-2.0)
- Android Liquid Glass (Apache-2.0)

### Disclaimer
> **WARNING**: ZKM requires root access and modifies system-level parameters. Users are fully responsible for any changes made. Zuan Technologies is not responsible for any device damage, data loss, or voided warranties that may occur.

---

<div align="center" style="margin-top: 40px; padding: 20px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 12px;">

  <img src="./logo/logo.jpg" width="60" height="60" style="border-radius: 12px; margin-bottom: 15px;" alt="Zuan Technologies"/>

  <p style="margin-top: 15px;">
    <a href="https://github.com/ZUANVFX01/ZKM">GitHub</a> • 
    <a href="#">Documentation</a> • 
    <a href="#">Website</a>
  </p>

</div>
