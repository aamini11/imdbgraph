import { PostgreSqlContainer } from "@testcontainers/postgresql";
import type { TestProject } from "vitest/node";

export default async function setup(project: TestProject) {
  const container = await new PostgreSqlContainer("postgres:17")
    .withExposedPorts(5432)
    .start();

  project.provide("dockerDbUrl", container.getConnectionUri());

  // Clean up
  return async () => {
    await container.stop();
  };
}

declare module "vitest" {
  export interface ProvidedContext {
    dockerDbUrl: string;
  }
}
