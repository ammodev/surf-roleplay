import type { Metadata } from "next";
import "./globals.css";

/** Page metadata shared by every route. */
export const metadata: Metadata = {
  title: "surf-roleplay Roadmap",
  description: "Roadmap and tracker for the surf-roleplay gamemode",
};

/**
 * Root layout wrapping every page in the document shell.
 *
 * @param props.children the page content
 */
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">{children}</body>
    </html>
  );
}
