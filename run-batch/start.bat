@echo off
if exist "./tools/" (

    if exist ".\tools\jdk-21.0.2\" (
        del .\tools\jdk-21.0.2\
    )
    if exist "./tools/javafx-sdk-21.0.9" (
        del ./tools/javafx-sdk-21.0.9
    )
	
	if exist "./tools/7z2501" (
		rem 7z found
	) else (
		curl https://www.7-zip.org/a/7zr.exe --output ./tools/7zr.exe
		
		curl https://www.7-zip.org/a/7z2501-extra.7z --output ./tools/7z2501-extra.7z
		.\tools\7zr.exe x -o./tools/7z2501 ./tools/7z2501-extra.7z
		
		del .\tools\7zr.exe
		del .\tools\7z2501-extra.7z
	)
	
	if exist "./tools/jdk-21" (
		echo OpenJDK OK
	) else (
		curl https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_windows-x64_bin.zip --output ./tools/openjdk-21.0.2_windows-x64_bin.zip
		.\tools\7z2501\7za.exe x -o./tools/ ./tools/openjdk-21.0.2_windows-x64_bin.zip
		move .\tools\jdk-21.0.2\ .\tools\jdk-21\
	)
	
	if exist "./tools/javafx-sdk-21" (
		echo OpenFX OK
	) else (
		curl https://download2.gluonhq.com/openjfx/21.0.10/openjfx-21.0.10_windows-x64_bin-sdk.zip --output ./tools/openjfx-21.0.10_windows-x64_bin-sdk.zip
		.\tools\7z2501\7za.exe x -o./tools/ ./tools/openjfx-21.0.10_windows-x64_bin-sdk.zip
		move ./tools/javafx-sdk-21.0.9 ./tools/javafx-sdk-21
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

	echo Starting...
	
)
.\tools\jdk-21\bin\java.exe --module-path "./tools/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml -jar ./VRCVideoLogViewer-1.0-SNAPSHOT-all.jar
@echo on
pause