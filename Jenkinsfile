pipeline {
  agent {
    node {
      label 'maven && jdk11'
    }
  }
  environment {
    DOCKER_CREDENTIAL_ID = 'aliyun-id'
    REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
    DOCKERHUB_NAMESPACE = 'geeksnail'
    GIT_CREDENTIAL_ID = 'github-id'
    GIT_URL = 'https://github.com/geeksnail/gulimall.git'
    GIT_BRANCH = 'main'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
  }
  parameters {
    string(name: 'TAG_NAME', defaultValue: '0.1', description: '')
    choice(name: 'APP_NAME', choices: ['gulimall-auth', 'gulimall-cart', 'gulimall-coupon', 'gulimall-gateway', 'gulimall-member', 'gulimall-order', 'gulimall-product', 'gulimall-search', 'gulimall-seckill', 'gulimall-thirdparty', 'gulimall-ware', 'renren-fast'], description: 'module')
  }
  stages {
    stage('clone code') {
      when {
        expression {params.APP_NAME && params.TAG_NAME}
      }
      agent none
      steps {
        //sh 'echo $GIT_CREDENTIAL_ID $GIT_URL $APP_NAME:$TAG_NAME'
        git credentialsId: env.GIT_CREDENTIAL_ID, url: env.GIT_URL, branch: env.GIT_BRANCH, changelog: true, poll: false
      }
    }
    stage('build & push') {
      steps {
        container('maven') {
          sh '''
          mvn -DskipTests -gs `pwd`/settings.xml package
          cd $APP_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME-SNAPSHOT-$BUILD_NUMBER .
          '''
          withCredentials([usernamePassword(passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME', credentialsId: "$DOCKER_CREDENTIAL_ID")]) {
            sh '''
            echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin
            docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME-SNAPSHOT-$BUILD_NUMBER
            '''
          }
        }
      }
    }
    stage('deploy to dev') {
      steps {
        container('maven') {
          //input(id: 'deploy-to-dev', message: 'deploy to dev?')
          withCredentials([kubeconfigFile(credentialsId: env.KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG')]) {
            sh 'envsubst < $APP_NAME/deploy-dev.yaml | kubectl apply -f -'
          }
        }
      }
    }
    stage('parallel push') {
      when {
        allOf{
          //anyOf { branch 'master'; branch 'main' }
          expression {params.APP_NAME && params.TAG_NAME}
        }
      }
      parallel {
        stage('push tag') {
          steps {
            container('maven') {
              sh '''
              docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME-SNAPSHOT-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME
              docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME
              '''
            }
          }
        }
        stage('push latest') {
          steps {
            container('maven') {
              sh '''
              docker tag $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME-SNAPSHOT-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest
              docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest
              '''
            }
          }
        }
      }
    }
  }
}