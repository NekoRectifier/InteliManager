#include <ESP8266WiFiMulti.h> //  ESP8266WiFiMulti库
#include <ESP8266WebServer.h>

ESP8266WiFiMulti wifiMulti;
ESP8266WebServer server(80);

int unlock_times = 0;

void setup()
{
    Serial.begin(115200);
    delay(10);

    // 设置WiFi
    wifiMulti.addAP("ssid", "password");
    while(wifiMulti.run() != WL_CONNECTED)){

        Serial.println("Connecting to WiFi..");

        // 控制内置LED发出指示灯闪烁
        digitalWrite(LED_BUILTIN, HIGH);
        delay(30);
        digitalWrite(LED_BUILTIN, LOW);
        delay(20);
        digitalWrite(LED_BUILTIN, HIGH);
        delay(10);
        digitalWrite(LED_BUILTIN, LOW);
    }

    digitalWrite(LED_BUILTIN, LOW);
    delay(60);
    digitalWrite(LED_BUILTIN, HIGH);
    delay(35);
    digitalWrite(LED_BUILTIN, LOW);

    // 设置服务器
    server.on("/unlock", unlock());
    server.on("/log", checkLog());
    server.onNotFound(handleNotFound);
    server.begin();

    Serial.println("HTTP server started");

}

void loop()
{
    server.handleClient();
}

void unlock()
{
    Serial.println("unlocking");

    unlock_times++;

    digitalWrite(LED_BUILTIN, HIGH);
    delay(5);
    // 舵机相关电平修改
    // digitalWrite(pin, value);
}

void checkLog()
{
    Serial.println("checking log");
    digitalWrite(LED_BUILTIN, HIGH);

    server.send(200, "text/json", "{ \"unlock times:\":" + String(unlock_times) + '}');

    digitalWrite(LED_BUILTIN, LOW);
}

void handleNotFound() 
{                           
  server.send(404, "text/plain", "404: Not found");
}
// 当浏览器请求的网络资源无法在服务器找到时，NodeMCU将调用此函数。
