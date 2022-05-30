pipeline {
 
  environment {
       IBM_ACCESS_KEY_ID     = credentials('ibmuser')
        IBM_SECRET_ACCESS_KEY = credentials('ibmkey')
        project=credentials('project-name')
    }
 
  agent any
 
  stages {
 


  stage('Checkout Source') {
      
      steps {
        git //here the json repository
      }
    }
 

    stage('Get & use Configuration') {
      
      steps {
        script{


def json = readJSON file: 'configurations.json'             
json.each { myData ->
 
 def nameActual=myData.projectName.toString()

if(nameActual == "${project}") {
            
            def archivoconf=myData.stepsFile.toString()

def pipelineB = load archivoconf
           
bat "IF not exist ${nameActual} (mkdir ${nameActual})"

dir("${nameActual}") {
pipelineB.test("%IBM_ACCESS_KEY_ID%","%IBM_SECRET_ACCESS_KEY%")
}

           }
       
  

        }
  }
   


    }
      }
        }
          }
