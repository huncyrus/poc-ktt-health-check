# KTT Health check
Proof of concept project, unfinished.  
.  
  
# Environment
 - Ubuntu Linux 20 (LTS)
 - MySql (V8.0.23-0)
 - NodeJS (v14, LTS)
 - NVM (https://github.com/nvm-sh/nvm)
 - NPM
 - OpenJDK 14  
  
---  
  
## Install required softwares
 - Install linux dependency:
`sudo apt install -y curl openjdk-14-jdk openjdk-14-jre openjdk-14-dbg gradle`  
 - Install NVM for nodeJS: 
`curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.37.2/install.sh | bash`  
   - Set shell: 
`export NVM_DIR="$([ -z "${XDG_CONFIG_HOME-}" ] && printf %s "${HOME}/.nvm" || printf %s "${XDG_CONFIG_HOME}/nvm")"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" # This loads nvm`  
 - Install latest nodeJS:
`nvm install --lts`  
  
 - Install MySQL:
`sudo apt install -y mysql-server mysql-client`
  
## Install project frontend dependency
`cd frontend && npm install`
  

## Compile backend
`cd backend/starter1 && ./gradle build`
  
.  
  
## Install project database
You can just simply import the ktt.sql file from the resources/database directory.  
`sudo mysql -u<username> -p<pass>`
`create database ktt_health_check;`
`exit;`
`sudo mysql -u<username> -p<pass> ktt_health_check < resources/database/ktt.sql`  
   
Ensure the MySQL user permission:  
Enter mysql: `sudo mysql`  
Create internal user: `CREATE USER 'testuser'@'%' IDENTIFIED BY 'pass';`  
Add permissions: `GRANT ALL PRIVILEGES ON *.* TO 'testuser'@'%';`  
Check permissions: `SHOW GRANTS FOR 'testuser';`  
Leave mysql: `exit;`  

---  
  
## Configuration
The project use a simple JSON file for backend in the folder of ```/src/main/conf/app-config.json``` you can found here the database credentials. You can change them. 
### Warning
If you going to change the webserver port, please change it in the frontend project also! You can found them in the `fetchData.js` file.  
  
---   
  
## Run project
Start the backend: ```cd backend/starter1 && ./java -jar ./build/libs/starter-1.0.0-SNAPSHOT-fat.jar -conf ./src/main/conf/app-config.json```  
  
Start the frontend: ```cd frontend && npm start``` then open a browser and use the following address: ```http://localhost:3000``` (standard React endpoint).  
  
.  
  
# Used tech
 - Backend
   - Java
   - MySql
 - Frontend
   - React
      - Bulma.io
      - Material Icons
      - .  
  
---  
  
# Note
 - The code quality and project quality is probably low, since I have zero-to-none experience or knowledge for Java, the project reflect a 16 hour or quick check & learn (and trial-by-fire) implementation session only.
 - The project is unfinished due technical issues with Vertx routes & payload handling (and lack of time).
 - Because of lack of time the project does not covered with unit tests, nor e2e tests.
 - The frontend is not released (ReactJs).
 - The project does not have authentication nor user handling just on database level. Every other interaction is hardcoded for testing purpose to the user "1".  
  
  .  

## Missing features:
 - Proper CRUD
 - User authentication & authorization
 - Update service entry
 - Enable/disable service entry
 - Remove service entry
 - Add service entry
