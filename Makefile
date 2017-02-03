all: CSftp.jar run
CSftp.jar:
	javac -d ./ ./src/ftpClient/Connector.java
	jar cmvf MANIFEST.MF CSftp.jar ./ftpClient/

run:
	java -jar CSftp.jar ftp.cs.ubc.ca  21

clean:
	rm -Rf ./ftpClient/
	rm -f ./CSftp.jar