#ifndef UBEEZ_LED
#define UBEEZ_LED

#include <mbed.h>
#include <rtos.h>
#include <mbed_wait_api.h>

void led_blink_R(int delay_ms);
void led_blink_G(int delay_ms);
void led_blink_B(int delay_ms);

void led_stop_R();
void led_stop_G();
void led_stop_B();

void handler_led_blink_R(int *delay_ms);
void handler_led_blink_G(int *delay_ms);
void handler_led_blink_B(int *delay_ms);

#endif