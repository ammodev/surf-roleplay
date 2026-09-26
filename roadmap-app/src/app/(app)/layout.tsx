import { Nav } from "@/components/nav";

/** Every page behind the login reads live data from the database. */
export const dynamic = "force-dynamic";

/**
 * Shell for every signed-in page: navigation plus a centred content column.
 *
 * @param props.children the page content
 */
export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <Nav />
      <main className="mx-auto max-w-7xl px-4 py-6">{children}</main>
    </>
  );
}
