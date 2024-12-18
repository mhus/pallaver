#!/bin/bash

cd /app
java  -Dspring.profiles.active=prod -jar /app/pallaver.jar
