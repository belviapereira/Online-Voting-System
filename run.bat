@echo off
cd /d "%~dp0"
setlocal

set M2=%USERPROFILE%\.m2\repository
set CP=%M2%\org\xerial\sqlite-jdbc\3.45.1.0\sqlite-jdbc-3.45.1.0.jar;%M2%\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;%M2%\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar;%M2%\org\jfree\jfreechart\1.5.4\jfreechart-1.5.4.jar;target\classes

if not exist "target\classes\com\votingsystem\Main.class" (
    echo Compiling project first...
    call mvn -q compile
    if errorlevel 1 (
        echo Build failed. Make sure Maven is installed and on your PATH.
        pause
        exit /b 1
    )
)

start javaw -cp "%CP%" com.votingsystem.Main
endlocal
