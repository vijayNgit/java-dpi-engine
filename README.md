# 📘 Java Deep Packet Inspection (DPI) Engine

**Beginner-Friendly Explanation & Architecture**

> This document explains **what this project does**, **why it exists**, and **how it works internally** — even if you are new to networking or Deep Packet Inspection.

---

## 📌 What is this project?

This project is a **Java-based Deep Packet Inspection (DPI) engine** that:

* Reads network traffic from a **PCAP file**
* Looks inside packets to **identify websites and applications**
* Applies **blocking rules** (YouTube, Facebook, domains, IPs)
* Writes only **allowed traffic** to a new PCAP file
* Produces **statistics** and **reports**
* Works using a **multi-threaded pipeline**

In short:

```
Input PCAP  →  Java DPI Engine  →  Filtered Output PCAP
```

---

## 🤔 What is Deep Packet Inspection (DPI)?

Normally, a firewall only checks:

* Source IP
* Destination IP
* Port number

**DPI goes deeper.**

It looks **inside the packet payload** to understand:

* Which website is being accessed
* Which application generated the traffic

### Real-world usage

* ISPs blocking torrent traffic
* Companies blocking social media
* Parental controls
* Security monitoring

---

## 🌐 How can we identify websites in HTTPS?

Even though HTTPS is encrypted, **the domain name is visible** in the first packet.

This happens using **TLS SNI (Server Name Indication)**.

Example:

```
https://www.youtube.com
```

During connection setup, the browser sends:

```
TLS Client Hello
└── SNI: www.youtube.com   ← visible!
```

Your DPI engine extracts this **SNI value**.

---

## 🧠 Core idea of this project

> **Identify the website → classify traffic → apply rules → forward or drop packets**

---

## 🧱 Key concepts used

### 1️⃣ Five-Tuple (Flow Identification)

Each network connection is uniquely identified using:

| Field            | Meaning                    |
| ---------------- | -------------------------- |
| Source IP        | Sender                     |
| Destination IP   | Receiver                   |
| Source Port      | Client port                |
| Destination Port | Service port (443 = HTTPS) |
| Protocol         | TCP / UDP                  |

All packets with the same **five-tuple** belong to the **same flow**.

---

### 2️⃣ Flow-based blocking (important)

Blocking is done **per flow**, not per packet.

Example:

```
Packet 1 → no SNI yet → allowed
Packet 2 → no SNI yet → allowed
Packet 3 → SNI = youtube → BLOCK FLOW
Packet 4 → dropped
Packet 5 → dropped
```

Once blocked, **all future packets are dropped**.

---

## ⚙️ High-Level Architecture

```
PCAP Reader
     |
     v
Reader Queue
     |
     v
Load Balancer Threads (LB)
     |
     v
Fast Path Workers (FP)
     |
     v
Output Queue
     |
     v
PCAP Writer
```

---

## 🧵 Why multi-threading?

Processing large PCAP files is CPU-intensive.

So the engine uses:

| Thread             | Responsibility          |
| ------------------ | ----------------------- |
| Reader             | Reads packets from file |
| Load Balancer (LB) | Distributes packets     |
| Fast Path (FP)     | DPI logic & blocking    |
| Writer             | Writes output PCAP      |

This allows:

* Faster processing
* Better CPU utilization
* Scalable design

---

## 🔁 How packets flow (simple explanation)

### Step 1: Read packet

The reader thread reads one packet from `test_dpi.pcap`.

### Step 2: Assign to Load Balancer

Packets are placed into a queue.

### Step 3: Load Balancer distributes packets

Using hash of the five-tuple, packets go to the **same Fast Path worker** every time.

### Step 4: Fast Path does DPI

Each FP:

* Tracks flows
* Extracts SNI
* Classifies app
* Applies blocking rules

### Step 5: Forward or Drop

* Allowed → sent to output queue
* Blocked → discarded

### Step 6: Writer writes output

Allowed packets are written to `output.pcap`.

---

## 🛑 Graceful Shutdown (Enhancement)

The engine shuts down **cleanly** using a **poison-pill mechanism**.

Why this matters:

* No hanging threads
* No corrupted output file
* Program exits automatically

---

## 📊 Statistics & Reporting (Enhancement)

The engine reports:

* Total packets
* Forwarded packets
* Dropped packets
* Dropped packets per application

Example:

```
Dropped by Application:
  YOUTUBE   : 5
  FACEBOOK  : 3
  UNKNOWN   : 0
```

---

## 🎛️ CLI Configuration (Enhancement)

You can configure the engine at runtime:

```bash
java -cp src MainMT test_dpi.pcap output.pcap \
  --fps 6 \
  --lbs 2 \
  --block-app YouTube \
  --block-domain facebook
```

### What these mean:

| Flag             | Purpose                  |
| ---------------- | ------------------------ |
| `--fps`          | Fast Path worker threads |
| `--lbs`          | Load Balancer threads    |
| `--block-app`    | Block application        |
| `--block-domain` | Block domain             |
| `--block-ip`     | Block IP address         |

---

## 🔍 Verification using Wireshark

### Input PCAP

* YouTube & Facebook visible

### Output PCAP

* YouTube & Facebook **missing**
* Other HTTPS traffic still present

This proves **selective blocking**.

---

## 📂 Project Structure (Java)

```
src/
├── util/     → Utilities & statistics
├── pcap/     → PCAP reader/writer
├── parser/   → Packet parsing
├── dpi/      → DPI logic (rules, SNI, flows)
├── mt/       → Multi-threaded pipeline
└── MainMT.java
```

---

## 🧠 Why this project is valuable

This project demonstrates:

* Networking fundamentals
* Deep Packet Inspection concepts
* TLS protocol understanding
* Multi-threaded system design
* Producer–consumer pattern
* Real-world traffic filtering
* Debugging & verification using Wireshark

---

## 🚀 Future Improvements

* Flow timeout & cleanup
* JSON export of statistics
* Regex-based domain rules
* QUIC / HTTP-3 support
* Maven packaging
* Live traffic capture

---

## 👨‍💻 Author

**Vijay Singh Negi**
MCA Student | Java | Networking | Systems Programming

---

## ✅ Final Note

This README is written so that **even a beginner** can understand:

* What DPI is
* Why this project exists
* How packets move
* How blocking works

You can expand it further as the project grows.

---

