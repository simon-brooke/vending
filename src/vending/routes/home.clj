(ns vending.routes.home
  (:use compojure.core)
  (:require [vending.views.layout :as layout]
            [noir.session :as session]
            [vending.core :as machine]
            [vending.util :as util]))


(def button-actions
  {"Coin return" machine/coin-return
   "Add Merk" machine/add-merk
   "Add Bawbee" machine/add-bawbee
   "Add Plack" machine/add-plack
   "Add Bodle" machine/add-bodle
   "Request Caramel Wafer" machine/get-caramel-wafer
   "Request Teacake" machine/get-teacake
   "Request Snowball" machine/get-snowball
   })


(defn- perform-action!
  "Apply this function to the machine in the session, if there is one, or else to a new default
  machine; cache the result in the session; and return a rendered page representation of the
  state of the machine."
  [function]
  (let [machine (apply function (list (or (session/get :machine) (machine/make-default-machine))))]
    (session/put! :machine machine)
    (layout/render
      "home.html" {:content (util/md->html "/md/docs.md")
                   :machine machine
                   :buttons (keys button-actions)})))


(defn home-page
  "Render the home page with the default machine; in so doing, reset the machine in the session."
  []
  (let [machine (machine/make-default-machine)]
    (session/put! :machine machine)
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")
                 :machine machine
                 :buttons (keys button-actions)})))


(defn about-page []
  (layout/render "about.html"))


(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (POST "/update" [action] (perform-action! (button-actions action)))
  )
