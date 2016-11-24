(ns ^{:doc "A set of REST requests which manipulate a vending machine held in the session server-side."
      :author "Simon Brooke"}
  vending.routes.json
  (:use compojure.core)
  (:require [clojure.data.json :as json]
            [noir.session :as session]
            [vending.core :as machine]
            [vending.util :as util]))


(defn- perform-action
  "Apply this function to the machine in the session, if there is one, or else to a new default
  machine; cache the result in the session; and return a JSON formatted representation of the result."
  [function]
  (let [machine (apply function (list (or (session/get :machine) (machine/make-default-machine))))]
    (session/put! :machine machine)
    (json/write-str machine)))


;;; Each of these action functions perform an action on the machine in the session, if there is one,
;;; or on a new default machine if there is no machine in the session. They return (and cache in the
;;; session) the new state of the machine after the action; the machine returned is returned as a
;;; JSON string.

(defn coin-return-action
  "Return all the coins that have been tendered since the last sale."
  []
  (perform-action machine/coin-return))


(defn add-merk-action
  "Insert one merk into the coin slot of the machine in the session."
  []
  (perform-action machine/add-merk))


(defn add-bawbee-action
  "Insert one bawbee into the coin slot of the machine in the session."
  []
  (perform-action machine/add-bawbee))


(defn add-plack-action
  "Insert one plack into the coin slot of the machine in the session."
  []
  (perform-action machine/add-plack))


(defn add-bodle-action
  "Insert one bodle into the coin slot of the machine in the session."
  []
  (perform-action machine/add-bodle))


(defn select-caramel-wafer-action
  "Request one caramel wafer from the machine in the session."
  []
  (perform-action machine/get-caramel-wafer))


(defn select-teacake-action
  "Request one teacake from the machine in the session."
  []
  (perform-action machine/get-teacake))


(defn select-snowball-action
  "Request one snowball from the machine in the session."
  []
  (perform-action machine/get-snowball))


(defroutes json-routes
  (GET "/coin-return" [] (coin-return-action))
  (GET "/add-merk" [] (add-merk-action))
  (GET "/add-bawbee" [] (add-bawbee-action))
  (GET "/add-plack" [] (add-plack-action))
  (GET "/add-bodle" [] (add-bodle-action))
  (GET "/select-caramel-wafer" [] (select-caramel-wafer-action))
  (GET "/select-teacake" [] (select-teacake-action))
  (GET "/select-snowball" [] (select-snowball-action))
  )
