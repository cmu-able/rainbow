@echo off

C:
chdir C:\server\cygwin\bin
set DEMO_MODE=%1
set DEMO_TITLE=%2

bash --login -i
