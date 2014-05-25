(ns phuutodo.core
  (:gen-class)
  (:require [oauth.client :as oauth]
            [clj-http.util :as util]
            [clj-http.client :as client]
            [clojure.pprint]
            [clojure.edn]
            [cemerick.url])
  (:use clojure.pprint))

(defn load-edn [path]
  (try
    (clojure.edn/read-string (slurp path))
    (catch Exception e nil)))

(defn make-oauth-data [config]
  {:consumer (oauth/make-consumer (-> config :app :id)
                                  (-> config :app :secret)
                                  "https://api.twitter.com/oauth/request_token"
                                  "https://api.twitter.com/oauth/access_token"
                                  "https://api.twitter.com/oauth/authorize"
                                  :hmac-sha1)
   :token (-> config :user :token)
   :secret (-> config :user :secret)})

(defn sign [{:keys [method url body query-params] :or {method :GET}}
            {:keys [consumer token secret]}]
  (oauth/credentials consumer
                     token
                     secret
                     method
                     url
                     query-params))

(defn req [method url {:keys [body query-params] :as args} oauth-data]
  (let [method (or method :GET)
        ; map supplied method to a client function
        func (method {:GET client/get
                      :POST client/post})
        sign-data (sign {:method method
                         :url (str url)
                         :query-params query-params}
                        oauth-data)]
    (println url)
    (pprint sign-data)
    (func url {:as :json
               :body body
               :query-params (merge sign-data query-params)})))

(defn -main
  [& args]
  (let [config (load-edn "./config.edn")
        oauth-data (make-oauth-data config)
        storage (load-edn "./storage.edn")]
    (pprint config)
    (pprint (->> (req :GET
                      "https://api.twitter.com/1.1/statuses/mentions_timeline.json"
                      {:query-params {:since_id (:since_id storage)}}
                      oauth-data)
                :body
                (map :id_str)))))
