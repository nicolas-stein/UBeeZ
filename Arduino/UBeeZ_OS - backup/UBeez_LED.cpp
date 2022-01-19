#include "UBeeZ_LED.h"

using namespace rtos;

Thread *thread_LEDR_blink = NULL;
Thread *thread_LEDG_blink = NULL;
Thread *thread_LEDB_blink = NULL;

int pinLEDR = LEDR;
int pinLEDG = LEDG;
int pinLEDB = LEDB;

int *led_R_delay_ms = (int*) malloc(sizeof(int));
int *led_G_delay_ms = (int*) malloc(sizeof(int));
int *led_B_delay_ms = (int*) malloc(sizeof(int));

bool stop_led_R_flag = false;
bool stop_led_G_flag = false;
bool stop_led_B_flag = false;

void led_blink_R(int delay_ms){
  *led_R_delay_ms = delay_ms;
  thread_LEDR_blink = new Thread();
  thread_LEDR_blink->start(mbed::callback(handler_led_blink_R, led_R_delay_ms));
}

void led_blink_G(int delay_ms){
  *led_G_delay_ms = delay_ms;
  thread_LEDG_blink = new Thread();
  thread_LEDG_blink->start(mbed::callback(handler_led_blink_G, led_G_delay_ms));
}

void led_blink_B(int delay_ms){
  *led_B_delay_ms = delay_ms;
  thread_LEDB_blink = new Thread();
  thread_LEDB_blink->start(mbed::callback(handler_led_blink_B, led_B_delay_ms));
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

void handler_led_blink_R(int *delay_ms){
  std::chrono::milliseconds delay_chrono(*delay_ms);
  while(!stop_led_R_flag){
    digitalWrite(LEDR, LOW);
    ThisThread::sleep_for(delay_chrono);
    digitalWrite(LEDR, HIGH);
    ThisThread::sleep_for(delay_chrono);
  }
  stop_led_R_flag = false;
}

void handler_led_blink_G(int *delay_ms){
  std::chrono::milliseconds delay_chrono(*delay_ms);
  while(!stop_led_G_flag){
    digitalWrite(LEDG, LOW);
    ThisThread::sleep_for(delay_chrono);
    digitalWrite(LEDG, HIGH);
    ThisThread::sleep_for(delay_chrono);
  }
  stop_led_G_flag = false;
}

void handler_led_blink_B(int *delay_ms){
  std::chrono::milliseconds delay_chrono(*delay_ms);
  while(!stop_led_B_flag){
    digitalWrite(LEDB, LOW);
    ThisThread::sleep_for(delay_chrono);
    digitalWrite(LEDB, HIGH);
    ThisThread::sleep_for(delay_chrono);
  }
  stop_led_B_flag = false;
}