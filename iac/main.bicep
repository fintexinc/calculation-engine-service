// Main Bicep template for Portfolio Calculation Engine
// Azure Container App deployment

// Parameters
@description('Container App name')
param containerAppName string = 'portfolio-calculation-engine'

@description('The Azure region for the deployment')
param location string = 'Canada Central'

@description('Managed Environment Resource ID')
param managedEnvironmentId string

@description('Container image and tag')
param containerImage string = 'mcr.microsoft.com/k8se/quickstart:latest'

@description('Container name')
param containerName string = 'portfolio-calculation-engine-container'

@description('Container registry server')
param containerRegistryServer string = 'crfintexfndservdevcc01.azurecr.io'

@description('Container registry username')
@secure()
param containerRegistryUsername string = ''

@description('Container registry password')
@secure()
param containerRegistryPassword string = ''

@description('Target port for ingress')
param targetPort int = 8080

@description('Minimum replica count')
@minValue(0)
@maxValue(30)
param minReplicas int = 1

@description('Maximum replica count')
@minValue(1)
@maxValue(30)
param maxReplicas int = 5

@description('CPU cores allocated to container')
param cpuCore string = '0.5'

@description('Memory allocated to container')
param memory string = '1Gi'

@description('Security Master Service')
param securityMasterServiceUrl string = 'https://security-master-service.ashybay-bfa8feae.canadacentral.azurecontainerapps.io'

@description('FMP API Key')
@secure()
param fmpApiKey string = ''

@description('Spring Profile')
param springProfile string = ''

@description('Tags to apply to resources')
param tags object = {}

// Build secrets array conditionally
var secretsArray = concat(
  (empty(containerRegistryPassword) ? [] : [
    {
      name: 'container-registry-password'
      value: containerRegistryPassword
    }
  ]),
  (empty(fmpApiKey) ? [] : [
    {
      name: 'fmp-api-key'
      value: fmpApiKey
    }
  ])
)

// Build registries array conditionally
var registriesArray = empty(containerRegistryServer) ? [] : [
  {
    server: containerRegistryServer
    username: containerRegistryUsername
    passwordSecretRef: 'container-registry-password'
  }
]

// Build environment variables array conditionally
var profileEnvVars = empty(springProfile) ? [] : [
  {
    name: 'SPRING_PROFILES_ACTIVE'
    value: springProfile
  }
]

var baseEnvVars = [
  {
    name: 'JAVA_OPTS'
    value: '-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC'
  }
  {
    name: 'SM_REST_BASE_URL'
    value: securityMasterServiceUrl
  }
]

var apiEnvVars = empty(fmpApiKey) ? [] : [
  {
    name: 'FMP_API_KEY'
    secretRef: 'fmp-api-key'
  }
]

var allEnvVars = concat(profileEnvVars, baseEnvVars, apiEnvVars)

// Container App
resource containerApp 'Microsoft.App/containerApps@2025-02-02-preview' = {
  name: containerAppName
  location: location
  kind: 'containerapps'
  tags: tags
  identity: {
    type: 'None'
  }
  properties: {
    managedEnvironmentId: managedEnvironmentId
    environmentId: managedEnvironmentId
    workloadProfileName: 'Consumption'
    configuration: {
      activeRevisionsMode: 'Single'
      ingress: {
        external: true
        targetPort: targetPort
        exposedPort: 0
        transport: 'Auto'
        allowInsecure: false
        traffic: [
          {
            weight: 100
            latestRevision: true
          }
        ]
      }
      registries: registriesArray
      secrets: secretsArray
      identitySettings: []
      maxInactiveRevisions: 100
    }
    template: {
      containers: [
        {
          image: containerImage
          imageType: 'ContainerImage'
          name: containerName
          resources: {
            cpu: json(cpuCore)
            memory: memory
          }
          env: allEnvVars
        }
      ]
      scale: {
        minReplicas: minReplicas
        maxReplicas: maxReplicas
        cooldownPeriod: 300
        pollingInterval: 30
      }
    }
  }
}

// Outputs
output containerAppFQDN string = containerApp.properties.configuration.ingress.fqdn
output containerAppName string = containerApp.name
output containerAppId string = containerApp.id