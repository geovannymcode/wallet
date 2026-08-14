# Lessons

## [2026-08-14] — Asumí la rama de deploy de Render a partir del README, no del dashboard real
**Mistake:** Al depurar por qué un fix no se reflejaba en Render, asumí que el Web Service deployaba desde `main` porque así lo dice el README (sección Fase 10). En realidad Geovanny configuró Render para trackear `fase-10`.
**Why it was wrong:** El README documenta la intención al momento de escribirlo, no necesariamente la config actual del servicio en Render — pueden divergir sin que el repo lo refleje.
**Rule:** Cuando el síntoma es "el fix no llegó a producción", no asumas la rama de deploy desde la documentación. Pregunta o verifica cuál rama está trackeando el servicio (Render/Vercel/etc.) antes de diagnosticar por qué un commit no se aplicó. Aquí bastaba con `git log origin/<rama-real>..<rama-real>` para ver el commit sin pushear.
