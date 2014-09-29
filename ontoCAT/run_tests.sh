mvn test site -DargLine="-Xms512m -Xmx2G -XX:PermSize=128m -XX:MaxPermSize=1G" \
    -Dsurefire.useFile=true \
 
	