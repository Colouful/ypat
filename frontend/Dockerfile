FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build:h5
CMD ["sh", "-c", "cp -r /app/dist/build/h5/* /output/"]
