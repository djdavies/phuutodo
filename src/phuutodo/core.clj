(ns phuutodo.core
  (:gen-class)
  (:require [oauth.client :as oauth]
            [clj-http.client :as client]
            [clojure.pprint]
            [clojure.edn])
  (:use clojure.pprint))

(defn sign [{:keys [method url body] :or {method :GET}}
            {:keys [consumer token secret]}]
  (oauth/credentials consumer
                     token
                     secret
                     method
                     url
                     body))

(defn req [method url {:keys [body query-params] :as args} oauth-data]
  (let [method (or method :GET)
        func (method {:GET client/get :POST client/post})]
    (func url {:as :json
               :body body
               :query-params (sign (merge args {:method method :url url}) oauth-data)})))

(map identity '(1 2 3))

(defn -main
  [& args]
  (let [config (clojure.edn/read-string (slurp "./config.edn"))
        oauth-data {:consumer (oauth/make-consumer (-> config :app :id)
                                                   (-> config :app :secret)
                                                   "https://api.twitter.com/oauth/request_token"
                                                   "https://api.twitter.com/oauth/access_token"
                                                   "https://api.twitter.com/oauth/authorize"
                                                   :hmac-sha1)
                    :token (-> config :user :token)
                    :secret (-> config :user :secret)}]
    (pprint config)
    (pprint (-> (req :GET "https://api.twitter.com/1.1/statuses/mentions_timeline.json" {} oauth-data)
               :body
                first
                :text))))
