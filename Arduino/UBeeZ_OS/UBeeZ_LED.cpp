#include "UBeeZ_LED.h"

using namespace rtos;

events::EventQueue *ledEventQueue;
Thread ledEventThread;

bool stop_led_R_flag = false;
bool stop_led_G_flag = false;
bool stop_led_B_flag = false;

int led_R_task = 0;
int led_G_task = 0;
int led_B_task = 0;

byte led_R_state = HIGH;
byte led_G_state = HIGH;
byte led_B_state = HIGH;

std::chrono::milliseconds led_R_delay_ms(1000);
std::chrono::milliseconds led_G_delay_ms(1000);
std::chrono::milliseconds led_B_delay_ms(1000);

void setupLedRGB(){
  pinMode(LEDB, OUTPUT);
  pinMode(LEDR, OUTPUT);
  pinMode(LEDG, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(LED_PWR, OUTPUT);
  
  digitalWrite(LEDR, HIGH);
  digitalWrite(LEDG, HIGH);
  digitalWrite(LEDB, HIGH);
  digitalWrite(LED_BUILTIN, LOW);
  digitalWrite(LED_PWR, LOW);
  ledEventQueue = new events::EventQueue(3 * EVENTS_EVENT_SIZE);
  ledEventThread.start(callback(ledEventQueue, &events::EventQueue::dispatch_forever));
}

void led_blink_R(int delay_ms){
  led_R_delay_ms = std::chrono::milliseconds(delay_ms);
  stop_led_R_flag = false;
  if(led_R_task == 0){
    led_R_task = ledEventQueue->call_in(led_R_delay_ms, (void(*)(void))handler_led_blink_R);
  }
}

void led_blink_G(int delay_ms){
  led_G_delay_ms = std::chrono::milliseconds(delay_ms);
  stop_led_G_flag = false;
  if(led_G_task == 0){
    led_G_task = ledEventQueue->call_in(led_G_delay_ms, (void(*)(void))handler_led_blink_G);
  }
}

void led_blink_B(int delay_ms){
  led_B_delay_ms = std::chrono::milliseconds(delay_ms);
  stop_led_B_flag = false;
  if(led_B_task == 0){
    led_B_task = ledEventQueue->call_in(led_B_delay_ms, (void(*)(void))handler_led_blink_B);
  }
}

void led_stop_R(){
  stop_led_R_flag = true;
}

void led_stop_G(){
  stop_led_G_flag = true;
}

void led_stop_B(){
  stop_led_B_flag = true;
}

void handler_led_blink_R(){
  if(!stop_led_R_flag){
    led_R_task = ledEventQueue->call_in(led_R_delay_ms, (void(*)(void))handler_led_blink_R);
    led_R_state = !led_R_state;
    digitalWrite(LEDR, led_R_state);
  }
  else{
    stop_led_R_flag = false;
    led_R_task = 0;
  }
}

void handler_led_blink_G(){
  if(!stop_led_G_flag){
    led_G_task = ledEventQueue->call_in(led_G_delay_ms, (void(*)(void))handler_led_blink_G);
    led_G_state = !led_G_state;
    digitalWrite(LEDG, led_G_state);
  }
  else{
    stop_led_G_flag = false;
    led_G_task = 0;
  }
}

void handler_led_blink_B(){
  if(!stop_led_B_flag){
    led_B_task = ledEventQueue->call_in(led_B_delay_ms, (void(*)(void))handler_led_blink_B);
    led_B_state = !led_B_state;
    digitalWrite(LEDB, led_B_state);
  }
  else{
    stop_led_B_flag = false;
    led_B_task = 0;
  }
}
