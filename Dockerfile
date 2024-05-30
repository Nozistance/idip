FROM clojure:openjdk-17-tools-deps

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN clojure -P

EXPOSE 3000

CMD ["clojure", "-M", "-m", "idip.core"]
