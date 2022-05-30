
def test(String IBM_ACCESS_KEY_ID,String IBM_SECRET_ACCESS_KEY) {

 environment {
      git_commit='123'
       docker_push_id='123'
    }
 
    stage('Checkout Source') {
      
    
     
      
      script{
      def git_command=  git 'https://github.com/eduardojc-met/test-micros.git'
      
           git_commit= git_command["GIT_COMMIT"]
           echo "${git_commit}"
          
      }
       
    }
 
 

    
    stage('Generating app') {
     script{
      bat 'mvn package -Dnative -Dquarkus.container-image.group=santander -X -Dquarkus.native.container-build=true -Dquarkus.native.resources.includes=*.p12,reflection-config.json,cacerts -Dquarkus.native.container-runtime=docker -Dquarkus.native.debug.enabled=true -DskipTests'

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
            bat label: 'Install ibmcloud ks plugin', script: '''echo n | ibmcloud.exe plugin install container-service ''' 
            bat label: 'Install ibmcloud cr plugin', script: '''echo n | ibmcloud.exe  plugin install container-registry ''' 
            bat label: 'Login to ibm cr', script: '''ibmcloud.exe  cr login '''
           
         }
 bat 'docker push de.icr.io/devops-tools/'+"${appName}"+'-test:'+"${appVersion}"
//  bat 'docker inspect '+"${appName}"+'-test:'+"${appVersion}"+' > dockerpushid.json'
  //     def packageJSON = readJSON file: 'dockerpushid.json' 
    //    docker_push_id = packageJSON[0].Id.toString().replace("sha256:","")
    

//		bat 'del dockerpushid.json'
}



 }



    stage('Deploying App to Kubernetes') {
      
        script {
 def appName=readMavenPom().getArtifactId()

        def pomVersion = readMavenPom().getVersion()
	  dir("../"){
       def datas = readYaml file:"Deployment_mgateway-nld.yml"
        datas.metadata["name"]="${appName}"+'-test'
         datas.metadata.labels["run"]="${appName}"+'-test'
         datas.metadata.annontations["last-image-push-id"]=docker_push_id
          
       
         datas.metadata.annontations["last-commit-sha"]=git_commit

          datas.spec.selector.matchLabels=['app.kubernetes.io/component': "${appName}"+'-test', 'app.kubernetes.io/instance' : "${appName}"+'-test' ]
        datas.spec.template.metadata.labels=['app.kubernetes.io/component': "${appName}"+'-test', 'app.kubernetes.io/instance' : "${appName}"+'-test', 'environment':'microgateway','run':"${appName}"+'-test' ]
         datas.spec.template.spec.containers[0]["name"]="${appName}"+'-test'
         datas.spec.template.spec.containers[0]["image"]='de.icr.io/devops-tools/'+"${appName}"+'-test:'+"${pomVersion}"
     
datas.spec.template.spec.containers[0]["envFrom"][1]["secretRef"]=["name":"${appName}"+'-test']        

         bat 'del Deployment_mgateway-nld.yml'
         writeYaml file: 'Deployment_mgateway-nld.yml', data: datas






def service = readYaml file:"Service_mgateway-nld.yml"
 service.metadata["name"]="${appName}"+'-test'
service.metadata.labels=['app.kubernetes.io/instance': "${appName}"+'-test',"app.kubernetes.io/managed-by":"Helm","app.kubernetes.io/name":"${appName}"+'-test']
service.metadata.annotations=["meta.helm.sh/release-name":"${appName}"+'-test',"meta.helm.sh/release-namespace":"microgateway"]

service.spec.selector=['environment': 'microgateway','run':"${appName}"+'-test']


 bat 'del Service_mgateway-nld.yml'
 writeYaml file: 'Service_mgateway-nld.yml', data: service
  



		def ingress = readYaml file:"Ingress_mgateway-nld.yml"
                	ingress.metadata["name"]= "${appName}"+'-test'
          		ingress.metadata.labels=['app.kubernetes.io/instance': "${appName}"+'-test',"app.kubernetes.io/managed-by":"Helm","app.kubernetes.io/name":"${appName}"+'-test',"helm.sh/chart":"${appName}"+'-test',"run":"${appName}"+'-test']
              ingress.spec.rules[0].http.paths.backend.service[0]["name"]="${appName}"+'-test'
             ingress.spec.rules[1].http.paths.backend.service[0]["name"]="${appName}"+'-test'
              
              bat 'del Ingress_mgateway-nld.yml'
                writeYaml file: 'Ingress_mgateway-nld.yml', data: ingress


	def secret = readYaml file:"Secret_mgateway-nld.yml"
	secret.metadata["name"]= "${appName}"+'-test'
 bat 'del Secret_mgateway-nld.yml'
 writeYaml file: 'Secret_mgateway-nld.yml', data: secret
















 
 dir("C:/Program Files/IBM/Cloud/bin"){
             bat label: 'Login to ibmcloud', script: '''ibmcloud.exe login -u %IBM_ACCESS_KEY_ID% -p %IBM_SECRET_ACCESS_KEY% -r eu-de ''' 
           bat label: 'Login to ibm cr', script: '''ibmcloud.exe  cr login '''
          bat label: 'Configuring kubernetes', script: '''ibmcloud.exe ks cluster config -c c7pb9jff0n7t4elurev0
 '''}
            
            bat 'kubectl apply -f Deployment_mgateway-nld.yml --namespace=microgateway'
             
               bat 'kubectl apply -f Service_mgateway-nld.yml --namespace=microgateway'
               bat 'kubectl apply -f Ingress_mgateway-nld.yml --namespace=microgateway'
               bat 'kubectl apply -f Secret_mgateway-nld.yml --namespace=microgateway'
           
  }
          
        
           
            
                  
            
        
         
                
          
      
        }
      
    }




}
return this
