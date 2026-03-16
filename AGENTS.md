# AGENTS.md

## Contexto de este repo

Este proyecto usa un sistema híbrido:

- Flujos y guías en `.agent/` (por ejemplo: `think`, `forge`, `ship`, `audit`).
- Skills nativas de Codex y skills instaladas en `~/.codex/skills` (incluyendo `gstack`).

## Regla principal

El agente puede usar **ambos sistemas** (`.agent` + skills de Codex) en la misma tarea, sin necesidad de que el usuario indique cada skill manualmente.

## Prioridad de decisión

1. Instrucción explícita del usuario (si pide un flujo/skill concreto, se respeta).
2. Convenciones de este repo (`.agent`).
3. Skills de Codex para complementar (QA, browser, imágenes, revisiones, etc.).

## Orquestación recomendada

- Features grandes: `plan-ceo-review` + `plan-eng-review` (gstack) -> `.agent think` -> `.agent forge` -> `review` + `qa` (gstack) -> `ship`.
- Features pequeñas: `.agent forge` -> `review` + `qa --quick` -> `ship`.

## Uso de skills sin invocación explícita

Si una tarea lo requiere, el agente debe activar automáticamente la skill adecuada (por ejemplo `imagegen` para generación/edición de imágenes), aunque el usuario no la mencione por nombre.

## Principios operativos

- Evitar pedir pasos innecesarios al usuario.
- Mantener consistencia con arquitectura y reglas del repo.
- Confirmar solo cuando haya decisiones con impacto no obvio.
