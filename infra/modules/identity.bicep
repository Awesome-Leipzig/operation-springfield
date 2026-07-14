// Standalone module for the app's user-assigned managed identity. Split out from
// resources.bicep because Postgres Flexible Server's AAD administrator resource
// requires its `name` (the identity's principalId) to be passed in as a plain
// module parameter rather than referenced directly via `identity.properties.principalId`
// in the same deployment scope (Bicep BCP120: that expression isn't considered
// calculable at the start of the deployment when referenced in-scope).
@description('Primary Azure region for the identity resource.')
param location string

@description('Name of the user-assigned managed identity.')
param identityName string

@description('Tags applied to the identity resource.')
param tags object

resource identity 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' = {
  name: identityName
  location: location
  tags: tags
}

output id string = identity.id
output name string = identity.name
output principalId string = identity.properties.principalId
output clientId string = identity.properties.clientId
