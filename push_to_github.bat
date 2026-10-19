@echo off
cd /d "%~dp0"
echo ========================================================
echo Pushing Online Voting System to GitHub...
echo Repository: https://github.com/belviapereira/Online-Voting-System
echo ========================================================
echo.
git push -u origin main
if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo SUCCESS: Project files successfully pushed to GitHub!
    echo Visit: https://github.com/belviapereira/Online-Voting-System
    echo ========================================================
) else (
    echo.
    echo Push failed. If prompted, please complete GitHub authentication in your browser.
)
pause
