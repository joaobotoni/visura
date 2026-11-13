# PostgreSQL 17 com a extensão pg_cron, para rodar a API localmente.
FROM postgres:17

RUN apt-get update \
    && apt-get install -y --no-install-recommends postgresql-17-cron \
    && rm -rf /var/lib/apt/lists/*

# O pg_cron precisa ser carregado no início e saber em qual banco agendar os jobs.
CMD ["postgres", "-c", "shared_preload_libraries=pg_cron", "-c", "cron.database_name=visura"]
