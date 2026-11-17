custom_build(
    ref = 'edge-service',
    command = './gradlew bootBuildImage --imageName $EXPECTED_REF && minikube image load $(tilt dump image-deploy-ref $EXPECTED_REF) --profile polar',
    deps = ['build.gradle', 'src'],
    disable_push = True,
)

k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])
k8s_resource('edge-service', port_forwards=['9000'])