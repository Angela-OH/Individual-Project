#!/usr/bin/env python3
import math
from ev3dev2.sensor.lego import TouchSensor, ColorSensor, UltrasonicSensor, GyroSensor
from ev3dev2.motor import OUTPUT_A, OUTPUT_B, MoveTank
from ev3dev2.sensor import INPUT_1, INPUT_2, INPUT_3, INPUT_4
from ev3dev2.sound import Sound
from ev3dev2.display import Display
from ev3dev2.button import Button

def using_gyroscope(gyro, move, input_angle, speed_left, speed_right):
    angle = gyro.angle
    while abs(gyro.angle - angle) != input_angle:
        move.on(50, 0)

def main():
    """Declare all inputs and outputs we are using."""
    # Outputs
    left_wheel = OUTPUT_A
    right_wheel = OUTPUT_B
    # Inputs
    input_color = INPUT_1
    input_gyro = INPUT_2
    input_touch = INPUT_3
    input_ultrasonic = INPUT_4

    """Create classes"""
    move = MoveTank(left_wheel, right_wheel)
    move.gyro = GyroSensor()
    #color = ColorSensor(input_color)
    touch = TouchSensor(input_touch)
    ultrasonic = UltrasonicSensor(input_ultrasonic)

    """For feedback from the robot"""
    display = Display()
    sound = Sound()
    button = Button()

    distance = ultrasonic.distance_centimeters
    move.on(30,30)
    while distance > 25:        
        distance=ultrasonic.distance_centimeters
    move.on(0,0)

    # move.gyro.calibrate()
    # move.turn_degrees(50, 180)

    using_gyroscope(gyro, move, 180, 50, 0)

    move.on_for_rotations(50, 50, 5)

    move.turn_degrees(50, 90)

    # using_gyroscope(gyro, move, 90, 50, 0)
    
    # move.on(50, 50)

    # detecting color

    # using_gyroscope(gyro, move, 90, 50, 0)

    move.on(0, 0)

   
if __name__ == '__main__':
    main()