(ns ^{:doc "A set of REST requests which manipulate a vending machine held in the session server-side."
      :author "Simon Brooke"}
  vending.routes.json
  (:use compojure.core)
  (:require [clojure.data.json :as json]
            [noir.session :as session]
            [vending.core :as machine]
            [vending.util :as util]))


(defn- perform-action!
  "Apply this function to the machine in the session, if there is one, or else to a new default
  machine; cache the result in the session; and return a JSON formatted representation of the state
  of the machine."
  [function]
  (let [machine (apply function (list (or (session/get :machine) (machine/make-default-machine))))]
    (session/put! :machine machine)
    (json/write-str machine)))


(defroutes json-routes
  (GET "/json/coin-return" [] (perform-action! machine/coin-return))
  (GET "/json/add-merk" [] (perform-action! machine/add-merk))
  (GET "/json/add-bawbee" [] (perform-action! machine/add-bawbee))
  (GET "/json/add-plack" [] (perform-action! machine/add-plack))
  (GET "/json/add-bodle" [] (perform-action! machine/add-bodle))
  (GET "/json/select-caramel-wafer" [] (perform-action! machine/get-caramel-wafer))
  (GET "/json/select-teacake" [] (perform-action! machine/get-teacake))
  (GET "/json/select-snowball" [] (perform-action! machine/get-snowball))
  )
