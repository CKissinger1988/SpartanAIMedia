# SpartanAI Media: Pi Node Deployment Guide

To run a Pi Network Node as part of the SpartanAI ecosystem, follow these steps. 

## Prerequisites
1.  **Docker Desktop:** Installed and running (Windows/macOS/Linux).
2.  **Pi Desktop App:** Installed on your computer to obtain your Node UUID.
3.  **Static IP/Port Forwarding:** Ports 31400-31409 must be open on your router.

## Quick Start (Docker)

1.  **Obtain Node UUID:** Open the Pi Desktop app, go to the Node section, and follow the instructions to generate your UUID.
2.  **Configure Environment:** Create a `.env` file in this directory:
    ```env
    PI_NODE_UUID=your_uuid_here
    ```
3.  **Start the Node:**
    ```bash
    docker-compose up -d
    ```

## Monitoring
The SpartanAI Media Android app can monitor your node status if you provide your node's public IP address in the **Pi Ecosystem** settings tab.

---
*Note: This node implementation uses the official Pi Network stellar-core based image.*
