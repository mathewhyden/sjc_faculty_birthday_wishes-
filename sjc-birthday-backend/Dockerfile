# Multi-stage production container for sjc-birthday-backend
FROM node:18-bullseye-slim

# Install OS libraries required by node-canvas, image processing, and fonts
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libcairo2-dev \
    libpango1.0-dev \
    libjpeg-dev \
    libgif-dev \
    librsvg2-dev \
    fonts-dejavu-core \
    fontconfig \
    imagemagick \
    curl \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy dependency manifests
COPY package*.json ./

# Install production dependencies (including building native canvas bindings)
RUN npm install --omit=dev

# Copy entire application source code
COPY . .

# Ensure storage directories exist with appropriate permissions
RUN mkdir -p /app/uploads/faculty_photos /app/uploads/cards /app/public

# Expose server port
EXPOSE 5000

# Set environment
ENV NODE_ENV=production
ENV PORT=5000
ENV HOST=0.0.0.0

# Start production server
CMD ["node", "server.js"]
