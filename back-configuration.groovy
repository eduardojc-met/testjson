
def test() {


 
    stage('Checkout Source') {
      
    
     
      
        git 'https://github.com/eduardojc-met/pocdeployment.git'
       
    }
 
 
 
    
    stage('Deploying App to Kubernetes') {
     
	echo "estoyyyyyy"
               writeYaml file: 'ocemi', data: "keloke"

         bat "dir"
            
      }

 
  


}
return this
/*
pipeline {
 
 // environment {
   //    IBM_ACCESS_KEY_ID     = credentials('ibmuser')
     //   IBM_SECRET_ACCESS_KEY = credentials('ibmkey')
    //}
 
 
  agent any
 
  stages {
 
    stage('Checkout Source') {
      
      steps {
        git 'https://github.com/eduardojc-met/pocdeployment.git'
      }
    }
 
 
 
    

    stage('Deploying App to Kubernetes') {
      steps {
        script {

	
               writeYaml file: 'kelokeeeeeeeeeeeee', data: "keloke"

         
             }
           




      
        }
      }
    

 
 

 
  }
  
  }
  */