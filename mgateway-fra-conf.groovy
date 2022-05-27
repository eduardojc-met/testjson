
def test(String IBM_ACCESS_KEY_ID,String IBM_SECRET_ACCESS_KEY) {


 
    stage('Checkout Source') {
      
    
     
      
        git 'https://github.com/eduardojc-met/test-micros.git'
       
    }
 
 
 
    
    stage('Generating app') {
     script{
    //  bat 'mvnw package -Dnative -Dquarkus.container-image.group=santander -X -Dquarkus.native.container-build=true -Dquarkus.native.resources.includes=*.p12,reflection-config.json,cacerts -Dquarkus.native.container-runtime=docker -Dquarkus.native.debug.enabled=true'

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
      
        script {
 def appName=readMavenPom().getArtifactId()

        def pomVersion = readMavenPom().getVersion()
	  dir("../"){
       def datas = readYaml file:"Deployment_mgateway-fra.yml"
        datas.metadata["name"]="${appName}"+'-test'
         datas.metadata.labels["run"]="${appName}"+'-test'
          datas.spec.selector.matchLabels=['app.kubernetes.io/component': "${appName}"+'-test', 'app.kubernetes.io/instance' : "${appName}"+'-test' ]
        datas.spec.template.metadata.labels=['app.kubernetes.io/component': "${appName}"+'-test', 'app.kubernetes.io/instance' : "${appName}"+'-test', 'environment':'develop','run':"${appName}"+'-test' ]
         datas.spec.template.spec.containers[0]["name"]="${appName}"
         datas.spec.template.spec.containers[0]["image"]='de.icr.io/devops-tools/'+"${appName}"+'-test:'+"${pomVersion}"
datas.spec.template.spec.containers[0]["envFrom"][0]["configMapRef"]=["name":"${appName}"+'-test']        
datas.spec.template.spec.containers[0]["envFrom"][1]["secretRef"]=["name":"${appName}"+'-test']        

         bat 'del Deployment_mgateway-fra.yml'
         writeYaml file: 'Deployment_mgateway-fra.yml', data: datas






def service = readYaml file:"Service_mgateway-fra.yml"
 service.metadata["name"]="${appName}"+'-test'
service.metadata.labels=['app.kubernetes.io/instance': "${appName}"+'-test',"app.kubernetes.io/managed-by":"Helm","app.kubernetes.io/name":"${appName}"+'-test']
service.metadata.annotations=["meta.helm.sh/release-name":"${appName}"+'-test',"meta.helm.sh/release-namespace":"develop"]

service.spec.selector=['environment': 'develop','run':"${appName}"+'-test']


 bat 'del Service_mgateway-fra.yml'
 writeYaml file: 'Service_mgateway-fra.yml', data: service
  



		def ingress = readYaml file:"Ingress_mgateway-fra.yml"
                	ingress.metadata["name"]= "${appName}"+'-test'
          		ingress.metadata.labels=['app.kubernetes.io/instance': "${appName}"+'-test',"app.kubernetes.io/managed-by":"Helm","app.kubernetes.io/name":"${appName}"+'-test',"helm.sh/chart":"${appName}"+'-test',"run":"${appName}"+'-test']
              ingress.spec.tls[0]["hosts"]=["${appName}"+'-test'+".auto.cross.dev.scf-hq.com"]
         //si falla es por este de abajo
              ingress.spec.rules[0]["host"]=["${appName}"+'-test'+".scfhq-crossdev01-391a523e0203d3683790f242c9079785-0001.eu-de.containers.appdomain.cloud"]
             ingress.spec.rules[0].http.paths.backend.service[0]["name"]="${appName}"+'-test'
              ingress.spec.rules[1]["host"]=["${appName}"+'-test'+".auto.cross.dev.scf-hq.com"]
             ingress.spec.rules[1].http.paths.backend.service[0]["name"]="${appName}"+'-test'
              
              bat 'del Ingress_mgateway-fra.yml'
                writeYaml file: 'Ingress_mgateway-fra.yml', data: ingress


	def secret = readYaml file:"Secret_mgateway-fra.yml"
	secret.metadata["name"]= "${appName}"+'-test'
 bat 'del Secret_mgateway-fra.yml'
 writeYaml file: 'Secret_mgateway-fra.yml', data: secret















/*
 
 dir("C:/Program Files/IBM/Cloud/bin"){
             bat label: 'Login to ibmcloud', script: '''ibmcloud.exe login -u %IBM_ACCESS_KEY_ID% -p %IBM_SECRET_ACCESS_KEY% -r eu-de ''' 
           bat label: 'Login to ibm cr', script: '''ibmcloud.exe  cr login '''
           bat label: 'Configuring kubernetes', script: '''ibmcloud.exe ks cluster config -c c7pb9mkf09cf7vh8tmu0
 '''}
             //   bat 'kubectl apply -f back.yaml --namespace=develop'
             */
  }
          
        
           
            
                  
            
        
         
                
          
      
        }
      
    }




}
return this
