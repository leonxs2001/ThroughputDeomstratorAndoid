# Throughput Demonstrator Android Client

This Android application is designed to demonstrate and measure upload and download speeds. It works in conjunction with the [Throughput Demonstrator Server](https://github.com/leonxs2001/ThroughputDemonstratorServer).

## Features

The client app supports four main modes of operation:
1. File upload
2. File download
3. Dummy data upload
4. Dummy data download

## Technologies Used

- Android SDK
- Kotlin
- Java

## Prerequisites

- Android Studio
- Android device or emulator running Android [specify minimum version]

## Setup

1. Clone the repository:
```ssh
git clone https://github.com/leonxs2001/ThroughputDeomstratorAndroidClient.git
```
2. Open the project in Android Studio.
3. Build and run the app on your Android device or emulator.

## Configuration

For detailed instructions on setting up and configuring the server component, please refer to the [Throughput Demonstrator Server repository](https://github.com/leonxs2001/ThroughputDemonstratorServer).

## Usage

1. Launch the app on your Android device.
2. Configure the server settings.
3. Choose the operation mode (file upload/download or dummy data upload/download).
4. For file operations, select a file or specify a destination.
5. Start the transfer and observe the throughput measurements.

## How it works

1. The app connects to the specified server.
2. It sends the selected operation mode to the server.
3. For file transfers, it handles actual file uploads or downloads.
4. For dummy data transfers, it sends or receives a specified amount of data to measure throughput.
5. The app calculates and displays the transfer speed in real-time.
