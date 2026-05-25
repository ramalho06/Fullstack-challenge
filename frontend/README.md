# Teams Tracking System Frontend

Frontend do `teams-tracking-system`, criado com Next.js 16, TypeScript, App Router, Tailwind CSS, shadcn/ui e TanStack Query.

## Rodando localmente

```bash
cp .env.example .env.local
npm install
npm run dev
```

A aplicação abre em:

```txt
http://localhost:3000
```

O backend deve estar disponível em:

```txt
http://localhost:8080
```

Configure a URL da API em `.env.local`:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## Scripts

```bash
npm run dev
npm run build
npm run start
npm run lint
```

## Estrutura

```txt
src/
├── app/
├── components/
│   ├── layout/
│   ├── providers/
│   └── ui/
├── features/
├── lib/
├── services/
└── types/
```

As telas atuais são placeholders. As integrações com o backend entram nos próximos passos.
