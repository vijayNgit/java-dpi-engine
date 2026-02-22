# Java Deep Packet Inspection (DPI) Engine – Multi-threaded

A **Java-based Deep Packet Inspection (DPI) engine** that parses PCAP files, extracts
application-layer information (TLS SNI), classifies traffic, applies blocking rules,
and writes filtered packets to a new PCAP file.

This project is a **Java port of a C++ DPI engine**, preserving the same architecture
and behavior while using Java concurrency primitives.

---

## ✨ Features (Current Version)

- PCAP file parsing (Ethernet → IP → TCP/UDP)
- TLS Client Hello inspection
- SNI (Server Name Indication) extraction
- Application classification (YouTube, Facebook, etc.)
- Flow tracking using 5-tuple
- Rule-based blocking:
  - Block by Application
  - Block by Domain
  - Block by IP
- Multi-threaded processing:
  - Reader
  - Load Balancers
  - Fast Path Workers
  - Output Writer
- Packet statistics:
  - Total packets
  - Forwarded packets
  - Dropped packets
- Output PCAP verification using Wireshark

---

## 🏗️ Architecture Overview

PCAP Reader
|
v
Load Balancer(s)
|
v
Fast Path Workers (per-flow DPI)
|
v
Output Writer → output.pcap


- Packets are distributed using **consistent hashing on Five-Tuple**
- Each Fast Path thread maintains its own flow table
- Blocking decisions are applied at flow level

---

## 📂 Project Structure
dpi-java/
├── src/
│ ├── util/ # Helpers & statistics
│ ├── pcap/ # PCAP reader & writer
│ ├── parser/ # Packet parsing logic
│ ├── dpi/ # DPI logic (SNI, rules, flow)
│ ├── mt/ # Multi-threaded pipeline
│ └── MainMT.java # Entry point
├── test_dpi.pcap # Input PCAP (generated)
├── output.pcap # Output PCAP (generated)
└── README.md


---

## ⚙️ Requirements

- Java JDK 8 or higher
- Windows / Linux / macOS
- Wireshark (for PCAP verification)

---

## ▶️ Build Instructions

From the project root (`dpi-java`):

```powershell
javac src\util\*.java src\pcap\*.java src\parser\*.java src\dpi\*.java src\mt\*.java src\MainMT.java

▶️ Run Instructions
Single-line (recommended on PowerShell)

java -cp src MainMT test_dpi.pcap output.pcap --block-app YouTube --block-domain facebook

Multi-line (PowerShell)
java -cp src MainMT test_dpi.pcap output.pcap `
  --block-app YouTube `
  --block-domain facebook

🔍 Verification Using Wireshark

1. Open test_dpi.pcap

Filter: tls.handshake.extensions_server_name contains "youtube"

YouTube traffic should be present

2. Open output.pcap

Apply the same filter

No packets found → traffic successfully blocked

Other HTTPS traffic will still be visible, confirming selective DPI blocking.

🚀 Future Enhancements (Planned)

Graceful thread shutdown

App-wise and thread-wise statistics

CLI flags for thread configuration

Flow timeout & cleanup

JSON export of statistics

Maven/Gradle packaging

Detailed architecture diagrams