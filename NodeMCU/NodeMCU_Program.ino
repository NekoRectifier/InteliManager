#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ArduinoJson.h>
#include <FS.h>

ESP8266WebServer server(80);

int unlock_times = 0;
const char *ssid = "叁壹零";
const char *password = "sanyiling";
const char *header_keys[] = {"token", "DEVICE_TYPE"};
const char *token = "226f76b55acb49701e06ded1d95165d179458f6fc37f5c6fc760ae30dec1c378";
String log_path = "/log.txt";

void setup()
{
    Serial.begin(115200);

    pinMode(LED_BUILTIN, OUTPUT);

    // 设置WiFi
    connectWiFi();

    // 设置服务器
    server.on("/unlock", unlock);
    server.on("/log", checkLog);
    server.onNotFound(handleNotFound);
    server.begin();
    server.collectHeaders(header_keys, sizeof(header_keys) / sizeof(header_keys[0]));
    Serial.println("HTTP server started");

    Serial.println("SPIFFS initializing");

    if (SPIFFS.begin())
    {
        Serial.println("SPIFFS Started.");
    }
    else
    {
        Serial.println("SPIFFS Failed to Start.");
    }
}

void loop()
{
    server.handleClient();
}

void unlock()
{
    Serial.println("unlocking");

    if (server.hasHeader("token"))
    {
        if (server.header("token") == token)
        {
            Serial.println("token is correct");
            Serial.println(server.header("Date")); // not working

            File log;

            if (SPIFFS.exists(log_path))
            {
                Serial.println("log file exists");
                log = SPIFFS.open(log_path, "a");
                log.println(server.header("Date") + " " + server.header("DEVICE_TYPE"));
            }
            else
            {
                log = SPIFFS.open(log_path, "w");
                log.println(server.header("Date") + " " + server.header("DEVICE_TYPE"));
            }
            log.close();

            server.send(200, "text/json", "{ \"result\":\"ok\", + \"reason:\":\"null\" }");
        }
        else
        {
            server.send(401, "text/json", "{ \"result\": \"failed\", \"reason\": \"invaild authorization token\" }");
        }
    }
    else
    {
        server.send(401, "text/json", "{ \"result\": \"failed\", \"reason\": \"missing authorization token\" }");
    }

    unlock_times++;
    File data_handle = SPIFFS.open("unlock_time.txt", "w");
    data_handle.println(String(unlock_times));
    data_handle.close();
}

void checkLog()
{
    Serial.println("checking log");
    File times;
    if (SPIFFS.exists("unlock_times.txt"))
    {
        times = SPIFFS.open("unlock_times.txt", "r");
        unlock_times = (int)times.read();
        Serial.println(unlock_times);
    }
    else
    {
        times = SPIFFS.open("unlock_times.txt", "w");
        times.println("0");
    }
    times.close();
    server.send(200, "text/json", "{ \"unlock times\":\"" + String(unlock_times) + "\"}");
}

bool authenticate()
{
    return false;
}

void handleNotFound()
{
    server.send(404, "text/plain", "404: Not found");
}
// 当浏览器请求的网络资源无法在服务器找到时，NodeMCU将调用此函数。

void connectWiFi()
{
    WiFi.begin(ssid, password);

    while (WiFi.status() != WL_CONNECTED)
    {
        Serial.println("Connecting...");
    }

    blink(3, 100);

    Serial.println("Connection established!");
    Serial.print("IP address:    ");
    Serial.println(WiFi.localIP());
}

void blink(int times, int delay_time)
{
    for (int i = 0; i < times; i++)
    {
        digitalWrite(LED_BUILTIN, LOW);
        delay(delay_time);
        digitalWrite(LED_BUILTIN, HIGH);
        delay(delay_time);
    }
}