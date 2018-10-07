set CLASSPATH=.
FOR %%i IN (..\lib\*.*) DO (
	set CLASSPATH=%CLASSPATH%:%%i
)
echo %CLASSPATH%
Java -cp %CLASSPATH% br.com.joaoborges.filemanager.app.FileManager