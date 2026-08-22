FROM node:20-alpine AS builder
WORKDIR /workspace
ENV NODE_OPTIONS=--openssl-legacy-provider
COPY apps/console/package*.json ./
RUN npm ci --legacy-peer-deps
COPY apps/console ./
RUN npm run build

FROM nginx:1.27-alpine
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /workspace/dist /usr/share/nginx/html
EXPOSE 80
