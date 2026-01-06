#!/bin/sh

# for Arch PKGBUILD / Ubuntu deb Package
## cd /usr/share/VRCVideoLogViewer

if [ -d ./tools ]; then
	
	if [ -d ./tools/7z2501 ]; then
		echo "7z OK"
	else
		curl https://www.7-zip.org/a/7z2501-linux-x64.tar.xz --output ./tools/7z2501-linux-x64.tar.xz
		tar -Jxvf ./tools/7z2501-linux-x64.tar.xz
		mkdir ./tools/7z2501
		mv ./7zz ./tools/7z2501
		mv ./7zzs ./tools/7z2501
		mv ./History.txt ./tools/7z2501
		mv ./License.txt ./tools/7z2501
		mv ./MANUAL ./tools/7z2501
		mv ./readme.txt ./tools/7z2501
		chmod +x ./tools/7z2501/7zz
	fi
	
	if [ -d ./tools/jdk-21.0.2 ]; then
		echo "OpenJDK21 OK"
	else
		curl https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_linux-x64_bin.tar.gz --output ./tools/openjdk-21.0.2_linux-x64_bin.tar.gz
		tar -zxvf ./tools/openjdk-21.0.2_linux-x64_bin.tar.gz
		mv ./jdk-21.0.2 ./tools
		chmod +x ./tools/jdk-21.0.2/bin/java
	fi

	if [ -d ./tools/javafx-sdk-21.0.9 ]; then
		echo "OpenFX OK"
	else
		curl https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_linux-x64_bin-sdk.zip --output ./tools/openjfx-21.0.9_linux-x64_bin-sdk.zip
		./tools/7z/7zz x ./tools/openjfx-21.0.9_linux-x64_bin-sdk.zip
		mv ./javafx-sdk-21.0.9 ./tools
		
	fi

	echo "Starting..."
	./tools/jdk-21.0.2/bin/java --module-path "./tools/javafx-sdk-21.0.9/lib" --add-modules javafx.controls,javafx.fxml -jar ./VRCVideoLogViewer-1.0-SNAPSHOT-all.jar
else 
	mkdir ./tools
	
	curl https://www.7-zip.org/a/7z2501-linux-x64.tar.xz --output ./tools/7z2501-linux-x64.tar.xz
	tar -Jxvf ./tools/7z2501-linux-x64.tar.xz
	mkdir ./tools/7z2501
	mv ./7zz ./tools/7z2501
	mv ./7zzs ./tools/7z2501
	mv ./History.txt ./tools/7z2501
	mv ./License.txt ./tools/7z2501
	mv ./MANUAL ./tools/7z2501
	mv ./readme.txt ./tools/7z2501
	chmod +x ./tools/7z2501/7zz
	
	curl https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_linux-x64_bin.tar.gz --output ./tools/openjdk-21.0.2_linux-x64_bin.tar.gz
	tar -zxvf ./tools/openjdk-21.0.2_linux-x64_bin.tar.gz
	mv ./jdk-21.0.2 ./tools
	chmod +x ./tools/jdk-21.0.2/bin/java

	curl https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_linux-x64_bin-sdk.zip --output ./tools/openjfx-21.0.9_linux-x64_bin-sdk.zip
	./tools/7z2501/7zz x ./tools/openjfx-21.0.9_linux-x64_bin-sdk.zip
	mv ./javafx-sdk-21.0.9 ./tools

	echo "Starting..."
	./tools/jdk-21.0.2/bin/java --module-path "./tools/javafx-sdk-21.0.9/lib" --add-modules javafx.controls,javafx.fxml -jar ./VRCVideoLogViewer-1.0-SNAPSHOT-all.jar
	
fi