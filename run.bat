@echo off
cd /d "%~dp0"
set CP="C:\Users\Belvia Pereira\.m2\repository\org\xerial\sqlite-jdbc\3.45.1.0\sqlite-jdbc-3.45.1.0.jar;C:\Users\Belvia Pereira\.m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;C:\Users\Belvia Pereira\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar;C:\Users\Belvia Pereira\.m2\repository\org\jfree\jfreechart\1.5.4\jfreechart-1.5.4.jar;target\classes"
start javaw -cp %CP% com.votingsystem.Main
