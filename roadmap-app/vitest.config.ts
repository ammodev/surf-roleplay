import { defineConfig } from "vitest/config";
import { fileURLToPath } from "node:url";

/** Vitest configuration: Node environment and the `@/` path alias used by the app. */
export default defineConfig({
  resolve: {
    alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) },
  },
  test: {
    environment: "node",
    include: ["src/**/*.test.ts"],
  },
});
