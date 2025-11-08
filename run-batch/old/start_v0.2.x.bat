@echo off
if exist "./tools/" (
	echo Starting...
) else (
	mkdir tools
	curl https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_windows-x64_bin.zip --output ./tools/openjdk-21.0.2_windows-x64_bin.zip
	tar -xf ./tools/openjdk-21.0.2_windows-x64_bin.zip
	move ./jdk-21.0.2 ./tools
	
	curl https://download2.gluonhq.com/openjfx/21.0.8/openjfx-21.0.8_windows-x64_bin-sdk.zip --output ./tools/openjfx-21.0.8_windows-x64_bin-sdk.zip
	tar -xf ./tools/openjfx-21.0.8_windows-x64_bin-sdk.zip
	move ./javafx-sdk-21.0.8 ./tools
	
	
	echo Starting...
	
)
.\tools\jdk-21.0.2\bin\java.exe --module-path "./tools/javafx-sdk-21.0.8/lib" --add-modules javafx.controls,javafx.fxml -jar ./VRCVideoLogViewer-1.0-SNAPSHOT-all.jar
@echo on
pause