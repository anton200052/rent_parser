# RentParser: German Real Estate Aggregator

![Java](https://img.shields.io/badge/Java-Spring_Boot-orange)
![Architecture](https://img.shields.io/badge/Architecture-Multi--Tenant-blue)
![Strategy](https://img.shields.io/badge/Strategy-Reverse_Engineering-red)
![Frontend](https://img.shields.io/badge/UI-Thymeleaf%2FReact-green)

**RentParser** is a high-performance, multi-user web application designed to monitor and parse real estate listings in Germany. Built with **Spring Boot**, it moves beyond traditional web scraping by leveraging reverse-engineered internal APIs to ensure speed, stability, and low detection rates.

---

## 🚀 Key Features

* **Multi-User Environment:** Full multi-tenancy support. Users can manage their own parsing jobs. Data isolation ensures users only see and manage their own search tasks.
* **Bypassing Bot Detection:** Unlike slow and heavy browser automation tools (Selenium/Puppeteer), RentParser constructs direct HTTP requests mimicking the mobile app/internal API signatures.
* **Parallel Job Execution:** Supports running multiple search tasks simultaneously (e.g., searching for "2-room in Berlin" and "Studio in Munich" at the same time).
* **Modular Architecture:** The system uses the **Strategy Pattern**, making it effortless to extend. While currently supporting **Immoscout24**, new providers (e.g., *WG-Gesucht, Immowelt*) can be added by simply implementing a new provider interface.
* **User-Friendly Dashboard:** A clean web interface to configure jobs.

---

## 🖼️ Interface

<img width="1643" height="914" alt="Image" src="https://github.com/user-attachments/assets/d6ca7b89-1243-44df-a576-cb1ea9d42bff" />

<img width="1635" height="863" alt="Image" src="https://github.com/user-attachments/assets/cadd0a44-2aa8-4f96-8cf8-7de0e82c6e73" />

---

## 🔧 Technical Deep Dive

### The "Stealth" Approach (Immoscout24)
Standard scraping of `Immoscout24` is difficult due to aggressive bot detection and Captchas. Instead of parsing HTML, this project implements the findings from [orangecoding's reverse engineering research](https://github.com/orangecoding/fredy/blob/master/reverse-engineered-immoscout.md).

By analyzing the network traffic of legitimate clients, RentParser constructs valid **POST requests** to the internal search API.
* **Benefits:**
    * **Speed:** Returns JSON directly (no HTML rendering overhead).
    * **Efficiency:** Consumes 90% less bandwidth than browser-based scrapers.
    * **Reliability:** API endpoints change less frequently than frontend HTML structure.

### Architecture Overview

The application is built on a scalable **Service-Oriented Architecture**:
---

## ⚙️ Getting Started
### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/anton200052/rent_parser
    cd rent_parser
    ```

2.  **Build and Run:**
    ```bash
    mvn spring-boot:run
    ```
    *The application will start on `http://localhost:8080` (or your configured port).*

---

## ⚠️ Disclaimer

This tool is intended for **personal and educational purposes only**.
Parsing data from websites may violate their Terms of Service. The author is not responsible for any misuse of this software or any IP bans incurred by using it.
