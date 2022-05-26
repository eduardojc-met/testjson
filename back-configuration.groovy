
def test(String IBM_ACCESS_KEY_ID,String IBM_SECRET_ACCESS_KEY) {


 
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
         }
 bat 'docker push de.icr.io/devops-tools/'+"${appName}"+'-test:'+"${appVersion}"
}



 }



    stage('Deploying App to Kubernetes') {
      steps {
        script {
 def appname=readMavenPom().getArtifactId()

        def pomVersion = readMavenPom().getVersion()
	  dir("../"){

              def datas = readYaml file:"Deployment_mgateway-fra.yml"
               datas.metadata["name"]="${appName}"+'-test'
               datas.metadata.labels["run"]="${appName}"+'-test'
               datas[0].spec.selector.matchLabels=['app.kubernetes.io/component': "${appName}"+'-test', 'app.kubernetes.io/instance' : "${appName}"+'-test' ]
               datas[0].spec.template.spec.template.metadata.labels=['app.kubernetes.io/component': "${appName}"+'-test', 'app.kubernetes.io/instance' : "${appName}"+'-test', 'environment':'develop','run':"${appName}"+'-test' ]
                datas[0].spec.template.spec.template.spec.containers.name="${appName}"+'-test'
                datas[0].spec.template.spec.template.spec.containers.image='de.icr.io/devops-tools/'+"${appName}"+'-test:'+"${pomVersion}"
                datas[0].spec.template.spec.template.spec.containers.envFrom.secretRef=["name":"${appName}"]
                bat 'del Deployment_mgateway-fra.yml'
                writeYaml file: 'Deployment_mgateway-fra.yml', data: datas[0]

             //   bat 'kubectl apply -f back.yaml --namespace=develop'
  }
          
       /*  dir("C:/Program Files/IBM/Cloud/bin"){
             bat label: 'Login to ibmcloud', script: '''ibmcloud.exe login -u %IBM_ACCESS_KEY_ID% -p %IBM_SECRET_ACCESS_KEY% -r eu-de ''' 
           bat label: 'Login to ibm cr', script: '''ibmcloud.exe  cr login '''
           bat label: 'Configuring kubernetes', script: '''ibmcloud.exe ks cluster config -c c7pb9mkf09cf7vh8tmu0
 '''}
           */
            
                  
            
        
         
                
          
      
        }
      }
    }




}
return this
