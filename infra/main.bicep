// Entry point for `azd provision` / `azd up`. Subscription-scoped: references an
// existing resource group (deploys into rg-swo-gh-hackathon-team2, pre-created by
// the org with its own SWO tagging conventions — this template does NOT create or
// modify the resource group itself, only deploys resources inside it), then
// delegates all resource definitions to resources.bicep.
targetScope = 'subscription'

@minLength(1)
@maxLength(64)
@description('Name of the azd environment; used to derive resource names.')
param environmentName string

@minLength(1)
@description('Primary Azure region for all resources.')
param location string

@minLength(1)
@description('Name of the pre-existing resource group to deploy into.')
param resourceGroupName string

@description('Id of the principal running `azd provision` (for local dev DB access). Optional.')
param principalId string = ''

var resourceToken = toLower(uniqueString(subscription().id, environmentName, location))
// Mirrors the org's tagging convention already applied to rg-swo-gh-hackathon-team2,
// so cost/ownership reporting on child resources lines up with the resource group.
var tags = {
  'azd-env-name': environmentName
  'swo-business-unit': 'Application Services'
  'swo-cost-center': '03.30'
  'swo-delivery-center': 'DACH'
}

resource rg 'Microsoft.Resources/resourceGroups@2025-04-01' existing = {
  name: resourceGroupName
}

module identity 'modules/identity.bicep' = {
  name: 'identity-${resourceToken}'
  scope: rg
  params: {
    location: location
    identityName: 'id-${resourceToken}'
    tags: tags
  }
}

module resources 'resources.bicep' = {
  name: 'resources-${resourceToken}'
  scope: rg
  params: {
    location: location
    resourceToken: resourceToken
    tags: tags
    principalId: principalId
    identityResourceId: identity.outputs.id
    identityPrincipalId: identity.outputs.principalId
    identityClientId: identity.outputs.clientId
    identityName: identity.outputs.name
  }
}

output AZURE_RESOURCE_GROUP string = rg.name
  output AZURE_CONTAINER_REGISTRY_ENDPOINT string = resources.outputs.containerRegistryEndpoint
output AZURE_CONTAINER_APPS_ENVIRONMENT_ID string = resources.outputs.containerAppsEnvironmentId
output AZURE_CONTAINER_APP_NAME string = resources.outputs.containerAppName
output SERVICE_WEB_URI string = resources.outputs.containerAppUri
output APPLICATIONINSIGHTS_CONNECTION_STRING string = resources.outputs.appInsightsConnectionString
output SPRING_DATASOURCE_URL string = resources.outputs.postgresJdbcUrl
output AZURE_KEY_VAULT_ENDPOINT string = resources.outputs.keyVaultEndpoint
