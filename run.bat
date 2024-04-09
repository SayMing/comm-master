@set ss=.;%classpath%

@set ss=%ss%;.\bin
@set ss=%ss%;.\lib\logback-core-1.2.13.jar
@set ss=%ss%;.\lib\logback-site-1.2.13.jar
@set ss=%ss%;.\lib\RXTXcomm.jar
@set ss=%ss%;.\lib\slf4j-api-1.7.36.jar
@set ss=%ss%;.\lib\sqlite-jdbc-3.44.1.0.jar
@set ss=%ss%;.\lib\swing-layout-1.0.3.jar
@set ss=%ss%;.\lib\AbsoluteLayout.jar
@set ss=%ss%;.\lib\hutool-all-5.8.16.jar
@set ss=%ss%;.\lib\hutool-all-5.8.16-sources.jar
@set ss=%ss%;.\lib\logback-access-1.2.13.jar
@set ss=%ss%;.\lib\logback-classic-1.2.13.jar

@set sss=%path%
@path %path%;.\lib

@echo running...
@D:\Java\JDK1.8_32\bin\javaw.exe -Dfile.encoding=UTF-8 -classpath %ss% com.yang.serialport.ui.CarMainFrame

@path %sss%

exit