# TTUAuth
Texas Tech University Authentication Library. This library allows programmatic client access to scrape Texas Tech University sites and data sources for given student user accounts.

## How to get HTTPS running with Fiddler:
1. Export Fiddler Root Certificate to desktop.
2. Copy the file to the <JDK_HOME>\bin\ directory. It should look like this: C:\Program Files\Java\jdk1.8.0_25\bin
3. CMD to the <JDK_HOME>\bin\ directory.
4. Type as follows: keytool.exe -import -file FiddlerRoot.cer -keystore FiddlerKeystore -alias Fiddler
5. Enter a password (remember it lol)
6. Create the file \src\com\maximusvladimir\ttuauth\tests\keystore.dat
7. In the first line put <JDK_HOME>\bin\FiddlerKeystore
8. In the second line put your password.
9. Make sure no other lines in the file exist.
10. Save.
