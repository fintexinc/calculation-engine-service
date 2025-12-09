// Development environment parameters for Calculation Engine Service
// Bicep parameter file format

using './main.bicep'

param managedEnvironmentId = '/subscriptions/7395443c-24f1-4f3e-8b05-e6cd06a64e0b/resourceGroups/rg-fintex-fndserv-dev-cc-01/providers/Microsoft.App/managedEnvironments/cae-fintex-fndserv-dev-cc-01'

// Container Configuration
param containerImage = 'crfintexfndservdevcc01.azurecr.io/calculation-engine-service:latest'
param containerName = 'calculation-engine-service-container'

// Ingress Configuration

// Scaling Configuration
param maxReplicas = 2

// Tags
param tags = {
  environment: 'dev'
  application: 'calculation-engine-service'
  managedBy: 'bicep'
  costCenter: 'engineering'
  project: 'fintex-fndserv'
}
