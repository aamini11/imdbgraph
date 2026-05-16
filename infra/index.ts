import { AppDatabase } from '@aamini/infra/src/components'
import * as azure from '@pulumi/azure-native'
import * as pulumi from '@pulumi/pulumi'

const azureConfig = new pulumi.Config('azure-native')
const currentStack = pulumi.getStack()
const globalStack = new pulumi.StackReference(
	`aamini11/aamini-platform/${currentStack}`,
)
const postgres = globalStack.getOutput('postgres')

function getPostgresAdminUser(postgresOutput: unknown): string {
	if (
		typeof postgresOutput === 'object' &&
		postgresOutput !== null &&
		'postgresAdminUser' in postgresOutput
	) {
		const adminUser = (postgresOutput as { postgresAdminUser?: unknown })
			.postgresAdminUser
		if (typeof adminUser === 'string') {
			return adminUser
		}
		if (
			typeof adminUser === 'object' &&
			adminUser !== null &&
			'name' in adminUser &&
			typeof (adminUser as { name?: unknown }).name === 'string'
		) {
			return (adminUser as { name: string }).name
		}
	}

	throw new Error('Platform stack postgresAdminUser output missing or invalid')
}

// Get server details from global stack
const serverResourceGroup = azureConfig.require('resourceGroup')
const serverName = postgres.apply((pg) => pg.postgresServerName)
const dbHost = postgres.apply((pg) => pg.postgresHost)
const adminUser = postgres.apply(getPostgresAdminUser)
const oidcIssuerUrl = globalStack
	.getOutput('aks')
	.apply((aks) => aks.oidcIssuerUrl)

const runtimeIdentity = new azure.managedidentity.UserAssignedIdentity(
	'imdbgraph-db-runtime-identity',
	{
		resourceGroupName: serverResourceGroup,
		resourceName: `id-imdbgraph-${currentStack}-db`,
	},
)

new azure.managedidentity.FederatedIdentityCredential(
	'imdbgraph-db-runtime-federated-credential',
	{
		resourceGroupName: serverResourceGroup,
		resourceName: runtimeIdentity.name,
		federatedIdentityCredentialResourceName: runtimeIdentity.name,
		audiences: ['api://AzureADTokenExchange'],
		issuer: oidcIssuerUrl,
		subject: 'system:serviceaccount:imdbgraph:imdbgraph',
	},
	{ dependsOn: [runtimeIdentity] },
)

const appDb = new AppDatabase('imdbgraph', {
	name: 'imdbgraph',
	serverResourceGroupName: serverResourceGroup,
	serverName: serverName,
	serverHost: dbHost,
	adminUser,
	runtimePrincipalObjectId: runtimeIdentity.principalId,
})

export const databaseUrl = pulumi.interpolate`postgresql://${appDb.userName}@${dbHost}:5432/${appDb.databaseName}?sslmode=require`
export const databaseRuntimeIdentityClientId = runtimeIdentity.clientId
