
def test() {


 
    stage('Checkout Source') {
      
    
     
      
        git 'https://github.com/eduardojc-met/test-micros.git'
       
    }
 
 
 
    
    stage('Generating app') {
     script{
//bat 'mvnw package -Dnative -Dquarkus.container-image.group=santander -X -Dquarkus.native.container-build=true -Dquarkus.native.resources.includes=*.p12,reflection-config.json,cacerts -Dquarkus.native.container-runtime=docker -Dquarkus.native.debug.enabled=true'

     }
	
        
            
      }

 stage('Create & push image'){

script{
  def appVersion=readMavenPom().getVersion()
  def appName=readMavenPom().getArtifactId()
  bat 'docker build -f src/main/docker/Dockerfile.native -t quarkus/mgateway .'
bat 'docker tag quarkus/mgateway de.icr.io/devops-tools/'+"${appName}"+'-test:'+"${appVersion}"

dir("C:/Program Files/IBM/Cloud/bin"){
             bat label: 'Login to ibmcloud', script: '''ibmcloud.exe login -u %IBM_ACCESS_KEY_ID% -p %IBM_SECRET_ACCESS_KEY% -r eu-de ''' 
           bat label: 'Login to ibm cr', script: '''ibmcloud.exe  cr login '''
           bat label: 'Configuring kubernetes', script: '''ibmcloud.exe ks cluster config -c c7pb9mkf09cf7vh8tmu0
 '''}
 bat 'docker push de.icr.io/devops-tools/'+"${appName}"+'-test:'+"${appVersion}"
}



 }

stage('test'){
dir("../"){
  bat "dir"
}
}
/*
    stage('Deploying App to Kubernetes') {
      steps {
        script {

	
            def appname=readMavenPom().getArtifactId()
            
            def packageJSON = readJSON file: 'package.json'
        def packageJSONVersion = packageJSON.version 
        def pomVersion = readMavenPom().getVersion()
             dir("src/main/docker/backend") {
            
              def datas = readYaml file:"back.yaml"
               datas[0].metadata.labels=['io.kompose.service': "${appname}"+'-bff']
               
               
               
               datas[0].metadata["name"]="${appname}"+'-bff'
               datas[0].spec.selector.matchLabels=['io.kompose.service': "${appname}"+'-bff']
               datas[0].spec.template.spec.containers[0]["name"]="${appname}"+'-bff'
                datas[0].spec.template.spec.containers[0]["image"]='de.icr.io/devops-tools/'+"${appname}"+'-bff:'+"${pomVersion}"
               
                datas[0].spec.template.metadata.labels=['io.kompose.service': "${appname}"+'-bff']
              
              
              datas[1].metadata.labels=['io.kompose.service': "${appname}"+'-bff']
              datas[1].metadata["name"]="${appname}"+'-bff'
               datas[1].spec.selector=['io.kompose.service': "${appname}"+'-bff']
              
                bat 'del back.yaml'
                writeYaml file: 'back.yaml', data: datas[0]
              
             }
        
            dir("C:/Program Files/IBM/Cloud/bin"){
             bat label: 'Login to ibmcloud', script: '''ibmcloud.exe login -u %IBM_ACCESS_KEY_ID% -p %IBM_SECRET_ACCESS_KEY% -r eu-de ''' 
           bat label: 'Login to ibm cr', script: '''ibmcloud.exe  cr login '''
           bat label: 'Configuring kubernetes', script: '''ibmcloud.exe ks cluster config -c c7pb9mkf09cf7vh8tmu0
 '''}
            
                    dir("src/main/docker/backend") {
            bat 'kubectl apply -f back.yaml --namespace=develop'
            bat 'kubectl apply -f serviceback.yaml --namespace=develop'
         
                }
          
      
        }
      }
    }

  */


}
return this
