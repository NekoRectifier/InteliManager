<h1 align="center">InteliManager</h1>
<hr>
<p align=center>Just a simple IoT manager app on Android</p>

## How to implement

1. Build an app with Android Studio like this.
2. Program a NodeMCU module using the file `node_mcu_program.ino`.
3. Setup module with a WiFi connection.
4. Run the app, then unlock the door.(or some other modules)

## FAQ

1. If the C header files cannot be found, try to use the official Arduino IDE then download the `ESP8266` library from there.
    Or check if there's any fault in the `import` statement in `node_mcu_program.ino`.
