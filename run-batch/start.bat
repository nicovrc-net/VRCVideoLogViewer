@echo off
if exist "./tools/" (
	
	if exist "./tools/7z2501" (
		rem 7z found
	) else (
		curl https://www.7-zip.org/a/7zr.exe --output ./tools/7zr.exe
		
		curl https://www.7-zip.org/a/7z2501-extra.7z --output ./tools/7z2501-extra.7z
		.\tools\7zr.exe x -o./tools/7z2501 ./tools/7z2501-extra.7z
		
		del .\tools\7zr.exe
		del .\tools\7z2501-extra.7z
	)
	
	if exist "./tools/jdk-21.0.2" (
		echo OpenJDK OK
	) else (
		curl https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_windows-x64_bin.zip --output ./tools/openjdk-21.0.2_windows-x64_bin.zip
		.\tools\7z2501\7za.exe x -o./tools/ ./tools/openjdk-21.0.2_windows-x64_bin.zip
	)
	
	if exist "./tools/javafx-sdk-21.0.9" (
		echo OpenFX OK
	) else (
		curl https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip --output ./tools/openjfx-21.0.9_windows-x64_bin-sdk.zip
		.\tools\7z2501\7za.exe x -o./tools/ ./tools/openjfx-21.0.9_windows-x64_bin-sdk.zip
	)
	
	if exist "./tools/ImageMagick-7.1.2-8-portable-Q16-x64" (
		echo ImageMagick OK
	) else (
		curl https://imagemagick.org/archive/binaries/ImageMagick-7.1.2-8-portable-Q16-x64.7z --output ./tools/ImageMagick-7.1.2-8-portable-Q16-x64.7z
		.\tools\7z2501\7za.exe x -o./tools/ImageMagick-7.1.2-8-portable-Q16-x64 ./tools/ImageMagick-7.1.2-8-portable-Q16-x64.7z
	)

	if exist "./fonts" (
	    if exist "./fonts/NotoSansCJK-Regular.ttc" (
	        echo "Font OK"
	    ) else (
	        curl -L "https://github.com/googlefonts/noto-cjk/raw/main/Sans/Variable/OTC/NotoSansCJK-VF.ttf.ttc" --output ./fonts/NotoSansCJK-Regular.ttc
	    )
	) else (
	    mkdir fonts
	    curl -L "https://github.com/googlefonts/noto-cjk/raw/main/Sans/Variable/OTC/NotoSansCJK-VF.ttf.ttc" --output ./fonts/NotoSansCJK-Regular.ttc
	)
	
	echo Starting...
	
) else (
	
	mkdir tools
	
	curl https://www.7-zip.org/a/7zr.exe --output ./tools/7zr.exe
	
	curl https://www.7-zip.org/a/7z2501-extra.7z --output ./tools/7z2501-extra.7z
	.\tools\7zr.exe x -o./tools/7z2501 ./tools/7z2501-extra.7z
	
	del .\tools\7zr.exe
	del .\tools\7z2501-extra.7z
	
	curl https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_windows-x64_bin.zip --output ./tools/openjdk-21.0.2_windows-x64_bin.zip
	.\tools\7z2501\7za.exe x -o./tools/ ./tools/openjdk-21.0.2_windows-x64_bin.zip
	
	curl https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip --output ./tools/openjfx-21.0.9_windows-x64_bin-sdk.zip
	.\tools\7z2501\7za.exe x -o./tools/ ./tools/openjfx-21.0.9_windows-x64_bin-sdk.zip
	
	curl https://imagemagick.org/archive/binaries/ImageMagick-7.1.2-8-portable-Q16-x64.7z --output ./tools/ImageMagick-7.1.2-8-portable-Q16-x64.7z
	.\tools\7z2501\7za.exe x -o./tools/ImageMagick-7.1.2-8-portable-Q16-x64 ./tools/ImageMagick-7.1.2-8-portable-Q16-x64.7z

	mkdir fonts
	curl -L "https://github.com/googlefonts/noto-cjk/raw/main/Sans/Variable/OTC/NotoSansCJK-VF.ttf.ttc" --output ./fonts/NotoSansCJK-Regular.ttc
	
	echo Starting...
	
)
.\tools\jdk-21.0.2\bin\java.exe --module-path "./tools/javafx-sdk-21.0.9/lib" --add-modules javafx.controls,javafx.fxml -jar ./VRCVideoLogViewer-1.0-SNAPSHOT-all.jar
@echo on
pause