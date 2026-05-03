@echo off
chcp 65001 >nul
echo ========================================
echo Draw.io 批量导出PNG图片脚本
echo ========================================
echo.
echo 请确保已安装 Draw.io 桌面版
echo 默认安装路径: C:\Program Files\draw.io\draw.io.exe
echo.

set DRAWIO_PATH="C:\Program Files\draw.io\draw.io.exe"
set INPUT_DIR=docs\figures
set OUTPUT_DIR=docs\figures\images

if not exist %OUTPUT_DIR% mkdir %OUTPUT_DIR%

echo 开始导出...
echo.

for %%f in (%INPUT_DIR%\*.drawio) do (
    echo 正在导出: %%~nxf
    %DRAWIO_PATH% --export --format png --scale 2 --crop --background #ffffff --output "%OUTPUT_DIR%\%%~nf.png" "%%f"
)

echo.
echo ========================================
echo 导出完成！
echo 图片保存位置: %OUTPUT_DIR%
echo ========================================
pause
