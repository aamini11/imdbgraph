/// <reference types="vite-plus/client" />

interface ImportMetaEnv {
	readonly VITE_PUBLIC_POSTHOG_KEY: string
}

interface ImportMeta {
	readonly env: ImportMetaEnv
}
