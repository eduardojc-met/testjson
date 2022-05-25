

pipeline {
 
  environment {
       IBM_ACCESS_KEY_ID     = credentials('ibmuser')
        IBM_SECRET_ACCESS_KEY = credentials('ibmkey')
    }
 
 
  agent any
 
  stages {
 
    stage('Checkout Source') {
      
      steps {
        git 'https://github.com/eduardojc-met/pocdeployment.git'
      }
    }
 
 
   stage('build image') {
       
      steps{
        script{
        def packageJSON = readJSON file: 'package.json'
        def packageJSONVersion = packageJSON.version
        def appname=readMavenPom().getArtifactId()
           bat 'docker build -t edujc/frontnginxjunto:'+"${packageJSONVersion}"+' -f src/main/docker/frontend/Dockerfile . && docker tag edujc/frontnginxjunto:'+"${packageJSONVersion}"+' de.icr.io/devops-tools/'+"${appname}"+'-fe:'+"${packageJSONVersion}"
           bat 'mvn clean package -DskipTests -Pprod,tls '
           bat 'docker build -t edujc/backjunto:'+readMavenPom().getVersion()+' -f src/main/docker/backend/Dockerfile . && docker tag edujc/backjunto:'+readMavenPom().getVersion()+' de.icr.io/devops-tools/'+"${appname}"+'-bff:'+readMavenPom().getVersion()
        }
 
        }
        
     
      }
    
 
    stage ('push images') {
        steps{
        script{
             def packageJSON = readJSON file: 'package.json'
        def packageJSONVersion = packageJSON.version 
        def pomVersion = readMavenPom().getVersion()
        def appname=readMavenPom().getArtifactId()
            dir("C:/Program Files/IBM/Cloud/bin"){
                
               
            bat label: 'Login to ibmcloud', script: '''ibmcloud.exe login -u %IBM_ACCESS_KEY_ID% -p %IBM_SECRET_ACCESS_KEY% -r eu-de ''' 
            bat label: 'Install ibmcloud ks plugin', script: '''echo n | ibmcloud.exe plugin install container-service ''' 
            bat label: 'Install ibmcloud cr plugin', script: '''echo n | ibmcloud.exe  plugin install container-registry ''' 
            bat label: 'Login to ibm cr', script: '''ibmcloud.exe  cr login '''
            bat label: 'Configuring kubernetes', script: '''ibmcloud.exe ks cluster config -c c7pb9mkf09cf7vh8tmu0
 '''
            bat 'docker push de.icr.io/devops-tools/'+"${appname}"+'-fe:'+"${packageJSONVersion}"
            bat 'docker push de.icr.io/devops-tools/'+"${appname}"+'-bff:'+"${pomVersion}"
            
           
    
            }
                    
         
        }
        }
  
    } 
 