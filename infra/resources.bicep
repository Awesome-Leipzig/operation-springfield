// Resource-group-scoped module: everything needed to run the Sector 7G Safety
// Ledger on Azure Container Apps with a passwordless (managed-identity) Postgres
// database, Key Vault for the remaining app secret (API key), and telemetry via
// Application Insights.
//
// NOTE: this is IaC scaffolding generated ahead of an actual `azd provision`/`azd up`
// run. Real provisioning requires an authenticated Azure subscription (`az login` /
// `azd auth login`) — see Phase 4 tracker item "DEPLOYED to Azure" for that
// human-in-the-loop checkpoint. This file has been validated with `az bicep build`
// but not yet deployed.

@description('Primary Azure region for all resources.')
param location string

@description('Short unique token derived from the azd environment, used to keep resource names globally unique.')
param resourceToken string

@description('Tags applied to every resource, including the azd environment tag.')
param tags object

@description('Principal id of the user running `azd provision`, granted DB/Key Vault access for local development. Optional.')
param principalId string = ''

@description('Resource id of the pre-created user-assigned managed identity (see modules/identity.bicep).')
param identityResourceId string

@description('Principal (object) id of the pre-created user-assigned managed identity.')
param identityPrincipalId string

@description('Client id of the pre-created user-assigned managed identity.')
param identityClientId string

@description('Name of the pre-created user-assigned managed identity.')
param identityName string

@description('Whether to create the RBAC role assignments (ACR pull, Key Vault Secrets User) for the managed identity. Requires Microsoft.Authorization/roleAssignments/write at the resource group scope (e.g. Owner or User Access Administrator) — Contributor alone is NOT sufficient. Set to false to fall back to ACR admin-credential-based pull and a plain env var for the API key; flip back to true and re-run `azd provision` once someone with sufficient permission is available.')
param assignRoles bool = true

var containerAppName = 'ca-${resourceToken}'
var containerRegistryName = 'acr${resourceToken}'
var keyVaultName = 'kv-${resourceToken}'
var postgresServerName = 'psql-${resourceToken}'
var postgresDatabaseName = 'sector7g'
var logAnalyticsName = 'log-${resourceToken}'
var appInsightsName = 'appi-${resourceToken}'
var managedEnvironmentName = 'cae-${resourceToken}'

var acrPullRoleId = '7f951dda-4ed3-4680-a7ca-43fe172d538d'
var keyVaultSecretsUserRoleId = '4633458b-17de-408a-b874-0445c86b69e6'

// --- Observability ------------------------------------------------------------

resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: logAnalyticsName
  location: location
  tags: tags
  properties: {
    retentionInDays: 30
    sku: {
      name: 'PerGB2018'
    }
  }
}

resource appInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: appInsightsName
  location: location
  tags: tags
  kind: 'web'
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: logAnalytics.id
  }
}

// --- Container registry ---------------------------------------------------

resource containerRegistry 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: containerRegistryName
  location: location
  tags: tags
  sku: {
    name: 'Basic'
  }
  properties: {
    // Admin user is only a fallback for when assignRoles=false (no permission to grant
    // AcrPull to the managed identity). Disable it once RBAC is wired up for real.
    adminUserEnabled: !assignRoles
  }
}

resource acrPullAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = if (assignRoles) {
  name: guid(containerRegistry.id, identityResourceId, acrPullRoleId)
  scope: containerRegistry
  properties: {
    principalId: identityPrincipalId
    principalType: 'ServicePrincipal'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', acrPullRoleId)
  }
}

// --- Key Vault (holds the remaining app secret: plant.security.api-key) -----

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: keyVaultName
  location: location
  tags: tags
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
  }
}

resource plantApiKeySecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = if (assignRoles) {
  parent: keyVault
  name: 'plant-api-key'
  properties: {
    // Placeholder value — rotate via `az keyvault secret set` post-deploy. Never
    // commit a real value here.
    value: 'REPLACE_ME_POST_DEPLOY'
  }
}

resource keyVaultSecretsUserForApp 'Microsoft.Authorization/roleAssignments@2022-04-01' = if (assignRoles) {
  name: guid(keyVault.id, identityResourceId, keyVaultSecretsUserRoleId)
  scope: keyVault
  properties: {
    principalId: identityPrincipalId
    principalType: 'ServicePrincipal'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', keyVaultSecretsUserRoleId)
  }
}

resource keyVaultSecretsUserForDeployer 'Microsoft.Authorization/roleAssignments@2022-04-01' = if (assignRoles && !empty(principalId)) {
  name: guid(keyVault.id, principalId, keyVaultSecretsUserRoleId)
  scope: keyVault
  properties: {
    principalId: principalId
    principalType: 'User'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', keyVaultSecretsUserRoleId)
  }
}

// --- Database: Postgres Flexible Server, Entra-only auth (zero passwords) ---

resource postgres 'Microsoft.DBforPostgreSQL/flexibleServers@2024-08-01' = {
  name: postgresServerName
  location: location
  tags: tags
  sku: {
    name: 'Standard_B1ms'
    tier: 'Burstable'
  }
  properties: {
    version: '16'
    storage: {
      storageSizeGB: 32
    }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: 'Disabled'
    }
    authConfig: {
      activeDirectoryAuth: 'Enabled'
      passwordAuth: 'Disabled'
    }
    highAvailability: {
      mode: 'Disabled'
    }
  }
}

resource postgresAppAdmin 'Microsoft.DBforPostgreSQL/flexibleServers/administrators@2024-08-01' = {
  parent: postgres
  name: identityPrincipalId
  properties: {
    principalType: 'ServicePrincipal'
    principalName: identityName
    tenantId: subscription().tenantId
  }
}

resource postgresDatabase 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2024-08-01' = {
  parent: postgres
  name: postgresDatabaseName
  properties: {
    charset: 'UTF8'
    collation: 'en_US.utf8'
  }
}

resource postgresAllowAzureServices 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2024-08-01' = {
  parent: postgres
  name: 'AllowAllAzureServices'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

// --- Container Apps environment + app -----------------------------------

resource managedEnvironment 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: managedEnvironmentName
  location: location
  tags: tags
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logAnalytics.properties.customerId
        sharedKey: logAnalytics.listKeys().primarySharedKey
      }
    }
  }
}

resource containerApp 'Microsoft.App/containerApps@2024-03-01' = {
  name: containerAppName
  location: location
  tags: union(tags, { 'azd-service-name': 'web' })
  identity: {
    type: 'UserAssigned'
    userAssignedIdentities: {
      '${identityResourceId}': {}
    }
  }
  properties: {
    managedEnvironmentId: managedEnvironment.id
    configuration: {
      ingress: {
        external: true
        targetPort: 8080
        transport: 'auto'
      }
      registries: [
        {
          server: containerRegistry.properties.loginServer
          identity: identityResourceId
        }
      ]
      secrets: [
        {
          name: 'plant-api-key'
          keyVaultUrl: plantApiKeySecret.properties.secretUri
          identity: identityResourceId
        }
      ]
    }
    template: {
      containers: [
        {
          // Placeholder image; `azd deploy` replaces this with the built/pushed
          // application image on first deploy.
          image: 'mcr.microsoft.com/k8se/quickstart:latest'
          name: 'sector-7g-safety-ledger'
          env: [
            { name: 'SERVER_PORT', value: '8080' }
            { name: 'SPRING_DATASOURCE_URL', value: 'jdbc:postgresql://${postgres.properties.fullyQualifiedDomainName}:5432/${postgresDatabaseName}?sslmode=require&authenticationPluginClassName=com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin' }
            { name: 'SPRING_DATASOURCE_USERNAME', value: identityName }
            { name: 'AZURE_CLIENT_ID', value: identityClientId }
            { name: 'PLANT_API_KEY', secretRef: 'plant-api-key' }
            { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: appInsights.properties.ConnectionString }
          ]
        }
      ]
      scale: {
        // Cost optimization (Side Quest): hackathon/dev workload doesn't need 24/7
        // uptime. Scale-to-zero when idle instead of always running >=1 replica —
        // this alone was the single largest line item in the cost estimate
        // (~$39/month for an always-on 0.5 vCPU / 1Gi replica). An HTTP concurrency
        // rule brings a replica back up on the next request (cold start adds a few
        // seconds of latency, acceptable for this workload). See COST-ESTIMATE.md.
        minReplicas: 0
        maxReplicas: 3
        rules: [
          {
            name: 'http-scale-rule'
            http: {
              metadata: {
                concurrentRequests: '10'
              }
            }
          }
        ]
      }
    }
  }
  dependsOn: [
    acrPullAssignment
    keyVaultSecretsUserForApp
  ]
}

output containerRegistryEndpoint string = containerRegistry.properties.loginServer
output containerAppsEnvironmentId string = managedEnvironment.id
output containerAppName string = containerApp.name
output containerAppUri string = 'https://${containerApp.properties.configuration.ingress.fqdn}'
output appInsightsConnectionString string = appInsights.properties.ConnectionString
output postgresJdbcUrl string = 'jdbc:postgresql://${postgres.properties.fullyQualifiedDomainName}:5432/${postgresDatabaseName}?sslmode=require&authenticationPluginClassName=com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin'
output keyVaultEndpoint string = keyVault.properties.vaultUri
