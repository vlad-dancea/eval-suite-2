import { join, normalize } from "node:path";

const root = join(import.meta.dir, "browser");
const indexHtml = join(root, "index.html");
const port = Number(process.env.PORT ?? 4000);

Bun.serve({
  port,
  idleTimeout: 0,
  async fetch(req) {
    const { pathname } = new URL(req.url);

    const relative = normalize(decodeURIComponent(pathname)).replace(/^(\.\.[/\\])+/, "");
    const candidate = Bun.file(join(root, relative));

    if (relative !== "/" && relative !== "" && (await candidate.exists())) {
      return new Response(candidate);
    }

    return new Response(Bun.file(indexHtml), {
      headers: { "Content-Type": "text/html" },
    });
  },
});

console.log(`Frontend listening on http://0.0.0.0:${port}`);
