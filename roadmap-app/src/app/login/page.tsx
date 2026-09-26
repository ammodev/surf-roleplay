import { LoginForm } from "./login-form";

/** Login page asking for the shared token and a display name. */
export default function LoginPage() {
  return (
    <main className="min-h-screen grid place-items-center px-4">
      <div className="w-full max-w-sm">
        <p className="eyebrow">surf-roleplay</p>
        <h1 className="text-2xl font-semibold mt-1 mb-6">Roadmap</h1>
        <LoginForm />
      </div>
    </main>
  );
}
