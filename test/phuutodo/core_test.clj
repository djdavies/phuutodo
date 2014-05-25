(ns phuutodo.core-test
  (:require [clojure.test :refer :all]
            [phuutodo.core :refer :all]
            [oauth.client :as oauth]
            [clj-http.client :as client]))

(def oauth-data {:consumer (oauth/make-consumer "abc"
                                                "123"
                                                "https://api.twitter.com/oauth/request_token"
                                                "https://api.twitter.com/oauth/access_token"
                                                "https://api.twitter.com/oauth/authorize"
                                                :hmac-sha1)
                 :token "token"
                 :secret "secret"})

(deftest sign
  (testing "Sign creates correct data"
    (let [sign-data (sign {:method :GET :url "http://test/url"} oauth-data)]
      (is (contains? sign-data :oauth_signature) "Creates OAuth signature")
      (is (= (:oauth_token sign-data) (:token oauth-data)) "Copies over OAuth token"))))
